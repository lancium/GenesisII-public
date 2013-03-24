package edu.virginia.vcgr.genii.ui.shell;

import java.awt.Color;
import java.awt.Font;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.ui.UIContext;
import edu.virginia.vcgr.genii.ui.prefs.shell.ShellUIPreferenceSet;

public class CommandDisplay extends JTextPane
{
	static final long serialVersionUID = 0L;

	static private Log _logger = LogFactory.getLog(CommandDisplay.class);

	private Style _plainStyle;
	private Style _commandStyle;
	private Style _headerStyle;
	private Style _errorStyle;

	private PrintWriter _headerWriter;
	private PrintWriter _commandWriter;
	private PrintWriter _outputWriter;
	private PrintWriter _errorWriter;

	private void write(String str, Style myStyle)
	{
		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(new WriterTask(str, myStyle));
			return;
		}

		StyledDocument doc = getStyledDocument();
		synchronized (doc) {
			try {
				doc.insertString(doc.getLength(), str, myStyle);
				setCaretPosition(doc.getLength());
			} catch (BadLocationException ble) {
				_logger.warn("Unable to insert text to end of document.", ble);
			}
		}
	}

	private void start()
	{
		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(new Starter());
			return;
		}

		StyledDocument doc = getStyledDocument();
		synchronized (doc) {
			if (doc.getLength() != 0) {
				try {
					doc.insertString(doc.getLength(), "\n\n", _plainStyle);
				} catch (BadLocationException ble) {
					_logger.warn("Unable to insert text to end of document.", ble);
				}
			}
		}
	}

	public CommandDisplay(UIContext uiContext)
	{
		super();

		StyledDocument doc = getStyledDocument();

		_plainStyle = doc.addStyle("Plain", null);
		Font font = uiContext.preferences().preferenceSet(ShellUIPreferenceSet.class).shellFont();
		StyleConstants.setFontFamily(_plainStyle, font.getFamily());
		StyleConstants.setFontSize(_plainStyle, font.getSize());
		int size = StyleConstants.getFontSize(_plainStyle);

		_headerStyle = doc.addStyle("Header", _plainStyle);
		StyleConstants.setFontSize(_headerStyle, size + 2);
		StyleConstants.setBold(_headerStyle, true);

		_commandStyle = doc.addStyle("Command", _plainStyle);
		StyleConstants.setFontSize(_commandStyle, size + 2);
		StyleConstants.setItalic(_commandStyle, true);
		StyleConstants.setForeground(_commandStyle, Color.BLUE);

		_errorStyle = doc.addStyle("Error", _plainStyle);
		StyleConstants.setForeground(_errorStyle, Color.RED);

		_headerWriter = new PrintWriter(new CommandDisplayWriter(_headerStyle));
		_commandWriter = new PrintWriter(new CommandDisplayWriter(_commandStyle));
		_outputWriter = new PrintWriter(new CommandDisplayWriter(_plainStyle));
		_errorWriter = new PrintWriter(new CommandDisplayWriter(_errorStyle));

		setEditable(false);
	}

	public Display display()
	{
		return new DisplayImpl();
	}

	private class DisplayImpl implements Display
	{
		@Override
		public void start()
		{
			CommandDisplay.this.start();
		}

		@Override
		public PrintWriter command()
		{
			return _commandWriter;
		}

		@Override
		public PrintWriter error()
		{
			return _errorWriter;
		}

		@Override
		public PrintWriter header()
		{
			return _headerWriter;
		}

		@Override
		public PrintWriter output()
		{
			return _outputWriter;
		}
	}

	private class CommandDisplayWriter extends Writer
	{
		private Style _myStyle;

		private CommandDisplayWriter(Style myStyle)
		{
			_myStyle = myStyle;
		}

		@Override
		public void close() throws IOException
		{
			// Ignore
		}

		@Override
		public void flush() throws IOException
		{
			// Ignore
		}

		@Override
		public void write(char[] cbuf, int off, int len) throws IOException
		{
			CommandDisplay.this.write(new String(cbuf, off, len), _myStyle);
		}
	}

	private class WriterTask implements Runnable
	{
		private String _string;
		private Style _myStyle;

		private WriterTask(String str, Style myStyle)
		{
			_string = str;
			_myStyle = myStyle;
		}

		@Override
		public void run()
		{
			write(_string, _myStyle);
		}
	}

	private class Starter implements Runnable
	{
		private Starter()
		{
		}

		@Override
		public void run()
		{
			start();
		}
	}
}