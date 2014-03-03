package org.morgan.dpage;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.servlet.http.HttpServletRequest;

public class NullValueInjectionHandlerFactory implements
		ObjectInjectionHandlerFactory {
	@Override
	public ObjectInjectionHandler createHandler(String target,
			HttpServletRequest request) throws InjectionException {
		return new ObjectInjectionHandler() {
			@Override
			public Object getMethodInjectionValue(Method method,
					InjectObject injectionAnnotation) throws InjectionException {
				return null;
			}

			@Override
			public Object getFieldInjectionValue(Field field,
					InjectObject injectionAnnotation) throws InjectionException {
				return null;
			}
		};
	}
}
