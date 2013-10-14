package edu.virginia.vcgr.genii.client.bes;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.apache.axis.types.URI;
import org.xmlsoap.schemas.soap.envelope.Detail;
import org.xmlsoap.schemas.soap.envelope.Fault;

public class BESFaultManager
{
	static final public String FAULT_NS = "http://genii.virginia.edu/faults";
	static final public QName FAULT_QNAME = new QName(FAULT_NS, "activity-fault");
	static final public QName FAULT_DETAIL_NAME = new QName(FAULT_NS, "detail");

	static private Detail constructDetail(Throwable[] causes)
	{
		Collection<MessageElement> faultStrings = new ArrayList<MessageElement>(causes.length);

		for (Throwable cause : causes) {
			StringWriter sWriter = new StringWriter();
			PrintWriter writer = new PrintWriter(sWriter);
			cause.printStackTrace(writer);
			writer.flush();
			faultStrings.add(new MessageElement(FAULT_DETAIL_NAME, sWriter.toString()));
		}

		return new Detail(faultStrings.toArray(new MessageElement[0]));
	}

	static public Fault constructFault(Throwable... causes) throws RemoteException
	{
		try {
			return new Fault(FAULT_QNAME, "Fault occurred in activity.", new URI("http://somefault-actor"),
				constructDetail(causes));
		} catch (Throwable cc) {
			throw new RemoteException("Unable to create soap fault.", cc);
		}
	}

	static public List<String> getFaultDetail(Fault fault)
	{
		List<String> ret = null;

		Detail d = fault.getDetail();
		if (d != null) {
			MessageElement[] any = d.get_any();
			if (any != null) {
				ret = new ArrayList<String>(any.length);
				for (MessageElement a : any) {
					if (a.getQName().equals(FAULT_DETAIL_NAME)) {
						ret.add(a.getValue());
					}
				}
			} else {
				ret = new ArrayList<String>();
			}
		}

		return ret;
	}
}