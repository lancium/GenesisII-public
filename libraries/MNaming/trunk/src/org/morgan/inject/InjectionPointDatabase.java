package org.morgan.inject;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

final class InjectionPointDatabase {
	static private Map<Class<?>, Collection<InjectionPoint>> _points = new HashMap<Class<?>, Collection<InjectionPoint>>();

	static private void validateInjectionInformation(Field field,
			MInject injectionInformation) throws InjectionException {
		if (injectionInformation.lazy()) {
			if (injectionInformation.recursive())
				throw new InjectionException(
						String.format(
								"Invalid injection annotation on %s -- cannot be both lazy and recursive.",
								field));

			Class<?>[] ifaces = injectionInformation.lazyTypes();
			if (ifaces == null || ifaces.length == 0)
				ifaces = new Class<?>[] { field.getType() };

			for (Class<?> iface : ifaces) {
				if (!iface.isInterface())
					throw new InjectionException(
							String.format(
									"Cannot lazily inject %s because %s is not an interface.",
									field, iface));
			}
		}
	}

	static private void validateInjectionInformation(Method method,
			MInject injectionInformation) throws InjectionException {
		if (injectionInformation.recursive())
			throw new InjectionException(
					String.format(
							"Invalid injection annotation on %s -- methods cannot be recursively injected.",
							method));

		Class<?>[] paramTypes = method.getParameterTypes();
		if (paramTypes.length != 1)
			throw new InjectionException(
					String.format(
							"Method %s is not a valid injection target -- must have exactly one parameter.",
							method));

		if (injectionInformation.lazy()) {
			Class<?>[] ifaces = injectionInformation.lazyTypes();
			if (ifaces == null || ifaces.length == 0)
				ifaces = paramTypes;

			for (Class<?> iface : ifaces)
				if (!iface.isInterface())
					throw new InjectionException(
							String.format(
									"Cannot lazily inject %s because %s is not an interface.",
									method, iface));
		}
	}

	static final Collection<InjectionPoint> injectionPoints(Class<?> targetClass)
			throws InjectionException {
		if (targetClass == null || targetClass.equals(Object.class))
			return new ArrayList<InjectionPoint>();

		synchronized (_points) {
			Collection<InjectionPoint> ret = _points.get(targetClass);

			if (ret == null) {
				ret = new LinkedList<InjectionPoint>();

				for (Class<?> iface : targetClass.getInterfaces())
					ret.addAll(injectionPoints(iface));

				ret.addAll(injectionPoints(targetClass.getSuperclass()));

				for (Field field : targetClass.getDeclaredFields()) {
					MInject injectionInformation = field
							.getAnnotation(MInject.class);
					if (injectionInformation != null) {
						validateInjectionInformation(field,
								injectionInformation);
						if (injectionInformation.recursive())
							ret.add(new RecursiveInjectionPoint(field,
									injectionInformation));
						else
							ret.add(new FieldInjectionPoint(
									injectionInformation, field));
					}
				}

				for (Method method : targetClass.getDeclaredMethods()) {
					MInject injectionInformation = method
							.getAnnotation(MInject.class);
					if (injectionInformation != null) {
						validateInjectionInformation(method,
								injectionInformation);

						ret.add(new MethodInjectionPoint(injectionInformation,
								method));
					}
				}

				_points.put(targetClass, ret);
			}

			return ret;
		}
	}
}