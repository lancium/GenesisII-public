package edu.virginia.vcgr.genii.container.pages;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.PrintStream;

import javax.imageio.ImageIO;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.DefaultKeyedValues;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;

import edu.virginia.vcgr.genii.container.dynpages.DynamicPage;

public class GraphGenerator implements DynamicPage
{
	static private BufferedImage createImage()
	{
		DefaultKeyedValues values = new DefaultKeyedValues();
		values.addValue("Group 1", 33.0);
		values.addValue("Group 2", 60.0);
		values.addValue("Group 3", 7.0);
		PieDataset pieDataset = new DefaultPieDataset(values);
		
		JFreeChart chart = ChartFactory.createPieChart3D("Example Pie Chart",
			pieDataset, true, true, false);
		return chart.createBufferedImage(400, 300);
	}
	
	@Override
	public void generate(PrintStream ps) throws IOException
	{
		BufferedImage image = createImage();
		
		ImageIO.write(image, "PNG", ps);
	}
}