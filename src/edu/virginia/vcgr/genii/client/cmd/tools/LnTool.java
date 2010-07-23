package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;

import org.morgan.util.io.StreamUtils;
import org.ws.addressing.EndpointReferenceType;
import org.xml.sax.InputSource;

import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.gpath.GeniiPath;
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
	private boolean _noLookup = false;
	
	public LnTool()
	{
		super(_DESCRIPTION, new FileResource(_USAGE_RESOURCE), false);
	}
	
	@Option({"epr-file"})
	public void setEpr_file(String eprFile)
	{
		_eprFile = eprFile;
	}
	
	@Option({"service-url"})
	public void setService_url(String serviceURL)
	{
		_serviceURL = serviceURL;
	}
	
	@Option({"no-lookup"})
	public void setNo_lookup()
	{
		_noLookup = true;
	}
	
	@Override
	protected int runCommand() throws Throwable
	{
		if (numArguments() == 1)
		{
			if (_eprFile != null)
				link(new GeniiPath(_eprFile),
					new GeniiPath(getArgument(0)));
			else if (_serviceURL != null)
				link(EPRUtils.makeEPR(_serviceURL, !_noLookup),
					new GeniiPath(getArgument(0)));
			else
				link(new GeniiPath(getArgument(0)), null);
		} else
			link(new GeniiPath(getArgument(0)),
				new GeniiPath(getArgument(1)));
		
		return 0;
	}

	@Override
	protected void verify() throws ToolException
	{
		if (numArguments() < 1 || numArguments() > 2)
			throw new InvalidToolUsageException();
	}
	

	static public void linkFromEPRFile(GeniiPath eprFile, GeniiPath target)
		throws RNSException, IOException, InvalidToolUsageException
	{
		InputStream in = null;
		
		try
		{
			in = eprFile.openInputStream();
			link((EndpointReferenceType)ObjectDeserializer.deserialize(
				new InputSource(in), EndpointReferenceType.class), 
				target);
		}
		finally
		{
			StreamUtils.close(in);
		}
	}
	
	static public void link(EndpointReferenceType epr, GeniiPath target)
		throws RNSException, RemoteException, InvalidToolUsageException
	{
		RNSPath path = lookup(target, RNSPathQueryFlags.MUST_NOT_EXIST);
		
		link(epr, path);
	}
	
	static public void link(GeniiPath source, GeniiPath target)
		throws RNSException, IOException, InvalidToolUsageException
	{
		RNSPath sourcePath;
		
		try
		{
			sourcePath = lookup(source, RNSPathQueryFlags.MUST_EXIST);
		}
		catch (RNSPathDoesNotExistException e)
		{
			throw new FileNotFoundException(e.getMessage());
		}
		
		if (target == null)
			target = new GeniiPath(sourcePath.getName());
		
		RNSPath targetPath = lookup(target, 
			RNSPathQueryFlags.MUST_NOT_EXIST);
		
		link(sourcePath.getEndpoint(), targetPath);
	}
	
	static public void link(EndpointReferenceType epr, RNSPath target)
		throws RNSException, RemoteException
	{
		target.link(epr);
	}
}