package edu.virginia.vcgr.genii.container.rfork.sd;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used by XMLStateTranslaters.  When an XMLStateTranslater
 * is used, the resource fork that implements SimpleStateResourceFork MUST
 * have this annotation on it's class.
 * 
 * @author mmm2a
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface XMLStateDescription
{
	/** The name space to use for the generated XML element */
	String namespace() default "";
	
	/** The local-name to use for the generated XML element */
	String localName();
}