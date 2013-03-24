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
package edu.virginia.vcgr.genii.client.ser;

import org.apache.axis.MessageContext;
import org.apache.axis.client.AxisClient;

public class Config
{
	static private AxisClient _client = null;

	static public AxisClient getClientEngine()
	{
		if (_client == null) {
			_client = new AxisClient();
		}

		return _client;
	}

	static private MessageContext _cached = null;

	static public MessageContext mooch()
	{
		synchronized (Config.class) {
			if (_cached == null) {
				_cached = new MessageContext(getClientEngine());
				_cached.setEncodingStyle("");
				_cached.setProperty(AxisClient.PROP_DOMULTIREFS, Boolean.FALSE);
			}
		}

		return _cached;
	}
}
