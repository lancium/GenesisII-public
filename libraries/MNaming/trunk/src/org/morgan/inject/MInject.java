package org.morgan.inject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.METHOD })
public @interface MInject {
	String name() default "";

	boolean lazy() default false;

	Class<?>[] lazyTypes() default {};

	boolean recursive() default false;

	Class<? extends MInjectFactory> injectionFactory() default MInjectFactory.class;
}