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

public class MethodHistogram extends ImageSourceDynamicPage implements DynamicPage
{
	static final int WIDTH = 1500;
	static final int HEIGHT = 400;

	@Override
	protected RenderedImage createImage() throws IOException
	{
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();

		for (Pair<String, Integer> dataPoint : edu.virginia.vcgr.genii.client.stats.ContainerStatistics.instance()
			.getMethodHistogramStatistics().histogram()) {
			dataset.setValue(dataPoint.second(), "Max Methods Active", dataPoint.first());
		}

		JFreeChart chart = ChartFactory.createBarChart3D("Container Method Statistics", "Time Interval", "Max Methods Active",
			dataset, PlotOrientation.VERTICAL, true, true, true);
		return chart.createBufferedImage(width(), height());
	}
}