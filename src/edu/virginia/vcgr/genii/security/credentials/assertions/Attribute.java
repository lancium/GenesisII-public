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

import java.io.Externalizable;
import java.security.cert.X509Certificate;
import java.util.Date;

import edu.virginia.vcgr.genii.security.Describable;

/**
 * A serializable statement
 * 
 * @author dmerrill
 */
public interface Attribute extends Externalizable, Describable
{

	/**
	 * Checks that the attribute is time-valid with respect to the supplied date
	 * and any delegation depth requirements are met by the supplied
	 * delegationDepth.
	 */
	public void checkValidity(int delegationDepth, Date date)
			throws AttributeInvalidException;

	/**
	 * Returns the identity of the attribute asserter
	 */
	public X509Certificate[] getAssertingIdentityCertChain();
}
