package edu.virginia.vcgr.genii.client.ogsa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.StringTokenizer;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;

import org.apache.axis.message.MessageElement;

import edu.virginia.vcgr.genii.client.rp.ResourcePropertyException;

public class OGSAQNameList
{
	private Collection<QName> _qNames;
	
	public OGSAQNameList(Collection<QName> qnames)
	{
		_qNames = new ArrayList<QName>(qnames);
	}
	
	public OGSAQNameList(MessageElement qnameElement)
		throws ResourcePropertyException
	{
		Collection<QName> names = new ArrayList<QName>();
		
		StringTokenizer tokenizer = new StringTokenizer(qnameElement.getValue(),
			" \t\r\n");
		while (tokenizer.hasMoreTokens())
		{
			String token = tokenizer.nextToken();
			int index = token.indexOf(':');
			if (index < 0)
				names.add(new QName("", token));
			else
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
		
		_qNames = new ArrayList<QName>(names);
	}
	
	public Collection<QName> getQNames()
	{
		return new ArrayList<QName>(_qNames);
	}
	
	public MessageElement toMessageElement(QName elementName)
		throws SOAPException
	{
		StringBuilder builder = new StringBuilder();
		boolean first = true;
		
		MessageElement ret = new MessageElement(elementName);
		
		for (QName name : _qNames)
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