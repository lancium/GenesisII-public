package edu.virginia.vcgr.genii.client.security.authz.acl;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import edu.virginia.vcgr.genii.security.acl.AclEntry;

/**
 * compatibility class that can read from old db blobs.
 */
public class Acl implements Serializable
{
	static final long serialVersionUID = 0L;

	public boolean requireEncryption = false;
	public List<AclEntry> readAcl = new ArrayList<AclEntry>();
	public List<AclEntry> writeAcl = new ArrayList<AclEntry>();
	public List<AclEntry> executeAcl = new ArrayList<AclEntry>();

	private Object readResolve() throws ObjectStreamException
	{
		edu.virginia.vcgr.genii.security.acl.Acl toReturn = new edu.virginia.vcgr.genii.security.acl.Acl();
		toReturn.readAcl = readAcl;
		toReturn.writeAcl = writeAcl;
		toReturn.executeAcl = executeAcl;
		return toReturn;
	}

	private Object writeReplace() throws ObjectStreamException
	{
		return readResolve();
	}
}
