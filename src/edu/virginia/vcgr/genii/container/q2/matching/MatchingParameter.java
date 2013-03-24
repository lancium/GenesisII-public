package edu.virginia.vcgr.genii.container.q2.matching;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;

import edu.virginia.vcgr.genii.client.jsdl.personality.GeniiOrFacet;
import edu.virginia.vcgr.genii.client.jsdl.personality.GeniiPropertyFacet;

public abstract class MatchingParameter
{
	abstract boolean matches(Collection<MatchingParameter> params);

	abstract boolean matches(MatchingParameter param);

	abstract boolean isRequired();

	public abstract edu.virginia.vcgr.genii.common.MatchingParameter toAxisType();

	static public boolean matches(MatchingParameters besParam, MatchingParameters jobParam)
	{
		// Check if bes has any requirements
		Collection<MatchingParameter> job = jobParam.getParameters();
		for (MatchingParameter p : besParam.getRequired()) {
			if (!p.matches(job))
				return false;
		}

		// Check job requirements
		Collection<MatchingParameter> bes = besParam.getParameters();
		for (MatchingParameter p : jobParam.getRequired()) {
			if (!p.matches(bes))
				return false;
		}

		return true;
	}

	static private MatchingParameter createDefaultMatchingParameter(MessageElement e, Boolean fromJSDL)
	{
		return new DefaultMatchingParameter(e.getAttribute(GeniiPropertyFacet.PROPERTY_NAME_ATTRIBUTE),
			e.getAttribute(GeniiPropertyFacet.PROPERTY_VALUE_ATTRIBUTE), fromJSDL);
	}

	static private MatchingParameter createOrMatchingParameter(MessageElement e)
	{
		OrMatchingParameter parameter = new OrMatchingParameter();

		Iterator<?> iter = e.getChildElements();
		while (iter.hasNext()) {
			MessageElement element = (MessageElement) iter.next();
			// or only comes from jsdl does not support nested OR
			DefaultMatchingParameter child = new DefaultMatchingParameter(
				element.getAttribute(GeniiPropertyFacet.PROPERTY_NAME_ATTRIBUTE),
				element.getAttribute(GeniiPropertyFacet.PROPERTY_VALUE_ATTRIBUTE), true);
			if (child != null)
				parameter.addMatchingParameter(child);
		}

		return parameter;
	}

	static private MatchingParameter createMatchingParameter(MessageElement e, Boolean fromJSDL)
	{
		QName name = e.getQName();
		if (name.equals(GeniiPropertyFacet.PROPERTY_ELEMENT))
			return createDefaultMatchingParameter(e, fromJSDL);
		else if (name.equals(GeniiOrFacet.OR_ELEMENT))
			return createOrMatchingParameter(e);

		return null;
	}

	static Collection<MatchingParameter> matchingParameters(MessageElement[] any, Boolean fromJSDL)
	{
		Collection<MatchingParameter> parameters = new LinkedList<MatchingParameter>();

		if (any != null) {
			for (MessageElement element : any) {
				MatchingParameter p = createMatchingParameter(element, fromJSDL);
				if (p != null)
					parameters.add(p);
			}
		}

		return parameters;
	}
}