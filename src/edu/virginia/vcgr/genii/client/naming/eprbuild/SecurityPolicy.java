package edu.virginia.vcgr.genii.client.naming.eprbuild;

import java.util.Collection;
import java.util.LinkedList;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;

import org.apache.axis.message.MessageElement;
import org.apache.axis.types.URI;
import org.apache.axis.types.URI.MalformedURIException;
import org.oasis_open.docs.ws_sx.ws_securitypolicy._200702.EmptyType;
import org.oasis_open.docs.ws_sx.ws_securitypolicy._200702.IncludeTokenOpenType;
import org.oasis_open.docs.ws_sx.ws_securitypolicy._200702.IncludeTokenType;
import org.oasis_open.docs.ws_sx.ws_securitypolicy._200702.NestedPolicyType;
import org.oasis_open.docs.ws_sx.ws_securitypolicy._200702.SePartsType;
import org.oasis_open.docs.ws_sx.ws_securitypolicy._200702.TokenAssertionType;
import org.oasis_open.docs.ws_sx.ws_trust._200512.ClaimsType;
import org.w3.www.ns.ws_policy.AppliesTo;
import org.w3.www.ns.ws_policy.Policy;
import org.w3.www.ns.ws_policy.PolicyAttachment;
import org.w3.www.ns.ws_policy.PolicyReference;

import edu.virginia.vcgr.genii.security.SecurityConstants;

public class SecurityPolicy
{
	static public MessageElement constructMetaPolicy(Collection<MessageElement> policyReferences)
	{
		// construct Meta Policy
		Policy metaPolicy = new Policy();
		metaPolicy.set_any(policyReferences.toArray(new MessageElement[policyReferences.size()]));

		// construct AppliesTo
		org.w3.www.ns.ws_policy.URI appliesToUri = new org.w3.www.ns.ws_policy.URI("urn:wsaaction:*");
		MessageElement[] appliesToAny = { new MessageElement(org.w3.www.ns.ws_policy.URI.getTypeDesc().getXmlType(),
			appliesToUri) };

		AppliesTo appliesTo = new AppliesTo(appliesToAny);

		// construct Policy Attachment
		PolicyAttachment policyAttachment = new PolicyAttachment();
		policyAttachment.setAppliesTo(appliesTo);
		policyAttachment.setPolicy(metaPolicy);
		MessageElement policyAttachmentMel = new MessageElement(PolicyAttachment.getTypeDesc().getXmlType(), policyAttachment);

		return policyAttachmentMel;
	}

	static public MessageElement usernamePasswordPolicy(boolean isOptional)
	{
		try {
			URI usernameTokenUri = new URI(SecurityConstants.USERNAME_TOKEN_URI);

			PolicyReference usernameTokenReference = new PolicyReference();
			usernameTokenReference.setURI(usernameTokenUri);
			MessageElement usernameTokenMel = new MessageElement(PolicyReference.getTypeDesc().getXmlType().getNamespaceURI(),
				"PolicyReference", usernameTokenReference);

			if (isOptional)
				usernameTokenMel.setAttribute(Policy.getTypeDesc().getXmlType().getNamespaceURI(), "Optional", "true");

			return usernameTokenMel;
		} catch (MalformedURIException e) {
			// This can't happen
			return null;
		}
	}

	static public MessageElement requireEncryptionPolicy()
	{
		SePartsType encryptedParts = new SePartsType();
		encryptedParts.setBody(new EmptyType());
		MessageElement encryptMel = new MessageElement(SePartsType.getTypeDesc().getXmlType().getNamespaceURI(),
			"EncryptedParts", encryptedParts);
		return encryptMel;
	}

	static public MessageElement includeServerTls()
	{
		try {
			org.apache.axis.types.URI serverTlsUri = new org.apache.axis.types.URI(SecurityConstants.SERVER_TLS_URI);
			PolicyReference serverTlsReference = new PolicyReference();
			serverTlsReference.setURI(serverTlsUri);
			MessageElement tlsMel = new MessageElement(PolicyReference.getTypeDesc().getXmlType().getNamespaceURI(),
				"PolicyReference", serverTlsReference);
			return tlsMel;
		} catch (MalformedURIException e) {
			// This can't happen
			return null;
		}
	}

	static public Collection<MessageElement> requireMessageSigning()
	{
		Collection<MessageElement> policyComponents = new LinkedList<MessageElement>();

		try {
			// add MutualX509 Ref
			org.apache.axis.types.URI mutualX509Uri = new org.apache.axis.types.URI(SecurityConstants.MUTUAL_X509_URI);
			PolicyReference mutualX509Reference = new PolicyReference();
			mutualX509Reference.setURI(mutualX509Uri);
			MessageElement x509Mel = new MessageElement(PolicyReference.getTypeDesc().getXmlType().getNamespaceURI(),
				"PolicyReference", mutualX509Reference);
			policyComponents.add(x509Mel);

			// add our optional blend of Credentials

			// create saml claim type
			ClaimsType claims = new ClaimsType();
			claims.setDialect(new org.apache.axis.types.URI(SecurityConstants.SAML_CLAIMS_URI));

			// create include attribute
			IncludeTokenOpenType includeToken = new IncludeTokenOpenType(IncludeTokenType._value3);

			// create saml token
			TokenAssertionType samlToken = new TokenAssertionType();
			samlToken.setIncludeToken(includeToken);
			MessageElement[] samlSubEls = { new MessageElement(ClaimsType.getTypeDesc().getXmlType().getNamespaceURI(),
				"Claims", claims) };
			samlToken.set_any(samlSubEls);

			// create policy
			Policy samlPolicy = new Policy();
			MessageElement[] policySubEls = { new MessageElement(TokenAssertionType.getTypeDesc().getXmlType()
				.getNamespaceURI(), "SamlToken", samlToken) };
			samlPolicy.set_any(policySubEls);

			// create SignedSupporting Tokens
			MessageElement signedSupportingTokensMel = new MessageElement(new QName(NestedPolicyType.getTypeDesc().getXmlType()
				.getNamespaceURI(), "SignedSupportingTokens"));
			signedSupportingTokensMel.setAttribute(Policy.getTypeDesc().getXmlType().getNamespaceURI(), "Optional", "true");
			signedSupportingTokensMel.addChild(new MessageElement(Policy.getTypeDesc().getXmlType().getNamespaceURI(),
				"Policy", samlPolicy));

			policyComponents.add(signedSupportingTokensMel);
		} catch (MalformedURIException e) {
			// This can't happen
		} catch (SOAPException e) {
			// This shouldn't happen
		}

		return policyComponents;
	}
}