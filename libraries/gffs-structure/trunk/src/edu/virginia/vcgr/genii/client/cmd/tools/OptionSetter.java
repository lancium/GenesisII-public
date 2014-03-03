package edu.virginia.vcgr.genii.client.cmd.tools;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

import edu.virginia.vcgr.genii.client.cmd.ITool;
import edu.virginia.vcgr.genii.client.cmd.ToolException;

public class OptionSetter
{
	private ITool _tool;
	private HashMap<String, Integer> _callsMade = new HashMap<String, Integer>();

	private AccessibleObject[] _options = null;
	private Field[] _fields = null;
	private Method[] _methods = null;

	public OptionSetter(ITool instance)
	{
		_tool = instance;
	}

	public void set(String option) throws ToolException
	{
		set(option, null);
	}

	public void set(String option, String value) throws ToolException
	{
		AccessibleObject obj;
		obj = get(option);
		String maxOccurences = obj.getAnnotation(Option.class).maxOccurances();
		Integer maxCalls = null;

		if (!maxOccurences.equals("unbounded"))
			maxCalls = Integer.parseInt(maxOccurences);
		if (maxCalls != null) {
			synchronized (_callsMade) {
				Integer callsMade = _callsMade.get(option);
				if (callsMade == null)
					callsMade = new Integer(0);
				if (maxCalls.compareTo(callsMade) <= 0)
					throw new ToolException("Invalid Usage.  Option \"" + option + "\" can only be given "
						+ maxCalls.intValue() + " times.");
				callsMade = new Integer(callsMade.intValue() + 1);
				_callsMade.put(option, callsMade);
			}
		}
		if (obj.getClass().equals(Method.class)) {
			((Method) obj).setAccessible(true);
			try {
				if (value == null)
					((Method) obj).invoke(_tool);
				else
					((Method) obj).invoke(_tool, value);
			} catch (InvocationTargetException ite) {
				throw new ToolException("Tool threw an exception while setting option \"" + option + "\".", ite.getCause());
			} catch (IllegalAccessException iae) {
				throw new ToolException("Tool cannot handle option \"" + option + "\" due to protection issue.", iae);
			} finally {
				((Method) obj).setAccessible(false);
			}
		} else {
			((Field) obj).setAccessible(true);
			try {
				Object setVal;
				if (value == null)
					setVal = true;
				else
					setVal = handleField(((Field) obj), value);
				((Field) obj).set(_tool, setVal);
			} catch (IllegalAccessException iae) {
				throw new ToolException("Tool cannot handle option \"" + option + "\" due to protection issue.", iae);
			} finally {
				((Field) obj).setAccessible(false);
			}
		}
	}

	private AccessibleObject get(String name) throws ToolException
	{
		if ((_options == null))
			_options = getOptions();
		for (AccessibleObject obj : _options) {
			Option option = obj.getAnnotation(Option.class);
			String values[] = option.value();
			for (String val : values) {
				if (val.equals(name))
					return obj;
			}
		}
		throw new ToolException("Tool cannot handle option \"" + name + "\"");

	}

	synchronized public AccessibleObject[] getOptions()
	{
		if (_options == null) {
			Method[] methods = getMethods();
			Field[] fields = getFields();
			Collection<AccessibleObject> temp = new ArrayList<AccessibleObject>();
			for (Method mthd : methods) {
				if (mthd.getAnnotation(Option.class) != null)
					temp.add(mthd);
			}
			for (Field fld : fields) {
				if (fld.getAnnotation(Option.class) != null)
					temp.add(fld);
			}
			_options = temp.toArray(new AccessibleObject[temp.size()]);
		}
		return _options;
	}

	synchronized private Method[] getMethods()
	{
		if (_methods == null) {
			LinkedList<Method> temp = recMethods(_tool.getClass());
			_methods = temp.toArray(new Method[temp.size()]);
		}
		return _methods;
	}

	private LinkedList<Method> recMethods(Class<?> cl)
	{
		LinkedList<Method> list = new LinkedList<Method>();
		if (cl.equals(Object.class)) {
			return list;
		}

		Method methods[] = cl.getDeclaredMethods();

		for (Method mthd : methods) {
			list.add(mthd);
		}

		LinkedList<Method> superMethods = recMethods(cl.getSuperclass());

		for (Method mthd : list) {
			superMethods.add(mthd);
		}
		return superMethods;
	}

	synchronized private Field[] getFields()
	{
		if (_fields == null) {
			LinkedList<Field> temp = recFields(_tool.getClass());
			_fields = temp.toArray(new Field[temp.size()]);
		}
		return _fields;
	}

	private LinkedList<Field> recFields(Class<?> cl)
	{
		LinkedList<Field> list = new LinkedList<Field>();
		if (cl.equals(Object.class)) {
			return list;
		}

		Field fields[] = cl.getDeclaredFields();

		for (Field fld : fields) {
			list.add(fld);
		}

		LinkedList<Field> superFields = recFields(cl.getSuperclass());

		for (Field fld : list) {
			superFields.add(fld);
		}
		return superFields;
	}

	private Object handleField(Field f, String val) throws ToolException
	{
		if (f.getType().equals(String.class))
			return val;
		if (f.getType().equals(boolean.class))
			return Boolean.parseBoolean(val);
		if (f.getType().equals(int.class))
			return Integer.parseInt(val);
		throw new ToolException("The grid does not know how to " + "set a field of this type via options");
	}
}
