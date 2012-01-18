/*
 * Copyright 2006 University of Virginia
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package edu.virginia.vcgr.genii.security.credentials.assertions;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.EnumSet;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;

import org.apache.axis.message.MessageElement;
import org.apache.ws.security.message.token.BinarySecurity;

import edu.virginia.vcgr.genii.security.RWXCategory;
import edu.virginia.vcgr.genii.security.VerbosityLevel;
import edu.virginia.vcgr.genii.security.WSSecurityUtils;

/**
 * A signed, delegated attribute assertion
 * 
 * @author dmerrill
 */
public class DelegatedAssertion extends SignedAssertionBaseImpl
{

	static public final long serialVersionUID = 0L;

	// A delegate attribute containing an existing signed assertion
	// and the identity of a delegatee
	protected DelegatedAttribute _delegatedAttribute = null;

	// The signature of the above delegated attribute by its signed
	// assertion's authorized identity
	protected byte[] _delegatorSignature = null;
	
	protected EnumSet<RWXCategory> _mask = 
			EnumSet.of(RWXCategory.READ, RWXCategory.WRITE, RWXCategory.EXECUTE);

	// zero-arg contstructor for externalizable use only!
	public DelegatedAssertion()
	{
	}

	public DelegatedAssertion(DelegatedAttribute delegatedAttribute,
			PrivateKey privateKey) throws GeneralSecurityException
	{

		_delegatedAttribute = delegatedAttribute;
		_delegatorSignature =
				SignedAttributeAssertion.sign(delegatedAttribute, privateKey);
	}

	/**
	 * Returns a URI (e.g., a WS-Security Token Profile URI) indicating the
	 * token type
	 */
	public String getTokenType()
	{
		return WSSecurityUtils.DELEGATED_GAML_TOKEN_TYPE;
	}

	/**
	 * Converts this credential to an Axis Message Element
	 * 
	 * @return
	 * @throws GeneralSecurityException
	 */
	public MessageElement toMessageElement() throws GeneralSecurityException
	{

		try
		{
			// Add RequestedSecurityToken element
			MessageElement binaryToken = null;
			try
			{
				binaryToken =
						new MessageElement(BinarySecurity.TOKEN_BST,
								SignedAssertionBaseImpl
										.base64encodeAssertion(this));
				binaryToken.setAttributeNS(null, "ValueType",
						WSSecurityUtils.DELEGATED_GAML_TOKEN_TYPE);
			}
			catch (IOException e)
			{
				throw new GeneralSecurityException(e.getMessage(), e);
			}

			MessageElement embedded =
					new MessageElement(new QName(
							org.apache.ws.security.WSConstants.WSSE11_NS,
							"Embedded"));
			embedded.addChild(binaryToken);

			MessageElement wseTokenRef =
					new MessageElement(new QName(
							org.apache.ws.security.WSConstants.WSSE11_NS,
							"SecurityTokenReference"));
			wseTokenRef.addChild(embedded);

			return wseTokenRef;

		}
		catch (SOAPException e)
		{
			throw new GeneralSecurityException(e.getMessage(), e);
		}
	}

	/**
	 * Returns the identity of the original attribute asserter
	 */
	public X509Certificate[] getAssertingIdentityCertChain()
	{
		return _delegatedAttribute.getAssertingIdentityCertChain();
	}

	/**
	 * Returns the identity of the delegator
	 */
	public X509Certificate[] getDelegatorIdentity()
	{
		return _delegatedAttribute.getAuthorizedIdentity();
	}

	/**
	 * Returns the identity authorized to use this assertion (the delegatee)
	 */
	public X509Certificate[] getAuthorizedIdentity()
	{
		return _delegatedAttribute.getDelegateeIdentity();
	}

	/**
	 * Returns the primary attribute that is being asserted
	 */
	public Attribute getAttribute()
	{
		return _delegatedAttribute.getSignedAssertion().getAttribute();
	}

	/**
	 * Checks that the attribute time-valid with respect to the supplied date
	 */
	public void checkValidity(Date date) throws AttributeInvalidException
	{
		checkValidity(0, date);
	}

	/**
	 * Checks that the assertion is time-valid with respect to the supplied date
	 * and any delegation depth requirements are met by the supplied
	 * delegationDepth.
	 */
	public void checkValidity(int delegationDepth, Date date)
			throws AttributeInvalidException
	{
		// check the validity of the attribute
		_delegatedAttribute.checkValidity(delegationDepth, date);
	}

	/**
	 * Verify the assertion. It is verified if all signatures successfully
	 * authenticate the signed-in authorizing identities
	 */
	public void validateAssertion() throws GeneralSecurityException
	{

		if ((_delegatorSignature == null) || (_delegatedAttribute == null))
		{
			throw new AssertionInvalidException(
					"No signature or data to verify");
		}

		try
		{

			// verify that the signature is from the authorizing identity
			// (delegator)
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(_delegatedAttribute);
			oos.close();

			Signature rsa = Signature.getInstance("SHA1withRSA");

			rsa.initVerify(getDelegatorIdentity()[0]);
			rsa.update(baos.toByteArray());
			if (!rsa.verify(_delegatorSignature))
			{
				throw new AssertionInvalidException(
						"Delegation signature does not authenticate authorizing identity");
			}

			// verify the delegated assertion
			_delegatedAttribute.getSignedAssertion().validateAssertion();

		}
		catch (IOException e)
		{
			throw new GeneralSecurityException(e.getMessage(), e);
		}
	}

	/**
	 * Unwraps the delegated assertion by one layer
	 */
	public SignedAssertion unwrap()
	{
		return _delegatedAttribute._assertion;
	}

	public String toString()
	{
		return describe(VerbosityLevel.HIGH);
	}
	
	@Override
	public String describe(VerbosityLevel verbosity)
	{
		if (verbosity.compareTo(VerbosityLevel.HIGH) >= 0)
			return String.format("(DelegatedAttribute)\n attribute : [%s]",
				_delegatedAttribute.describe(verbosity));
		else
			return _delegatedAttribute.describe(verbosity);
	}
	
	public void writeExternal(ObjectOutput out) throws IOException
	{
		out.writeObject(_delegatedAttribute);
		out.writeObject(_mask);
		out.writeInt(_delegatorSignature.length);
		out.write(_delegatorSignature);
	}

	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException
	{
		_delegatedAttribute = (DelegatedAttribute) in.readObject();
		_mask = (EnumSet<RWXCategory>) in.readObject();
		int sigLen = in.readInt();
		_delegatorSignature = new byte[sigLen];
		in.readFully(_delegatorSignature);
	}

	@Override
	public boolean checkAccess(RWXCategory category)
			throws GeneralSecurityException {
		
		
		Boolean retval = false;
		//Check if my assertion can access this category
		retval = _delegatedAttribute._assertion.checkAccess(category);
		
		//Check if I can
		if (!_mask.contains(category))
			throw new GeneralSecurityException(
					"Credential does not have " + 
			category.toString() + " access");
		
		return retval;
		
	}

	@Override
	public void setMask(EnumSet<RWXCategory> perms) {
		_mask = perms;
	}

}
