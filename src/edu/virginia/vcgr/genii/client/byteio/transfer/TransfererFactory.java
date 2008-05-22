package edu.virginia.vcgr.genii.client.byteio.transfer;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.TreeSet;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.oasis_open.docs.wsrf.rp_2.GetResourcePropertyResponse;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.byteio.ByteIOConstants;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.common.GeniiCommon;

/**
 * This is an abstract base class for a convenience factory that can
 * be used to create transferer agents for both streamable and random
 * byte io's.
 * 
 * @author mmm2a
 */
abstract class TransfererFactory
{
	/**
	 * These are the known transfer types supported by the transferers at this
	 * time.  They are sorted in order from most preferred, to least preferred
	 * in terms of efficiency.
	 */
	static final private String []_SORTED_TRANSFER_TYPES = new String[] 
    {
		ByteIOConstants.TRANSFER_TYPE_MTOM,
		ByteIOConstants.TRANSFER_TYPE_DIME,
		ByteIOConstants.TRANSFER_TYPE_SIMPLE
	};
	
	/**
	 * Each given target byteIO may or may not supported all of the known 
	 * transfer types (it depends on the target implementation).  This
	 * variable keeps track of which ones are supported by the current
	 * target byteio.
	 */
	private TreeSet<String> _supportedTransferTypes = new TreeSet<String>();
	
	private String _preferredTransferType = null;
	
	/**
	 * The target stub to talk to.
	 */
	private Object _clientStub;
	
	/**
	 * Create a new transferer factory for a given target stub.
	 * 
	 * @param clientStub THe target stub to use for remote communication.
	 * Note that it's an object and type a parameterized type.  This is
	 * because it will either be a RandomByteIO client stub, or a
	 * streamable byteio client stub.  The two are auto generated classes
	 * and don't have any thing in common with each other (code-wise) so
	 * a parameterized type here wouldn't help.
	 * 
	 * @throws ConfigurationExceptionMOOCH
	 * @throws RemoteException
	 */
	protected TransfererFactory(Object clientStub)
		throws RemoteException, IOException
	{
		_clientStub = clientStub;
		
		fillInPreferences();
	}
	
	/**
	 * Given the XML attributes of a given target ByteIO, parse those
	 * attributes and determine what transfer types the target claims
	 * it supports.
	 * 
	 * @param any The XML elements to parse.
	 */
	private void parseAttributes(MessageElement []any)
	{
		if (any == null)
			return;
		
		for (MessageElement element : any)
		{
			String xfer = element.getValue();
			_supportedTransferTypes.add(xfer);
		}
	}
	
	/**
	 * Given a target ByteIO, ask for it's attributes and figure out
	 * which transfer protocols it supports.
	 * 
	 * @throws ConfigurationExceptionMOOCH
	 * @throws RemoteException
	 */
	private void fillInPreferences()
		throws RemoteException, IOException
	{
		EndpointReferenceType epr = ClientUtils.extractEPR(_clientStub);
		GeniiCommon stub = ClientUtils.createProxy(GeniiCommon.class, epr);
		TypeInformation ti = new TypeInformation(epr);
		QName attrName;
		
		/* We have to figure out if the target is a Random ByteIO or a
		 * streamable ByteIO because the namespaces are different for the
		 * attributes of each.
		 */
		if (ti.isSByteIO())
			attrName = new QName(ByteIOConstants.STREAMABLE_BYTEIO_NS, 
				ByteIOConstants.XFER_MECHS_ATTR_NAME);
		else
			attrName = new QName(ByteIOConstants.RANDOM_BYTEIO_NS,
				ByteIOConstants.XFER_MECHS_ATTR_NAME);
		
		/* Make the remote call to get the supported transfer mechanisms
		 * attribute and the parse the result.
		 */
		GetResourcePropertyResponse resp = stub.getResourceProperty(attrName);
		parseAttributes(resp.get_any());
		
		/**
		 * Given all of the supported transfer types, figure out the "best" 
		 * one.
		 */
		for (String xfer : _SORTED_TRANSFER_TYPES)
		{
			if (_supportedTransferTypes.contains(xfer))
			{
				_preferredTransferType = xfer;
				break;
			}
		}
		
		if (_supportedTransferTypes.size() == 0 ||
			_preferredTransferType == null)
			throw new IOException(
				"Unable to determine supported or preferred " +
				"transfer type for ByteIO.");
	}
	
	/**
	 * Create a transferer that uses the MTOM attachment specification for
	 * transferring bytes.
	 * 
	 * @param clientStub The client stub to use for remote communication
	 * when doing transfers.
	 * 
	 * @return A newly create MTOM transferer.
	 */
	protected abstract Object createMTOMTransferer(Object clientStub);
	
	/**
	 * Create a transferer that uses the DIME attachment specification for
	 * transferring bytes.
	 * 
	 * @param clientStub The client stub to use for remote communication
	 * when doing transfers.
	 * 
	 * @return A newly create DIME transferer.
	 */
	protected abstract Object createDimeTransferer(Object clientStub);
	
	/**
	 * Create a transferer that uses the Simple attachment specification for
	 * transferring bytes.
	 * 
	 * @param clientStub The client stub to use for remote communication
	 * when doing transfers.
	 * 
	 * @return A newly create Simple transferer.
	 */
	protected abstract Object createSimpleTransferer(Object clientStub);
	
	/**
	 * Create a transferer of the requested transfer type.
	 * 
	 * @param requestedTransferType The transfer type requested.
	 * 
	 * @return A newly create transferer.
	 * 
	 * @throws ConfigurationExceptionMOOCH
	 */
	protected Object createTransferer(String requestedTransferType)
		throws IOException
	{
		if (requestedTransferType == null)
			requestedTransferType = _preferredTransferType;
		
		if (!_supportedTransferTypes.contains(requestedTransferType))
			throw new IOException(
				"Target ByteIO does not support the \"" + 
				requestedTransferType + "\" transfer type.");
		
		if (requestedTransferType.equals(ByteIOConstants.TRANSFER_TYPE_MTOM))
			return createMTOMTransferer(_clientStub);
		else if (requestedTransferType.equals(ByteIOConstants.TRANSFER_TYPE_DIME))
			return createDimeTransferer(_clientStub);
		else if (requestedTransferType.equals(ByteIOConstants.TRANSFER_TYPE_SIMPLE))
			return createSimpleTransferer(_clientStub);
		else
			throw new IOException("Internal error -- Don't know " +
				"how to create a transferer for transfer type \"" + 
				requestedTransferType + "\".");
	}
	
	/**
	 * Create a transferer of the "preferred" or "best" type.
	 * 
	 * @return The newly created transferer.
	 * 
	 * @throws ConfigurationExceptionMOOCH
	 */
	protected Object createTransferer()
		throws IOException
	{
		return createTransferer(_preferredTransferType);
	}
}