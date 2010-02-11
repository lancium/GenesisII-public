package edu.virginia.vcgr.genii.container.rfork.cmd;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.client.cmd.CommandLineFormer;
import edu.virginia.vcgr.genii.client.utils.instantiation.FromString;

public class CommandChannelManager
{
	static private Log _logger = LogFactory.getLog(CommandChannelManager.class);
	
	static private Map<Class<?>, Map<String, Method>> _handlerMap =
		new HashMap<Class<?>, Map<String,Method>>();
	
	static private Map<String, Method> determineMethodMap(Class<?> targetClass,
		Map<String, Method> methodMap)
	{
		if (targetClass == null || targetClass.equals(Object.class))
			return methodMap;
		
		for (Class<?> iface : targetClass.getInterfaces())
			determineMethodMap(iface, methodMap);
		determineMethodMap(targetClass.getSuperclass(), methodMap);
		
		for (Method m : targetClass.getDeclaredMethods())
		{
			CommandHandler handler = m.getAnnotation(CommandHandler.class);
			if (handler != null)
			{
				String name = handler.value();
				if (name.length() <= 0)
					name = m.getName();
				
				methodMap.put(name, m);
			}
		}
		
		return methodMap;
	}
	
	static private Method getHandler(Class<?> targetClass, String command)
	{
		Map<String, Method> methodMap;
		
		synchronized(_handlerMap)
		{
			methodMap = _handlerMap.get(targetClass);
		}
		
		if (methodMap == null)
		{
			methodMap = determineMethodMap(targetClass,
				new HashMap<String, Method>());
			synchronized(_handlerMap)
			{
				_handlerMap.put(targetClass, methodMap);
			}
		}
		
		return methodMap.get(command);
	}
	
	static private Map<String, Method> getHandlers(Class<?> targetClass)
	{
		Map<String, Method> methodMap;
		
		synchronized(_handlerMap)
		{
			methodMap = _handlerMap.get(targetClass);
		}
		
		if (methodMap == null)
		{
			methodMap = determineMethodMap(targetClass,
				new HashMap<String, Method>());
			synchronized(_handlerMap)
			{
				_handlerMap.put(targetClass, methodMap);
			}
		}
		
		return methodMap;
	}
	
	static public void handleCommand(Object target,
		InputStream commandContent) throws IOException
	{
		StringBuilder builder = new StringBuilder();
		BufferedReader reader = new BufferedReader(
			new InputStreamReader(commandContent));
		String line;
		
		while ( (line = reader.readLine()) != null)
		{
			if (builder.length() != 0)
				builder.append(' ');
			builder.append(line);
		}
		
		String []cmd = CommandLineFormer.formCommandLine(builder.toString());
		if (cmd.length < 1)
			return;
		
		Method handler = getHandler(target.getClass(), cmd[0]);
		if (handler != null)
		{
			Class<?> []paramTypes = handler.getParameterTypes();
			Object []args = new Object[paramTypes.length];
			for (int lcv = 0; lcv < args.length; lcv++)
			{
				if ( (lcv + 1) >= cmd.length)
					args[lcv] = null;
				else
					args[lcv] = FromString.fromString(cmd[lcv + 1], paramTypes[lcv]);
			}
			
			try
			{
				handler.invoke(target, args);
			}
			catch (IllegalArgumentException e)
			{
				_logger.error(String.format(
					"Unable to invoke command handler for command '%s'.",
					builder.toString()), e);
				throw new IOException(String.format(
					"Unable to invoke command handler for command '%s'.",
					builder.toString()), e);
						
			} 
			catch (IllegalAccessException e)
			{
				_logger.error(String.format(
					"Unable to invoke command handler for command '%s'.",
					builder.toString()), e);
				throw new IOException(String.format(
					"Unable to invoke command handler for command '%s'.",
					builder.toString()), e);
			} 
			catch (InvocationTargetException e)
			{
				Throwable cause = e.getCause();
				if (cause instanceof IOException)
					throw ((IOException)cause);
				
				_logger.error(String.format(
					"Unable to invoke command handler for command '%s'.",
					builder.toString()), cause);
				throw new IOException(String.format(
					"Unable to invoke command handler for command '%s'.",
					builder.toString()), cause);
			}
		}
	}
	
	static public String[] describeCommands(Object target)
	{
		Map<String, Method> handlers = getHandlers(target.getClass());
		Collection<String> ret = new Vector<String>(handlers.size());
		
		for (String command : handlers.keySet())
		{
			StringBuilder builder = new StringBuilder(command + "(");
			Method m = handlers.get(command);
			Class<?> []paramTypes = m.getParameterTypes();
			Annotation [][]annotations = m.getParameterAnnotations();
			
			for (int lcv = 0; lcv < paramTypes.length; lcv++)
			{
				if (lcv > 0)
					builder.append(", ");
				
				Annotation []parameterAnnotations = annotations[lcv];
				CommandParameter cp = null;
				
				for (Annotation annot : parameterAnnotations)
				{
					if (annot instanceof CommandParameter)
					{
						cp = (CommandParameter)annot;
						break;
					}
				}
				
				builder.append(paramTypes[lcv].toString());
				
				if (cp != null)
					builder.append(" " + cp.value());
			}
			
			builder.append(")");
			ret.add(builder.toString());
		}
		
		return ret.toArray(new String[ret.size()]);
	}
}