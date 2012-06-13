package edu.virginia.vcgr.genii.client.cache.unified.subscriptionmanagement;

import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.TopicPath;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.wellknown.RNSTopics;

public class PendingRNSSubscription implements PendingSubscription {

	private EndpointReferenceType EPROfRNS;
	
	public PendingRNSSubscription(EndpointReferenceType EPROfRNS) {
		this.EPROfRNS = EPROfRNS;
	}

	@Override
	public EndpointReferenceType getNewsSource() {
		return EPROfRNS;
	}

	@Override
	public TopicPath getTopicOfInterest() {
		return RNSTopics.RNS_CONTENT_CHANGE_TOPIC;
	}

	@Override
	public String getProducerPropertiesFilter() {
		return null;
	}

	@Override
	public String getMessageContentFilter() {
		return null;
	}
}
