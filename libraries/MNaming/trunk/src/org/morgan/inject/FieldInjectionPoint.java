package org.morgan.inject;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;

class FieldInjectionPoint extends InjectionPoint {
	private Field _field;

	FieldInjectionPoint(MInject injectionInformation, Field field) {
		super(injectionInformation);

		if (field == null)
			throw new IllegalArgumentException("Field cannot be null.");

		_field = field;
	}

	@Override
	final void inject(Object target, MInjectResolver resolver)
			throws InjectionException {
		Object value;

		if (_injectionInformation.lazy()) {
			Class<?>[] types = _injectionInformation.lazyTypes();
			if (types == null || types.length == 0)
				types = new Class<?>[] { _field.getType() };

			value = Proxy.newProxyInstance(FieldInjectionPoint.class
					.getClassLoader(), types,
					new LazyInjectionPointHandler(getInjectFactory(resolver),
							_injectionInformation, _field.getType()));
		} else
			value = doResolve(resolver, _field.getType());

		synchronized (_field) {
			try {
				_field.setAccessible(true);
				_field.set(target, value);
			} catch (IllegalAccessException e) {
				throw new InjectionException(String.format(
						"Unable to inject value into field %s.", _field), e);
			} finally {
				_field.setAccessible(false);
			}
		}
	}
}