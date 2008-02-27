package edu.virginia.vcgr.genii.client.ogsa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.StringTokenizer;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;

import org.apache.axis.message.MessageElement;

import edu.virginia.vcgr.genii.client.rp.ResourcePropertyException;

/**
 * OGSA has a rather idiotic definition of lists of qnames for resource
 * properties.  It involves putting them into a whitespace seperated list
 * inside of an XML element.  To make it possible to parse/serialize these, we
 * have to implement a specialize class to "wrap" these lists.  That's what
 * this is.
 * 
 * @author mmm2a
 */
public class OGSAQNameList extends ArrayList<QName>
{
	static final long serialVersionUID = -6994256303342467777L;
	
	/**
	 * Construct a new OGSAQNameList based off of an existing
	 * collection of QNames.
	 * 
	 * @param qnames The qnames to seed the list with.
	 */
	public OGSAQNameList(Collection<QName> qnames)
	{
		super(qnames);
	}
	
	/**
	 * Create a new OGSAQNameList based off of an XML Element to
	 * deserialize.
	 * 
	 * @param qnameElement The element to deserialize.
	 * @throws ResourcePropertyException
	 */
	public OGSAQNameList(MessageElement qnameElement)
		throws ResourcePropertyException
	{
		Collection<QName> names = new ArrayList<QName>();
		
		/* The qnames come in whitespace seperated, so create a tokenizer to
		 * parse that.
		 */
		StringTokenizer tokenizer = new StringTokenizer(qnameElement.getValue(),
			" \t\r\n");
		while (tokenizer.hasMoreTokens())
		{
			String token = tokenizer.nextToken();
			
			/* See if the token has a colon.  If so, it's a fully qualified
			 * qname that we have to split into a prefix and a local part.  If
			 * not, then it's the whole name.
			 */
			int index = token.indexOf(':');
			if (index < 0)
			{
				// It didn't have a prefix, so let's put it in the default namespace
				String ns = qnameElement.getNamespaceURI("");
				if (ns == null)
					ns = "";
				names.add(new QName(ns, token));
			} else
			{
				String prefix = token.substring(0, index);
				String local = token.substring(index + 1);
				String ns = qnameElement.getNamespaceURI(prefix);
				if (ns == null)
					throw new ResourcePropertyException(
						"Unable to parse returned qname \"" + token + "\".");
				names.add(new QName(ns, local));
			}
		}
		
		addAll(names);
	}
	
	/**
	 * Turn the current QName list into a message element (XML serialize it).
	 * 
	 * @param elementName The name to give the new element when you serialize
	 * it.
	 * @return The newly created XML element.
	 * @throws SOAPException
	 */
	public MessageElement toMessageElement(QName elementName)
		throws SOAPException
	{
		StringBuilder builder = new StringBuilder();
		boolean first = true;
		
		MessageElement ret = new MessageElement(elementName);
		
		for (QName name : this)
		{
			if (!first)
				builder.append(" ");
			first = false;
			
			String ns = name.getNamespaceURI();
			if (ns == null || ns.isEmpty())
				builder.append(name.getLocalPart());
			else
			{
				String prefix = ret.getPrefix(ns);
				if (prefix == null)
				{
					for (int lcv = 0; true; lcv++)
					{
						prefix = "ns" + lcv;
						if (ret.getNamespaceURI(prefix) == null)
						{
							ret.addNamespaceDeclaration(prefix, ns);
							break;
						}
					}
				}
				
				builder.append(prefix + ":" + name.getLocalPart());
			}
		}

		ret.addTextNode(builder.toString());
		return ret;
	}
}