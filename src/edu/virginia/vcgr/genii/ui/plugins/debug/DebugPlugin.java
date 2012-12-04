package edu.virginia.vcgr.genii.ui.plugins.debug;

import java.util.Collection;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JScrollPane;

import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.ui.plugins.AbstractCombinedUIPlugin;
import edu.virginia.vcgr.genii.ui.plugins.EndpointDescription;
import edu.virginia.vcgr.genii.ui.plugins.MenuType;
import edu.virginia.vcgr.genii.ui.plugins.UIPluginContext;
import edu.virginia.vcgr.genii.ui.plugins.UIPluginException;
import edu.virginia.vcgr.genii.ui.plugins.UIPopupMenuPlugin;
import edu.virginia.vcgr.genii.ui.plugins.UITabPlugin;
import edu.virginia.vcgr.genii.ui.plugins.UITopMenuPlugin;

@SuppressWarnings("rawtypes")
public class DebugPlugin extends AbstractCombinedUIPlugin
	implements UITopMenuPlugin, UIPopupMenuPlugin, UITabPlugin
{
	@Override
	protected void performMenuAction(UIPluginContext context, MenuType menuType)
			throws UIPluginException
	{
		for (RNSPath path : context.endpointRetriever().getTargetEndpoints())
		{
			System.err.format("%s Menu:  %s\n", 
				(menuType == MenuType.POPUP_MENU) ? "Popup" : "Top",
				path.pwd());
		}
	}

	@Override
	public boolean isEnabled(
		Collection<EndpointDescription> selectedDescriptions)
	{
		return selectedDescriptions.size() > 0;
	}

	@SuppressWarnings("unchecked")
    @Override
	public JComponent getComponent(UIPluginContext context)
	{
		Vector<String> vector = new Vector<String>();
		for (RNSPath path : context.endpointRetriever().getTargetEndpoints())
			vector.add(path.pwd());
		
		JList list = new JList(vector);
		return new JScrollPane(list);
	}
}