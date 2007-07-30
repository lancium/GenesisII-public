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

package edu.virginia.vcgr.genii.client.security;

import java.util.Calendar;

import org.oasis_open.wsrf.basefaults.BaseFaultType;
import org.oasis_open.wsrf.basefaults.BaseFaultTypeDescription;

public class GenesisIISecurityException extends BaseFaultType
{
	static final long serialVersionUID = 0;

	protected Throwable _myCause = null; 
	
	public GenesisIISecurityException(String msg)
	{
		super(null, Calendar.getInstance(), null, null,
			new BaseFaultTypeDescription[] {
				new BaseFaultTypeDescription(msg)}, null);
		this.setFaultString(msg);
	}

	public GenesisIISecurityException(String msg, Throwable cause)
	{
		super(null, Calendar.getInstance(), null, null,
			getDescriptions(msg, cause), null);
		
		_myCause = cause;
		this.setFaultString(msg);
	}

	public Throwable getCause() {
		return _myCause;
	}	
	
	public String getMessage() {
		BaseFaultTypeDescription[] descs = getDescription();
		if ((descs != null) && (descs.length > 0)) {
			return getDescription(0).get_value();
		}
		return super.getMessage();
	}
	
	static private BaseFaultTypeDescription[] getDescriptions(
		String msg, Throwable cause)
	{
		if (msg == null)
			msg = cause.toString();
		
		return new BaseFaultTypeDescription[] {
			new BaseFaultTypeDescription(msg),
			new BaseFaultTypeDescription("Caused by:"),
			new BaseFaultTypeDescription(cause.toString())
		};
	}
}