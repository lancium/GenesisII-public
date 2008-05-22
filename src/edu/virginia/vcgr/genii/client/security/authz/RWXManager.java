package edu.virginia.vcgr.genii.client.security.authz;

import java.lang.reflect.Method;
import java.util.HashMap;

import edu.virginia.vcgr.genii.container.appdesc.ApplicationDescriptionServiceImpl;

public class RWXManager
{
	static private HashMap<String, HashMap<String, RWXCategory>> _cachedValues =
			new HashMap<String, HashMap<String, RWXCategory>>();

	static private HashMap<String, RWXCategory> retrieveClassMap(
			Class<?> service)
	{
		synchronized (_cachedValues)
		{
			HashMap<String, RWXCategory> classMap =
					_cachedValues.get(service.getName());
			if (classMap == null)
			{
				classMap = createClassMap(service);
				_cachedValues.put(service.getName(), classMap);
			}

			return classMap;
		}
	}

	static private void fillInClassMap(Class<?> service,
			HashMap<String, RWXCategory> classMap)
	{
		String methodName;

		if (service == null)
			return;

		for (Method m : service.getMethods())
		{
			methodName = m.getName();
			if (classMap.containsKey(methodName))
				continue;

			RWXMapping mapping = m.getAnnotation(RWXMapping.class);
			if (mapping != null && (mapping.value() != RWXCategory.INHERITED))
			{
				classMap.put(methodName, mapping.value());
			}
		}

		for (Class<?> iface : service.getInterfaces())
			fillInClassMap(iface, classMap);
		fillInClassMap(service.getSuperclass(), classMap);
	}

	static private HashMap<String, RWXCategory> createClassMap(Class<?> service)
	{
		HashMap<String, RWXCategory> classMap =
				new HashMap<String, RWXCategory>();

		fillInClassMap(service, classMap);

		return classMap;
	}

	static private RWXCategory lookup(Class<?> service, String methodName)
	{
		HashMap<String, RWXCategory> classMap = retrieveClassMap(service);

		RWXCategory category = classMap.get(methodName);
		if (category == null)
			throw new RWXMappingException(
					"Couldn't find RWX mapping for method \"" + methodName
							+ "\" in class \"" + service + "\".");

		return category;
	}

	static public RWXCategory lookup(Method operation)
	{
		Class<?> declaringClass = operation.getDeclaringClass();
		return lookup(declaringClass, operation.getName());
	}

	static public void main(String[] args) throws Throwable
	{
		HashMap<String, RWXCategory> classMapping =
				createClassMap(ApplicationDescriptionServiceImpl.class);
		for (String method : classMapping.keySet())
		{
			System.err.println(method + ":  " + classMapping.get(method));
		}
	}
}