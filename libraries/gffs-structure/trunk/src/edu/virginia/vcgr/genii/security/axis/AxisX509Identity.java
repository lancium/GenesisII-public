package edu.virginia.vcgr.genii.security.axis;

import java.security.GeneralSecurityException;

import org.apache.axis.message.MessageElement;
import org.w3c.dom.Element;

import edu.virginia.vcgr.genii.security.XMLCompatible;
import edu.virginia.vcgr.genii.security.credentials.X509Identity;

/**
 * implements axis message element support for x509 identities.
 * 
 * @author dmerrill
 * @author ckoeritz
 */
public class AxisX509Identity implements XMLCompatible
{
	X509Identity _realId;

	public AxisX509Identity(X509Identity toOutput)
	{
		_realId = toOutput;
	}

	public AxisX509Identity(MessageElement secRef) throws GeneralSecurityException
	{
		_realId.setIdentity(WSSecurityUtils.getChainFromPkiPathSecTokenRef(secRef));
	}

	@Override
	public String getTokenType()
	{
		return WSSecurityUtils.X509PKIPathv1_URI;
	}

	@Override
	public Element convertToMessageElement() throws GeneralSecurityException
	{
		return (Element) WSSecurityUtils.makePkiPathSecTokenRef(_realId.getOriginalAsserter());
	}
}
