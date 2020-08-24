package edu.virginia.vcgr.genii.container.cservices.percall;

import java.io.Closeable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.bes.factory.DestroyActivitiesResponseType;
import org.ggf.bes.factory.DestroyActivitiesType;
import org.ggf.bes.factory.DestroyActivityResponseType;
import org.morgan.util.io.StreamUtils;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.bes.GeniiBESPortType;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.comm.attachments.GeniiAttachment;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.history.HistoryEvent;
import edu.virginia.vcgr.genii.client.history.HistoryEventSource;
import edu.virginia.vcgr.genii.client.history.SequenceNumber;
import edu.virginia.vcgr.genii.client.history.SimpleStringHistoryEventSource;
import edu.virginia.vcgr.genii.client.iterator.WSIterable;
import edu.virginia.vcgr.genii.client.ser.DBSerializer;
import edu.virginia.vcgr.genii.container.cservices.ContainerServices;
import edu.virginia.vcgr.genii.container.cservices.history.HistoryContainerService;
import edu.virginia.vcgr.genii.container.cservices.history.HistoryEventToken;

import edu.virginia.vcgr.genii.common.GeniiCommon;
import edu.virginia.vcgr.genii.common.HistoryEventBundleType;
import edu.virginia.vcgr.genii.common.IterateHistoryEventsRequestType;
import edu.virginia.vcgr.genii.common.IterateHistoryEventsResponseType;

public class BESActivityDestroyActor implements OutcallActor
{
	static final long serialVersionUID = 0L;

	static private Log _logger = LogFactory.getLog(BESActivityDestroyActor.class);

	private EndpointReferenceType _activityEPR;
	private String _historyKey;
	private HistoryEventToken _historyToken;
	private String _besName;

	public BESActivityDestroyActor(String historyKey, HistoryEventToken historyToken, String besName, EndpointReferenceType activityEPR)
	{
		_historyKey = historyKey;
		_historyToken = historyToken;
		_besName = besName;
		_activityEPR = activityEPR;
	}

	@Override
	public boolean enactOutcall(ICallingContext callingContext, EndpointReferenceType target, GeniiAttachment attachment) throws Throwable
	{
		if (_logger.isDebugEnabled())
			_logger.debug("Persistent Outcall Actor attempting to destroy a bes activity.");

		if (target == null)
		{
			_logger.warn("LAK: calling destroy activity on a null activity, we are doing nothing.");
			return true;
		}
		
		Closeable assumedContextToken = null;

		try {
			assumedContextToken = ContextManager.temporarilyAssumeContext(callingContext);

			GeniiCommon common = ClientUtils.createProxy(GeniiCommon.class, _activityEPR, callingContext);

			GeniiBESPortType bes = ClientUtils.createProxy(GeniiBESPortType.class, target, callingContext);
		
			// First, attempt to get the history log
			if (_historyToken != null && _historyKey != null) {
				WSIterable<HistoryEventBundleType> iter = null;
				SequenceNumber parentNumber;

				try {
					parentNumber = _historyToken.retrieve();
					if (parentNumber != null) {
						HistoryContainerService service = ContainerServices.findService(HistoryContainerService.class);

						IterateHistoryEventsResponseType resp = common.iterateHistoryEvents(new IterateHistoryEventsRequestType());
						if (resp != null) {
							iter = WSIterable.axisIterable(HistoryEventBundleType.class, resp.getResult(), 25);
							for (HistoryEventBundleType bundle : iter) {
								HistoryEvent event = (HistoryEvent) DBSerializer.deserialize(bundle.getData());

								HistoryEventSource source = event.eventSource();
								if (_besName != null)
									source = new SimpleStringHistoryEventSource(String.format("BES Resource %s", _besName), null, source);

								service.addRecord(_historyKey, event.eventNumber().wrapWith(parentNumber), event.eventTimestamp(),
									event.eventCategory(), event.eventLevel(), event.eventProperties(), source, event.eventData(), null);
							}
						}
					}
				} catch (Throwable cause) {
					if (_logger.isDebugEnabled())
						_logger.debug("Error trying to get history events for activity.", cause);
				} finally {
					StreamUtils.close(iter);
				}
			}

			// Now, go ahead and kill it.
			DestroyActivitiesResponseType resp = bes.destroyActivities(new DestroyActivitiesType(new EndpointReferenceType[] { _activityEPR }, null));
			if (resp != null) {
				DestroyActivityResponseType[] resps = resp.getResponse();
				if (resps != null && resps.length == 1) {
					if (resps[0].isDestroyed() || (resps[0].getFault() != null))
						return true;
					_logger.error("Response says that we didn't destroy the activity:  " + resps[0].getFault());
				}
			}
		}
		finally
		{
			StreamUtils.close(assumedContextToken);
		}

		_logger.error("Tried to destroy activity, but didn't get right number of response values back.");
		return false;
	}
}