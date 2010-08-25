package edu.virginia.vcgr.genii.container.common.notification;

import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.common.ConstructionParameters;
import edu.virginia.vcgr.genii.client.naming.jaxb.EPRAdapter;
import edu.virginia.vcgr.genii.client.ser.DBSerializer;
import edu.virginia.vcgr.genii.client.wsrf.wsn.AdditionalUserData;
import edu.virginia.vcgr.genii.client.wsrf.wsn.subscribe.policy.SubscriptionPolicy;
import edu.virginia.vcgr.genii.client.wsrf.wsn.subscribe.policy.SubscriptionPolicyTypes;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.TopicQueryExpression;

@XmlAccessorType(XmlAccessType.NONE)
public class SubscriptionConstructionParameters extends ConstructionParameters
{
	static final long serialVersionUID = 0L;
	
	@XmlElement(namespace = GenesisIIConstants.GENESISII_NS,
		name = "publisher-resource-key", nillable = false, required = true)
	private String _publisherResourceKey;
	
	@XmlElement(namespace = GenesisIIConstants.GENESISII_NS,
		name = "consumer-reference", nillable = false, required = false)
	@XmlJavaTypeAdapter(EPRAdapter.class)
	private EndpointReferenceType _consumerReference;
	
	private TopicQueryExpression _topicQuery;
	private Map<SubscriptionPolicyTypes, SubscriptionPolicy> _policies =
		new EnumMap<SubscriptionPolicyTypes, SubscriptionPolicy>(
			SubscriptionPolicyTypes.class);
	
	@XmlElement(namespace = GenesisIIConstants.GENESISII_NS,
		name = "additional-user-data", nillable = true, required = false)
	private AdditionalUserData _additionalUserData = null;
	
	@SuppressWarnings("unused")
	@XmlElement(namespace = GenesisIIConstants.GENESISII_NS,
		name = "topic-query", nillable = true, required = false)
	@XmlJavaTypeAdapter(HexBinaryAdapter.class)
	private byte[] getTopicQuery() throws IOException
	{
		return DBSerializer.serialize(_topicQuery, -1);
	}
	
	@SuppressWarnings("unused")
	private void setTopicQuery(byte []topicQuery) throws IOException,
		ClassNotFoundException
	{
		_topicQuery = (TopicQueryExpression)DBSerializer.deserialize(
			topicQuery);
	}
	
	@SuppressWarnings("unused")
	@XmlElement(namespace = GenesisIIConstants.GENESISII_NS,
		name = "subscription-policies", 
		nillable = true, required = false)
	@XmlJavaTypeAdapter(HexBinaryAdapter.class)
	private byte[] getPolicies() throws IOException
	{
		return DBSerializer.serialize(_policies, -1);
	}
	
	@SuppressWarnings({ "unused", "unchecked" })
	private void setPolicies(byte []policies) throws IOException,
		ClassNotFoundException
	{
		_policies = 
			(Map<SubscriptionPolicyTypes, SubscriptionPolicy>)DBSerializer.deserialize(
				policies);
	}
	
	@SuppressWarnings("unused")
	private SubscriptionConstructionParameters()
	{
	}
	
	public SubscriptionConstructionParameters(
		String publisherResourceKey,
		EndpointReferenceType consumerReference,
		TopicQueryExpression topicQuery,
		Map<SubscriptionPolicyTypes, SubscriptionPolicy> policies,
		AdditionalUserData additionalUserData)
	{
		_publisherResourceKey = publisherResourceKey;
		_consumerReference = consumerReference;
		_topicQuery = topicQuery;
		_policies = policies;
		_additionalUserData = additionalUserData;
	}
	
	final public EndpointReferenceType consumerReference()
	{
		return _consumerReference;
	}
	
	final public String publisherResourceKey()
	{
		return _publisherResourceKey;
	}
	
	final public TopicQueryExpression topicQuery()
	{
		return _topicQuery;
	}
	
	final public Map<SubscriptionPolicyTypes, SubscriptionPolicy> policies()
	{
		return _policies;
	}
	
	final public AdditionalUserData additionalUserData()
	{
		return _additionalUserData;
	}
}
