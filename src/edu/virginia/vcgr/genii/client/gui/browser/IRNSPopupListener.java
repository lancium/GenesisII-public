package edu.virginia.vcgr.genii.client.gui.browser;

import java.util.Collection;

import javax.swing.JMenuItem;

import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.resource.TypeInformation;

public interface IRNSPopupListener
{
	public void addPopupItems(EndpointReferenceType target,
		TypeInformation targetTypeInformation, Collection<JMenuItem> popups);
}