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
package edu.virginia.vcgr.genii.client.rns;

import org.oasis_open.wsrf.basefaults.BaseFaultType;
import org.oasis_open.wsrf.basefaults.BaseFaultTypeDescription;

public class RNSException extends Exception
{
	static final long serialVersionUID = 0;
	
	private RNSException(Throwable cause, boolean isUnwrapped)
	{
		super(cause.toString(), cause);
	}
	
	public RNSException(String msg)
	{
		super(msg);
	}
	
	public RNSException(String msg, Throwable cause)
	{
		super(msg, unwrap(cause));
	}
	
	public RNSException(Throwable cause)
	{
		this(unwrap(cause), true);
	}
	
	public RNSException(BaseFaultType bft)
	{
		super(convertDescription(bft.getDescription()), bft);
	}
	
	static private String convertDescription(
		BaseFaultTypeDescription []desc)
	{
		StringBuilder builder = new StringBuilder();
		
		for (BaseFaultTypeDescription d : desc)
		{
			builder.append(d.get_value() + "\n");
		}
		
		return builder.toString();
	}
	
	static private Throwable unwrap(Throwable t)
	{
		Throwable tmp;
		
		while (true)
		{
			tmp = t.getCause();
			if (tmp == null)
				return t;
			t = tmp;
		}
	}
}
