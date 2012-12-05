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
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.ui.plugins.AbstractUITabPlugin;
import edu.virginia.vcgr.genii.ui.plugins.EndpointDescription;
import edu.virginia.vcgr.genii.ui.plugins.LazilyLoadedTab;
import edu.virginia.vcgr.genii.ui.plugins.LazyLoadTabHandler;
import edu.virginia.vcgr.genii.ui.plugins.UIPluginContext;

public class FileDisplayPlugin extends AbstractUITabPlugin
{
	@Override
	public JComponent getComponent(UIPluginContext context)
	{
		FileDisplayWidget widget = new FileDisplayWidget();
		TextLoadHandler impl = new TextLoadHandler(context, widget);
		return new LazilyLoadedTab(impl, new JScrollPane(widget));
	}

	@Override
	public boolean isEnabled(
			Collection<EndpointDescription> selectedDescriptions)
	{
		if (selectedDescriptions == null || selectedDescriptions.size() != 1)
			return false;
		
		return selectedDescriptions.iterator().next().typeInformation().isByteIO();
	}
	
	static private class TextLoadHandler implements LazyLoadTabHandler
	{
		private UIPluginContext _context;
		private FileDisplayWidget _widget;
		
		private TextLoadHandler(UIPluginContext context,
			FileDisplayWidget widget)
		{
			_widget = widget;
			_context = context;
		}
		
		@Override
		public void load()
		{
			Collection<RNSPath> paths = 
				_context.endpointRetriever().getTargetEndpoints();
			
			_context.uiContext().executor().submit(new DocumentRetriever(
				_widget, paths.iterator().next()));
		}
	}
	
	static private class DocumentRetriever implements Runnable
	{
		// large buffer size to help us jump ahead of all possible writes and arrive
		// at the end before log4j can add more lines.
		static final private int BUFFER_SIZE = 1024 * 1024 * 1;
		
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
			SwingUtilities.invokeLater(new DocumentUpdater(false,
				_widget.UPDATING_STYLE,
				"Reading file contents...", _widget));
			
			StringBuilder builder = new StringBuilder();
			char []data = new char[BUFFER_SIZE];
			int read;
			
			InputStream in = null;
			try
			{
				in = ByteIOStreamFactory.createInputStream(_path);
				Reader reader = new InputStreamReader(in);
				while ((read = reader.read(data, 0, BUFFER_SIZE)) > 0)
				{
					builder.append(data, 0, read);
					if (builder.length() > 0) {
						SwingUtilities.invokeLater(new DocumentUpdater(true,
							_widget.PLAIN_STYLE, builder.toString(), _widget));
						builder.delete(0, builder.length());
					}
					if (!reader.ready()) {
						// if we finally got to the end of the file once, we break out.
						break;
					}
					// attempt to not be so huge.
					System.gc();
					// yield the thread to the gui updater.
					Thread.sleep(200);
				}
				
			}
			catch (Throwable e)
			{
				SwingUtilities.invokeLater(new DocumentUpdater(false,
					_widget.ERROR_STYLE,
					"Unable to read file contents:  " + e, _widget));
			}
			finally
			{
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
		
		private DocumentUpdater(boolean append, Style style,
			String content, FileDisplayWidget widget)
		{
			_style = style;
			_content = content;
			_widget = widget;
			_append = append;
		}
		
		@Override
		public void run()
		{
			if (!_append) _widget.clear();
			
			_widget.append(_style, _content);
			if (!_append)
				_widget.setCaretPosition(0);
			else
				_widget.setCaretPosition(_widget.getDocument().getLength() - 1);
		}
	}
}
