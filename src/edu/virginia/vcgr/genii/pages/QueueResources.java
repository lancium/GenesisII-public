package edu.virginia.vcgr.genii.pages;

import java.awt.image.RenderedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.morgan.dpage.DynamicPage;
import org.morgan.dpage.ImageSourceDynamicPage;
import org.morgan.dpage.InjectParameter;

import edu.virginia.vcgr.genii.container.q2.QueueManager;
import edu.virginia.vcgr.genii.container.q2.summary.HostDescription;
import edu.virginia.vcgr.genii.container.q2.summary.ResourceSummary;
import edu.virginia.vcgr.genii.container.q2.summary.SlotSummary;

public class QueueResources extends ImageSourceDynamicPage
	implements DynamicPage
{
	static final int WIDTH = 800;
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
			
			List<HostDescription> descriptions = new Vector<HostDescription>(
				summary.hostDescriptions());
			Collections.sort(descriptions);
			for (HostDescription description : descriptions)
			{
				SlotSummary slotSummary = summary.get(description);
				
				dataset.setValue(slotSummary.slotsUsed(), "In Use", description);
				dataset.setValue(slotSummary.slotsAvailable(), "Free", description);
			}
			
			JFreeChart chart = ChartFactory.createStackedBarChart3D(
				"XCG Current Load", "Machine Type", "Job Slots", dataset,
				PlotOrientation.VERTICAL, true, true, true);
			return chart.createBufferedImage(width(), height());
		}
		catch (SQLException sqe)
		{
			throw new IOException("Unable to genreate graph.", sqe);
		}
	}
}