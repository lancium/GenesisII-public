package edu.virginia.vcgr.genii.ui.plugins.jobs;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JFileChooser;

import org.morgan.util.io.StreamUtils;
import org.morgan.utils.gui.GUIUtils;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.byteio.ByteIOStreamFactory;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.history.HistoryEvent;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathDoesNotExistException;
import edu.virginia.vcgr.genii.ui.UIContext;
import edu.virginia.vcgr.genii.ui.errors.ErrorHandler;
import edu.virginia.vcgr.genii.ui.plugins.AbstractCombinedUIMenusPlugin;
import edu.virginia.vcgr.genii.ui.plugins.EndpointDescription;
import edu.virginia.vcgr.genii.ui.plugins.MenuType;
import edu.virginia.vcgr.genii.ui.plugins.UIPluginContext;
import edu.virginia.vcgr.genii.ui.plugins.UIPluginException;
import edu.virginia.vcgr.genii.ui.plugins.queue.history.JobHistoryFrame;

public class JobHistoryPlugin extends AbstractCombinedUIMenusPlugin
{
	private Collection<HistoryEvent> readHistoryEvents(UIPluginContext context, MenuType menuType) throws IOException,
		RNSPathDoesNotExistException, ClassNotFoundException
	{
		Collection<HistoryEvent> events = null;
		InputStream in = null;
		Closeable token = null;

		try {
			token = ContextManager.temporarilyAssumeContext(context.uiContext().callingContext());

			Collection<RNSPath> paths = context.endpointRetriever().getTargetEndpoints();
			if (paths != null && paths.size() == 1) {
				EndpointReferenceType target = paths.iterator().next().getEndpoint();
				TypeInformation typeInfo = new TypeInformation(target);
				if (typeInfo.isByteIO())
					in = ByteIOStreamFactory.createInputStream(target);
			}

			if (in == null) {
				JFileChooser chooser = new JFileChooser();
				chooser.setMultiSelectionEnabled(false);
				int answer = chooser.showOpenDialog(null);
				if (answer != JFileChooser.APPROVE_OPTION)
					return null;
				File file = chooser.getSelectedFile();
				in = new FileInputStream(file);
			}

			ObjectInputStream ois = new ObjectInputStream(in);
			int count = ois.readInt();
			events = new ArrayList<HistoryEvent>(count);
			while (count-- > 0)
				events.add((HistoryEvent) ois.readObject());
			ois.close();

			return events;
		} finally {
			StreamUtils.close(in);
			StreamUtils.close(token);
		}
	}

	@Override
	protected void performMenuAction(UIPluginContext context, MenuType menuType) throws UIPluginException
	{
		try {
			Collection<HistoryEvent> events = readHistoryEvents(context, menuType);
			if (events == null)
				return;

			JobHistoryFrame frame = new JobHistoryFrame((UIContext) context.uiContext().clone(), null, null, events);
			frame.pack();
			GUIUtils.centerWindow(frame);
			frame.setVisible(true);
		} catch (Throwable cause) {
			ErrorHandler.handleError(context.uiContext(), context.ownerComponent(), cause);
		}
	}

	@Override
	public boolean isEnabled(Collection<EndpointDescription> selectedDescriptions)
	{
		return true;
	}
}