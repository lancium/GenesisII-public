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
package edu.virginia.vcgr.genii.container.bes.resource;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Properties;

import org.ggf.bes.factory.UnknownActivityIdentifierFaultType;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.container.bes.BES;
import edu.virginia.vcgr.genii.container.bes.BESPolicy;
import edu.virginia.vcgr.genii.container.bes.activity.BESActivity;
import edu.virginia.vcgr.genii.container.resource.IResource;

public interface IBESResource extends IResource
{
	static public final String STORED_ACCEPTING_NEW_ACTIVITIES = 
		"edu.virginia.bes.resource.stored-accepting-new-activities";
	static public final String THRESHOLD_DB_PROPERTY_NAME =
		"edu.virginia.bes.resource.threshold";
	
	public BESPolicy getPolicy() throws RemoteException;
	public void setPolicy(BESPolicy policy) throws RemoteException;
	
	public BES getBES() throws RemoteException;
	
	public Collection<BESActivity> getContainedActivities() 
		throws RemoteException;
	
	public BESActivity getActivity(String activityid)
		throws RemoteException, UnknownActivityIdentifierFaultType;
	public BESActivity getActivity(EndpointReferenceType activity)
		throws RemoteException, UnknownActivityIdentifierFaultType;
	
	public boolean isAcceptingNewActivities()
		throws RemoteException;
	
	public void nativeQProperties(Properties props) throws RemoteException;
	public Properties nativeQProperties() throws RemoteException;
}