package edu.virginia.vcgr.genii.container.rns;

import javax.xml.namespace.QName;

public class GeniiDirPolicy
{
	static public final String GENII_DIR_NS =
		"http://vcgr.cs.virginia.edu/genii/genii-dir";
	static public final QName RESOLVER_POLICY_QNAME =
		new QName(GENII_DIR_NS, "resolver-policy");
	static public final String RESOLVER_POLICY_PROP_NAME =
		RESOLVER_POLICY_QNAME.toString();
	static public final QName REPLICATION_POLICY_QNAME =
		new QName(GENII_DIR_NS, "replication-policy");
	static public final String REPLICATION_POLICY_PROP_NAME =
		REPLICATION_POLICY_QNAME.toString();
}
