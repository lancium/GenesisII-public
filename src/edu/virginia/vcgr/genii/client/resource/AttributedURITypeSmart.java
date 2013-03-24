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
package edu.virginia.vcgr.genii.client.resource;

import java.net.UnknownHostException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ws.addressing.AttributedURIType;

import edu.virginia.vcgr.genii.client.configuration.Hostname;

public class AttributedURITypeSmart extends AttributedURIType
{
	static private Log _logger = LogFactory.getLog(AttributedURITypeSmart.class);

	static final long serialVersionUID = 0;

	public AttributedURITypeSmart(String url)
	{
		super(smartifyURL(url));
	}

	static private String smartifyURL(String url)
	{
		try {
			return Hostname.normalizeURL(url);
		} catch (UnknownHostException uhe) {
			_logger.warn(uhe);
			return url;
		}
	}
}
