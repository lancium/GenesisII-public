package edu.virginia.vcgr.genii.ui.plugins.epr;

import java.util.Collection;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.tree.TreeSelectionModel;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.ui.plugins.AbstractUITabPlugin;
import edu.virginia.vcgr.genii.ui.plugins.EndpointDescription;
import edu.virginia.vcgr.genii.ui.plugins.UIPluginContext;
import edu.virginia.vcgr.genii.ui.plugins.UITabPlugin;
import edu.virginia.vcgr.genii.ui.prefs.UIPreferences;
import edu.virginia.vcgr.genii.ui.prefs.xml.XMLUIPreferenceSet;
import edu.virginia.vcgr.genii.ui.xml.DefaultXMLTreeSource;
import edu.virginia.vcgr.genii.ui.xml.XMLTextWidget;
import edu.virginia.vcgr.genii.ui.xml.XMLTree;
import edu.virginia.vcgr.genii.ui.xml.XMLTreeSelectionWidget;
import edu.virginia.vcgr.genii.ui.xml.XMLTreeSource;

public class EPRDisplayPlugin extends AbstractUITabPlugin implements UITabPlugin
{
	static final private QName EPR_NAME = new QName(GenesisIIConstants.GENESISII_NS, "EPR");

	@Override
	public JComponent getComponent(UIPluginContext context)
	{
		UIPreferences prefs = context.uiContext().preferences();
		XMLUIPreferenceSet set = prefs.preferenceSet(XMLUIPreferenceSet.class);

		if (set.preferText()) {
			XMLTextWidget widget = new XMLTextWidget();

			Collection<RNSPath> paths = context.endpointRetriever().getTargetEndpoints();
			for (RNSPath path : paths) {
				widget.appendHeader(String.format("EPR for %s:\n\n", path.pwd()));
				try {
					widget.appendDocument(EPR_NAME, path.getEndpoint());
				} catch (Throwable cause) {
					widget.appendError(String.format("Unable to format/lookup EPR:  %s", cause.getLocalizedMessage()));
				}

				widget.append(widget.PLAIN_STYLE, "\n\n");
			}

			return new JScrollPane(widget);
		} else {
			XMLTree widget = new XMLTree("EPRs");

			Collection<RNSPath> paths = context.endpointRetriever().getTargetEndpoints();
			for (RNSPath path : paths) {
				widget.add(context.uiContext(), path.pwd(), new EPRTreeSource(path));
			}

			XMLTreeSelectionWidget textWidget = new XMLTreeSelectionWidget();
			widget.addTreeSelectionListener(textWidget);
			widget.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

			return new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(widget), new JScrollPane(textWidget));
		}
	}

	@Override
	public boolean isEnabled(Collection<EndpointDescription> selectedDescriptions)
	{
		return selectedDescriptions.size() > 0;
	}

	static private class EPRTreeSource implements XMLTreeSource
	{
		private RNSPath _path;

		private EPRTreeSource(RNSPath path)
		{
			_path = path;
		}

		@Override
		public XMLEventReader getReader() throws Throwable
		{
			return new DefaultXMLTreeSource(_path.getEndpoint()).getReader();
		}
	}
}