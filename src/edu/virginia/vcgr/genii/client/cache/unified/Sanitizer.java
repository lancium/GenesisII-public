package edu.virginia.vcgr.genii.client.cache.unified;

import javax.xml.namespace.QName;

import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.security.authz.AuthZSecurityException;
import edu.virginia.vcgr.genii.client.security.authz.acl.Acl;
import edu.virginia.vcgr.genii.client.ser.ObjectDeserializer;
import edu.virginia.vcgr.genii.client.ser.ObjectSerializer;
import edu.virginia.vcgr.genii.common.security.AuthZConfig;

/*
 * This is the utility class that removes references to SOAPMessages from objects that we want to store in cache.
 * If we don't remove all references of a SOAPMessage from the going to be stored item then the cache will quickly
 * get out of space and bring down the system with OutOfMemoryException.
 * */
public class Sanitizer {

	public static AuthZConfig getSanitizedAuthZConfig(AuthZConfig authZConfig) {
		try {
			Acl acl = Acl.decodeAcl(authZConfig);
			return Acl.encodeAcl(acl);
		} catch (AuthZSecurityException e) {
			throw new RuntimeException("failed to sanitize AuthZConfig", e);
		}
	}
	
	public static EndpointReferenceType getSanitizedEpr(EndpointReferenceType epr) {
		try {
			byte[] eprBytes = ObjectSerializer.toBytes(epr, new QName(GenesisIIConstants.GENESISII_NS, "endpoint"));
			return ObjectDeserializer.fromBytes(EndpointReferenceType.class, eprBytes);
		} catch (ResourceException e) {
			throw new RuntimeException("failed to sanitize EndpointReferenceType", e);
		} 
	}
}
