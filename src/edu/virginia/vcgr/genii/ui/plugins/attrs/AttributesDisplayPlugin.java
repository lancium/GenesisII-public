package edu.virginia.vcgr.genii.ui.plugins.attrs;

import java.util.Collection;
import java.util.LinkedList;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreeSelectionModel;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;

import org.morgan.util.Pair;
import org.oasis_open.docs.wsrf.rp_2.GetResourcePropertyDocument;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.common.GeniiCommon;
import edu.virginia.vcgr.genii.ui.UIContext;
import edu.virginia.vcgr.genii.ui.plugins.AbstractUITabPlugin;
import edu.virginia.vcgr.genii.ui.plugins.EndpointDescription;
import edu.virginia.vcgr.genii.ui.plugins.LazilyLoadedTab;
import edu.virginia.vcgr.genii.ui.plugins.LazyLoadTabHandler;
import edu.virginia.vcgr.genii.ui.plugins.UIPluginContext;
import edu.virginia.vcgr.genii.ui.prefs.UIPreferences;
import edu.virginia.vcgr.genii.ui.prefs.xml.XMLUIPreferenceSet;
import edu.virginia.vcgr.genii.ui.xml.DefaultXMLTreeSource;
import edu.virginia.vcgr.genii.ui.xml.XMLTextWidget;
import edu.virginia.vcgr.genii.ui.xml.XMLTree;
import edu.virginia.vcgr.genii.ui.xml.XMLTreeSelectionWidget;
import edu.virginia.vcgr.genii.ui.xml.XMLTreeSource;

public class AttributesDisplayPlugin extends AbstractUITabPlugin
{
	static private final QName ATTR_NAME = new QName(GenesisIIConstants.GENESISII_NS, "resource-properties");

	@Override
	public JComponent getComponent(UIPluginContext context)
	{
		UIPreferences prefs = context.uiContext().preferences();
		XMLUIPreferenceSet set = prefs.preferenceSet(XMLUIPreferenceSet.class);

		if (set.preferText()) {
			XMLTextWidget widget = new XMLTextWidget();
			TextLoadHandler impl = new TextLoadHandler(context, widget);
			return new LazilyLoadedTab(impl, new JScrollPane(widget));
		} else {
			XMLTree tree = new XMLTree("Attributes");
			XMLLoadHandler handler = new XMLLoadHandler(context, tree);
			tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
			XMLTreeSelectionWidget textWidget = new XMLTreeSelectionWidget();
			tree.addTreeSelectionListener(textWidget);
			return new LazilyLoadedTab(handler, new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(tree),
				new JScrollPane(textWidget)));
		}
	}

	@Override
	public boolean isEnabled(Collection<EndpointDescription> selectedDescriptions)
	{
		return selectedDescriptions.size() == 1;
	}

	static private class TextLoadHandler implements LazyLoadTabHandler
	{
		private UIPluginContext _context;
		private XMLTextWidget _widget;

		private TextLoadHandler(UIPluginContext context, XMLTextWidget widget)
		{
			_widget = widget;
			_context = context;
		}

		@Override
		public void load()
		{
			Collection<RNSPath> paths = _context.endpointRetriever().getTargetEndpoints();

			_context.uiContext().executor().submit(new AttributeRetriever(_context.uiContext(), _widget, paths));
		}
	}

	static private class AttributeRetriever implements Runnable
	{
		private UIContext _context;
		private Collection<RNSPath> _paths;
		private XMLTextWidget _widget;

		private AttributeRetriever(UIContext context, XMLTextWidget widget, Collection<RNSPath> paths)
		{
			_context = context;
			_paths = paths;
			_widget = widget;
		}

		@Override
		public void run()
		{
			Collection<Pair<RNSPath, AttributeResult>> results = new LinkedList<Pair<RNSPath, AttributeResult>>();

			for (RNSPath path : _paths)
				results.add(new Pair<RNSPath, AttributeResult>(path, null));

			XMLWidgetUpdater updater = new XMLWidgetUpdater(_widget, results);

			SwingUtilities.invokeLater(updater);

			for (Pair<RNSPath, AttributeResult> pair : results) {
				try {
					GeniiCommon common = ClientUtils.createProxy(GeniiCommon.class, pair.first().getEndpoint(),
						_context.callingContext());
					synchronized (pair) {
						pair.second(new AttributeResult(common.getResourcePropertyDocument(new GetResourcePropertyDocument())));
					}
				} catch (Throwable cause) {
					pair.second(new AttributeResult("Unable to acquire resource properties for endpoint.", cause));
				} finally {
					SwingUtilities.invokeLater(updater);
				}
			}
		}
	}

	static private class XMLLoadHandler implements LazyLoadTabHandler
	{
		private UIPluginContext _context;
		private XMLTree _widget;

		private XMLLoadHandler(UIPluginContext context, XMLTree widget)
		{
			_widget = widget;
			_context = context;
		}

		@Override
		public void load()
		{
			Collection<RNSPath> paths = _context.endpointRetriever().getTargetEndpoints();
			for (RNSPath path : paths) {
				_widget.add(_context.uiContext(), path.pwd(), new AttributesTreeSource(_context.uiContext(), path));
			}
		}
	}

	static private class XMLWidgetUpdater implements Runnable
	{
		private XMLTextWidget _widget;
		private Collection<Pair<RNSPath, AttributeResult>> _results;

		private XMLWidgetUpdater(XMLTextWidget widget, Collection<Pair<RNSPath, AttributeResult>> results)
		{
			_widget = widget;
			_results = results;
		}

		@Override
		public void run()
		{
			_widget.clear();

			for (Pair<RNSPath, AttributeResult> pair : _results) {
				synchronized (pair) {
					_widget.appendHeader(String.format("Resource properties for %s:", pair.first().pwd()));

					AttributeResult result = pair.second();
					if (result == null) {
						_widget.appendHeader("  Updating...");
					} else {
						_widget.append(_widget.PLAIN_STYLE, "\n\n");

						if (result.isError())
							_widget.appendError(result.message(), result.cause());
						else {
							try {
								_widget.appendDocument(ATTR_NAME, result.properties());
							} catch (Throwable cause) {
								_widget.appendError("Unable to format XML document.", cause);
							}
						}
					}
				}

				_widget.append(_widget.PLAIN_STYLE, "\n\n");
				_widget.setCaretPosition(0);
			}
		}
	}

	static private class AttributesTreeSource implements XMLTreeSource
	{
		private UIContext _context;
		private RNSPath _path;

		private AttributesTreeSource(UIContext uiContext, RNSPath path)
		{
			_context = uiContext;
			_path = path;
		}

		@Override
		public XMLEventReader getReader() throws Throwable
		{
			GeniiCommon common = ClientUtils.createProxy(GeniiCommon.class, _path.getEndpoint(), _context.callingContext());
			return new DefaultXMLTreeSource(common.getResourcePropertyDocument(new GetResourcePropertyDocument())).getReader();
		}
	}
}