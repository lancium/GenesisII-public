package edu.virginia.vcgr.genii.container.rfork.sd;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to decorate SimpleStateResourceFork sub-classes. It describes whether or
 * not the piece of state is readable or writable, and which translator to use for state
 * translations.
 * 
 * @author mmm2a
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface StateDescription {
	/**
	 * The list of translators to use for translate to/from text/binary states.
	 * 
	 * @return
	 */
	Class<? extends StateTranslator>[] value() default { TextStateTranslator.class };

	/** Indicates whether or not this state variable is readable */
	boolean readable() default true;

	/** Indicates whether or not this state variable is writable */
	boolean writable() default true;
}