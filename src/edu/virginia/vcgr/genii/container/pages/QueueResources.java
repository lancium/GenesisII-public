package edu.virginia.vcgr.genii.container.pages;

import java.awt.image.RenderedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import edu.virginia.vcgr.genii.container.dynpages.DynamicPage;
import edu.virginia.vcgr.genii.container.dynpages.ImageSourceDynamicPage;
import edu.virginia.vcgr.genii.container.dynpages.InjectParameter;
import edu.virginia.vcgr.genii.container.q2.QueueManager;
import edu.virginia.vcgr.genii.container.q2.summary.HostDescription;
import edu.virginia.vcgr.genii.container.q2.summary.ResourceSummary;
import edu.virginia.vcgr.genii.container.q2.summary.SlotSummary;

public class QueueResources extends ImageSourceDynamicPage
	implements DynamicPage
{
	static final int WIDTH = 600;
	static final int HEIGHT = 400;
	
	@InjectParameter("queueID")
	private String _queueID;
	
	@Override
	protected RenderedImage createImage() throws IOException
	{
		try
		{
			QueueManager queue = QueueManager.getManager(_queueID);
			if (queue == null)
				throw new FileNotFoundException("Couldn't find target Queue.");
			
			
			ResourceSummary summary = queue.summarize();
			DefaultCategoryDataset dataset = new DefaultCategoryDataset();
			
			for (HostDescription description : summary.hostDescriptions())
			{
				SlotSummary slotSummary = summary.get(description);
				
				dataset.setValue(slotSummary.slotsUsed(), "In Use", description);
				dataset.setValue(slotSummary.slotsAvailable(), "Free", description);
			}
			
			JFreeChart chart = ChartFactory.createStackedBarChart3D(
				"Current Load", "Machine Type", "Slots", dataset,
				PlotOrientation.VERTICAL, true, true, true);
			return chart.createBufferedImage(WIDTH, HEIGHT);
		}
		catch (SQLException sqe)
		{
			throw new IOException("Unable to genreate graph.", sqe);
		}
	}
}