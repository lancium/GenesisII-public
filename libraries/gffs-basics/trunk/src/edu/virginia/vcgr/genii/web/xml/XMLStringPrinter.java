package edu.virginia.vcgr.genii.web.xml;

import java.io.StringWriter;
import java.io.Writer;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

import edu.virginia.vcgr.genii.text.TextHelper;

/**
 * Takes a string with XML code and returns a string of nicely formatted XML.
 * 
 * @author Chris Koeritz inspired by posting:
 *         http://stackoverflow.com/questions/139076/how-to-pretty-print-xml-from-java
 */
public class XMLStringPrinter
{
	static private Log _logger = LogFactory.getLog(XMLStringPrinter.class);

	// formats the chunk of XML in xmlString with reasonably nice indentation, i.e. it pretty-prints
	// the XML code. if there's a failure during parsing/formatting, then null is returned.
	public static String format(String xmlString)
	{
		try {
			final Document doc = XMLUtilities.parseXML(xmlString);
			OutputFormat format = new OutputFormat(doc);
			format.setLineWidth(79);
			format.setIndenting(true);
			format.setIndent(4);
			Writer w = new StringWriter();
			XMLSerializer s = new XMLSerializer(w, format);
			s.serialize(doc);
			return w.toString();
		} catch (Throwable e) {
			String msg = "failed to parse and print XML" + e.getMessage();
			_logger.info(msg);
			return msg;
		}
	}

	/**
	 * dives down into the node and shows it and its kids' names.
	 */
	static public String showNodes(Node n, int indent)
	{
		if (n == null)
			return "";
		StringBuilder toReturn = new StringBuilder(TextHelper.indentation(indent) + "name=" + n.getNodeName());
		NodeList kids = n.getChildNodes();
		for (int i = 0; i < kids.getLength(); i++) {
			Node kid = kids.item(i);
			toReturn.append(showNodes(kid, indent + 2));
		}
		return toReturn.toString();
	}

	/**
	 * a simple dump of the current node and descendants, with no extra formatting. good for
	 * comparing fouled-up bits of xml against each other.
	 */
	public static String nodeToString(Node node, boolean omitXmlDeclare)
	{
		if (node == null)
			return "null";
		StringWriter sw = new StringWriter();
		try {
			Transformer t = TransformerFactory.newInstance().newTransformer();
			if (omitXmlDeclare)
				t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			// t.setOutputProperty(OutputKeys.INDENT, "yes");
			t.transform(new DOMSource(node), new StreamResult(sw));
		} catch (TransformerException te) {
			_logger.error("nodeToString Transformer Exception", te);
		}
		return sw.toString();
	}

}
