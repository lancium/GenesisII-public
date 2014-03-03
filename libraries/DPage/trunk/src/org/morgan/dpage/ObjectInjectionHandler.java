package org.morgan.dpage;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public interface ObjectInjectionHandler {
	public Object getFieldInjectionValue(Field field,
			InjectObject injectionAnnotation) throws InjectionException;

	public Object getMethodInjectionValue(Method method,
			InjectObject injectionAnnotation) throws InjectionException;
}