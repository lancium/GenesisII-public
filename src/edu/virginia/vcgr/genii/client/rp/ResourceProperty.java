package edu.virginia.vcgr.genii.client.rp;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This is the annotation type used to indicate that a method or "property"
 * in another Java interface relates to a resource property.
 * 
 * @author mmm2a
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ResourceProperty
{
	/**
	 * The namespace that the resource property exists in.
	 * 
	 * @return The string namespace.
	 */
	String namespace() default "";
	
	/**
	 * The local name of the resource property
	 * 
	 * @return The local name.
	 */
	String localname();
	
	/**
	 * The minimum number of elements that the target resource can have
	 * (this loosely corresponds to whether the target resource property
	 * is a single valued, or multi-valued property).  Note that this
	 * property is a String (and not an integer) because the WSRF-RP
	 * specification allows for the string constant "unbounded" to be
	 * used.
	 * 
	 * @return The minimum cardinality of the target resource property.
	 */
	String min() default "0";
	
	/**
	 * The maximum number of elements that the target resource can have
	 * (this loosely corresponds to whether the target resource property
	 * is a single valued, or multi-valued property).  Note that this
	 * property is a String (and not an integer) because the WSRF-RP
	 * specification allows for the string constant "unbounded" to be
	 * used.
	 * 
	 * @return The minimum cardinality of the target resource property.
	 */
	String max() default "1";
	
	/**
	 * A class that is used to translate the resource property to/from
	 * XML.  By default, the default XML serialization/deserialization
	 * provided by Apache AXIS will be used.  However, users can plug
	 * in their own serializers for non-default behavior.
	 * 
	 * @return The resource property translator class to use.  This class
	 * MUST have a default constructor.
	 */
	Class<? extends ResourcePropertyTranslator> translator()
		default ResourcePropertyTranslator.class;
}