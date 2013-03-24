package edu.virginia.vcgr.genii.client.utils.xml;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

/**
 * Takes a string with XML code and returns a string of nicely formatted XML.
 * 
 * @author Chris Koeritz inspired by posting:
 *         http://stackoverflow.com/questions/139076/how-to-pretty-print-xml-from-java
 */
public class XMLStringPrinter
{
	static private Log _logger = LogFactory.getLog(XMLStringPrinter.class);

	// formats the chunk of XML in xmlString with reasonably nice indentation, i.e. it pretty prints
	// the XML code. if there's a failure during parsing/formatting, then null is returned.
	public static String format(String xmlString)
	{
		try {
			final Document doc = parseXML(xmlString);
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

	private static Document parseXML(String xmlString) throws ParserConfigurationException, SAXException, IOException
	{
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = dbf.newDocumentBuilder();
		InputSource source = new InputSource(new StringReader(xmlString));
		return builder.parse(source);
	}
}
