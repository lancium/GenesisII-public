package edu.virginia.vcgr.genii.ui.plugins.files;

import java.awt.Color;

import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FileDisplayWidget extends JTextPane
{
	static final long serialVersionUID = 0L;
	
	static private Log _logger = LogFactory.getLog(FileDisplayWidget.class);
	
	public Style PLAIN_STYLE = null;
	public Style UPDATING_STYLE = null;
	public Style ERROR_STYLE = null;
	
	private void createStyles(StyledDocument exemplar)
	{
		PLAIN_STYLE = exemplar.addStyle("Plain", null);
		
		UPDATING_STYLE = exemplar.addStyle("Updating", PLAIN_STYLE);
		StyleConstants.setItalic(UPDATING_STYLE, true);
		StyleConstants.setForeground(UPDATING_STYLE, Color.darkGray);
		
		ERROR_STYLE = exemplar.addStyle("Error", PLAIN_STYLE);
		StyleConstants.setItalic(ERROR_STYLE, true);
		StyleConstants.setForeground(ERROR_STYLE, Color.red);
	}
	
	public FileDisplayWidget()
	{
		createStyles(getStyledDocument());
		
		setFocusable(false);
		setEditable(false);
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
	
	public void append(Style style, String text)
	{
		try
		{
			StyledDocument doc = getStyledDocument();
			doc.insertString(doc.getLength(), text, style);
		}
		catch (BadLocationException ble)
		{
			_logger.warn("Unable to append content to text document.", ble);
		}
	}
	
	@Override
	public boolean getScrollableTracksViewportWidth()
	{
		return false;
	}
}