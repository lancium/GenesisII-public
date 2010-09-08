package edu.virginia.vcgr.genii.ui.plugins.files;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;

import javax.activation.MimetypesFileTypeMap;
import javax.swing.JComponent;
import javax.swing.JOptionPane;

import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.externalapp.ApplicationDatabase;
import edu.virginia.vcgr.externalapp.ExternalApplication;
import edu.virginia.vcgr.externalapp.ExternalApplicationCallback;
import edu.virginia.vcgr.genii.client.byteio.ByteIOStreamFactory;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
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
	static private final int BUFFER_SIZE = 1024 * 8;
	
	static private class DownloadTask 
		extends AbstractContextSwitchingTask<File>
	{
		private RNSPath _sourcePath;
		
		private DownloadTask(UIContext context, RNSPath sourcePath)
		{
			super(context);
			
			_sourcePath = sourcePath;
		}

		@Override
		protected File executeInsideContext(
			TaskProgressListener progressListener) throws Exception
		{
			
			File toEdit = null;
			
			String extension = _sourcePath.getName();
			int index = extension.lastIndexOf('.');
			if (index > 0)
				extension = extension.substring(index);
			
			File tmpFile = File.createTempFile("gridedit", extension);
			tmpFile.deleteOnExit();
			
			InputStream in = null;
			OutputStream out = null;
			
			try
			{
				if (wasCancelled())
					return null;
				
				in = ByteIOStreamFactory.createInputStream(_sourcePath);
				
				if (wasCancelled())
					return null;
				
				out = new FileOutputStream(tmpFile);
				
				if (wasCancelled())
					return null;
				
				byte []data = new byte[BUFFER_SIZE];
				int read;
				while ( (read = in.read(data)) > 0)
				{
					if (wasCancelled())
						return null;
					
					out.write(data, 0, read);
					
					if (wasCancelled())
						return null;
				}
				
				toEdit = tmpFile;
				tmpFile = null;
			}
			finally
			{
				StreamUtils.close(in);
				StreamUtils.close(out);
				
				if (tmpFile != null)
					tmpFile.delete();
			}
			
			return toEdit;
		}
	}
	
	static private class DownloadTaskCompletionListener
		implements TaskCompletionListener<File>
	{
		private JComponent _ownerComponent;
		private UIContext _context;
		private RNSPath _source;
		private ExternalApplication _application;
		
		private DownloadTaskCompletionListener(JComponent ownerComponent,
			UIContext context, RNSPath source, ExternalApplication application)
		{
			_ownerComponent = ownerComponent;
			_context = context;
			_source = source;
			_application = application;
		}
		
		@Override
		public void taskCompleted(Task<File> task, File result)
		{
			try
			{
				_application.launch(result, new ExternalApplicationCallbackImpl(
					_ownerComponent, _context, _source, result));
			}
			catch (Throwable cause)
			{
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
	
	static private class ExternalApplicationCallbackImpl
		implements ExternalApplicationCallback
	{
		private JComponent _ownerComponent;
		private UIContext _context;
		private RNSPath _source;
		private File _tmpFile;
		
		private ExternalApplicationCallbackImpl(
			JComponent ownerComponent, UIContext context,
			RNSPath source, File tmpFile)
		{
			_ownerComponent = ownerComponent;
			_context = context;
			_source = source;
			_tmpFile = tmpFile;
		}
		
		@Override
		public void externalApplicationFailed(Throwable cause)
		{
			_tmpFile.delete();
			ErrorHandler.handleError(_context, _ownerComponent, cause);
		}

		@Override
		public void externalApplicationExited(File contentFile)
		{
			if (contentFile != null)
			{
				_context.progressMonitorFactory().monitor(
					_ownerComponent, "Uploading File", "Uploading edited file.",
					1L, new UploadTask(_context, contentFile, _source),
					new UploadCompletionListener(_context, _ownerComponent, 
						contentFile));
			}
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
		protected Integer executeInsideContext(
			TaskProgressListener progressListener) throws Exception
		{
			InputStream in = null;
			OutputStream out = null;
			
			try
			{
				in = new FileInputStream(_source);
				out = ByteIOStreamFactory.createOutputStream(_targetPath);
				byte []data = new byte[BUFFER_SIZE];
				int read;
				
				while ( (read = in.read(data)) > 0)
				{
					if (wasCancelled())
						return null;
					
					out.write(data, 0, read);
				}
				
				return 0;
			}
			finally
			{
				StreamUtils.close(in);
				StreamUtils.close(out);
			}
		}
	}
	
	static private class UploadCompletionListener
		implements TaskCompletionListener<Integer>
	{
		private File _tmpFile;
		private UIContext _context;
		private JComponent _ownerComponent;
		
		private UploadCompletionListener(UIContext context, 
			JComponent ownerComponent, File tmpFile)
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
	
	static public void performEdit(JComponent ownerComponent,
		UIContext context, RNSPath path)
	{
		Closeable contextToken = null;
		
		try
		{
			ContextManager.temporarilyAssumeContext(context.callingContext());
			ProgressMonitorFactory factory = context.progressMonitorFactory();
			String mimeType =
				MimetypesFileTypeMap.getDefaultFileTypeMap().getContentType(
					path.getName());
			ExternalApplication externalApplication = 
				ApplicationDatabase.database().getExternalApplication(
					mimeType);
			if (externalApplication == null)
			{
				JOptionPane.showMessageDialog(ownerComponent,
					"No editor registered for this file type!",
					"No Editor Available", JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			factory.monitor(ownerComponent,
				"Edit",
				"Downloading grid file for edit.",
				1L, new DownloadTask(context, path),
				new DownloadTaskCompletionListener(
					ownerComponent, context, path, externalApplication));
				
		}
		catch (Throwable cause)
		{
			ErrorHandler.handleError(context,
				ownerComponent, cause);
		}
		finally
		{
			StreamUtils.close(contextToken);
		}
	}
	
	@Override
	protected void performMenuAction(UIPluginContext context, MenuType menuType)
			throws UIPluginException
	{	
		Collection<RNSPath> paths = 
			context.endpointRetriever().getTargetEndpoints();
		RNSPath path = paths.iterator().next();
		performEdit(context.ownerComponent(),
			context.uiContext(), path);
	}

	@Override
	public boolean isEnabled(
		Collection<EndpointDescription> selectedDescriptions)
	{
		if (selectedDescriptions == null || selectedDescriptions.size() != 1)
			return false;
		
		return selectedDescriptions.iterator().next().typeInformation().isByteIO();
	}
}