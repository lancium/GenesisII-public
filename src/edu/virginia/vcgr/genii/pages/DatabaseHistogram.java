package edu.virginia.vcgr.genii.pages;

import java.awt.image.RenderedImage;
import java.io.IOException;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.morgan.dpage.DynamicPage;
import org.morgan.dpage.ImageSourceDynamicPage;
import org.morgan.util.Pair;

public class DatabaseHistogram extends ImageSourceDynamicPage implements DynamicPage
{
	static final int WIDTH = 1500;
	static final int HEIGHT = 400;

	@Override
	protected RenderedImage createImage() throws IOException
	{
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();

		for (Pair<String, Integer> dataPoint : edu.virginia.vcgr.genii.client.stats.ContainerStatistics.instance()
			.getDatabaseHistogramStatistics().histogram()) {
			dataset.setValue(dataPoint.second(), "Max Connections Open", dataPoint.first());
		}

		JFreeChart chart = ChartFactory.createBarChart3D("Container DB Connection Statistics", "Time Interval",
			"Max Connections Open", dataset, PlotOrientation.VERTICAL, true, true, true);
		return chart.createBufferedImage(width(), height());
	}
}