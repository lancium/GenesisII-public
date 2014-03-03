package edu.virginia.vcgr.genii.security.rwx;

import java.lang.reflect.Method;

import edu.virginia.vcgr.genii.security.RWXCategory;

public interface MappingResolver
{
	public RWXCategory resolve(Class<?> serviceClass, Method operation);
}