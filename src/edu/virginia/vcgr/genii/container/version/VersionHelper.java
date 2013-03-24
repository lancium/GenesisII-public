package edu.virginia.vcgr.genii.container.version;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import edu.virginia.vcgr.appmgr.version.Version;
import edu.virginia.vcgr.genii.client.version.MinimumVersionException;

public class VersionHelper
{
	static private Map<Method, Version> _versions = new HashMap<Method, Version>();

	static private Version getMinimumVersion(Class<?> cl, String methodName, Class<?>[] parameterTypes)
	{
		if (cl == null || cl.equals(Object.class))
			return null;

		try {
			Method m = cl.getDeclaredMethod(methodName, parameterTypes);
			MinimumVersion mv = m.getAnnotation(MinimumVersion.class);
			if (mv != null)
				return new Version(mv.value());
		} catch (SecurityException e) {
			// Don't worry about it
		} catch (NoSuchMethodException e) {
			// Don't worry about it
		}

		MinimumVersion mv = cl.getAnnotation(MinimumVersion.class);
		if (mv != null)
			return new Version(mv.value());

		return getMinimumVersion(cl.getSuperclass(), methodName, parameterTypes);
	}

	static private Version getUncachedMinimumVersion(Method targetMethod)
	{
		MinimumVersion mv = targetMethod.getAnnotation(MinimumVersion.class);
		if (mv != null)
			return new Version(mv.value());

		Class<?> cl = targetMethod.getDeclaringClass();
		mv = cl.getAnnotation(MinimumVersion.class);
		if (mv != null)
			return new Version(mv.value());

		return getMinimumVersion(cl.getSuperclass(), targetMethod.getName(), targetMethod.getParameterTypes());
	}

	static public Version getMinimumVersion(Method targetMethod)
	{
		Version v;

		synchronized (_versions) {
			v = _versions.get(targetMethod);
			if (v != null)
				return v;
			if (_versions.containsKey(targetMethod))
				return null;
		}

		v = getUncachedMinimumVersion(targetMethod);

		synchronized (_versions) {
			_versions.put(targetMethod, v);
		}

		return v;
	}

	static public void checkVersion(Version clientVersion, Method targetMethod) throws MinimumVersionException
	{
		if (clientVersion == null)
			clientVersion = Version.EMPTY_VERSION;

		if (targetMethod != null) {
			Version min = getMinimumVersion(targetMethod);
			if (min != null && clientVersion.compareTo(min) < 0)
				throw new MinimumVersionException(clientVersion, min);
		}
	}
}