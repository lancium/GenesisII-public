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

import java.io.*;
import java.security.cert.*;
import java.util.Date;

/**
 * Simple GII attribute that restricts the lifetime of a GII assertion and how
 * many times it can be delegated
 * 
 * @author dmerrill
 */
public abstract class BasicAttribute implements Attribute
{

	static public final long serialVersionUID = 0L;

	protected AttributeConstraints _constraints;

	// zero-arg contstructor for externalizable use only!
	public BasicAttribute()
	{
	}

	public BasicAttribute(AttributeConstraints constraints)
	{
		_constraints = constraints;
	}

	/**
	 * Checks that the attribute is time-valid with respect to the supplied date
	 * and any delegation depth requirements are met by the supplied
	 * delegationDepth.
	 */
	public void checkValidity(int delegationDepth, Date date)
			throws AttributeInvalidException
	{

		if (_constraints != null)
		{
			_constraints.checkValidity(delegationDepth, date);
		}

		try
		{
			for (X509Certificate cert : this.getAssertingIdentityCertChain())
			{
				cert.checkValidity(date);
			}
		}
		catch (CertificateException e)
		{
			throw new AttributeInvalidException(
					"Security attribute asserting identity contains an invalid certificate: "
							+ e.getMessage(), e);
		}
	}

	public void writeExternal(ObjectOutput out) throws IOException
	{
		out.writeObject(_constraints);
	}

	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException
	{
		_constraints = (AttributeConstraints) in.readObject();
	}

}
