package edu.virginia.vcgr.genii.client.security.authz;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class RWXManager
{
	static private class ConstantMappingResolver implements MappingResolver
	{
		private RWXCategory _value;
		
		public ConstantMappingResolver(RWXCategory value)
		{
			_value = value;
		}

		@Override
		public RWXCategory resolve(Class<?> serviceClass, Method method)
		{
			return _value;
		}
	}
	
	static private class ClassMapEntry
	{
		private MappingResolver _resolver;
		private Method _originatingMethod;
		
		public ClassMapEntry(Method originatingMethod, 
			MappingResolver resolver)
		{
			_resolver = resolver;
			_originatingMethod = originatingMethod;
		}
		
		public RWXCategory resolve(Class<?> serviceClass)
		{
			return _resolver.resolve(serviceClass, _originatingMethod);
		}
	}
	
	static private HashMap<String, HashMap<String, ClassMapEntry>> _cachedValues =
			new HashMap<String, HashMap<String, ClassMapEntry>>();

	static private HashMap<String, ClassMapEntry> retrieveClassMap(
			Class<?> service)
	{
		synchronized (_cachedValues)
		{
			HashMap<String, ClassMapEntry> classMap =
					_cachedValues.get(service.getName());
			if (classMap == null)
			{
				classMap = createClassMap(service);
				_cachedValues.put(service.getName(), classMap);
			}

			return classMap;
		}
	}

	static private MappingResolver newResolver(
		Class<? extends MappingResolver> resolver)
	{
		try
		{
			Constructor<? extends MappingResolver> cons =
				resolver.getConstructor();
			return cons.newInstance();
		}
		catch (Throwable cause)
		{
			throw new RWXMappingException(
				"Unable to instantiate mapping resolver.", cause);
		}
	}
	
	static private void fillInClassMap(Class<?> service,
			HashMap<String, ClassMapEntry> classMap)
	{
		String methodName;

		if (service == null)
			return;

		for (Method m : service.getDeclaredMethods())
		{
			methodName = m.getName();
			if (classMap.containsKey(methodName))
				continue;

			RWXMappingResolver resolver = m.getAnnotation(
				RWXMappingResolver.class);
			if (resolver != null)
				classMap.put(methodName, 
					new ClassMapEntry(m, newResolver(resolver.value())));
			
			RWXMapping mapping = m.getAnnotation(RWXMapping.class);
			if (mapping != null && (mapping.value() != RWXCategory.INHERITED))
			{
				classMap.put(methodName, 
					new ClassMapEntry(m,
						new ConstantMappingResolver(mapping.value())));
			}
		}

		for (Class<?> iface : service.getInterfaces())
			fillInClassMap(iface, classMap);
		fillInClassMap(service.getSuperclass(), classMap);
	}

	static private HashMap<String, ClassMapEntry> createClassMap(Class<?> service)
	{
		HashMap<String, ClassMapEntry> classMap =
				new HashMap<String, ClassMapEntry>();

		fillInClassMap(service, classMap);

		return classMap;
	}
	
	static public RWXCategory lookup(Class<?> cl, String methodName)
	{
		Map<String, ClassMapEntry> classMap = retrieveClassMap(cl);

		ClassMapEntry resolver = classMap.get(methodName);
		if (resolver == null)
			throw new RWXMappingException(
				"Couldn't find RWX mapping for method \"" + methodName
					+ "\" in class \"" + cl + "\".");
		
		RWXCategory category = resolver.resolve(cl);
		if (category == null)
			throw new RWXMappingException(
				"Couldn't find RWX mapping for method \"" + methodName
					+ "\" in class \"" + cl + "\".");

		return category;
	}

	static public RWXCategory lookup(Class<?> service, Method operation)
	{
		String methodName = operation.getName();
		Map<String, ClassMapEntry> classMap = retrieveClassMap(service);

		ClassMapEntry resolver = classMap.get(methodName);
		if (resolver == null)
			throw new RWXMappingException(
				"Couldn't find RWX mapping for method \"" + methodName
					+ "\" in class \"" + service + "\".");
		
		RWXCategory category = resolver.resolve(service);
		if (category == null)
			throw new RWXMappingException(
				"Couldn't find RWX mapping for method \"" + methodName
					+ "\" in class \"" + service + "\".");

		return category;
	}
}