package edu.virginia.vcgr.genii.container.rfork;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ForkRoot
{
	Class<? extends RNSResourceFork> value();
}