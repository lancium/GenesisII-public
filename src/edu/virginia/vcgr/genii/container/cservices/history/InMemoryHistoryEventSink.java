package edu.virginia.vcgr.genii.container.cservices.history;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.history.HistoryEvent;
import edu.virginia.vcgr.genii.client.history.HistoryEventCategory;
import edu.virginia.vcgr.genii.client.history.HistoryEventData;
import edu.virginia.vcgr.genii.client.history.HistoryEventLevel;
import edu.virginia.vcgr.genii.client.history.HistoryEventSource;
import edu.virginia.vcgr.genii.client.history.SequenceNumber;
import edu.virginia.vcgr.genii.client.history.SimpleStringHistoryEventSource;
import edu.virginia.vcgr.genii.client.ser.DBSerializer;
import edu.virginia.vcgr.genii.common.HistoryEventBundleType;
import edu.virginia.vcgr.genii.container.cservices.ContainerServices;

public class InMemoryHistoryEventSink
{
	static private Log _logger = LogFactory.getLog(InMemoryHistoryEventSink.class);

	static final private String SERIALIZATION_NS = GenesisIIConstants.GENESISII_NS;
	static final private String SERIALIZATION_NAME = "history-event";
	static final private QName SERIALIZATION_QNAME = new QName(SERIALIZATION_NS, SERIALIZATION_NAME);

	static private class InMemoryHistoryEventToken implements HistoryEventToken
	{
		static final long serialVersionUID = 0L;

		private SequenceNumber _token;

		private InMemoryHistoryEventToken(SequenceNumber token)
		{
			_token = token;
		}

		@Override
		final public SequenceNumber retrieve() throws SQLException
		{
			return _token;
		}
	}

	static private List<HistoryEvent> extractHistoryEvents(SequenceNumber parentNumber, String stringName,
		HistoryEventSource knownTo, MessageElement[] any)
	{
		List<HistoryEvent> ret = new ArrayList<HistoryEvent>(any == null ? 0 : any.length);

		if (any != null) {
			for (MessageElement e : any) {
				QName name = e.getQName();
				if (name.equals(SERIALIZATION_QNAME)) {
					try {
						HistoryEventBundleType bundle = (HistoryEventBundleType) e.getObjectValue(HistoryEventBundleType.class);
						HistoryEvent event = (HistoryEvent) DBSerializer.deserialize(bundle.getData());
						ret.add(new HistoryEvent(event.eventNumber().wrapWith(parentNumber), event.eventTimestamp(),
							new SimpleStringHistoryEventSource(stringName, knownTo, event.eventSource()), event.eventLevel(),
							event.eventCategory(), event.eventProperties(), event.eventData()));
					} catch (Throwable cause) {
						_logger.warn("Error trying to retrieve sub events.", cause);
					}
				}
			}
		}

		return ret;
	}

	static public HistoryEventToken wrapEvents(SequenceNumber parentNumber, String responsibleName, HistoryEventSource knownTo,
		String resourceKey, MessageElement[] elements)
	{
		List<HistoryEvent> besEvents = extractHistoryEvents(parentNumber, responsibleName, knownTo, elements);

		HistoryContainerService service = ContainerServices.findService(HistoryContainerService.class);

		for (HistoryEvent event : besEvents) {
			service.addRecord(resourceKey, event.eventNumber(), event.eventCategory(), event.eventLevel(),
				event.eventProperties(), event.eventSource(), event.eventData(), null);

			if (event.eventNumber().compareTo(parentNumber) > 0)
				parentNumber = event.eventNumber();
		}

		return new VerbatimHistoryEventToken(parentNumber);
	}

	private SequenceNumber _nextNumber = new SequenceNumber();
	private List<HistoryEvent> _events = new LinkedList<HistoryEvent>();

	final public List<HistoryEvent> events()
	{
		return new ArrayList<HistoryEvent>(_events);
	}

	final public MessageElement[] eventMessages()
	{
		try {
			List<HistoryEvent> events = events();
			if (events == null || events.size() == 0)
				return null;

			MessageElement[] ret = new MessageElement[events.size()];
			int lcv = 0;
			for (HistoryEvent event : events) {
				ret[lcv] = new MessageElement(SERIALIZATION_QNAME, new HistoryEventBundleType(
					DBSerializer.serialize(event, -1L)));
				lcv++;
			}

			return ret;
		} catch (Throwable cause) {
			_logger.warn("Unable to create message elements for history events.", cause);
			return null;
		}
	}

	final HistoryEventToken add(HistoryEventCategory category, HistoryEventLevel level, Map<String, String> properties,
		HistoryEventSource eventSource, HistoryEventData eventData)
	{
		synchronized (_events) {
			_events.add(new HistoryEvent(_nextNumber, Calendar.getInstance(), eventSource, level, category, properties,
				eventData));
			HistoryEventToken token = new InMemoryHistoryEventToken(_nextNumber);
			_nextNumber = _nextNumber.next();
			return token;
		}
	}
}