package org.morgan.dpage;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.Cookie;

class Injector
{
	static private void injectFieldValue(Object instance, Field field, Object value) throws InjectionException
	{
		try {
			field.setAccessible(true);
			field.set(instance, value);
		} catch (IllegalArgumentException e) {
			throw new InjectionException(String.format("Unable to inject value into field \"%s.%s\".", field
				.getDeclaringClass().getName(), field.getName()), e);
		} catch (IllegalAccessException e) {
			throw new InjectionException(String.format("Unable to inject value into field \"%s.%s\".", field
				.getDeclaringClass().getName(), field.getName()), e);
		} finally {
			field.setAccessible(false);
		}
	}

	static private void injectMethodValue(Object instance, Method method, Object value) throws InjectionException
	{
		try {
			method.setAccessible(true);
			method.invoke(instance, value);
		} catch (IllegalArgumentException e) {
			throw new InjectionException(String.format("Unable to inject value into method \"%s.%s\".", method
				.getDeclaringClass().getName(), method.getName()), e);
		} catch (IllegalAccessException e) {
			throw new InjectionException(String.format("Unable to inject value into method \"%s.%s\".", method
				.getDeclaringClass().getName(), method.getName()), e);
		} catch (InvocationTargetException e) {
			throw new InjectionException(String.format("Unable to inject value into method \"%s.%s\".", method
				.getDeclaringClass().getName(), method.getName()), e.getCause());
		} finally {
			method.setAccessible(false);
		}
	}

	static private void injectFields(Object instance, Field[] fields, InjectionContext injectionContext)
		throws InjectionException
	{
		InjectParameter iParameter;
		InjectCookie iCookie;
		InjectObject iObject;

		for (Field field : fields) {
			Class<?> fieldType = field.getType();

			iParameter = field.getAnnotation(InjectParameter.class);
			if (iParameter != null) {
				String parameterName = iParameter.value();
				String[] values = injectionContext.parameter(parameterName);

				if (fieldType.equals(String[].class))
					injectFieldValue(instance, field, values);
				else if (fieldType.equals(String.class)) {
					Object value = null;

					if (values != null && values.length > 0)
						value = values[0];

					injectFieldValue(instance, field, value);
				} else
					throw new InjectionException(String.format("Cannot inject parameter into \"%s.%s\".  "
						+ "Type is not String, or String[]!", field.getDeclaringClass().getName(), field.getName()));
			}

			iCookie = field.getAnnotation(InjectCookie.class);
			if (iCookie != null) {
				String cookieName = iCookie.value();
				Cookie cookie = injectionContext.cookie(cookieName);

				if (fieldType.equals(Cookie.class))
					injectFieldValue(instance, field, cookie);
				else
					throw new InjectionException(String.format("Cannot inject parameter into \"%s.%s\".  "
						+ "Type is not Cookie!", field.getDeclaringClass().getName(), field.getName()));
			}

			iObject = field.getAnnotation(InjectObject.class);
			if (iObject != null) {
				injectFieldValue(instance, field, injectionContext.injectionHandler().getFieldInjectionValue(field, iObject));
			}
		}
	}

	static private void injectMethods(Object instance, Method[] methods, InjectionContext injectionContext)
		throws InjectionException
	{
		InjectParameter iParameter;
		InjectCookie iCookie;
		InjectObject iObject;

		for (Method method : methods) {
			Class<?>[] parameterTypes = method.getParameterTypes();

			iParameter = method.getAnnotation(InjectParameter.class);
			if (iParameter != null) {
				if (parameterTypes.length != 1)
					throw new InjectionException(String.format("Cannot inject into method \"%s.%s\".  "
						+ "Signature must have exactly 1 parameter.", method.getDeclaringClass().getName(), method.getName()));

				String parameterName = iParameter.value();
				String[] values = injectionContext.parameter(parameterName);

				if (parameterTypes[0].equals(String[].class))
					injectMethodValue(instance, method, values);
				else if (parameterTypes[0].equals(String.class)) {
					Object value = null;

					if (values != null && values.length > 0)
						value = values[0];

					injectMethodValue(instance, method, value);
				} else
					throw new InjectionException(String.format("Cannot inject parameter into \"%s.%s\".  "
						+ "Parameter type is not String, or String[]!", method.getDeclaringClass().getName(), method.getName()));
			}

			iCookie = method.getAnnotation(InjectCookie.class);
			if (iCookie != null) {
				if (parameterTypes.length != 1)
					throw new InjectionException(String.format("Cannot inject into method \"%s.%s\".  "
						+ "Signature must have exactly 1 parameter.", method.getDeclaringClass().getName(), method.getName()));

				String cookieName = iCookie.value();
				Cookie cookie = injectionContext.cookie(cookieName);

				if (parameterTypes[0].equals(Cookie.class))
					injectMethodValue(instance, method, cookie);
				else
					throw new InjectionException(String.format("Cannot inject parameter into \"%s.%s\".  "
						+ "Parameter type is not Cookie!", method.getDeclaringClass().getName(), method.getName()));
			}

			iObject = method.getAnnotation(InjectObject.class);
			if (iObject != null) {
				if (parameterTypes.length != 1)
					throw new InjectionException(String.format("Cannot inject into method \"%s.%s\".  "
						+ "Signature must have exactly 1 parameter.", method.getDeclaringClass().getName(), method.getName()));

				injectMethodValue(instance, method, injectionContext.injectionHandler()
					.getMethodInjectionValue(method, iObject));
			}
		}
	}

	static private void injectValues(Set<Class<?>> handledInterfaces, Class<?> instanceClass, Object instance,
		InjectionContext injectionContext) throws InjectionException
	{
		if (instanceClass == null || instanceClass.equals(Object.class))
			return;

		injectValues(handledInterfaces, instanceClass.getSuperclass(), instance, injectionContext);

		for (Class<?> iface : instanceClass.getInterfaces()) {
			if (!handledInterfaces.contains(iface)) {
				handledInterfaces.add(iface);
				injectValues(handledInterfaces, iface, instance, injectionContext);
			}
		}

		injectFields(instance, instanceClass.getDeclaredFields(), injectionContext);
		injectMethods(instance, instanceClass.getDeclaredMethods(), injectionContext);
	}

	static public void injectValues(Object instance, InjectionContext injectionContext) throws InjectionException
	{
		Set<Class<?>> handledInterfaces = new HashSet<Class<?>>();

		injectValues(handledInterfaces, instance.getClass(), instance, injectionContext);
	}
}