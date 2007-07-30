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

package edu.virginia.vcgr.genii.client.security.gamlauthz.assertions;

import java.io.*;
import java.security.cert.*;
import java.util.Date;

/**
 * Simple GAML attribute that restricts the lifetime of a GAML
 * assertion and how many times it can be delegated
 *  
 * @author dmerrill
 */
public abstract class BasicAttribute implements Attribute {

	static public final long serialVersionUID = 0L;

	protected long _notValidBeforeMillis;
	protected long _durationValidMillis;
	protected long _maxDelegationDepth;
	
	// zero-arg contstructor for externalizable use only!
	public BasicAttribute() {}
	
	public BasicAttribute(long notValidBeforeMillis, long durationValidMillis, long maxDelegationDepth) {
		_durationValidMillis = durationValidMillis;
		_notValidBeforeMillis = notValidBeforeMillis;
		_maxDelegationDepth = maxDelegationDepth;
	}
	
	/**
	 * Checks that the attribute is time-valid with respect to the supplied 
	 * date and any delegation depth requirements are met by the supplied
	 * delegationDepth.
	 */
	public void checkValidity(int delegationDepth, Date date) throws AttributeInvalidException {
		long currentTime = System.currentTimeMillis();
		
		if (currentTime < _notValidBeforeMillis) {
			throw new AttributeNotYetValidException("Security attribute is not yet valid");
		}

		if (currentTime > _notValidBeforeMillis + _durationValidMillis) {
			throw new AttributeExpiredException("Security attribute has expired");
		}
		
		if (delegationDepth > _maxDelegationDepth) {
			throw new AttributeInvalidException("Security attribute exceeds maximum delegation depth");
		}
		
		try {
			for (X509Certificate cert : this.getAssertingIdentityCertChain()) {
				cert.checkValidity();
			}
		} catch (CertificateException e) {
			throw new AttributeInvalidException("Security attribute asserting identity contains an invalid certificate: " + e.getMessage(), e);
		}
	}
	
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeLong(_durationValidMillis);
		out.writeLong(_notValidBeforeMillis);
		out.writeLong(_maxDelegationDepth);
	}

	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		_durationValidMillis = in.readLong();
		_notValidBeforeMillis = in.readLong();
		_maxDelegationDepth = in.readLong();
	}
	
	
}
