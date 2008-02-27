package edu.virginia.vcgr.genii.client.rns;

import java.rmi.RemoteException;

import org.apache.axis.message.MessageElement;
import org.ggf.rns.Add;
import org.ggf.rns.CreateFile;
import org.ggf.rns.EntryPropertiesType;
import org.ggf.rns.EntryType;
import org.ggf.rns.List;
import org.ggf.rns.Query;
import org.ggf.rns.RNSDirectoryNotEmptyFaultType;
import org.ggf.rns.RNSEntryExistsFaultType;
import org.ggf.rns.RNSEntryNotDirectoryFaultType;
import org.ggf.rns.RNSFaultType;
import org.ggf.rns.RNSPortType;
import org.ggf.rns.Remove;
import org.morgan.util.configuration.ConfigurationException;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.comm.ClientUtils;

import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;

public class RNSCache
{
	static public EndpointReferenceType add(EndpointReferenceType target,
		String entryName, EndpointReferenceType entryReference,
		MessageElement []any) throws ConfigurationException, RemoteException, RNSEntryExistsFaultType, RNSFaultType, 
			ResourceUnknownFaultType, RNSEntryNotDirectoryFaultType
	{
		RNSPortType rpt = ClientUtils.createProxy(RNSPortType.class, target);
		return rpt.add(new Add(entryName, entryReference, any)).getEntry_reference();
	}

	static public EndpointReferenceType createFile(EndpointReferenceType target, String filename)
		throws ConfigurationException, RemoteException, RNSEntryExistsFaultType, RNSFaultType,
			ResourceUnknownFaultType, RNSEntryNotDirectoryFaultType
	{
		RNSPortType rpt = ClientUtils.createProxy(RNSPortType.class, target);
		return rpt.createFile(new CreateFile(filename)).getEntry_reference();
	}

	static public EntryType[] list(EndpointReferenceType target, String entryNameRegExp) 
		throws ConfigurationException, RemoteException, RNSFaultType, ResourceUnknownFaultType, 
			RNSEntryNotDirectoryFaultType
	{
		RNSPortType rpt = ClientUtils.createProxy(RNSPortType.class, target);
		return rpt.list(new List(entryNameRegExp)).getEntryList();
	}

	static public EntryPropertiesType query(EndpointReferenceType target, String entryPattern) 
		throws ConfigurationException, RemoteException, RNSFaultType, ResourceUnknownFaultType
	{
		RNSPortType rpt = ClientUtils.createProxy(RNSPortType.class, target);
		return rpt.query(new Query(entryPattern)).getEntryProperties();
	}

	static public String[] remove(EndpointReferenceType target, String entryNameRegExp)
		throws ConfigurationException, RemoteException, RNSFaultType, ResourceUnknownFaultType, 
			RNSDirectoryNotEmptyFaultType
	{
		RNSPortType rpt = ClientUtils.createProxy(RNSPortType.class, target);
		return rpt.remove(new Remove(entryNameRegExp));
	}
}