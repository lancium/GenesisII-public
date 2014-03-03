package edu.virginia.vcgr.genii.client.security.axis;

import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.util.Collection;

import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.common.GenesisIIBaseRP;
import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.rp.ResourcePropertyException;
import edu.virginia.vcgr.genii.client.rp.ResourcePropertyManager;
import edu.virginia.vcgr.genii.common.security.AuthZConfig;
import edu.virginia.vcgr.genii.security.RWXCategory;
import edu.virginia.vcgr.genii.security.acl.Acl;
import edu.virginia.vcgr.genii.security.acl.AclEntry;
import edu.virginia.vcgr.genii.security.credentials.X509Identity;

public final class ResourceSecurityPolicy
{
	private GenesisIIBaseRP _propertyManager;
	private Acl _acl;
	private X509Identity _identity;

	public ResourceSecurityPolicy(EndpointReferenceType epr) throws ResourcePropertyException, AuthZSecurityException,
		GeneralSecurityException
	{
		_propertyManager = (GenesisIIBaseRP) ResourcePropertyManager.createRPInterface(epr, GenesisIIBaseRP.class);
		AuthZConfig config = _propertyManager.getAuthZConfig();
		_acl = (config == null ? new Acl() : AxisAcl.decodeAcl(config));
		X509Certificate[] certChain = EPRUtils.extractCertChain(epr);
		_identity = new X509Identity(certChain);
	}

	public void addResource(ResourceSecurityPolicy resource) throws AuthZSecurityException
	{
		_acl.readAcl.add(resource._identity);
		_acl.writeAcl.add(resource._identity);
		_acl.executeAcl.add(resource._identity);
		commit();
	}

	public void addResource(ResourceSecurityPolicy resource, RWXCategory category) throws AuthZSecurityException
	{
		if (category == RWXCategory.READ)
			_acl.readAcl.add(resource._identity);
		if (category == RWXCategory.WRITE)
			_acl.writeAcl.add(resource._identity);
		if (category == RWXCategory.EXECUTE)
			_acl.executeAcl.add(resource._identity);
		commit();
	}

	public void copyFrom(ResourceSecurityPolicy resource) throws AuthZSecurityException
	{
		copyFrom(resource._acl.readAcl, this._acl.readAcl);
		copyFrom(resource._acl.writeAcl, this._acl.writeAcl);
		copyFrom(resource._acl.executeAcl, this._acl.executeAcl);
		commit();
	}

	private static void copyFrom(Collection<AclEntry> src, Collection<AclEntry> dst)
	{
		for (AclEntry entry : src) {
			if (!dst.contains(entry))
				dst.add(entry);
		}
	}

	private void commit() throws AuthZSecurityException
	{
		_propertyManager.setAuthZConfig(AxisAcl.encodeAcl(_acl));
	}
}
