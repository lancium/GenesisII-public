package edu.virginia.vcgr.genii.container.bes;

import java.rmi.RemoteException;

import javax.xml.namespace.QName;

import org.apache.axis.types.URI;
import org.xmlsoap.schemas.soap.envelope.Detail;
import org.xmlsoap.schemas.soap.envelope.Fault;

public class FaultConstructor
{
	static public Fault constructFault(Throwable cause)
		throws RemoteException
	{
		try
		{
			return new Fault(
				new QName("http://tempuri.org/", "unknown-fault"),
				"Fault occurred in activity.", 
				new URI("http://somefault-actor"),
				new Detail(null));
		}
		catch (Throwable cc)
		{
			throw new RemoteException("Unable to create soap fault.", cc);
		}
	}
}