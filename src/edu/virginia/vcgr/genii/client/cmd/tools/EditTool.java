package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.MimetypesFileTypeMap;

import org.morgan.util.io.StreamUtils;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.externalapp.ApplicationDatabase;
import edu.virginia.vcgr.externalapp.ExternalApplication;
import edu.virginia.vcgr.externalapp.ExternalApplicationToken;
import edu.virginia.vcgr.genii.client.byteio.ByteIOStreamFactory;
import edu.virginia.vcgr.genii.client.byteio.RandomByteIORP;
import edu.virginia.vcgr.genii.client.byteio.StreamableByteIORP;
import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.gpath.GeniiPath;
import edu.virginia.vcgr.genii.client.gpath.GeniiPathType;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rp.ResourcePropertyManager;

public class EditTool extends BaseGridTool
{
	static final private String DESCRIPTION =
		"Edits a file with a registered editor.";
	static final private String USAGE =
		"edit <path-to-file>";
	
	@Override
	final protected void verify() throws ToolException
	{
		if (numArguments() != 1)
			throw new InvalidToolUsageException(
				"Edit requires a file argument.");
	}

	@Override
	final protected int runCommand() throws Throwable
	{
		GeniiPath path = new GeniiPath(getArgument(0));
		if (path.pathType() == GeniiPathType.Local)
			return editLocalFile(new File(path.path()));
		else
		{
			return editGridFile(RNSPath.getCurrent().lookup(
				path.path()));
		}
	}
	
	public EditTool()
	{
		super(DESCRIPTION, USAGE, false);
	}
	
	final public int editLocalFile(File file) throws Throwable
	{
		String mimeType = 
			MimetypesFileTypeMap.getDefaultFileTypeMap().getContentType(file);
		ExternalApplication app = 
			ApplicationDatabase.database().getExternalApplication(mimeType);
		if (app == null)
		{
			stderr.format(
				"Unable to find registered application for file [%s] %s.\n",
				mimeType, file);
			return 1;
		}
		
		ExternalApplicationToken token = app.launch(file);
		token.getResult();
		return 0;
	}
	
	final public int editGridFile(RNSPath gridPath) throws Throwable
	{
		InputStream in = null;
		OutputStream out = null;
		
		String mimeType =
			MimetypesFileTypeMap.getDefaultFileTypeMap().getContentType(
				gridPath.getName());
		ExternalApplication app =
			ApplicationDatabase.database().getExternalApplication(mimeType);
		if (app == null)
		{
			stderr.format(
				"Unable to find registered application for file [%s] %s.\n",
				mimeType, gridPath);
			return 1;
		}
		
		String gridPathExtension = gridPath.getName();
		int index = gridPathExtension.lastIndexOf('.');
		if (index > 0)
			gridPathExtension = gridPathExtension.substring(index);
		
		File tmpFile = File.createTempFile(
			"gridedit", gridPathExtension);
		tmpFile.deleteOnExit();
		
		try
		{
			if (gridPath.exists())
			{
				stdout.format("Downloading grid file \"%s\" for editing.\n",
					gridPath);
				
				in = ByteIOStreamFactory.createInputStream(gridPath);
				out = new FileOutputStream(tmpFile);
				StreamUtils.copyStream(in, out);
				in.close();
				in = null;
				out.close();
				out = null;
				
				EndpointReferenceType epr = gridPath.getEndpoint();
				TypeInformation typeInfo = new TypeInformation(epr);
				boolean isReadOnly = false;
				
				if (typeInfo.isRByteIO())
				{
					RandomByteIORP rp = (RandomByteIORP)ResourcePropertyManager.createRPInterface(
						epr, RandomByteIORP.class);
					Boolean val = rp.getWriteable();
					if (val != null)
						isReadOnly = (!val);
				} else
				{
					StreamableByteIORP rp = (StreamableByteIORP)ResourcePropertyManager.createRPInterface(
						epr, StreamableByteIORP.class);
					Boolean val = rp.getWriteable();
					if (val != null)
						isReadOnly = (!val);
				}
				
				if (isReadOnly)
					tmpFile.setReadOnly();
			}
			
			ExternalApplicationToken token;
			token = app.launch(tmpFile);
			File result = token.getResult();
			
			if (result != null)
			{
				stdout.format("Uploading grid file \"%s\".\n",
					gridPath);
				in = new FileInputStream(result);
				out = ByteIOStreamFactory.createOutputStream(gridPath);
				StreamUtils.copyStream(in, out);
				in.close();
				in = null;
				out.close();
				out = null;
			}
			
			return 0;
		}
		finally
		{
			tmpFile.delete();
			StreamUtils.close(in);
			StreamUtils.close(out);
		}
	}
}