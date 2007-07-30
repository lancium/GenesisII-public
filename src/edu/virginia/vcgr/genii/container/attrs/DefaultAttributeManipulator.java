/*
 * Copyright 2006 University of Virginia
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package edu.virginia.vcgr.genii.container.attrs;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;

import edu.virginia.vcgr.genii.common.rattrs.AttributeNotSettableFaultType;
import edu.virginia.vcgr.genii.common.rattrs.IncorrectAttributeCardinalityFaultType;
import edu.virginia.vcgr.genii.common.resource.ResourceUnknownFaultType;
import edu.virginia.vcgr.genii.container.util.FaultManipulator;

public class DefaultAttributeManipulator implements IAttributeManipulator
{
	private Object _target;
	private QName _attrQName;
	private Method _getMethod;
	private Method _setMethod;
	private boolean _isSingleSet;
	
	private DefaultAttributeManipulator(
		Object target,
		QName attrQName, Method getterMethod,
		Method setterMethod, boolean isSingleSet)
	{
		_target = target;
		_attrQName = attrQName;
		_getMethod = getterMethod;
		_setMethod = setterMethod;
		_isSingleSet = isSingleSet ;
	}
	
	public QName getAttributeQName()
	{
		return _attrQName;
	}

	public boolean allowsSet()
	{
		return _setMethod != null;
	}

	@SuppressWarnings("unchecked")
	public Collection<MessageElement> getAttributeValues()
		throws ResourceUnknownFaultType, RemoteException
	{
		Object ret;
		
		try
		{
			ret = _getMethod.invoke(
				_target, new Object[0]);
			
			if (ret instanceof MessageElement)
			{
				ArrayList<MessageElement> tmp =
					new ArrayList<MessageElement>(1);
				tmp.add((MessageElement)ret);
				return tmp;
			} else
				return (Collection<MessageElement>)ret;
		}
		catch (InvocationTargetException ite)
		{
			Throwable t = ite.getCause();
			if (t == null)
				t = ite;
			
			if (t instanceof RemoteException)
				throw (RemoteException)t;
			
			throw new RemoteException(t.getMessage(), t);
		}
		catch (IllegalAccessException iae)
		{
			throw new RemoteException(iae.getMessage(), iae);
		}
	}

	public void setAttributeValues(Collection<MessageElement> values) 
		throws ResourceUnknownFaultType, RemoteException,
			AttributeNotSettableFaultType, IncorrectAttributeCardinalityFaultType
	{
		Object []params = null;
		
		if (_setMethod == null)
			throw FaultManipulator.fillInFault(
				new AttributeNotSettableFaultType());
		
		if (_isSingleSet)
		{
			if (values.size() == 0)
				params = new Object[] { null };
			else if (values.size() == 1)
				params = new Object[] { values.iterator().next() };
			else
				throw FaultManipulator.fillInFault(
					new IncorrectAttributeCardinalityFaultType());
		} else
		{
			params = new Object[] { values };
		}
		
		try
		{
			_setMethod.invoke(_target, params);
		}
		catch (InvocationTargetException ite)
		{
			Throwable t = ite.getCause();
			if (t == null)
				t = ite;
			
			if (t instanceof RemoteException)
				throw (RemoteException)t;
			
			throw new RemoteException(t.getMessage(), t);
		}
		catch (IllegalAccessException iae)
		{
			throw new RemoteException(iae.getMessage(), iae);
		}
	}
	
	static public IAttributeManipulator createManipulator(
		Object target, QName attributeName, 
		String getMethodName) throws NoSuchMethodException
	{
		return createManipulator(target, attributeName, getMethodName, null);
	}
	
	static public IAttributeManipulator createManipulator(
		Object target, QName attributeName, 
		String getMethodName, String setMethodName) throws NoSuchMethodException
	{
		Class<? extends Object> clazz;
		
		if (target == null)
			throw new IllegalArgumentException(
				"Parameter \"target\" cannot be null.");
		
		if (target instanceof Class)
		{
			clazz = (Class<? extends Object>)target;
			target = null;
		} else
			clazz = target.getClass();
		
		if (attributeName == null)
			throw new IllegalArgumentException(
				"Parameter \"attributeName\" cannot be null.");
		if (getMethodName == null)
			throw new IllegalArgumentException(
				"Parameter \"getMethodName\" cannot be null.");

		Method getter = clazz.getMethod(getMethodName, new Class[0]); 
		Class<? extends Object> returnType = getter.getReturnType();
		if (!(
			(MessageElement.class.isAssignableFrom(returnType)) ||
			(Collection.class.isAssignableFrom(returnType))) )
			throw new IllegalArgumentException("Method \"" +
				getMethodName + "\" does not appear to return the correct type.");
		
		Method setter;
		boolean isSingleSet = true;
		if (setMethodName == null)
			setter = null;
		else
		{
			try
			{
				setter = clazz.getMethod(setMethodName,
					new Class[] { Collection.class });
				isSingleSet = false;
			}
			catch (NoSuchMethodException nsme)
			{
				setter = clazz.getMethod(setMethodName,
					new Class[] { MessageElement.class });
			}
		}
		
		return new DefaultAttributeManipulator(target,
			attributeName, getter, setter, isSingleSet);
	}
}
