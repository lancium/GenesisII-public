package edu.virginia.vcgr.genii.client.byteio.transfer;

import java.rmi.RemoteException;
import java.util.TreeSet;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.morgan.util.configuration.ConfigurationException;
import org.oasis_open.docs.wsrf.rp_2.GetResourcePropertyResponse;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.byteio.ByteIOConstants;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.common.GeniiCommon;

abstract class TransfererFactory
{
	static final private String []_SORTED_TRANSFER_TYPES = new String[] {
		ByteIOConstants.TRANSFER_TYPE_MTOM,
		ByteIOConstants.TRANSFER_TYPE_DIME,
		ByteIOConstants.TRANSFER_TYPE_SIMPLE
	};
	
	private TreeSet<String> _supportedTransferTypes = new TreeSet<String>();
	private String _preferredTransferType = null;
		
	private Object _clientStub;
	
	protected TransfererFactory(Object clientStub)
		throws ConfigurationException, RemoteException
	{
		_clientStub = clientStub;
		
		fillInPreferences();
	}
	
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
	
	private void fillInPreferences()
		throws ConfigurationException, RemoteException
	{
		EndpointReferenceType epr = ClientUtils.extractEPR(_clientStub);
		GeniiCommon stub = ClientUtils.createProxy(GeniiCommon.class, epr);
		TypeInformation ti = new TypeInformation(epr);
		QName attrName;
		
		if (ti.isSByteIO())
			attrName = new QName(ByteIOConstants.STREAMABLE_BYTEIO_NS, 
				ByteIOConstants.XFER_MECHS_ATTR_NAME);
		else
			attrName = new QName(ByteIOConstants.RANDOM_BYTEIO_NS,
				ByteIOConstants.XFER_MECHS_ATTR_NAME);
		
		GetResourcePropertyResponse resp = stub.getResourceProperty(attrName);
		parseAttributes(resp.get_any());
		
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
			throw new ConfigurationException(
				"Unable to determine supported or preferred " +
				"transfer type for ByteIO.");
	}
	
	protected abstract Object createMTOMTransferer(Object clientStub);
	protected abstract Object createDimeTransferer(Object clientStub);
	protected abstract Object createSimpleTransferer(Object clientStub);
	
	protected Object createTransferer(String requestedTransferType)
		throws ConfigurationException
	{
		if (requestedTransferType == null)
			requestedTransferType = _preferredTransferType;
		
		if (!_supportedTransferTypes.contains(requestedTransferType))
			throw new ConfigurationException(
				"Target ByteIO does not support the \"" + 
				requestedTransferType + "\" transfer type.");
		
		if (requestedTransferType.equals(ByteIOConstants.TRANSFER_TYPE_MTOM))
			return createMTOMTransferer(_clientStub);
		else if (requestedTransferType.equals(ByteIOConstants.TRANSFER_TYPE_DIME))
			return createDimeTransferer(_clientStub);
		else if (requestedTransferType.equals(ByteIOConstants.TRANSFER_TYPE_SIMPLE))
			return createSimpleTransferer(_clientStub);
		else
			throw new ConfigurationException("Internal error -- Don't know " +
				"how to create a transferer for transfer type \"" + 
				requestedTransferType + "\".");
	}
	
	protected Object createTransferer()
		throws ConfigurationException
	{
		return createTransferer(_preferredTransferType);
	}
}