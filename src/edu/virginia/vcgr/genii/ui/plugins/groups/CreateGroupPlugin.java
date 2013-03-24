package edu.virginia.vcgr.genii.ui.plugins.groups;

import java.util.Collection;

import javax.swing.JOptionPane;

import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.cmd.tools.IdpTool;
import edu.virginia.vcgr.genii.client.cmd.tools.RmTool;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;
import edu.virginia.vcgr.genii.client.utils.io.EmptyReader;
import edu.virginia.vcgr.genii.client.utils.io.EmptyWriter;
import edu.virginia.vcgr.genii.ui.errors.ErrorHandler;
import edu.virginia.vcgr.genii.ui.plugins.AbstractCombinedUIMenusPlugin;
import edu.virginia.vcgr.genii.ui.plugins.EndpointDescription;
import edu.virginia.vcgr.genii.ui.plugins.EndpointRetriever;
import edu.virginia.vcgr.genii.ui.plugins.MenuType;
import edu.virginia.vcgr.genii.ui.plugins.UIPluginContext;
import edu.virginia.vcgr.genii.ui.plugins.UIPluginException;
import edu.virginia.vcgr.genii.ui.progress.AbstractTask;
import edu.virginia.vcgr.genii.ui.progress.Task;
import edu.virginia.vcgr.genii.ui.progress.TaskCompletionListener;
import edu.virginia.vcgr.genii.ui.progress.TaskProgressListener;

public class CreateGroupPlugin extends AbstractCombinedUIMenusPlugin
{
	static final private String SERVICE_PATH = "/containers/BootstrapContainer/Services/X509AuthnPortType";

	static private class RemoveTask implements Runnable
	{
		private String _path;

		private RemoveTask(String path)
		{
			_path = path;
		}

		@Override
		public void run()
		{
			RmTool tool = new RmTool();
			try {
				tool.addArgument(_path);
				tool.run(new EmptyWriter(), new EmptyWriter(), new EmptyReader());
			} catch (Throwable cause) {
				// Just ignore for now
			}
		}
	}

	static private class GroupCreationListener implements TaskCompletionListener<Boolean>
	{
		private UIPluginContext _context;
		private EndpointRetriever _retriever;

		private GroupCreationListener(UIPluginContext context, EndpointRetriever retriever)
		{
			_context = context;
			_retriever = retriever;
		}

		@Override
		public void taskCompleted(Task<Boolean> task, Boolean result)
		{
			_retriever.refresh();
		}

		@Override
		public void taskCancelled(Task<Boolean> task)
		{
			// Do nothing
		}

		@Override
		public void taskExcepted(Task<Boolean> task, Throwable cause)
		{
			ErrorHandler.handleError(_context.uiContext(), _context.ownerComponent(), cause);
		}
	}

	static private class GroupCreatorTask extends AbstractTask<Boolean>
	{
		private UIPluginContext _context;
		private RNSPath _parent;
		private String _newName;

		private GroupCreatorTask(UIPluginContext context, RNSPath parent, String newName)
		{
			_context = context;
			_parent = parent;
			_newName = newName;
		}

		@Override
		final public Boolean execute(TaskProgressListener progressListener) throws Exception
		{
			String sourcePath = null;

			try {
				IdpTool tool = new IdpTool();
				tool.addArgument(SERVICE_PATH);
				tool.addArgument(_newName);
				int result = tool.run(new EmptyWriter(), new EmptyWriter(), new EmptyReader());
				if (result != 0)
					return false;

				sourcePath = SERVICE_PATH + "/" + _newName;
				if (wasCancelled())
					return false;

				RNSPath source = _parent.lookup(sourcePath, RNSPathQueryFlags.MUST_EXIST);

				if (wasCancelled())
					return false;

				RNSPath target = _parent.lookup(_newName, RNSPathQueryFlags.MUST_NOT_EXIST);

				if (wasCancelled())
					return false;

				EndpointReferenceType sourceEPR = source.getEndpoint();

				if (wasCancelled())
					return false;

				target.link(sourceEPR);

				if (wasCancelled())
					return false;

				sourcePath = null;
				return true;
			} catch (Throwable cause) {
				if (cause instanceof Exception)
					throw (Exception) cause;

				throw new RuntimeException("Unable to create group.", cause);
			} finally {
				if (sourcePath != null)
					_context.uiContext().executor().equals(new RemoveTask(sourcePath));
			}
		}
	}

	@Override
	protected void performMenuAction(UIPluginContext context, MenuType menuType) throws UIPluginException
	{
		String answer = JOptionPane.showInputDialog(context.ownerComponent(), "What would you like to call the new group?");
		if (answer == null)
			return;

		EndpointRetriever retriever = context.endpointRetriever();

		Collection<RNSPath> paths = retriever.getTargetEndpoints();
		RNSPath path = paths.iterator().next();

		context
			.uiContext()
			.progressMonitorFactory()
			.createMonitor(context.ownerComponent(), "Creating Group", "Create Group Object", 1000L,
				new GroupCreatorTask(context, path, answer), new GroupCreationListener(context, retriever)).start();
	}

	@Override
	public boolean isEnabled(Collection<EndpointDescription> selectedDescriptions)
	{
		if (selectedDescriptions == null || selectedDescriptions.size() != 1)
			return false;

		return selectedDescriptions.iterator().next().typeInformation().isRNS();
	}
}