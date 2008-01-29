package edu.virginia.vcgr.genii.client.gui.browser.plugins.sys;

import java.awt.Component;
import java.io.StringWriter;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.xml.namespace.QName;

import edu.virginia.vcgr.genii.client.gui.browser.plugins.ITabPlugin;
import edu.virginia.vcgr.genii.client.gui.browser.plugins.PluginException;
import edu.virginia.vcgr.genii.client.gui.browser.plugins.PluginStatus;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathDoesNotExistException;
import edu.virginia.vcgr.genii.client.ser.ObjectSerializer;

public class DefaultSelectionDisplayPlugin implements ITabPlugin
{
	private JScrollPane _scrollPane;
	private JTextArea _textArea;
	
	public DefaultSelectionDisplayPlugin()
	{
		_textArea = new JTextArea();
		_scrollPane = new JScrollPane(_textArea);
	}
	
	@Override
	public Component getComponent(RNSPath[] selectedPaths)
			throws PluginException
	{
		boolean first = true;
		StringBuilder builder = new StringBuilder();
		
		for (RNSPath path : selectedPaths)
		{
			if (!first)
				builder.append("\n\n");
			first = false;
			
			builder.append("RNS Path:  " + path.pwd() + "\n");
			builder.append("Endpoint:");
			
			try
			{
				StringWriter writer = new StringWriter();
				ObjectSerializer.serialize(writer, path.getEndpoint(),
					new QName("http://tempuri.org", "EPR"));
				writer.flush();
				builder.append("\n");
				builder.append(writer.toString());
				builder.append("\n");
			}
			catch (RNSPathDoesNotExistException e)
			{
				builder.append("  Does not exist!\n");
			}
			catch (ResourceException re)
			{
				builder.append("  Unable to serialize EPR!\n");
			}
		}
		
		try
		{
			Document doc = _textArea.getDocument();
			doc.remove(0, doc.getLength());
			_textArea.insert(builder.toString(), 0);
			_textArea.setCaretPosition(0);
			
			return _scrollPane;
		}
		catch (BadLocationException ble)
		{
			// This really shouldn't happen
			throw new PluginException(
				"Unexpected exception trying to describe endpoint.", ble);
		}
	}

	@Override
	public PluginStatus getStatus(RNSPath[] selectedResources)
			throws PluginException
	{
		return PluginStatus.ACTIVTE;
	}
}