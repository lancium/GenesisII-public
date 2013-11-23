package edu.virginia.vcgr.genii.ui.plugins.files;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Collection;

import javax.swing.JComponent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.io.StreamUtils;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.byteio.ByteIOStreamFactory;
import edu.virginia.vcgr.genii.client.byteio.RandomByteIORP;
import edu.virginia.vcgr.genii.client.byteio.StreamableByteIORP;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rp.ResourcePropertyManager;
import edu.virginia.vcgr.genii.ui.ClientApplication;
import edu.virginia.vcgr.genii.ui.UIContext;
import edu.virginia.vcgr.genii.ui.errors.ErrorHandler;
import edu.virginia.vcgr.genii.ui.plugins.AbstractCombinedUIMenusPlugin;
import edu.virginia.vcgr.genii.ui.plugins.EndpointDescription;
import edu.virginia.vcgr.genii.ui.plugins.MenuType;
import edu.virginia.vcgr.genii.ui.plugins.UIPluginContext;
import edu.virginia.vcgr.genii.ui.plugins.UIPluginException;
import edu.virginia.vcgr.genii.ui.progress.AbstractContextSwitchingTask;
import edu.virginia.vcgr.genii.ui.progress.ProgressMonitorFactory;
import edu.virginia.vcgr.genii.ui.progress.Task;
import edu.virginia.vcgr.genii.ui.progress.TaskCompletionListener;
import edu.virginia.vcgr.genii.ui.progress.TaskProgressListener;

public class EditPlugin extends AbstractCombinedUIMenusPlugin
{
	static private Log _logger = LogFactory.getLog(EditPlugin.class);
	static private final int BUFFER_SIZE = 1024 * 8;

	static private class DownloadTask extends AbstractContextSwitchingTask<File>
	{
		private RNSPath _sourcePath;

		private DownloadTask(UIContext context, RNSPath sourcePath)
		{
			super(context);

			_sourcePath = sourcePath;
		}

		@Override
		protected File executeInsideContext(TaskProgressListener progressListener) throws Exception
		{

			File toEdit = null;
			// System.out.println("ASG: downloading" + _sourcePath);

			String extension = _sourcePath.getName();
			int index = extension.lastIndexOf('.');
			if (index > 0)
				extension = extension.substring(index);

			File tmpFile = File.createTempFile("gridedit", extension);
			tmpFile.deleteOnExit();

			InputStream in = null;
			OutputStream out = null;

			try {
				if (wasCancelled())
					return null;

				in = ByteIOStreamFactory.createInputStream(_sourcePath);

				if (wasCancelled()) {
					StreamUtils.close(in);
					return null;
				}

				out = new FileOutputStream(tmpFile);

				if (wasCancelled()) {
					StreamUtils.close(in);
					StreamUtils.close(out);
					return null;
				}

				byte[] data = new byte[BUFFER_SIZE];
				int read;
				while ((read = in.read(data)) > 0) {
					if (wasCancelled())
						break;

					out.write(data, 0, read);

					if (wasCancelled())
						break;
				}

				if (wasCancelled()) {
					StreamUtils.close(in);
					StreamUtils.close(out);
					return null;
				}

				EndpointReferenceType epr = _sourcePath.getEndpoint();
				TypeInformation typeInfo = new TypeInformation(epr);
				boolean isReadOnly = false;

				if (typeInfo.isRByteIO()) {
					RandomByteIORP rp = (RandomByteIORP) ResourcePropertyManager.createRPInterface(epr, RandomByteIORP.class);
					Boolean val = rp.getWriteable();
					if (val != null)
						isReadOnly = (!val);
				} else {
					StreamableByteIORP rp =
						(StreamableByteIORP) ResourcePropertyManager.createRPInterface(epr, StreamableByteIORP.class);
					Boolean val = rp.getWriteable();
					if (val != null)
						isReadOnly = (!val);
				}

				if (isReadOnly) {
					tmpFile.setReadOnly();
					// System.out.println("ASG: readonly" + tmpFile.getPath());
				}

				toEdit = tmpFile;
				tmpFile = null;
			} finally {
				StreamUtils.close(in);
				StreamUtils.close(out);

				if (tmpFile != null)
					tmpFile.delete();
			}
			// System.out.println("ASG: downloaded " + toEdit.getName() + " " + toEdit.length());
			return toEdit;
		}
	}

	static private class DownloadTaskCompletionListener implements TaskCompletionListener<File>
	{
		private JComponent _ownerComponent;
		private UIContext _context;
		private RNSPath _source;
		private boolean _edit;

		private DownloadTaskCompletionListener(JComponent ownerComponent, UIContext context, RNSPath source, boolean edit)
		{
			_ownerComponent = ownerComponent;
			_context = context;
			_source = source;
			_edit = edit;
		}

		private int WaitForEditor(String filestr)
		{
			/*
			 * WaitForEditor(String editFile) that searches the process list on windows or unix
			 * looking for a process opened with an argument that includes the specified file name
			 * that will be of the form grid<somefilename>integers
			 * 
			 * We start by getting this list of processes using some form of ps, note this varies by
			 * OS. We then search for the existence of the file string in the output line. If the
			 * processes is still running, we sleep for a second and do it all over again. Written
			 * by ASG while on sabbatical in Munich. September 10, 2013
			 */
			int found = 0, everFound = -1;
			do {
				found = 0;

				try {
					String line;
					Process p;

					String osName = System.getProperty("os.name");
					if (osName.contains("OS X")) {
						/* Mac OS code */
						p = Runtime.getRuntime().exec("ps ef ");
					} else if (osName.contains("Windows")) {
						/* Windows code */

						p = Runtime.getRuntime().exec(System.getenv("windir") + "\\system32\\" + "tasklist.exe /v");
					} else if (osName.contains("")) {
						p = Runtime.getRuntime().exec("ps x ");
						/* Unix OS code */
					} else
						return -1;
					BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
					while ((line = input.readLine()) != null) {
						if (line.indexOf(filestr) >= 0) {
							// Found it, sleep
							found = 1;
							everFound = 1;
							continue;
						}
						// System.out.println("FILE is " + filestr + " process entry is " + line);
						// //<-- Parse data here.
					}
					input.close();
				} catch (Exception err) {
					err.printStackTrace();
				}
				// Now wait a few seconds to see if the process is still there
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} while (found == 1);

			return everFound;
		}

		@Override
		public void taskCompleted(Task<File> task, File result)
		{
			try {
				// System.out.println("ASG: about to call external application with input " +
				// result.getAbsolutePath());
				// ASG August 18, 2013, change to not use _application, instead the OS file
				// associations
				if (Desktop.isDesktopSupported()) {
					Desktop desktop = Desktop.getDesktop();
					if (desktop.isSupported(Desktop.Action.OPEN)) {
						try {
							long lastModified = result.lastModified();
							if (!_edit) {
								if (_ownerComponent.getTopLevelAncestor() instanceof ClientApplication) {
									ClientApplication top = (ClientApplication) _ownerComponent.getTopLevelAncestor();
									top.addStatusLine(
										"Please note:  you are viewing " + _source.getName()
											+ ", updates WILL NOT be propagated.",
										"Double clicking on a file invokes the viewer, not the editor. Changes made with the viewer WILL NOT be propagated back to the GFFS.");
								}
							} else {
								if (_ownerComponent.getTopLevelAncestor() instanceof ClientApplication) {
									ClientApplication top = (ClientApplication) _ownerComponent.getTopLevelAncestor();
									top.addStatusLine("Please note:  you are editing " + _source.getName()
										+ ", the GUI remains locked while editing due to how Java Swing is implemented.");
								}
							}
							desktop.open(result);
							if (!_edit)
								return;
							Thread.sleep(3000); // Wait for the application to start
							if (WaitForEditor(result.getName()) < 0) {
								System.err.println("Could not determine editor status");
							}
							if (result.lastModified() > lastModified) {
								// The file was updated
								// Now upload
								_context
									.progressMonitorFactory()
									.createMonitor(_ownerComponent, "Uploading File", "Uploading edited file.", 1L,
										new UploadTask(_context, result, _source),
										new UploadCompletionListener(_context, _ownerComponent, result)).start();
							} else {
								// We need to clean up and throw away the old files
								result.delete();
							}
						} catch (IOException ioe) {
							_logger.error("Cannot perform the given operation on the file: " + result.getAbsolutePath());
						}
					}
				}
				// _application.launch(result, new ExternalApplicationCallbackImpl(_ownerComponent,
				// _context, _source, result));

			} catch (Throwable cause) {
				ErrorHandler.handleError(_context, _ownerComponent, cause);
			}
		}

		@Override
		public void taskCancelled(Task<File> task)
		{
			// Just let it go.
		}

		@Override
		public void taskExcepted(Task<File> task, Throwable cause)
		{
			ErrorHandler.handleError(_context, _ownerComponent, cause);
		}
	}

	static private class UploadTask extends AbstractContextSwitchingTask<Integer>
	{
		private RNSPath _targetPath;
		private File _source;

		private UploadTask(UIContext context, File source, RNSPath target)
		{
			super(context);

			_source = source;
			_targetPath = target;
		}

		@Override
		protected Integer executeInsideContext(TaskProgressListener progressListener) throws Exception
		{
			InputStream in = null;
			OutputStream out = null;
			// System.out.println("ASG: should be uploading 2ASG");

			try {
				in = new FileInputStream(_source);
				out = ByteIOStreamFactory.createOutputStream(_targetPath);
				byte[] data = new byte[BUFFER_SIZE];
				int read;

				while ((read = in.read(data)) > 0) {
					if (wasCancelled())
						break;

					out.write(data, 0, read);
				}
				if (wasCancelled()) {
					StreamUtils.close(in);
					StreamUtils.close(out);
					return null;
				}

				return 0;
			} finally {
				StreamUtils.close(in);
				StreamUtils.close(out);
			}
		}
	}

	static private class UploadCompletionListener implements TaskCompletionListener<Integer>
	{
		private File _tmpFile;
		private UIContext _context;
		private JComponent _ownerComponent;

		private UploadCompletionListener(UIContext context, JComponent ownerComponent, File tmpFile)
		{
			_tmpFile = tmpFile;
			_context = context;
			_ownerComponent = ownerComponent;
		}

		@Override
		public void taskCompleted(Task<Integer> task, Integer result)
		{
			_tmpFile.delete();
		}

		@Override
		public void taskCancelled(Task<Integer> task)
		{
			_tmpFile.delete();
		}

		@Override
		public void taskExcepted(Task<Integer> task, Throwable cause)
		{
			_tmpFile.delete();
			ErrorHandler.handleError(_context, _ownerComponent, cause);
		}
	}

	static public void performEdit(JComponent ownerComponent, UIContext context, RNSPath path, boolean edit)
	/*
	 * ASG 9-28-2013 Added boolean edit, to indicate whether it is an edit op or a view op Also
	 * removed the external application type stuff, we're just going to use the java desktop to find
	 * it for us.
	 */
	{
		Closeable contextToken = null;

		try {
			ContextManager.temporarilyAssumeContext(context.callingContext());
			ProgressMonitorFactory factory = context.progressMonitorFactory();

			/*
			 * String mimeType =
			 * MimetypesFileTypeMap.getDefaultFileTypeMap().getContentType(path.getName());
			 * ExternalApplication externalApplication =
			 * ApplicationDatabase.database().getExternalApplication(mimeType); if
			 * (externalApplication == null) { JOptionPane.showMessageDialog(ownerComponent,
			 * "No editor registered for this file type!", "No Editor Available",
			 * JOptionPane.ERROR_MESSAGE); return; }
			 */
			factory.createMonitor(ownerComponent, "Edit", "Downloading grid file for edit.", 1L,
				new DownloadTask(context, path), new DownloadTaskCompletionListener(ownerComponent, context, path, edit))
				.start();

		} catch (Throwable cause) {
			ErrorHandler.handleError(context, ownerComponent, cause);
		} finally {
			StreamUtils.close(contextToken);
		}
	}

	@Override
	protected void performMenuAction(UIPluginContext context, MenuType menuType) throws UIPluginException
	{
		Collection<RNSPath> paths = context.endpointRetriever().getTargetEndpoints();
		RNSPath path = paths.iterator().next();
		performEdit(context.ownerComponent(), context.uiContext(), path, true);
	}

	@Override
	public boolean isEnabled(Collection<EndpointDescription> selectedDescriptions)
	{
		if (selectedDescriptions == null || selectedDescriptions.size() != 1)
			return false;

		TypeInformation tp = selectedDescriptions.iterator().next().typeInformation();
		return (tp.isByteIO() && !(tp.isContainer() || tp.isBESContainer() || tp.isQueue() || tp.isIDP()));
	}
}
