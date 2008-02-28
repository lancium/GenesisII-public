package edu.virginia.vcgr.genii.client.gui.browser.plugins.attrs;

import java.awt.Component;
import java.io.StringWriter;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.xml.namespace.QName;

import org.oasis_open.docs.wsrf.rp_2.GetResourcePropertyDocument;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.gui.browser.grid.ExpensiveInitializationPanel;
import edu.virginia.vcgr.genii.client.gui.browser.plugins.PluginException;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathDoesNotExistException;
import edu.virginia.vcgr.genii.client.ser.ObjectSerializer;
import edu.virginia.vcgr.genii.common.GeniiCommon;

/**
 * This is a special panel that the attributes plugin uses to display
 * the attributes document for a target resource.  We derive off of the
 * ExpensiveInitializationPanel class because getting the attributes
 * from a target endpoint could potentially be time consuming.  Therefor,
 * we implement a lazy resolution protocol suggested by that base class
 * panel.
 * 
 * @author mmm2a
 */
class ShowAttrsPanel extends ExpensiveInitializationPanel
{
	static final long serialVersionUID = 0L;

	private EndpointReferenceType _target;
	
	/**
	 * Create a new attributes panel with a given RNSPath as it's target
	 * 
	 * @param rnsPath The target RNSPath to show the attributes for.
	 * 
	 * @throws PluginException
	 */
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
		
		/*
		 * Go ahead and ask the target endpoint for it's attributes document.
		 */
		GeniiCommon common = ClientUtils.createProxy(GeniiCommon.class, _target);
		ObjectSerializer.serialize(writer, common.getResourcePropertyDocument(
			new GetResourcePropertyDocument()),
			new QName("http://tempuri.org", "object-attributes"));
		writer.flush();
		
		JTextArea area = new JTextArea(writer.toString());
		area.setEditable(false);
		
		return new JScrollPane(area);
	}
}