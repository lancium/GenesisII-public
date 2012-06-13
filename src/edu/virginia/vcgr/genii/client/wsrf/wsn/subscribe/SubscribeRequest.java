package edu.virginia.vcgr.genii.client.wsrf.wsn.subscribe;

import java.rmi.RemoteException;
import java.util.EnumMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.transform.dom.DOMResult;

import org.apache.axis.message.MessageElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.configuration.ConfigurationException;
import org.oasis_open.wsn.base.FilterType;
import org.oasis_open.wsn.base.InvalidFilterFaultType;
import org.oasis_open.wsn.base.Subscribe;
import org.oasis_open.wsn.base.SubscriptionPolicyType;
import org.oasis_open.wsn.base.TopicNotSupportedFaultType;
import org.oasis_open.wsn.base.UnrecognizedPolicyRequestFaultType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.wsrf.WSRFConstants;
import edu.virginia.vcgr.genii.client.wsrf.wsn.AdditionalUserData;
import edu.virginia.vcgr.genii.client.wsrf.wsn.AdditionalUserDataConstants;
import edu.virginia.vcgr.genii.client.wsrf.wsn.subscribe.policy.SubscriptionPolicy;
import edu.virginia.vcgr.genii.client.wsrf.wsn.subscribe.policy.SubscriptionPolicyTypes;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.TopicQueryDialects;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.TopicQueryExpression;
import edu.virginia.vcgr.genii.container.util.FaultManipulator;

public class SubscribeRequest
{
	static private Log _logger = LogFactory.getLog(SubscribeRequest.class);
	
	static final private QName TOPIC_EXPRESSION_QNAME = new QName(
		WSRFConstants.WSN_BASE_NOT_NS, "TopicExpression", "wsnt");
	static final private String TOPIC_NS_PREFIX_PATTERN = "ts%d";
	
	private EndpointReferenceType _consumerReference = null;
	private TopicQueryExpression _topicFilter = null;
	private TerminationTimeType _terminationTime = null;
	private Map<SubscriptionPolicyTypes, SubscriptionPolicy> _policies =
		new EnumMap<SubscriptionPolicyTypes, SubscriptionPolicy>(
			SubscriptionPolicyTypes.class);
	private AdditionalUserData _additionalUserData = null;
	
	private TopicQueryExpression topicExpressionFromFilterElement(
		Element e) throws InvalidFilterFaultType, TopicNotSupportedFaultType
	{
		return TopicQueryDialects.createFromElement(e);
	}
	
	private FilterType createFilter() throws SOAPException
	{
		if (_topicFilter == null)
			return null;
		
		return new FilterType(new MessageElement[] { 
			_topicFilter.toTopicExpressionElement(
				TOPIC_EXPRESSION_QNAME, TOPIC_NS_PREFIX_PATTERN)
			});
	}
	
	private SubscriptionPolicyType createPolicy() throws JAXBException
	{
		if (_policies.size() == 0)
			return null;
		
		MessageElement []any = new MessageElement[_policies.size()];
		
		JAXBContext context = SubscriptionPolicyTypes.context();
		Marshaller m = context.createMarshaller();
		
		int lcv = 0;
		for (SubscriptionPolicy policy : _policies.values())
		{
			DOMResult result = new DOMResult();
			m.marshal(policy, result);
			any[lcv++] = new MessageElement(
				((Document)result.getNode()).getDocumentElement());
		}
		
		return new SubscriptionPolicyType(any);
	}
	
	public SubscribeRequest(EndpointReferenceType consumerReference,
		TopicQueryExpression topicFilter,
		TerminationTimeType terminationTime,
		AdditionalUserData additionalUserData,
		SubscriptionPolicy...policies)
	{
		_consumerReference = consumerReference;
		_topicFilter = topicFilter;
		_terminationTime = terminationTime;
		_additionalUserData = additionalUserData;
		for (SubscriptionPolicy policy : policies)
			_policies.put(policy.policyType(), policy);
	}
	
	public SubscribeRequest(Subscribe request)
		throws RemoteException
	{
		_consumerReference = request.getConsumerReference();
		
		FilterType filter = request.getFilter();
		if (filter != null)
		{
			MessageElement []any = filter.get_any();
			if (any != null)
			{
				for (MessageElement e : any)
				{
					QName eName = e.getQName();
					if (!eName.equals(TOPIC_EXPRESSION_QNAME))
						throw FaultManipulator.fillInFault(
							new InvalidFilterFaultType());
					
					_topicFilter = topicExpressionFromFilterElement(e);
				}
			}
		}
		
		_terminationTime = TerminationTimeType.newInstance(
			request.getInitialTerminationTime());
		
		SubscriptionPolicyType policy = request.getSubscriptionPolicy();
		if (policy != null)
		{
			MessageElement []any = policy.get_any();
			if (any != null)
			{
				try
				{
					JAXBContext context = SubscriptionPolicyTypes.context();
					Unmarshaller u = context.createUnmarshaller();
					
					for (MessageElement e : any)
					{
						try
						{
							SubscriptionPolicy policyInstance = 
								(SubscriptionPolicy)u.unmarshal(e);
							_policies.put(policyInstance.policyType(), policyInstance);
						}
						catch (JAXBException exception)
						{
							_logger.warn(
								"Error attempting to create subscription type.",
								exception);
							
							throw FaultManipulator.fillInFault(
								new UnrecognizedPolicyRequestFaultType());
						}
					}
				}
				catch (JAXBException e)
				{
					throw new ConfigurationException(
						"Unable to set up policy deserializer.", e);
				}
			}
		}
		
		MessageElement []any = request.get_any();
		if (any != null)
		{
			for (MessageElement me : any)
			{
				QName name = me.getQName();
				if (name.equals(AdditionalUserDataConstants.ELEMENT_QNAME))
				{
					try
					{
						_additionalUserData = AdditionalUserData.fromElement(
							AdditionalUserData.class, me);
					}
					catch (JAXBException e)
					{
						throw new RemoteException(
							"Unable to deserialize user data.", e);
					}
				}
			}
		}
	}
	
	final public EndpointReferenceType consumerReference()
	{
		return _consumerReference;
	}
	
	final public TopicQueryExpression topicFilter()
	{
		return _topicFilter;
	}
	
	final public TerminationTimeType terminationTime()
	{
		return _terminationTime;
	}
	
	final public SubscriptionPolicy policy(SubscriptionPolicyTypes policyType)
	{
		return _policies.get(policyType);
	}
	
	final public Map<SubscriptionPolicyTypes, SubscriptionPolicy> policies()
	{
		return _policies;
	}
	
	final public AdditionalUserData additionalUserData()
	{
		return _additionalUserData;
	}
	
	final public Subscribe asRequestType()
	{
		try
		{
			return new Subscribe(_consumerReference,
				createFilter(),
				(_terminationTime == null) ? null : _terminationTime.toAxisType(),
				createPolicy(), (_additionalUserData == null) ? null :
					new MessageElement[] {
						AdditionalUserData.toMessageElement(
							_additionalUserData)
					});
		}
		catch (SOAPException e)
		{
			throw new ConfigurationException(
				"Unable to create topic expression.", e);
		}
		catch (JAXBException e)
		{
			throw new ConfigurationException(
				"Unable to serialize subscription policy.", e);
		}
	}
}