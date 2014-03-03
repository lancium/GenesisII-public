package edu.virginia.vcgr.genii.container.rfork.sd;

import java.lang.reflect.Method;

import edu.virginia.vcgr.genii.security.RWXCategory;
import edu.virginia.vcgr.genii.security.rwx.MappingResolver;
import edu.virginia.vcgr.genii.security.rwx.RWXManager;
import edu.virginia.vcgr.genii.security.rwx.RWXMappingException;

public class SimpleStateRWXMappingResolver implements MappingResolver
{
	@Override
	public RWXCategory resolve(Class<?> serviceClass, Method operation)
	{
		Method getterMethod;
		Method translateMethod;
		String operationName = operation.getName();

		try {
			getterMethod = serviceClass.getDeclaredMethod("get");

			if (operationName.equals("snapshotState"))
				translateMethod = getterMethod;
			else if (operationName.equals("modifyState"))
				translateMethod = serviceClass.getDeclaredMethod("set", getterMethod.getReturnType());
			else
				throw new RWXMappingException("Target operation was not snapshotState or modifyState.");

			return RWXManager.lookup(serviceClass, translateMethod);
		} catch (NoSuchMethodException nsme) {
			throw new RWXMappingException("Couldn't find appropriate get/set methods on class \"" + serviceClass.getName()
				+ "\".");
		}
	}
}