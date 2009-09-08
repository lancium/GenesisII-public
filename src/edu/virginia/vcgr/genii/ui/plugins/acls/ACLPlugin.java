package edu.virginia.vcgr.genii.ui.plugins.acls;

import java.util.Collection;

import javax.swing.JComponent;

import org.morgan.utils.gui.tearoff.TearoffPanel;

import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.ui.IconBasedTearoffThumb;
import edu.virginia.vcgr.genii.ui.plugins.AbstractUITabPlugin;
import edu.virginia.vcgr.genii.ui.plugins.EndpointDescription;
import edu.virginia.vcgr.genii.ui.plugins.LazilyLoadedTab;
import edu.virginia.vcgr.genii.ui.plugins.UIPluginContext;

public class ACLPlugin extends AbstractUITabPlugin
{
	@Override
	public JComponent getComponent(UIPluginContext context)
	{
		Collection<RNSPath> paths = 
			context.endpointRetriever().getTargetEndpoints();
		
		ACLPanel aclPanel = new ACLPanel(context, paths.iterator().next());
		return new LazilyLoadedTab(aclPanel, new TearoffPanel(
			aclPanel, aclPanel.createTearoffHandler(),
			new IconBasedTearoffThumb()));
	}

	@Override
	public boolean isEnabled(
			Collection<EndpointDescription> selectedDescriptions)
	{
		return selectedDescriptions.size() == 1;
	}
}