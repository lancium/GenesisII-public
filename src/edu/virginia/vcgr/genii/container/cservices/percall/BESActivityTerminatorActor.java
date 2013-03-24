package edu.virginia.vcgr.genii.container.cservices.percall;

import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.StreamCorruptedException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.bes.factory.TerminateActivitiesResponseType;
import org.ggf.bes.factory.TerminateActivitiesType;
import org.ggf.bes.factory.TerminateActivityResponseType;
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
import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.ser.DBSerializer;
import edu.virginia.vcgr.genii.common.GeniiCommon;
import edu.virginia.vcgr.genii.common.HistoryEventBundleType;
import edu.virginia.vcgr.genii.common.IterateHistoryEventsRequestType;
import edu.virginia.vcgr.genii.common.IterateHistoryEventsResponseType;
import edu.virginia.vcgr.genii.container.cservices.ContainerServices;
import edu.virginia.vcgr.genii.container.cservices.history.HistoryContainerService;
import edu.virginia.vcgr.genii.container.cservices.history.HistoryEventToken;

public class BESActivityTerminatorActor implements OutcallActor
{
	static final long serialVersionUID = 0L;

	static private Log _logger = LogFactory.getLog(BESActivityTerminatorActor.class);

	private HistoryEventToken _historyToken;
	private String _historyKey;
	private String _besName;
	private EndpointReferenceType _activityEPR;

	public BESActivityTerminatorActor(String historyKey, HistoryEventToken historyToken, String besName,
		EndpointReferenceType activityEPR)
	{
		_historyKey = historyKey;
		_historyToken = historyToken;
		_activityEPR = activityEPR;
		_besName = besName;
	}

	@Override
	public boolean enactOutcall(ICallingContext callingContext, EndpointReferenceType target, GeniiAttachment attachment)
		throws Throwable
	{
		if (_logger.isDebugEnabled())
			_logger.debug("Persistent Outcall Actor attempting to kill a bes activity.");

		Closeable token = null;

		try {
			token = ContextManager.temporarilyAssumeContext(callingContext);

			GeniiCommon common = ClientUtils.createProxy(GeniiCommon.class, _activityEPR, callingContext);

			GeniiBESPortType bes = ClientUtils.createProxy(GeniiBESPortType.class, target, callingContext);

			// First, attmept to get the history log
			if (_historyToken != null && _historyKey != null) {
				WSIterable<HistoryEventBundleType> iter = null;
				SequenceNumber parentNumber;

				try {
					parentNumber = _historyToken.retrieve();
					if (parentNumber != null) {
						HistoryContainerService service = ContainerServices.findService(HistoryContainerService.class);

						IterateHistoryEventsResponseType resp = common
							.iterateHistoryEvents(new IterateHistoryEventsRequestType());
						if (resp != null) {
							iter = WSIterable.axisIterable(HistoryEventBundleType.class, resp.getResult(), 25);
							for (HistoryEventBundleType bundle : iter) {
								HistoryEvent event = (HistoryEvent) DBSerializer.deserialize(bundle.getData());

								HistoryEventSource source = event.eventSource();
								if (_besName != null)
									source = new SimpleStringHistoryEventSource(String.format("BES Resource %s", _besName),
										null, source);

								service.addRecord(_historyKey, event.eventNumber().wrapWith(parentNumber),
									event.eventTimestamp(), event.eventCategory(), event.eventLevel(), event.eventProperties(),
									source, event.eventData(), null);
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
			ClientUtils.setTimeout(bes, 8 * 1000);
			TerminateActivitiesResponseType resp = bes.terminateActivities(new TerminateActivitiesType(
				new EndpointReferenceType[] { _activityEPR }, null));
			if (resp != null) {
				TerminateActivityResponseType[] resps = resp.getResponse();
				if (resps != null && resps.length == 1) {
					if (resps[0].isTerminated() || (resps[0].getFault() != null))
						return true;
					_logger.warn("Response says that we didn't terminate the activity:  " + resps[0].getFault());
				}
			}

			_logger.warn("Tried to kill activity, but didn't get right number of response values back.");
			return false;
		} finally {
			StreamUtils.close(token);
		}
	}

	private void writeObject(ObjectOutputStream out) throws IOException
	{
		EPRUtils.serializeEPR(out, _activityEPR);
		out.writeObject(_historyToken);
		out.writeObject(_besName);
		out.writeObject(_historyKey);
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		_activityEPR = EPRUtils.deserializeEPR(in);
		_historyToken = (HistoryEventToken) in.readObject();
		_besName = (String) in.readObject();
		_historyKey = (String) in.readObject();
	}

	@SuppressWarnings("unused")
	private void readObjectNoData() throws ObjectStreamException
	{
		throw new StreamCorruptedException();
	}
}