package edu.virginia.vcgr.genii.pages;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.SQLException;
import java.util.Map;

import org.morgan.dpage.InjectParameter;

import edu.virginia.vcgr.genii.container.dynpages.templates.GenesisIIStyledPage;
import edu.virginia.vcgr.genii.container.q2.QueueManager;

public class QueueInformation extends GenesisIIStyledPage
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
	
	private void generateResourcesAvailable(PrintStream ps, QueueManager queue)
		throws IOException
	{
		ps.format("<H2>Total Resources Available:  %d</H2><BR/>", queue.totalSlots());
		ps.format("<H2>Total Jobs Finished Since Inception:  %d</H2><BR/>",
			queue.totalFinishedAllTime());
		ps.println("<TABLE border=\"0\" cellpadding=\"50\">");
		ps.println("<TR>");
		ps.println("<TD>");
		ps.println("<UL>");
		Map<String, Long> jobMap = queue.summarizeJobs();
		for (String category : jobMap.keySet())
		{
			if (category.equals("Error"))
				ps.format("<LI>%d Jobs Currently in Error</LI>", jobMap.get(category));
			else if (category.equals("Finished"))
				ps.format("<LI>%d Jobs Finished but Not Reaped</LI>", jobMap.get(category));
			else
				ps.format("<LI>%d Jobs Currently %s</LI>", jobMap.get(category), category);
		}
		ps.println("</UL>");
		ps.println("</TD>");
		ps.println("<TD>");
		ps.format("<IMG SRC=\"queue-resources.png?queueID=%s\" " +
			"ALT=\"*\" width\"%d\" height=\"%d\"/>",
			_queueID, QueueResources.WIDTH, QueueResources.HEIGHT);
		ps.println("</TD>");
		ps.println("</TR>");
		ps.println("</TABLE>");
	}
	
	private void generateQueuePage(PrintStream ps, String queueID)
		throws IOException
	{
		try
		{
			QueueManager queue = QueueManager.getManager(queueID);
			if (queue == null)
				throw new FileNotFoundException("Couldn't find target Queue.");
			else
			{
				generateResourcesAvailable(ps, queue);
			}
		}
		catch (SQLException sqe)
		{
			ps.println("<BOLD>Unable to find queue</BOLD>");
			return;
		}
	}
	
	@Override
	protected void generateContent(PrintStream ps) throws IOException
	{
		if (_queueID == null)
			generateAllQueuesPage(ps);
		else
			generateQueuePage(ps, _queueID);
	}
	
	public QueueInformation()
	{
		super("images/grid_logo_medium.jpg", PAGE_TITLE);
	}
}