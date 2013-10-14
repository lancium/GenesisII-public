package edu.virginia.vcgr.genii.ui.plugins.queue.history;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JFrame;

import edu.virginia.vcgr.genii.client.history.HistoryEvent;
import edu.virginia.vcgr.genii.client.history.HistoryEventSource;
import edu.virginia.vcgr.genii.client.history.SimpleStringHistoryEventSource;
import edu.virginia.vcgr.genii.client.naming.WSName;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.ui.UIContext;

public class JobHistoryFrame extends JFrame
{
	static final long serialVersionUID = 0L;

	public JobHistoryFrame(UIContext context, RNSPath queue, String ticketNumber, Collection<HistoryEvent> events)
	{
		super(String.format("History for Job %s on Queue %s", ticketNumber, queue));

		try {
			WSName queueName = new WSName(queue.getEndpoint());
			if (queueName.isValidWSName()) {
				Collection<HistoryEvent> tempEvents = new ArrayList<HistoryEvent>(events.size());
				for (HistoryEvent event : events) {
					HistoryEventSource source = event.eventSource();
					WSName id = (WSName) source.identity();
					if (id != null && (id.equals(queueName)))
						source = new SimpleStringHistoryEventSource(queue.toString(), null, source);

					tempEvents.add(new HistoryEvent(event.eventNumber(), event.eventTimestamp(), source, event.eventLevel(),
						event.eventCategory(), event.eventProperties(), event.eventData()));
				}

				events = tempEvents;
			}
		} catch (Throwable cause) {
			// Can't rename the queue -- dont' worry about it.
		}

		Container content = getContentPane();
		content.setLayout(new GridBagLayout());

		content.add(new HistoryTreePanel(context, queue, ticketNumber, events), new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 5, 5));

		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	}
}