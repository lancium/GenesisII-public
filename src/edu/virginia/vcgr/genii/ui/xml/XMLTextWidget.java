package edu.virginia.vcgr.genii.ui.xml;

import java.awt.Color;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedList;

import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;

import org.apache.axis.message.MessageElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.client.resource.ResourceException;

public class XMLTextWidget extends JTextPane
{
	static final long serialVersionUID = 0L;
	
	public Style PLAIN_STYLE = null;
	public Style HEADER_STYLE = null;
	public Style ERROR_STYLE = null;
	public Style ELEMENT_STYLE = null;
	public Style ATTRIBUTE_STYLE = null;
	
	static private Log _logger = LogFactory.getLog(XMLTextWidget.class);
	
	private void createStyles(StyledDocument exemplar)
	{
		PLAIN_STYLE = exemplar.addStyle("Plain", null);
		int size = StyleConstants.getFontSize(PLAIN_STYLE);
		
		HEADER_STYLE = exemplar.addStyle("Header", PLAIN_STYLE);
		StyleConstants.setFontSize(HEADER_STYLE, size + 2);
		StyleConstants.setBold(HEADER_STYLE, true);
		
		ERROR_STYLE = exemplar.addStyle("Error", PLAIN_STYLE);
		StyleConstants.setForeground(ERROR_STYLE, Color.RED);
		
		ELEMENT_STYLE = exemplar.addStyle("Element", PLAIN_STYLE);
		StyleConstants.setFontSize(ELEMENT_STYLE, size + 2);
		StyleConstants.setForeground(ELEMENT_STYLE, Color.BLUE);
		StyleConstants.setBold(ELEMENT_STYLE, true);
		
		ATTRIBUTE_STYLE = exemplar.addStyle("Attribute", ELEMENT_STYLE);
		StyleConstants.setFontSize(ATTRIBUTE_STYLE, size + 1);
		StyleConstants.setBold(ATTRIBUTE_STYLE, false);
		StyleConstants.setItalic(ATTRIBUTE_STYLE, true);
	}
	
	public XMLTextWidget()
	{
		createStyles(getStyledDocument());
		
		setFocusable(true);
		setEditable(false);
	}
	
	public XMLTextWidget(QName objectName, Object...objects)
	{
		this();
		
		for (Object object : objects)
		{
			try
			{
				appendDocument(objectName, object);
			}
			catch (Throwable cause)
			{
				appendError("Unable to serialize object to XML.", cause);
			}
		}
	}
	
	public XMLTextWidget(MessageElement...elements)
	{
		this();
		
		for (MessageElement me : elements)
		{
			try
			{
				appendDocument(me);
			}
			catch (Throwable cause)
			{
				appendError(
					"Unable to serialize MessageElement to XML.", cause);
			}
		}
	}
	
	public void clear()
	{
		try
		{
			StyledDocument doc = getStyledDocument();
			doc.remove(0, doc.getLength());
		}
		catch (Throwable cause)
		{
			_logger.warn("Unable to clear Text area.", cause);
		}
	}

	public void appendDocument(QName objectName, Object object) 
		throws ResourceException, XMLStreamException, FactoryConfigurationError
	{
		try
		{
			XMLPrettyPrinter pp = new XMLPrettyPrinter(
				new XMLTextWidgetFormatHandler());
			pp.formatDocument(objectName, object);
		}
		catch (IOException ioe)
		{
			appendError("Unable to format XML document.", ioe);
		}
		
		append(PLAIN_STYLE, "\n\n");
	}
	
	public void appendDocument(MessageElement me)
		throws XMLStreamException, FactoryConfigurationError, Exception
	{
		try
		{
			XMLPrettyPrinter pp = new XMLPrettyPrinter(
				new XMLTextWidgetFormatHandler());
			pp.formatDocument(me);
		}
		catch (IOException ioe)
		{
			appendError("Unable to format XML document.", ioe);
		}
		
		append(PLAIN_STYLE, "\n\n");
	}
	
	public void appendHeader(String contents)
	{
		append(HEADER_STYLE, contents);
	}
	
	public void appendDocument(XMLEventReader reader) throws XMLStreamException
	{
		try
		{
			XMLPrettyPrinter pp = new XMLPrettyPrinter(
				new XMLTextWidgetFormatHandler());
			pp.formatDocument(reader);
		}
		catch (IOException ioe)
		{
			appendError("Unable to format XML document.", ioe);
		}
		
		append(PLAIN_STYLE, "\n\n");
	}
	
	public void appendError(String contents)
	{
		appendError(contents, null);
	}
	
	public void appendError(String contents, Throwable cause)
	{
		StringWriter writer = new StringWriter();
		PrintWriter pWriter = new PrintWriter(writer);
		pWriter.format("%s:\n", contents);
		cause.printStackTrace(pWriter);
		pWriter.flush();
		pWriter.close();
		append(ERROR_STYLE, writer.toString());
	}
	
	public void append(Style style, String content)
	{
		StyledDocument doc = getStyledDocument();
		
		try
		{
			doc.insertString(doc.getLength(), content, style);
		}
		catch (BadLocationException e)
		{
			_logger.warn("Unable to append content to text document.");
		}
	}
	
	public boolean getScrollableTracksViewportWidth()
	{
		return false;
	}
	
	private class XMLTextWidgetFormatHandler implements XMLFormatHandler
	{
		private LinkedList<Style> _styles = new LinkedList<Style>();
		
		private XMLTextWidgetFormatHandler()
		{
			_styles.addFirst(PLAIN_STYLE);
		}
		
		@Override
		public void appendText(String text) throws IOException
		{
			append(_styles.peek(), text);
		}

		@Override
		public void endAttribute() throws IOException
		{
			_styles.pop();
		}

		@Override
		public void endElement() throws IOException
		{
			_styles.pop();
		}

		@Override
		public void startAttribute() throws IOException
		{
			_styles.push(ATTRIBUTE_STYLE);
		}

		@Override
		public void startElement() throws IOException
		{
			_styles.push(ELEMENT_STYLE);
		}
	}
}
