package edu.virginia.vcgr.genii.container.attrs;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/* We haven't started using this yet.  I'd like to though */
@Target({ ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ResourceProperty {
	String value();

	String namespace() default "";
}