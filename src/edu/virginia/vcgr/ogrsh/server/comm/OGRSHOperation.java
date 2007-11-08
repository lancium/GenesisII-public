package edu.virginia.vcgr.ogrsh.server.comm;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface OGRSHOperation
{
	// Indicates the name of the method coming in
	// empty string indicates use the name of the attributed method.
	String value() default "";
}