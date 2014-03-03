package edu.virginia.vcgr.genii.client.resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.io.StreamUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.virginia.vcgr.genii.client.tty.TTYConstants;
import edu.virginia.vcgr.genii.client.utils.bvm.BitVectorMap;
import edu.virginia.vcgr.genii.system.classloader.GenesisClassLoader;

public class PortTypeAdministrator
{
	static private Log _logger = LogFactory.getLog(PortTypeAdministrator.class);

	static private BitVectorMap<PortType> _vectorMap = null;
	static private Map<QName, PortType> _knownPortTypes = null;
	static private int _largestDescription = 0;

	public static final String KNOWN_PORT_TYPES_RESOURCE = "config/known-porttypes.xml";

	/**
	 * the constructor that sets up all the static objects. this should only be called from the
	 * PortType.portTypeFactory method.
	 */
	public PortTypeAdministrator()
	{
		InputStream in = null;
		_knownPortTypes = new LinkedHashMap<QName, PortType>();

		try {
			ClassLoader l = GenesisClassLoader.classLoaderFactory();
			InputStream inputStr = l.getResourceAsStream(PortTypeAdministrator.KNOWN_PORT_TYPES_RESOURCE);
			if (inputStr == null) {
				String msg = "could not locate: " + PortTypeAdministrator.KNOWN_PORT_TYPES_RESOURCE;
				_logger.info(msg);
				throw new IOException(msg);
			}
			in = inputStr;
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(false);
			factory.setValidating(false);
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(in);
			Element docElement = doc.getDocumentElement();
			NodeList list = docElement.getChildNodes();
			int length = list.getLength();
			boolean badPortType = false;
			Node n = null;
			for (int lcv = 0; lcv < length; lcv++) {
				n = list.item(lcv);
				if (n.getNodeType() == Node.ELEMENT_NODE) {
					if (!n.getNodeName().equals("portType")) {
						badPortType = true;
						break;
					}

					PortType pt = parsePortType((Element) n);
					int dLength = pt.getDescription().length();
					if (dLength > get_largestDescription()) {
						set_largestDescription(dLength);
					}
					_knownPortTypes.put(pt.getQName(), pt);
				}
			}
			if (badPortType == true) {
				StreamUtils.close(in);
				throw new IOException("Invalid entry found in " + PortTypeAdministrator.KNOWN_PORT_TYPES_RESOURCE
					+ " file.\nExpected <portType> but saw <" + n.getNodeName() + ">");
			}

			_vectorMap = new BitVectorMap<PortType>(_knownPortTypes.values());
		} catch (Throwable cause) {
			_logger.info("exception occurred in constructor", cause);
			System.exit(1);
		} finally {
			StreamUtils.close(in);
		}
	}

	private PortType parsePortType(Element e) throws IOException
	{
		String name = e.getAttribute("name");
		String description = null;
		String rankString = null;

		if (name == null || name.length() == 0)
			throw new IOException("Port type found without a name.");

		NodeList children = e.getChildNodes();
		int length = children.getLength();
		for (int lcv = 0; lcv < length; lcv++) {
			Node child = children.item(lcv);
			if (child.getNodeType() == Node.ELEMENT_NODE) {
				Element ce = (Element) child;
				if (ce.getNodeName().equals("description"))
					description = ce.getTextContent();
				else if (ce.getNodeName().equals("display-rank"))
					rankString = ce.getTextContent();
				else
					throw new IOException("Unexpected node \"" + ce.getNodeName() + "\" found.\n"
						+ "Expected \"description\" or \"display-rank\".");
			}
		}

		if (rankString == null)
			throw new IOException("Error parsing known-porttypes -- couldn't find a " + "display rank for port type \"" + name
				+ "\".");

		return new PortType(QName.valueOf(name), Integer.parseInt(rankString), description);
	}

	public int getLargestKnownDescriptionLength()
	{
		return get_largestDescription();
	}

	public PortType getHighestRankedPortType(PortType... portTypes)
	{
		PortType ret = null;

		for (PortType portType : portTypes) {
			if (ret == null)
				ret = portType;
			else {
				if (ret.getDisplayRank() < portType.getDisplayRank())
					ret = portType;
			}
		}

		return ret;
	}

	public int get_largestDescription()
	{
		return _largestDescription;
	}

	public void set_largestDescription(int _largestDescription)
	{
		PortTypeAdministrator._largestDescription = _largestDescription;
	}

	public boolean isKnown(QName portType)
	{
		PortType pt = _knownPortTypes.get(portType);
		return pt != null;
	}

	public PortType get(QName portType)
	{
		PortType pt = _knownPortTypes.get(portType);
		if ((pt == null) && portType.getLocalPart().endsWith("JNDIAuthnPortType")) {
			/*
			 * skipping the error message for grids that are missing the jndi auth port type. only
			 * the sdiact-123 (xsede activity 123) grid had an issue with this, due to when it came
			 * online. this should never be triggered in any other grid, which is why we have it set
			 * to log at warning level.
			 */
			_logger.warn("unexpectedly providing alternative jndi auth port type return.");
			return TTYConstants.TTY_PORT_TYPE();
		}
		if (pt == null)
			throw new IllegalArgumentException("Port type \"" + portType + "\" is unknown to the system.");
		return pt;
	}

	public String translate(Collection<PortType> portTypes)
	{
		return _vectorMap.translate(portTypes);
	}

	public String translate(PortType portType)
	{
		return _vectorMap.translate(portType);
	}

	public String translate(PortType[] portTypes)
	{
		return _vectorMap.translate(portTypes);
	}

	public Collection<PortType> translate(String stringRep)
	{
		return _vectorMap.translate(stringRep);
	}
}
