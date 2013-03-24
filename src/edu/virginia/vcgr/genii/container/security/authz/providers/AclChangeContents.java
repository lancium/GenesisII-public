package edu.virginia.vcgr.genii.container.security.authz.providers;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import edu.virginia.vcgr.genii.client.wsrf.wsn.NotificationMessageContents;
import edu.virginia.vcgr.genii.common.security.AclEntryListType;
import edu.virginia.vcgr.genii.container.sync.VersionVector;

@XmlRootElement(namespace = AclChangeContents.GENII_ACL_NAMESPACE, name = "AclContentsChangedContents")
public class AclChangeContents extends NotificationMessageContents
{
	static final long serialVersionUID = 0L;

	static public final String GENII_ACL_NAMESPACE = "http://vcgr.cs.virginia.edu/genii/saml-acl";

	@XmlElement(namespace = GENII_ACL_NAMESPACE, name = "aclEntryList")
	private AclEntryListType _aclEntryList;

	/**
	 * aclEntryList[i] was given mode tagList[i], where the mode is "+" or "-" on "r", "w", or "x".
	 */
	@XmlElement(namespace = GENII_ACL_NAMESPACE, name = "tagList")
	private String[] _tagList;

	@XmlElement(namespace = GENII_ACL_NAMESPACE, name = "versionVector")
	private VersionVector _versionVector;

	protected AclChangeContents()
	{
	}

	public AclChangeContents(AclEntryListType aclEntryList, String[] tagList, VersionVector versionVector)
	{
		_aclEntryList = aclEntryList;
		_tagList = tagList;
		_versionVector = versionVector;
	}

	final public AclEntryListType aclEntryList()
	{
		return _aclEntryList;
	}

	final public String[] tagList()
	{
		return _tagList;
	}

	final public VersionVector versionVector()
	{
		return _versionVector;
	}
}
