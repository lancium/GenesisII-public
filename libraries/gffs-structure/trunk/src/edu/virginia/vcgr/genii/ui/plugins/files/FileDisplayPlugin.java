package edu.virginia.vcgr.genii.ui.plugins.files;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collection;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.text.Style;

import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.client.byteio.ByteIOStreamFactory;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.gui.swing.GuiStatusTools;
import edu.virginia.vcgr.genii.ui.plugins.AbstractUITabPlugin;
import edu.virginia.vcgr.genii.ui.plugins.EndpointDescription;
import edu.virginia.vcgr.genii.ui.plugins.LazilyLoadedTab;
import edu.virginia.vcgr.genii.ui.plugins.LazyLoadTabHandler;
import edu.virginia.vcgr.genii.ui.plugins.UIPluginContext;

public class FileDisplayPlugin extends AbstractUITabPlugin
{
	/*
	 * how large of a text chunk we will fling at the window for display. this is carefully chosen
	 * to avoid swamping the UI.
	 */
	static final private int BUFFER_SIZE = 32 * 1024; // 32k chunks.

	/*
	 * interval between blasts of text updates. these are also carefully chosen to avoid swamping
	 * the UI.
	 */
	static final private int LAUNCHER_SNOOZE_DURATION = 100;
	static final private int UPDATER_SNOOZE_DURATION = 10;

	@Override
	public JComponent getComponent(UIPluginContext context)
	{
		FileDisplayWidget widget = new FileDisplayWidget();
		TextLoadHandler impl = new TextLoadHandler(context, widget);
		return new LazilyLoadedTab(impl, new JScrollPane(widget));
	}

	@Override
	public boolean isEnabled(Collection<EndpointDescription> selectedDescriptions)
	{
		if (selectedDescriptions == null || selectedDescriptions.size() != 1)
			return false;

		TypeInformation tp = selectedDescriptions.iterator().next().typeInformation();
		return (tp.isByteIO() && !(/* tp.isContainer() || tp.isBESContainer() || */tp.isQueue() || tp.isIDP()));
	}

	static private class TextLoadHandler implements LazyLoadTabHandler
	{
		private UIPluginContext _context;
		private FileDisplayWidget _widget;

		private TextLoadHandler(UIPluginContext context, FileDisplayWidget widget)
		{
			_widget = widget;
			_context = context;
		}

		@Override
		public void load()
		{
			Collection<RNSPath> paths = _context.endpointRetriever().getTargetEndpoints();

			_context.uiContext().executor().submit(new DocumentRetriever(_widget, paths.iterator().next()));
		}
	}

	static private class DocumentRetriever implements Runnable
	{
		private RNSPath _path;
		private FileDisplayWidget _widget;

		private DocumentRetriever(FileDisplayWidget widget, RNSPath path)
		{
			_path = path;
			_widget = widget;
		}

		@Override
		public void run()
		{
			// start the busy spinner since app is going to block on IO.
			GuiStatusTools.showApplicationIsBusy(_widget);

			SwingUtilities.invokeLater(new DocumentUpdater(false, _widget.UPDATING_STYLE, "Reading file contents...", _widget,
				false));

			StringBuilder builder = new StringBuilder();
			char[] data = new char[BUFFER_SIZE];
			int read;

			InputStream in = null;
			Reader reader = null;
			try {
				in = ByteIOStreamFactory.createInputStream(_path);
				reader = new InputStreamReader(in);
				boolean readAnything = false;
				while ((read = reader.read(data, 0, BUFFER_SIZE)) > 0) {
					builder.append(data, 0, read);
					if (builder.length() > 0) {
						if (!readAnything) {
							// this is our first chunk of data, so we want to also clear the panel.
							SwingUtilities.invokeLater(new DocumentUpdater(false, _widget.PLAIN_STYLE, "", _widget, false));
							readAnything = true; // now we have.
						}
						SwingUtilities.invokeLater(new DocumentUpdater(true, _widget.PLAIN_STYLE, builder.toString(), _widget,
							false));
						builder.delete(0, builder.length());
					}
					if (!reader.ready()) {
						// if we finally got to the end of the file once, we break out.
						break;
					}
					// attempt to not be so huge in memory usage.
					System.gc();
					// yield the thread to the gui updater.
					Thread.sleep(LAUNCHER_SNOOZE_DURATION);

					// paranoia check to see if we still basically exist.
					if (!_widget.isDisplayable() || !_widget.isEnabled()) {
						// we got moved away from or something.
						break;
					}
				}
				if (!readAnything) {
					// we never read any data, so remove the "reading file" label.
					_widget.clear();
					// also turn off busy spinner, since we are done doing nothing.
					GuiStatusTools.showApplicationIsResponsive(_widget);
				} else {
					// send a last invisible update just to turn off busy spinner.
					SwingUtilities.invokeLater(new DocumentUpdater(true, _widget.PLAIN_STYLE, "", _widget, true));
				}
			} catch (Throwable e) {
				SwingUtilities.invokeLater(new DocumentUpdater(false, _widget.ERROR_STYLE, "Unable to read file contents:  "
					+ e, _widget, true));
			} finally {
				StreamUtils.close(reader);
				StreamUtils.close(in);
			}
		}
	}

	static private class DocumentUpdater implements Runnable
	{
		private String _content;
		private Style _style;
		private FileDisplayWidget _widget;
		private boolean _append;
		private boolean _lastUpdate;

		private DocumentUpdater(boolean append, Style style, String content, FileDisplayWidget widget, boolean lastUpdate)
		{
			_style = style;
			_content = content;
			_widget = widget;
			_append = append;
			_lastUpdate = lastUpdate;
		}

		@Override
		public void run()
		{
			/*
			 * it seems like the busy spinner can still disappear before the app is responsive
			 * again, so this is intended to keep the busy cursor turned on until all the updates
			 * are finished.
			 */
			GuiStatusTools.showApplicationIsBusy(_widget);

			if (!_widget.isDisplayable() || !_widget.isEnabled()) {
				// we got moved away from or something.
				return;
			}
			if (!_append) {
				_widget.clear();
			}
			_widget.append(_style, _content);
			if (!_append) {
				_widget.setCaretPosition(0);
			} else {
				_widget.setCaretPosition(_widget.getDocument().getLength() - 1);
			}

			// force the screen to update now.
			_widget.getRootPane().validate();
			_widget.getRootPane().repaint();

			if (_lastUpdate) {
				// now we can totally clear out the busy indicator.
				GuiStatusTools.showApplicationIsResponsive(_widget);
			} else {
				try {
					// ensure that we're trashing our temporaries.
					System.gc();
					/*
					 * snooze to yield the processor here also, which should allow some of the
					 * queued updates to occur.
					 */
					Thread.sleep(UPDATER_SNOOZE_DURATION);
				} catch (InterruptedException e) {
					// ignored.
				}
			}
		}
	}
}
