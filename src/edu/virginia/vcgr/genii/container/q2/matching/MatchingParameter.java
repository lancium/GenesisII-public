package edu.virginia.vcgr.genii.container.q2.matching;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;

import edu.virginia.vcgr.genii.client.jsdl.personality.GeniiOrFacet;
import edu.virginia.vcgr.genii.client.jsdl.personality.GeniiPropertyFacet;

abstract class MatchingParameter
{
	abstract boolean matches(Map<String, Collection<String>> besParameters);
	abstract boolean supportsRequired(String parameterName, Collection<String> values);
	
	static private MatchingParameter createDefaultMatchingParameter(
		MessageElement e)
	{
		return new DefaultMatchingParameter(e.getAttribute(
			GeniiPropertyFacet.PROPERTY_NAME_ATTRIBUTE),
			e.getAttribute(
				GeniiPropertyFacet.PROPERTY_VALUE_ATTRIBUTE));
	}
	
	static private MatchingParameter createOrMatchingParameter(
		MessageElement e)
	{
		OrMatchingParameter parameter = new OrMatchingParameter();
		
		Iterator<?> iter = e.getChildElements();
		while (iter.hasNext())
		{
			MessageElement element = (MessageElement)iter.next();
			MatchingParameter child = createMatchingParameter(element);
			if (child != null)
				parameter.addMatchingParameter(child);
		}
		
		return parameter;
	}
	
	static private MatchingParameter createMatchingParameter(MessageElement e)
	{
		QName name = e.getQName();
		if (name.equals(GeniiPropertyFacet.PROPERTY_ELEMENT))
			return createDefaultMatchingParameter(e);
		else if (name.equals(GeniiOrFacet.OR_ELEMENT))
			return createOrMatchingParameter(e);
		
		return null;
	}
	
	static MatchingParameter[] matchingParameters(MessageElement []any)
	{
		Collection<MatchingParameter> parameters = 
			new LinkedList<MatchingParameter>();
		
		if (any != null)
		{
			for (MessageElement element : any)
			{
				MatchingParameter p = createMatchingParameter(element);
				if (p != null)
					parameters.add(p);
			}
		}
		
		return parameters.toArray(new MatchingParameter[parameters.size()]);
	}
}