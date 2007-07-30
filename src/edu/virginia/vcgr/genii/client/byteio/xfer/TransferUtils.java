package edu.virginia.vcgr.genii.client.byteio.xfer;

import java.rmi.RemoteException;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.apache.axis.types.URI;
import org.apache.axis.types.URI.MalformedURIException;
import org.morgan.util.configuration.ConfigurationException;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.byteio.ByteIOConstants;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.security.GenesisIISecurityException;
import edu.virginia.vcgr.genii.common.GeniiCommon;
import edu.virginia.vcgr.genii.common.rattrs.AttributeUnknownFaultType;
import edu.virginia.vcgr.genii.common.rattrs.GetAttributesResponse;
import edu.virginia.vcgr.genii.common.resource.ResourceUnknownFaultType;

public class TransferUtils
{
	private EndpointReferenceType _target;
	private URI _preferredTransferType = null;
	private int _preferredBlockSize = -1;
	
	static private final int _XFER_MECH_SIMPLE_RANK = 2;
	static private final int _XFER_MECH_DIME_RANK = 1;
	static private final int _XFER_MECH_MTOM_RANK = 0;
	
	static private final int _XFER_MECH_SIMPLE_BLOCK_SIZE = 1024 * 256 * 2;
	static private final int _XFER_MECH_DIME_BLOCK_SIZE = 1024 * 1024;
	static private final int _XFER_MECH_MTOM_BLOCK_SIZE = 1024 * 1024;
	
	public TransferUtils(EndpointReferenceType epr)
	{
		_target = epr;
	}
	
	private void parseAttributes(MessageElement []elements)
	{
		URI []xfers = new URI[3];
		for (int lcv = 0; lcv < xfers.length; lcv++)
			xfers[lcv] = null;
		
		for (MessageElement element : elements)
		{
			try
			{
				URI xfer = new URI(element.getValue());
				if (xfer.equals(ByteIOConstants.TRANSFER_TYPE_DIME_URI))
					xfers[_XFER_MECH_DIME_RANK] = xfer;
				else if (xfer.equals(ByteIOConstants.TRANSFER_TYPE_MTOM_URI))
					xfers[_XFER_MECH_MTOM_RANK] = xfer;
				else
					xfers[_XFER_MECH_SIMPLE_RANK] =
						ByteIOConstants.TRANSFER_TYPE_SIMPLE_URI;
				
			}
			catch (MalformedURIException mui)
			{
			}
		}
		
		for (URI xfer : xfers)
		{
			if (xfer != null)
			{
				if (xfer.equals(ByteIOConstants.TRANSFER_TYPE_DIME_URI))
					_preferredBlockSize = _XFER_MECH_DIME_BLOCK_SIZE;
				else if (xfer.equals(ByteIOConstants.TRANSFER_TYPE_MTOM_URI))
					_preferredBlockSize = _XFER_MECH_MTOM_BLOCK_SIZE;
				else
					_preferredBlockSize = _XFER_MECH_SIMPLE_BLOCK_SIZE;
				
				_preferredTransferType = xfer;
				return;
			}
		}
		
		_preferredBlockSize = _XFER_MECH_SIMPLE_BLOCK_SIZE;
		_preferredTransferType = ByteIOConstants.TRANSFER_TYPE_SIMPLE_URI;
	}
	
	private void fillInPreferences()
		throws ConfigurationException, GenesisIISecurityException, 
			ResourceException, AttributeUnknownFaultType,
			ResourceUnknownFaultType, RemoteException
	{
		synchronized(this)
		{
			if (_preferredTransferType != null)
				return;
			
			GeniiCommon stub = ClientUtils.createProxy(GeniiCommon.class, 
				_target);
			
			TypeInformation ti = new TypeInformation(_target);
			QName attrName;
			if (ti.isRByteIO())
				attrName = new QName(ByteIOConstants.RANDOM_BYTEIO_NS,
					ByteIOConstants.XFER_MECHS_ATTR_NAME);
			else
				attrName = new QName(ByteIOConstants.STREAMABLE_BYTEIO_NS,
					ByteIOConstants.XFER_MECHS_ATTR_NAME);
			
			GetAttributesResponse resp = stub.getAttributes(new QName[] {
				attrName } );
			MessageElement []elem = resp.get_any();
			parseAttributes(elem);
		}
	}
	
	public URI getPreferredTransferType()
		throws RemoteException, ConfigurationException
	{
		fillInPreferences();
		
		return _preferredTransferType;
	}
	
	public int getPreferredBlockSize()
		throws RemoteException, ConfigurationException
	{
		fillInPreferences();
		
		return _preferredBlockSize;
	}
}