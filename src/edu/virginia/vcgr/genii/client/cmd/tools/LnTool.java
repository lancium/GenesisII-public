package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.RemoteException;

import org.morgan.util.configuration.ConfigurationException;
import org.morgan.util.io.StreamUtils;
import org.ws.addressing.EndpointReferenceType;
import org.xml.sax.InputSource;

import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.io.FileResource;
import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathDoesNotExistException;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;
import edu.virginia.vcgr.genii.client.ser.ObjectDeserializer;

public class LnTool extends BaseGridTool
{
	static final private String _DESCRIPTION =
		"Links eprs into RNS space.";
	static final private String _USAGE_RESOURCE =
		"edu/virginia/vcgr/genii/client/cmd/tools/resources/ln-usage.txt";
	
	private String _eprFile = null;
	private String _serviceURL = null;
	
	public LnTool()
	{
		super(_DESCRIPTION, new FileResource(_USAGE_RESOURCE), false);
	}
	
	public void setEpr_file(String eprFile)
	{
		_eprFile = eprFile;
	}
	
	public void setService_url(String serviceURL)
	{
		_serviceURL = serviceURL;
	}
	
	@Override
	protected int runCommand() throws Throwable
	{
		if (numArguments() == 1)
		{
			if (_eprFile != null)
				link(new File(_eprFile),
					getArgument(0));
			else if (_serviceURL != null)
				link(EPRUtils.makeEPR(_serviceURL),
					getArgument(0));
			else
				link(getArgument(0), null);
		} else
			link(getArgument(0), getArgument(1));
		
		return 0;
	}

	@Override
	protected void verify() throws ToolException
	{
		if (numArguments() < 1 || numArguments() > 2)
			throw new InvalidToolUsageException();
	}
	

	static public void link(File eprFile, String target)
		throws RNSException, IOException, ConfigurationException
	{
		FileInputStream fin = null;
		
		try
		{
			fin = new FileInputStream(eprFile);
			link((EndpointReferenceType)ObjectDeserializer.deserialize(
				new InputSource(fin), EndpointReferenceType.class), 
				target);
		}
		finally
		{
			StreamUtils.close(fin);
		}
	}
	
	static public void link(EndpointReferenceType epr, String target)
		throws RNSException, ConfigurationException, RemoteException
	{
		RNSPath currentPath = RNSPath.getCurrent();
		RNSPath path = currentPath.lookup(target, RNSPathQueryFlags.MUST_NOT_EXIST);
		
		link(epr, path);
	}
	
	static public void link(String source, String target)
		throws RNSException, ConfigurationException, IOException
	{
		RNSPath currentPath = RNSPath.getCurrent();
		RNSPath sourcePath;
		
		try
		{
			sourcePath = currentPath.lookup(source, RNSPathQueryFlags.MUST_EXIST);
		}
		catch (RNSPathDoesNotExistException e)
		{
			throw new FileNotFoundException(e.getMessage());
		}
		
		if (target == null)
			target = sourcePath.getName();
		
		RNSPath targetPath = currentPath.lookup(target, 
			RNSPathQueryFlags.MUST_NOT_EXIST);
		
		link(sourcePath.getEndpoint(), targetPath);
	}
	
	static public void link(EndpointReferenceType epr, RNSPath target)
		throws RNSException, RemoteException
	{
		target.link(epr);
	}
}