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

/**
 * Because there are so many flavors of ByteIO stream that can be
 * created (and so many ways to create them), I try and collect the
 * most popular ways into this one factory class.  This class should
 * generally be used for creating MOST ByteIO streams.
 * 
 * @author mmm2a
 */
public class ByteIOStreamFactory
{
	/**
	 * Given a remote byteIO, create an input stream that can read from it.
	 * 
	 * @param target The target resource to read bytes from (can be streamable
	 * or random).
	 * @param desiredTransferType The desired transfer protocol (if any) to use.
	 * THis value CAN be null.
	 * @param createBuffered Whether or not the stream should be buffered.
	 * 
	 * @return A newly created Java InputStream to use for reading bytes from
	 * the target ByteIO.
	 * 
	 * @throws FileNotFoundException
	 * @throws RemoteException
	 * @throws ConfigurationException
	 */
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
	
	/**
	 * Given a remote byteIO, create an input stream that can read from it.
	 * 
	 * @param target The target resource to read bytes from (can be streamable
	 * or random).
	 * @param createBuffered Whether or not the stream should be buffered.
	 * 
	 * @return A newly created Java InputStream to use for reading bytes from
	 * the target ByteIO with the default preferred transfer mechanism.
	 * 
	 * @throws FileNotFoundException
	 * @throws RemoteException
	 * @throws ConfigurationException
	 */
	static public InputStream createInputStream(EndpointReferenceType target,
		boolean createBuffered) throws FileNotFoundException, RemoteException,
			ConfigurationException
	{
		return createInputStream(target, null, createBuffered);
	}
	
	/**
	 * Given a remote byteIO, create an input stream that can read from it.
	 * 
	 * @param target The target resource to read bytes from (can be streamable
	 * or random).
	 * 
	 * @return A newly created, buffered, Java InputStream to use for reading 
	 * bytes from the target ByteIO using the default transfer mechanism.
	 * 
	 * @throws FileNotFoundException
	 * @throws RemoteException
	 * @throws ConfigurationException
	 */
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
	
	/**
	 * Given a remote byteIO, create an input stream that can read from it.
	 * 
	 * @param target The target resource to read bytes from (can be streamable
	 * or random).
	 * @param createBuffered Whether or not the stream should be buffered.
	 * 
	 * @return A newly created Java InputStream to use for reading bytes from
	 * the target ByteIO using the preferred transfer mechanism.
	 * 
	 * @throws FileNotFoundException
	 * @throws RemoteException
	 * @throws ConfigurationException
	 * @throws RNSException
	 */
	static public InputStream createInputStream(RNSPath target,
		boolean createBuffered)	throws FileNotFoundException, RemoteException,
			ConfigurationException, RNSException
	{
			return createInputStream(target, null, createBuffered);
	}
	
	/**
	 * Given a remote byteIO, create an input stream that can read from it.
	 * 
	 * @param target The target resource to read bytes from (can be streamable
	 * or random).
	 * 
	 * @return A newly created buffered Java InputStream to use for reading 
	 * bytes from the target ByteIO using the preferred transfer mechanism.
	 * 
	 * @throws FileNotFoundException
	 * @throws RemoteException
	 * @throws ConfigurationException
	 * @throws RNSException
	 */
	static public InputStream createInputStream(RNSPath target)
		throws FileNotFoundException, RemoteException,
			ConfigurationException, RNSException
	{
			return createInputStream(target, null, true);
	}
	
	/**
	 * Given a remote byteIO, create an output stream that can write to it.
	 * 
	 * @param target The target resource to write bytes to (can be streamable
	 * or random).
	 * @param desiredTransferType The desired transfer protocol (if any) to use.
	 * THis value CAN be null.
	 * @param createBuffered Whether or not the stream should be buffered.
	 * 
	 * @return A newly created Java OutputStream to use for writing bytes to
	 * the target ByteIO.
	 * 
	 * @throws FileNotFoundException
	 * @throws RemoteException
	 * @throws ConfigurationException
	 */
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
	
	/**
	 * Given a remote byteIO, create an output stream that can write to it.
	 * 
	 * @param target The target resource to write bytes to (can be streamable
	 * or random).
	 * @param createBuffered Whether or not the stream should be buffered.
	 * 
	 * @return A newly created Java OutputStream to use for writing bytes to
	 * the target ByteIO using the preferred transfer mechanism.
	 * 
	 * @throws FileNotFoundException
	 * @throws RemoteException
	 * @throws ConfigurationException
	 */
	static public OutputStream createOutputStream(EndpointReferenceType target,
		boolean createBuffered) throws FileNotFoundException, RemoteException,
			ConfigurationException
	{
		return createOutputStream(target, null, createBuffered);
	}
	
	/**
	 * Given a remote byteIO, create an output stream that can write to it.
	 * 
	 * @param target The target resource to write bytes to (can be streamable
	 * or random).
	 *  
	 * @return A newly created, buffered Java OutputStream to use for writing
	 * bytes to the target ByteIO using the preferred transfer mechanism.
	 * 
	 * @throws FileNotFoundException
	 * @throws RemoteException
	 * @throws ConfigurationException
	 */
	static public OutputStream createOutputStream(EndpointReferenceType target)
		throws FileNotFoundException, RemoteException, ConfigurationException
	{
		return createOutputStream(target, null, true);
	}
	
	/**
	 * Given a remote byteIO, create an output stream that can write to it.
	 * 
	 * @param target The target resource to write bytes to (can be streamable
	 * or random).
	 * @param desiredTransferType The desired transfer protocol (if any) to use.
	 * THis value CAN be null.
	 * @param createBuffered Whether or not the stream should be buffered.
	 * 
	 * @return A newly created Java OutputStream to use for writing bytes to
	 * the target ByteIO.
	 * 
	 * @throws FileNotFoundException
	 * @throws RemoteException
	 * @throws ConfigurationException
	 * @throws RNSException
	 */
	static public OutputStream createOutputStream(RNSPath target, 
		URI desiredTransferType, boolean createBuffered)
			throws FileNotFoundException, RemoteException,
				ConfigurationException, RNSException
	{
		if (!target.exists())
			target.createNewFile();
		
		return createOutputStream(target.getEndpoint(), desiredTransferType, 
			createBuffered);
	}
	
	/**
	 * Given a remote byteIO, create an output stream that can write to it.
	 * 
	 * @param target The target resource to write bytes to (can be streamable
	 * or random).
	 * @param createBuffered Whether or not the stream should be buffered.
	 * 
	 * @return A newly created Java OutputStream to use for writing bytes to
	 * the target ByteIO using the preferred transfer mechanism.
	 * 
	 * @throws FileNotFoundException
	 * @throws RemoteException
	 * @throws ConfigurationException
	 * @throws RNSException
	 */
	static public OutputStream createOutputStream(RNSPath target,
		boolean createBuffered)	throws FileNotFoundException, RemoteException,
			ConfigurationException, RNSException
	{
			return createOutputStream(target, null, createBuffered);
	}
	
	/**
	 * Given a remote byteIO, create an output stream that can write to it.
	 * 
	 * @param target The target resource to write bytes to (can be streamable
	 * or random).
	 * @return A newly created, buffered Java OutputStream to use for writing
	 * bytes to the target ByteIO using the preferred transfer mechanism.
	 * 
	 * @throws FileNotFoundException
	 * @throws RemoteException
	 * @throws ConfigurationException
	 * @throws RNSException
	 */
	static public OutputStream createOutputStream(RNSPath target)
		throws FileNotFoundException, RemoteException,
			ConfigurationException, RNSException
	{
			return createOutputStream(target, null, true);
	}
}