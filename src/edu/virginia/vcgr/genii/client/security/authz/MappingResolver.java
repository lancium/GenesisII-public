package edu.virginia.vcgr.genii.client.security.authz;

import java.lang.reflect.Method;

public interface MappingResolver
{
	public RWXCategory resolve(Class<?> serviceClass, Method operation);
}