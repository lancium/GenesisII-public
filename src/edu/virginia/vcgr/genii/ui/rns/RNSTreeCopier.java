package edu.virginia.vcgr.genii.ui.rns;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

import javax.swing.tree.TreePath;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.Pair;
import org.morgan.util.io.StreamUtils;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.byteio.ByteIOStreamFactory;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.IContextResolver;
import edu.virginia.vcgr.genii.client.context.MemoryBasedContextResolver;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathAlreadyExistsException;
import edu.virginia.vcgr.genii.client.rns.RNSPathDoesNotExistException;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;
import edu.virginia.vcgr.genii.ui.UIContext;
import edu.virginia.vcgr.genii.ui.errors.ErrorHandler;
import edu.virginia.vcgr.genii.ui.progress.AbstractTask;
import edu.virginia.vcgr.genii.ui.progress.TaskProgressListener;

public class RNSTreeCopier extends RNSTreeOperator
{
	static private Log _logger = LogFactory.getLog(RNSTreeCopier.class);
	
	static public RNSTreeOperator copy(RNSTree sourceTree,
		RNSTree targetTree, TreePath targetPath,
		UIContext sourceContext,
		Collection<Pair<RNSTreeNode, RNSPath>> paths)
	{
		return new RNSTreeCopier(sourceContext, targetTree, targetPath,
			new RNSTreeOperatorSource(sourceTree, paths));
	}
	
	static public RNSTreeOperator copy(RNSTree targetTree, TreePath targetPath,
		UIContext targetContext, List<?> files)
	{
		List<File> fileSources = new Vector<File>(files.size());
		for (Object obj : files)
			fileSources.add((File)obj);

		return new RNSTreeCopier(targetContext, targetTree, targetPath,
			new FilesystemOperatorSource(fileSources));
	}
	
	private void doCopy(InputStream in, EndpointReferenceType fileEPR) 
		throws FileNotFoundException, RemoteException, IOException
	{
		OutputStream out = null;
		
		try
		{
			out = ByteIOStreamFactory.createOutputStream(fileEPR);
			StreamUtils.copyStream(in, out);
		}
		finally
		{
			StreamUtils.close(out);
		}
	}
	
	private void doCopy(TaskProgressListener progressListener,
		File source, RNSPath target)
		throws RNSPathAlreadyExistsException, RNSPathDoesNotExistException,
		RNSException, RemoteException, IOException
	{
		if (source.isDirectory())
		{
			target.mkdir();
			
			for (File entry : source.listFiles())
			{
				doCopy(progressListener, entry, target.lookup(
					entry.getName(), RNSPathQueryFlags.MUST_NOT_EXIST));
			}
		} else
		{
			progressListener.updateSubTitle(String.format(
				"Copying %s", source.getName()));
			EndpointReferenceType fileEPR = target.createNewFile();
			InputStream in = null;
			
			try
			{
				in = new FileInputStream(source);
				doCopy(in, fileEPR);
			}
			finally
			{
				StreamUtils.close(in);
			}
		}
	}
	
	private void doCopy(TaskProgressListener progressListener,
		RNSPath source, RNSPath target)
		throws RNSPathAlreadyExistsException, RNSPathDoesNotExistException, RNSException,
			FileNotFoundException, RemoteException, IOException
	{
		TypeInformation tInfo = new TypeInformation(source.getEndpoint());
		
		if (tInfo.isRNS())
		{
			target.mkdir();
			
			for (RNSPath entry : source.listContents())
			{
				doCopy(progressListener, entry, target.lookup(
					entry.getName(), RNSPathQueryFlags.MUST_NOT_EXIST));
			}
		} else if (tInfo.isByteIO())
		{
			progressListener.updateSubTitle(String.format(
				"Copying %s", source.getName()));
			EndpointReferenceType fileEPR = target.createNewFile();
			InputStream in = null;
			
			try
			{
				in = ByteIOStreamFactory.createInputStream(source);
				doCopy(in, fileEPR);
			}
			finally
			{
				StreamUtils.close(in);
			}
		} else
		{
			target.link(source.getEndpoint());
		}
	}
	
	private RNSTreeCopier(UIContext uiContext, RNSTree targetTree,
			TreePath targetPath, OperatorSource sourceInformation)
	{
		super(uiContext, targetTree, targetPath, sourceInformation);
	}

	@Override
	public boolean performOperation()
	{
		_logger.debug("RNSTreeCopier called.");
		_uiContext.progressMonitorFactory().createMonitor(
			_targetTree, "Copying Endpoints", "Copying endpoints.",
			1000L, new CopierTask(), null).start();
		return true;
	}
	
	private class CopierTask extends AbstractTask<Integer>
	{
		@Override
		public Integer execute(TaskProgressListener progressListener)
				throws Exception
		{
			RNSTreeNode targetParentNode = (RNSTreeNode)_targetPath.getLastPathComponent();
			RNSFilledInTreeObject targetParentObject = 
				(RNSFilledInTreeObject)targetParentNode.getUserObject();
			
			if (_sourceInformation.isRNSSource())
			{
				RNSTreeOperatorSource source = (RNSTreeOperatorSource)_sourceInformation;
				
				for (Pair<RNSTreeNode, RNSPath> path : source.sourcePaths())
				{
					progressListener.updateSubTitle(String.format("Copying %s", 
						path.second().getName()));
					RNSPath target = getTargetPath(
						targetParentObject.path(), path.second().getName());
					if (target != null)
					{
						IContextResolver resolver = ContextManager.getResolver();
						
						try
						{
							ContextManager.setResolver(
								new MemoryBasedContextResolver(
									_uiContext.callingContext()));
							doCopy(progressListener, path.second(), target);
							new RefreshWorker(_targetTree, targetParentNode).run();
						}
						catch (Throwable cause)
						{
							if (wasCancelled())
								return null;
							
							_logger.warn(String.format(
								"Unable to copy source \"%s\".", 
								path.second().pwd()), cause);
							ErrorHandler.handleError(
								_uiContext, _targetTree, cause);
						}
						finally
						{
							ContextManager.setResolver(resolver);
						}
					}
				}
			} else
			{
				FilesystemOperatorSource source = (FilesystemOperatorSource)_sourceInformation;
				
				for (File sourceFile : source.sources())
				{
					progressListener.updateSubTitle(String.format("Copying %s", 
						sourceFile.getName()));
					RNSPath target = getTargetPath(
						targetParentObject.path(), sourceFile.getName());
					
					if (target != null)
					{
						IContextResolver resolver = ContextManager.getResolver();
						
						try
						{
							ContextManager.setResolver(
								new MemoryBasedContextResolver(
									_uiContext.callingContext()));
							doCopy(progressListener, sourceFile, target);
							new RefreshWorker(_targetTree, targetParentNode).run();
						}
						catch (Throwable cause)
						{
							if (wasCancelled())
								return null;
							
							_logger.warn(String.format(
								"Unable to copy source \"%s\".", 
								sourceFile), cause);
							ErrorHandler.handleError(
								_uiContext, _targetTree, cause);
						}
						finally
						{
							ContextManager.setResolver(resolver);
						}
					}
				}
			}
			
			return null;
		}

		@Override
		public boolean showProgressDialog()
		{
			return true;
		}
	}
}