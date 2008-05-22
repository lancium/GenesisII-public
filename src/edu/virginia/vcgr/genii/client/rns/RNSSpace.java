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

import java.rmi.RemoteException;

import org.ggf.rns.RNSPortType;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.naming.EPRUtils;

public class RNSSpace
{
	static public RNSPath createNewSpace(String serviceURL)
		throws RemoteException
	{
		return RNSSpace.createNewSpace(EPRUtils.makeEPR(serviceURL));
	}
	
	static public RNSPath createNewSpace(EndpointReferenceType serviceEPR)
		throws RemoteException
	{
		RNSPortType factory = ClientUtils.createProxy(RNSPortType.class, serviceEPR);

		EndpointReferenceType rootEPR = 
			factory.add(null).getEntry_reference();
		return new RNSPath(rootEPR);
	}
}
