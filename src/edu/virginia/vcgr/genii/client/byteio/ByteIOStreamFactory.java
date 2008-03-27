package edu.virginia.vcgr.genii.client.byteio;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.rmi.RemoteException;

import org.apache.axis.types.URI;
import org.morgan.util.configuration.ConfigurationException;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rns.RNSPath;

public class ByteIOStreamFactory
{
	static public InputStream createInputStream(EndpointReferenceType target, 
		URI desiredTransferType, boolean createBuffered)
			throws FileNotFoundException, RemoteException,
				ConfigurationException
	{
		TypeInformation tInfo = new TypeInformation(target);
		if (tInfo.isRByteIO())
		{
			RandomByteIOInputStream stream = 
				new RandomByteIOInputStream(target, desiredTransferType);
			if (createBuffered)
				return stream.createPreferredBufferedStream();
			return stream;
		} else if (tInfo.isSByteIO() || tInfo.isSByteIOFactory())
		{
			StreamableByteIOInputStream stream =
				new StreamableByteIOInputStream(target, desiredTransferType);
			if (createBuffered)
				return stream.createPreferredBufferedStream();
			return stream;
		} else
		{
			throw new FileNotFoundException(
				"Endpoint specified is not a file.");
		}
	}
	
	static public InputStream createInputStream(EndpointReferenceType target,
		boolean createBuffered) throws FileNotFoundException, RemoteException,
			ConfigurationException
	{
		return createInputStream(target, null, createBuffered);
	}
	
	static public InputStream createInputStream(EndpointReferenceType target)
		throws FileNotFoundException, RemoteException, ConfigurationException
	{
		return createInputStream(target, null, true);
	}
	
	static public InputStream createInputStream(RNSPath target, 
		URI desiredTransferType, boolean createBuffered)
			throws FileNotFoundException, RemoteException,
				ConfigurationException, RNSException
	{
		if (!target.exists())
			throw new FileNotFoundException("Couldn't find file \"" + 
				target.pwd() + "\".");
		
		return createInputStream(target.getEndpoint(), desiredTransferType, 
			createBuffered);
	}
	
	static public InputStream createInputStream(RNSPath target,
		boolean createBuffered)	throws FileNotFoundException, RemoteException,
			ConfigurationException, RNSException
	{
			return createInputStream(target, null, createBuffered);
	}
	
	static public InputStream createInputStream(RNSPath target)
		throws FileNotFoundException, RemoteException,
			ConfigurationException, RNSException
	{
			return createInputStream(target, null, true);
	}
	
	static public OutputStream createOutputStream(EndpointReferenceType target, 
		URI desiredTransferType, boolean createBuffered)
			throws FileNotFoundException, RemoteException,
				ConfigurationException
	{
		TypeInformation tInfo = new TypeInformation(target);
		if (tInfo.isRByteIO())
		{
			RandomByteIOOutputStream stream = 
				new RandomByteIOOutputStream(target, desiredTransferType);
			if (createBuffered)
				return stream.createPreferredBufferedStream();
			return stream;
		} else if (tInfo.isSByteIO() || tInfo.isSByteIOFactory())
		{
			StreamableByteIOOutputStream stream =
				new StreamableByteIOOutputStream(target, desiredTransferType);
			if (createBuffered)
				return stream.createPreferredBufferedStream();
			return stream;
		} else
		{
			throw new FileNotFoundException(
				"Endpoint specified is not a file.");
		}
	}
	
	static public OutputStream createOutputStream(EndpointReferenceType target,
		boolean createBuffered) throws FileNotFoundException, RemoteException,
			ConfigurationException
	{
		return createOutputStream(target, null, createBuffered);
	}
	
	static public OutputStream createOutputStream(EndpointReferenceType target)
		throws FileNotFoundException, RemoteException, ConfigurationException
	{
		return createOutputStream(target, null, true);
	}
	
	static public OutputStream createOutputStream(RNSPath target, 
		URI desiredTransferType, boolean createBuffered)
			throws FileNotFoundException, RemoteException,
				ConfigurationException, RNSException
	{
		if (!target.exists())
			target.createFile();
		
		return createOutputStream(target.getEndpoint(), desiredTransferType, 
			createBuffered);
	}
	
	static public OutputStream createOutputStream(RNSPath target,
		boolean createBuffered)	throws FileNotFoundException, RemoteException,
			ConfigurationException, RNSException
	{
			return createOutputStream(target, null, createBuffered);
	}
	
	static public OutputStream createOutputStream(RNSPath target)
		throws FileNotFoundException, RemoteException,
			ConfigurationException, RNSException
	{
			return createOutputStream(target, null, true);
	}
}