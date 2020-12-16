/*
 * Copyright 2006 University of Virginia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package edu.virginia.vcgr.genii.container.bes.resource;

import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.bes.factory.UnknownActivityIdentifierFaultType;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.bes.BESConstructionParameters;
import edu.virginia.vcgr.genii.client.bes.envvarexp.EnvironmentExport;
import edu.virginia.vcgr.genii.client.common.ConstructionParameters;
import edu.virginia.vcgr.genii.client.common.GenesisHashMap;
import edu.virginia.vcgr.genii.client.resource.AddressingParameters;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.common.MatchingParameter;
import edu.virginia.vcgr.genii.container.bes.BES;
import edu.virginia.vcgr.genii.container.bes.GeniiBESServiceImpl;
import edu.virginia.vcgr.genii.container.bes.activity.BESActivity;
import edu.virginia.vcgr.genii.container.db.ServerDatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.db.BasicDBResource;

public class DBBESResource extends BasicDBResource implements IBESResource
{
	static private Log _logger = LogFactory.getLog(DBBESResource.class);

	@Override
	public void initialize(GenesisHashMap constructionParams) throws ResourceException
	{
		super.initialize(constructionParams);

		ConstructionParameters cParams =
			(ConstructionParameters) constructionParams.get(ConstructionParameters.CONSTRUCTION_PARAMETERS_QNAME);

		try {
			if (!isServiceResource())
				BES.createBES(_resourceKey, cParams);
		} catch (SQLException sqe) {
			throw new ResourceException("Unable to create resource -- database error.", sqe);
		}
	}

	@Override
	public void destroy() throws ResourceException
	{
		try {
			BES.deleteBES(getConnection(), _resourceKey);
		} catch (SQLException sqe) {
			throw new ResourceException("Unable to delete resource.", sqe);
		}

		super.destroy();
	}

	public DBBESResource(ResourceKey parentKey, ServerDatabaseConnectionPool connectionPool) throws SQLException
	{
		super(parentKey, connectionPool);
	}

	public BES getBES() throws RemoteException
	{
		BES bes = null;
		try {
			if (_logger.isDebugEnabled())
				_logger.debug("Grabbing BES with resource key: " + _resourceKey);
			bes = BES.getBES(_resourceKey);
		} catch (IllegalStateException e) {
			return null;
		}
		if (bes == null) {
			// this is a bit different than the bes instances not being ready. somehow we got a null result with no exception.
			throw new RemoteException("Couldn't find active BES entity.");
		}
		return bes;
	}

	@Override
	public Collection<BESActivity> getContainedActivities() throws RemoteException
	{
		BES bes = null;
		String msg = "failed to locate BES";
		try {
			bes = BES.getBES(_resourceKey);
		} catch (IllegalStateException e) {
			msg = "caught illegal state exception trying to get BES information when looking up contained activities";
			_logger.error(msg);
		}
		if (bes == null)
			throw new RemoteException(msg);
		return bes.getContainedActivities();
	}

	@Override
	public BESActivity getActivity(String activityid) throws RemoteException, UnknownActivityIdentifierFaultType
	{
		BES bes = null;
		try {
			bes = BES.getBES(_resourceKey);
		} catch (IllegalStateException e) {
			_logger.error("caught illegal state exception trying to get BES information while looking up activity " + activityid);
		}
		BESActivity activity = bes.findActivity(activityid);
		if (activity == null)
			throw new UnknownActivityIdentifierFaultType("Unknown activity \"" + activityid + "\".", null);
		return activity;
	}

	@Override
	public BESActivity getActivity(EndpointReferenceType activity) throws RemoteException, UnknownActivityIdentifierFaultType
	{
		AddressingParameters ap = new AddressingParameters(activity.getReferenceParameters());
		String id = ap.getResourceKey();
		return getActivity(id);
	}

	public boolean isAcceptingNewActivities() throws RemoteException
	{
		Boolean storedAccepting = (Boolean) getProperty(IBESResource.STORED_ACCEPTING_NEW_ACTIVITIES);
		if (storedAccepting != null && !storedAccepting.booleanValue())
			return false;

		Integer threshold = (Integer) getProperty(IBESResource.THRESHOLD_DB_PROPERTY_NAME);

		try {
			BES bes = getBES();
			if (bes != null)
				return bes.isAcceptingActivites(threshold);
			else
				return false;
		} catch (IllegalStateException e) {
			// squash the exception; the BES instances are not ready.
			return false;
		}
	}

	@Override
	public Collection<MatchingParameter> getMatchingParameters() throws ResourceException
	{
		Collection<MatchingParameter> ret = super.getMatchingParameters();

		EnvironmentExport exp = EnvironmentExport.besExport((BESConstructionParameters) (constructionParameters(GeniiBESServiceImpl.class)));
		for (String key : exp.keySet())
			ret.add(new MatchingParameter("supports:environment-variable", key));

		return ret;
	}
}