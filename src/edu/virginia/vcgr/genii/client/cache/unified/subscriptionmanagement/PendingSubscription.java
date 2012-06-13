package edu.virginia.vcgr.genii.client.cache.unified.subscriptionmanagement;

import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.TopicPath;

public interface PendingSubscription {

	// Represents the producer or publisher of notification message.
	EndpointReferenceType getNewsSource();
	
	TopicPath getTopicOfInterest();
	
	String getProducerPropertiesFilter();
	
	String getMessageContentFilter();
}
