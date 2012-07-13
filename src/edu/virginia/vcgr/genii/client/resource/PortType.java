package edu.virginia.vcgr.genii.client.resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.morgan.util.io.StreamUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.virginia.vcgr.genii.client.io.FileResource;
import edu.virginia.vcgr.genii.client.utils.bvm.BitVectorMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PortType
{
	static private Log _logger = LogFactory.getLog(PortType.class);

	static private final FileResource KNOWN_PORT_TYPES_RESOURCE =
		new FileResource("edu/virginia/vcgr/genii/client/resource/known-porttypes.xml");
	
	static private BitVectorMap<PortType> _vectorMap;
	static private Map<QName, PortType> _knownPortTypes;
	
	static private PortType parsePortType(Element e) throws IOException
	{
		String name = e.getAttribute("name");
		String description = null;
		String rankString = null;
		
		if (name == null || name.length() == 0)
			throw new IOException("Port type found without a name.");
		
		NodeList children = e.getChildNodes();
		int length = children.getLength();
		for (int lcv = 0; lcv < length; lcv++)
		{
			Node child = children.item(lcv);
			if (child.getNodeType() == Node.ELEMENT_NODE)
			{
				Element ce = (Element)child;
				if (ce.getNodeName().equals("description"))
					description = ce.getTextContent();
				else if (ce.getNodeName().equals("display-rank"))
					rankString = ce.getTextContent();
				else
					throw new IOException("Unexpected node \"" + 
						ce.getNodeName() + "\" found.\n" +
						"Expected \"description\" or \"display-rank\".");
			}
		}
		
		if (rankString == null)
			throw new IOException(
				"Error parsing known-porttypes -- couldn't find a " +
				"display rank for port type \"" + name + "\".");
		
		return new PortType(QName.valueOf(name), 
			Integer.parseInt(rankString), description);
	}
	
	static
	{
		InputStream in = null;
		_knownPortTypes = new LinkedHashMap<QName, PortType>();
		
		try
		{
			in = KNOWN_PORT_TYPES_RESOURCE.open();
			DocumentBuilderFactory factory = 
				DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(false);
			factory.setValidating(false);
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(in);
			Element docElement = doc.getDocumentElement();
			NodeList list = docElement.getChildNodes();
			int length = list.getLength();
			for (int lcv = 0; lcv < length; lcv++)
			{
				Node n = list.item(lcv);
				if (n.getNodeType() == Node.ELEMENT_NODE)
				{
					if (!n.getNodeName().equals("portType"))
						throw new IOException(
							"Invalid entry found in known-porttypes.xml file.\n" +
							"Expected <portType> but saw <" + n.getNodeName() + ">");
					
					PortType pt = parsePortType((Element)n);
					_knownPortTypes.put(pt.getQName(), pt);
				}
			}
			
			_vectorMap = new BitVectorMap<PortType>(_knownPortTypes.values());
		}
		catch (Throwable cause)
		{
			_logger.info("exception occurred in static init", cause);
			System.exit(1);
		}
		finally
		{
			StreamUtils.close(in);
		}
	}
	
	static private int _largestDescription = 0;
	
	static public int getLargestKnownDescriptionLength()
	{
		return _largestDescription;
	}
	
	static public PortType getHighestRankedPortType(PortType...portTypes)
	{
		PortType ret = null;
		
		for (PortType portType : portTypes)
		{
			if (ret == null)
				ret = portType;
			else
			{
				if (ret._displayRank < portType._displayRank)
					ret = portType;
			}
		}
		
		return ret;
	}
	
	private QName _portTypeName;
	private String _description;
	private int _displayRank;
	
	private PortType(QName portTypeName, int displayRank, String description)
	{
		_portTypeName = portTypeName;
		_displayRank = displayRank;
		_description = (description == null) ? "" : description;
		
		int dLength = _description.length();
		if (dLength > _largestDescription)
			_largestDescription = dLength;
	}
	
	public QName getQName()
	{
		return _portTypeName;
	}
	
	public int getDisplayRank()
	{
		return _displayRank;
	}
	
	public String getDescription()
	{
		return _description;
	}
	
	public String toString()
	{
		return _portTypeName.toString();
	}
	
	public boolean equals(PortType other)
	{
		return _portTypeName.equals(other._portTypeName);
	}
	
	public boolean equals(Object other)
	{
		if (other instanceof PortType)
			return equals((PortType)other);
		
		return false;
	}
	
	public int hashCode()
	{
		return _portTypeName.hashCode();
	}
	
	static public boolean isKnown(QName portType)
	{
		PortType pt = _knownPortTypes.get(portType);
		return pt != null;
	}
	
	static public PortType get(QName portType)
	{
		PortType pt = _knownPortTypes.get(portType);
		if (pt == null)
			throw new IllegalArgumentException("Port type \"" + portType + 
				"\" is unknown to the system.");
		
		return pt;
	}
	
	static public String translate(Collection<PortType> portTypes)
	{
		return _vectorMap.translate(portTypes);
	}
	
	static public String translate(PortType...portTypes)
	{
		return _vectorMap.translate(portTypes);
	}
	
	static public Collection<PortType> translate(String stringRep)
	{
		return _vectorMap.translate(stringRep);
	}
}
