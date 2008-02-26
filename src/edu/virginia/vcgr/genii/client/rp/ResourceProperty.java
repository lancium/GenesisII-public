package edu.virginia.vcgr.genii.client.rp;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ResourceProperty
{
	String namespace() default "";
	String localname();
	String min() default "0";
	String max() default "1";
	Class<? extends ResourcePropertyTranslator> translator()
		default ResourcePropertyTranslator.class;
}