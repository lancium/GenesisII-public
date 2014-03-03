package org.morgan.inject;

import java.lang.reflect.Field;

class RecursiveInjectionPoint extends InjectionPoint {
	private Field _field;

	RecursiveInjectionPoint(Field field, MInject injectionInformation) {
		super(injectionInformation);

		_field = field;
	}

	@Override
	void inject(Object target, MInjectResolver resolver)
			throws InjectionException {
		synchronized (_field) {
			try {
				_field.setAccessible(true);
				MInjector injector = new MInjector(resolver);
				injector.inject(_field.get(target));
			} catch (IllegalAccessException e) {
				throw new InjectionException(String.format(
						"Unable to inject field %s.", _field), e);
			} finally {
				_field.setAccessible(false);
			}
		}
	}
}