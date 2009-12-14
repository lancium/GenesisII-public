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

package edu.virginia.vcgr.genii.client.security.credentials.assertions;

import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.io.*;
import java.util.*;

import edu.virginia.vcgr.genii.client.security.credentials.GIICredential;

import edu.virginia.vcgr.genii.client.ser.Base64;

public abstract class SignedAssertionBaseImpl implements SignedAssertion,
		Externalizable, GIICredential
{
	static public final long serialVersionUID = 0L;

	/** Cached serialized value for comparison checking * */
	protected transient String _encodedValue = null;

	/**
	 * Returns the primary attribute that is being asserted
	 */
	abstract public Attribute getAttribute();

	/**
	 * Returns the certchain of the identity authorized to use this assertion
	 * (same as the asserter)
	 */
	public abstract X509Certificate[] getAuthorizedIdentity();

	public int hashCode()
	{
		try
		{
			return base64encodeAssertion(this).hashCode();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return 0;
	}

	public boolean equals(Object o)
	{
		if (!(o instanceof SignedAssertionBaseImpl))
			return false;
		
		SignedAssertionBaseImpl other = (SignedAssertionBaseImpl) o;
		// force encoded values to represent signed assertion
		try
		{
			if (base64encodeAssertion(this)
					.equals(base64encodeAssertion(other)))
			{
				return true;
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		return false;
	}

	/**
	 * Verify the assertion. It is verified if all signatures successfully
	 * authenticate the signed-in authorizing identities
	 */
	abstract public void validateAssertion() throws GeneralSecurityException;

	abstract public String toString();

	/**
	 * Utility method for Base64-encoding a signed assertion.
	 * 
	 * @param signedAssertion
	 * @return
	 * @throws IOException
	 */
	public static String base64encodeAssertion(SignedAssertion signedAssertion)
			throws IOException
	{
		synchronized (signedAssertion)
		{
			SignedAssertionBaseImpl baseSignedAssertion = null;
			if (signedAssertion instanceof SignedAssertionBaseImpl)
			{
				baseSignedAssertion = (SignedAssertionBaseImpl) signedAssertion;
				if (baseSignedAssertion._encodedValue != null)
				{
					return baseSignedAssertion._encodedValue;
				}
			}

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(signedAssertion);
			oos.close();
			String retval = Base64.byteArrayToBase64(baos.toByteArray());

			if (baseSignedAssertion != null)
			{
				baseSignedAssertion._encodedValue = retval;
			}

			return retval;
		}
	}

	/**
	 * Utility method for Base64-encoding signed assertions.
	 * 
	 * @param signedAssertions
	 * @return
	 * @throws IOException
	 */
	public static String base64encodeAssertions(
			ArrayList<SignedAssertion> signedAssertions) throws IOException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(signedAssertions);
		oos.close();

		return Base64.byteArrayToBase64(baos.toByteArray());
	}

	/**
	 * Utility method for Base64-decoding a signed assertion.
	 * 
	 * @param encoded
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static SignedAssertion base64decodeAssertion(String encoded)
			throws IOException, ClassNotFoundException
	{
		ObjectInputStream ois =
				new ObjectInputStream(new ByteArrayInputStream(Base64
						.base64ToByteArray(encoded)));

		return (SignedAssertion) ois.readObject();
	}

	/**
	 * Utility method for Base64-decoding signed assertions.
	 * 
	 * @param encoded
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	@SuppressWarnings("unchecked")
	public static ArrayList<SignedAssertion> base64decodeAssertions(
			String encoded) throws IOException, ClassNotFoundException
	{
		ObjectInputStream ois =
				new ObjectInputStream(new ByteArrayInputStream(Base64
						.base64ToByteArray(encoded)));

		return (ArrayList<SignedAssertion>) ois.readObject();
	}
}
