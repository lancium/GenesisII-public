package edu.virginia.vcgr.genii.container.sync;

import java.util.EmptyStackException;
import java.util.List;
import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.io.StreamUtils;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.naming.ResolverDescription;
import edu.virginia.vcgr.genii.client.naming.ResolverUtils;
import edu.virginia.vcgr.genii.client.naming.WSName;
import edu.virginia.vcgr.genii.client.wsrf.wsn.subscribe.AbstractSubscriptionFactory;
import edu.virginia.vcgr.genii.client.wsrf.wsn.subscribe.DefaultSubscriptionFactory;
import edu.virginia.vcgr.genii.client.wsrf.wsn.subscribe.SubscribeRequest;
import edu.virginia.vcgr.genii.client.wsrf.wsn.subscribe.SubscriptionFactory;
import edu.virginia.vcgr.genii.client.wsrf.wsn.subscribe.policy.PersistentNotificationSubscriptionPolicy;
import edu.virginia.vcgr.genii.client.wsrf.wsn.subscribe.policy.SubscriptionPolicy;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.TopicPath;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.TopicQueryExpression;
import edu.virginia.vcgr.genii.container.common.GenesisIIBase;
import edu.virginia.vcgr.genii.container.context.WorkingContext;
import edu.virginia.vcgr.genii.container.resource.IResource;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.ResourceLock;
import edu.virginia.vcgr.genii.container.resource.ResourceManager;
import edu.virginia.vcgr.genii.container.security.authz.providers.GamlAclTopics;
import edu.virginia.vcgr.genii.resolver.UpdateResponseType;

public class ReplicationThread extends Thread
{
	static private Log _logger = LogFactory.getLog(ReplicationThread.class);
	
	private WorkingContext _context;
	private Stack<ReplicationItem> _workstack;
	
	public ReplicationThread(WorkingContext context)
	{
		this._context = (WorkingContext) context.clone();
		this._workstack = new Stack<ReplicationItem>();
	}

	public void add(ReplicationItem item)
	{
		synchronized(_workstack)
		{
			_workstack.push(item);
		}
	}
	
	public void run()
	{
		_logger.debug("ReplicationThread: entered run()");
		WorkingContext.setCurrentWorkingContext(_context);
		try
		{
			while (true)
			{
				ReplicationItem item = null;
				synchronized(_workstack)
				{
					item = _workstack.pop();
				}
				doSync(item);
			}
		}
		catch (EmptyStackException ese) {}
		StreamUtils.close(_context);
		_logger.debug("ReplicationThread: exiting");
	}
	
	private void doSync(ReplicationItem item)
	{
		EndpointReferenceType myEPR = item.localEPR;
		ResourceSyncRunner runner = item.runner;
		ResourceKey rKey = null;
		try
		{
			if (myEPR == null)
			{
				myEPR = (EndpointReferenceType) WorkingContext.getCurrentWorkingContext().
					getProperty(WorkingContext.EPR_PROPERTY_NAME);
			}
			else
			{
				WorkingContext.temporarilyAssumeNewIdentity(myEPR);
			}
			rKey = ResourceManager.getCurrentResource();
		}
		catch (Exception exception)
		{
			_logger.error("ReplicationThread: error getting resource", exception);
			return;
		}
		ResourceLock resourceLock = rKey.getResourceLock();
		try
		{
			resourceLock.lock();
			IResource resource = rKey.dereference();
			String state = (String) resource.getProperty(SyncProperty.ERROR_STATE_PROP_NAME);
			if (state == null)
			{
				_logger.debug("ReplicationThread: replica is not in error state");
				return;
			}
			byte[] primaryData = (byte[]) resource.getProperty(SyncProperty.PRIMARY_EPR_PROP_NAME);
			if (primaryData == null)
			{
				_logger.debug("ReplicationThread: primaryEPR is undefined");
				return;
			}
			EndpointReferenceType primaryEPR = EPRUtils.fromBytes(primaryData);
			WSName primaryName = new WSName(primaryEPR);
			Integer idValue = (Integer) resource.getProperty(SyncProperty.TARGET_ID_PROP_NAME);
			if (idValue == null)
			{
				List<ResolverDescription> resolverList = ResolverUtils.getResolvers(primaryName);
				if ((resolverList == null) || (resolverList.size() == 0))
				{
					_logger.debug("ReplicationThread: primaryEPR has no resolver element");
					return;
				}
				UpdateResponseType response = VersionedResourceUtils.updateResolver(resolverList, myEPR,
						rKey.getResourceKey());
				// myEPR = response.getNew_EPR();
				idValue = new Integer(response.getTargetID());
				_logger.debug("ReplicationThread: my targetID=" + idValue);
				resource.setProperty(SyncProperty.TARGET_ID_PROP_NAME, idValue);
				resource.commit();
			}
			int myTargetID = idValue;
			
			// For setting up subscriptions, myEPR must refer to a physical resource.
			WSName myName = new WSName(myEPR);
			if (myName.hasValidResolver())
			{
				myName.removeAllResolvers();
				myEPR = myName.getEndpoint();
			}
			if (state.equals("unsubscribed"))
			{
				// Subscribe before downloading version vector, to avoid race condition.
				TopicPath topic = runner.getSyncTopic();
				TopicQueryExpression topicFilter = topic.asConcreteQueryExpression();
				TopicPath secondTopic = GamlAclTopics.GAML_ACL_CHANGE_TOPIC;
				TopicQueryExpression secondFilter = secondTopic.asConcreteQueryExpression();
				SubscriptionPolicy policy = new PersistentNotificationSubscriptionPolicy();
				SubscriptionFactory factory = new DefaultSubscriptionFactory(myEPR);
				EndpointReferenceType[] replicaList = VersionedResourceUtils.getTargetEPRs(primaryName);
				if (replicaList == null)
				{
					_logger.debug("ReplicationThread: failed to get list of replicas");
					return;
				}
				for (int targetID = 0; targetID < myTargetID; targetID++)
				{
					EndpointReferenceType replicaEPR = replicaList[targetID];
					if (replicaEPR == null)
						continue;
					_logger.debug("ReplicationThread: subscribe targetID=" + targetID);
					// Create subscription so that will send to this.
					factory.subscribe(replicaEPR, topicFilter, null, null, policy);
					factory.subscribe(replicaEPR, secondFilter, null, null, policy);
					// Create subscription so this will send to that.
					SubscribeRequest request = AbstractSubscriptionFactory.createRequest(
						replicaEPR, topicFilter, null, null, policy);
					GenesisIIBase.processSubscribeRequest(resource.getKey(), request);
					request = AbstractSubscriptionFactory.createRequest(
							replicaEPR, secondFilter, null, null, policy);
					GenesisIIBase.processSubscribeRequest(resource.getKey(), request);
				}
			}
			resource.setProperty(SyncProperty.ERROR_STATE_PROP_NAME, "error");
			resource.commit();

			// If the physical resource dies before or during the synchronization process,
			// do not try to synchronize from another replica.
			// If our state contains half of resource A and half of resource B,
			// then it may be an illegal state.
			// In the worst case, this resource gets confused and tries to copy itself.
			// Bottom line: primaryEPR should refer to a physical resource, not a logical resource.
			if (primaryName.hasValidResolver())
			{
				primaryName.removeAllResolvers();
				primaryEPR = primaryName.getEndpoint();
			}
			
			// Get the version vector of the primary instance.
			VersionVector remoteVector = VersionedResourceUtils.getVersionVector(primaryEPR);
			VersionVector localVector = new VersionVector();
			localVector.setVersion(myTargetID, 0);
			if (remoteVector != null)
				localVector.copy(remoteVector);
			resource.setProperty(SyncProperty.VERSION_VECTOR_PROP_NAME, localVector);

			// Get the state data from the primary instance.
			// Save it in the local database and filesystem.
			runner.doSync(resource, primaryEPR, myEPR, this);

			// The replica may service read and write requests as soon as we release the lock.
			resource.setProperty(SyncProperty.ERROR_STATE_PROP_NAME, null);
			resource.commit();
		}
		catch (Throwable fault)
		{
			_logger.error("ReplicationThread: failed", fault);
		}
		finally
		{
			resourceLock.unlock();
			try
			{
				if (item.localEPR != null)
				{
					WorkingContext.releaseAssumedIdentity();
				}
			}
			catch (Exception exception)
			{
				_logger.debug(exception);
			}
		}
		// _logger.debug("ReplicationThread: done");
	}
}
