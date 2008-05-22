package edu.virginia.vcgr.genii.client.invoke;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.configuration.ConfigurationException;
import org.morgan.util.configuration.IXMLConfigurationSectionHandler;
import org.morgan.util.configuration.XMLConfiguration;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.virginia.vcgr.genii.client.configuration.NamedInstances;

public class ClientPipelineSectionHandler implements IXMLConfigurationSectionHandler
{
	static private Log _logger = LogFactory.getLog(ClientPipelineSectionHandler.class);
	
	static private final String _GENII_NS = "http://vcgr.cs.virginia.edu/Genesis-II";
	static private final String _PIPELINE_HANDLER_NAME = "pipeline-handler";
	
	static private QName _PIPELINE_HANDLER_QNAME = new QName(_GENII_NS, _PIPELINE_HANDLER_NAME);
	
	@Override
	public Object parse(Node n)
	{
		InvocationInterceptorManager mgr = new InvocationInterceptorManager();
		
		NodeList children = n.getChildNodes();
		int length = children.getLength();
		
		for (int lcv = 0; lcv < length; lcv++)
		{
			Node child = children.item(lcv);
			if (child.getNodeType() == Node.ELEMENT_NODE)
			{
				QName childQName = XMLConfiguration.getQName(child);
				if (!childQName.equals(_PIPELINE_HANDLER_QNAME))
					throw new ConfigurationException(
						"Found element with unexpected QName of \"" + 
						childQName + "\".");
				
				Node textNode = child.getFirstChild();
				if (textNode.getNodeType() != Node.TEXT_NODE)
					throw new ConfigurationException(
						"Found class node whose child was NOT a text node.");
				
				String handler = textNode.getTextContent();
				Object instance = NamedInstances.getClientInstances().lookup(handler);
				if (instance == null)
					_logger.warn("Unable to find named instance \"" + handler + "\" for client pipeline.");
				
				try
				{
					mgr.addInterceptorClass(instance);
				}
				catch (NoSuchMethodException nsme)
				{
					_logger.warn("Unable to find method on client pipeline handler instance \"" 
						+ handler + "\".", nsme);
				}
			}
		}
		
		return mgr;
	}
}