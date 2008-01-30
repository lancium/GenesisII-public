package edu.virginia.vcgr.genii.client.gui.browser.plugins.attrs;

import java.awt.Component;
import java.io.StringWriter;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.xml.namespace.QName;

import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.gui.browser.grid.ExpensiveInitializationPanel;
import edu.virginia.vcgr.genii.client.gui.browser.plugins.PluginException;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathDoesNotExistException;
import edu.virginia.vcgr.genii.client.ser.ObjectSerializer;
import edu.virginia.vcgr.genii.common.GeniiCommon;

public class ShowAttrsPanel extends ExpensiveInitializationPanel
{
	static final long serialVersionUID = 0L;

	private EndpointReferenceType _target;
	
	public ShowAttrsPanel(RNSPath rnsPath) throws PluginException
	{
		try
		{
			_target = rnsPath.getEndpoint();
		}
		catch (RNSPathDoesNotExistException e)
		{
			throw new PluginException("RNS Path \"" + rnsPath.pwd() 
				+ "\" does not exist.", e);
		}
	}
	
	@Override
	protected Component resolveComponent() throws Throwable
	{
		StringWriter writer = new StringWriter();
		GeniiCommon common = ClientUtils.createProxy(GeniiCommon.class, _target);
		ObjectSerializer.serialize(writer, common.getAttributesDocument(null),
			new QName("http://tempuri.org", "object-attributes"));
		writer.flush();
		
		JTextArea area = new JTextArea(writer.toString());
		area.setEditable(false);
		return new JScrollPane(area);
	}
}