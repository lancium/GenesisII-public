package edu.virginia.vcgr.genii.container.pages;

import java.io.IOException;
import java.io.PrintStream;
import java.sql.SQLException;

import org.ggf.rns.EntryType;

import edu.virginia.vcgr.genii.client.queue.QueueStates;
import edu.virginia.vcgr.genii.container.dynpages.InjectParameter;
import edu.virginia.vcgr.genii.container.dynpages.SimpleTitledHtmlPage;
import edu.virginia.vcgr.genii.container.q2.QueueManager;
import edu.virginia.vcgr.genii.queue.ReducedJobInformationType;

public class QueueInformation extends SimpleTitledHtmlPage
{
	static final private String PAGE_TITLE = "Grid Queue Information";
	
	@InjectParameter("queueID")
	private String _queueID;
	
	private void generateAllQueuesPage(PrintStream ps) throws IOException
	{
		ps.println("<H2>Grid Queues Available</H2>");
		ps.println("<UL>");
		for (String queueID : QueueManager.listAllQueues())
		{
			ps.format("<LI><A HREF=\"queue-info.html?queueID=%s\">Grid Queue %s</A></LI>",
				queueID, queueID);
		}
		ps.println("</UL>");
	}
	
	private void generateQueuePage(PrintStream ps, String queueID)
		throws IOException
	{
		ps.format("<H2>Information for Grid Queue %s</H2>", queueID);
		
		try
		{
			QueueManager queue = QueueManager.getManager(queueID);
			if (queue == null)
			{
				ps.println("<BOLD>None Available</BOLD>");
				return;
			} else
			{
				ps.println("<TABLE border=\"1\">");
				ps.println("<CAPTION>Container BES Resources</CAPTION>");
				ps.println("<TR bgcolor=\"#7FFD4\"><TH>BES Name</TH><TH>Slots Configured</TH></TR>");
				for (EntryType entry : queue.listBESs(null))
				{
					ps.format("<TR><TD>%s</TD><TD>%d</TD></TR>",
						entry.getEntry_name(), 
						queue.getBESConfiguration(entry.getEntry_name()));
				}
				ps.println("</TABLE>");
				
				ps.println("<BR>");
				
				ps.println("<TABLE border=\"1\">");
				ps.println("<CAPTION>Contained Jobs</CAPTION>");
				ps.println("<TR bgcolor=\"#7FFD4\"><TH>Job Ticket</TH><TH>Job State</TH></TR>");
				for (ReducedJobInformationType rji : queue.listJobs(null))
				{
					String ticket = rji.getJobTicket();
					QueueStates state = QueueStates.fromQueueStateType(rji.getJobStatus());
					
					ps.format("<TR><TD>%s</TD><TD>%s</TD></TR>",
						ticket, state);
				}
				ps.println("</TABLE>");
			}
		}
		catch (SQLException sqe)
		{
			ps.println("<BOLD>Unable to find queue</BOLD>");
			return;
		}
	}
	
	@Override
	protected void generateBody(PrintStream ps) throws IOException
	{
		if (_queueID == null)
			generateAllQueuesPage(ps);
		else
			generateQueuePage(ps, _queueID);
	}
	
	public QueueInformation()
	{
		super(PAGE_TITLE);
	}
}