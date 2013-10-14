package edu.virginia.vcgr.genii.client.wsrf.wsn.topic;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import edu.virginia.vcgr.genii.client.wsrf.wsn.NotificationMessageContents;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface WSNTopic {
	Class<? extends NotificationMessageContents> contentsType();
}