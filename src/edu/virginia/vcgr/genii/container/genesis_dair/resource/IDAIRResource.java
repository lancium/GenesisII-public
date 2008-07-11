package edu.virginia.vcgr.genii.container.genesis_dair.resource;

import java.util.Collection;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;

import org.ggf.rns.EntryType;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.container.rns.IRNSResource;

public interface IDAIRResource extends IRNSResource{
	
	static public QName _DB_DRIVER_NAME_PARAM =
		new QName(GenesisIIConstants.GENESISII_NS, "driver-param");
	static public QName _CONNECT_STRING_PARAM =
		new QName(GenesisIIConstants.GENESISII_NS, "connect_string-param");
	static public QName _USERNAME_PARAM =
		new QName(GenesisIIConstants.GENESISII_NS, "username-param");
	static public QName _PASSWORD_PARAM =
		new QName(GenesisIIConstants.GENESISII_NS, "password-param");
	
	public void addEntry( EndpointReferenceType serviceEPR, String resourceName, 
			EndpointReferenceType resourceEPR, String query) 
		throws ResourceException;
	
	public Collection<String> removeEntries(String name) 
		throws ResourceException;
	
	public Collection<EntryType> listResources(Pattern pattern) 
		throws ResourceException;
	
	public Collection<String> remove(Pattern pattern) 
		throws ResourceException;
	
	public void configureResource(String resourceName, int numSlots) 
		throws ResourceException;
}
