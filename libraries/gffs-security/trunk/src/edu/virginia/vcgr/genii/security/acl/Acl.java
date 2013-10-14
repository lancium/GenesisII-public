package edu.virginia.vcgr.genii.security.acl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * An access-control policy data-structure comprised of policy-sets. Conveys sets of policies for
 * three types of access: "read", write", and "execute".
 * 
 * Additionally conveys a flag indicating a confidentiality requirement for secure communication.
 * 
 * @author dgm4d
 * 
 */
public class Acl implements Serializable
{
	static final long serialVersionUID = 0L;

	public boolean requireEncryption = false;
	public List<AclEntry> readAcl = new ArrayList<AclEntry>();
	public List<AclEntry> writeAcl = new ArrayList<AclEntry>();
	public List<AclEntry> executeAcl = new ArrayList<AclEntry>();

	public Acl()
	{
	}

	@Override
	public Object clone()
	{
		Acl ret = new Acl();

		ret.requireEncryption = requireEncryption;

		ret.readAcl = new ArrayList<AclEntry>(readAcl);
		ret.writeAcl = new ArrayList<AclEntry>(writeAcl);
		ret.executeAcl = new ArrayList<AclEntry>(executeAcl);

		return ret;
	}

}
