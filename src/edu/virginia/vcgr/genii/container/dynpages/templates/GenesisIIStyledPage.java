package edu.virginia.vcgr.genii.container.dynpages.templates;

import java.awt.Color;
import java.io.IOException;
import java.io.PrintStream;

import org.morgan.dpage.DynamicPage;

public abstract class GenesisIIStyledPage implements DynamicPage
{
	static protected String toHTMLColor(Color color)
	{
		return String.format("rgb(%d, %d, %d)", color.getRed(), color.getGreen(), color.getBlue());
	}

	private String _logoLocation;
	private String _title;

	protected void generateHeader(PrintStream ps) throws IOException
	{
		ps.println("<P>");
		ps.format("<IMG SRC=\"%s\" ALT=\"*\" align=\"left\" " + "width=\"175\" height=\"178\"/>\n", _logoLocation);
		ps.format("<BR/><H1 style=\"margin-left:250px\">%s</H1>\n", _title);
		ps.println("</P><P style=\"clear:left\"><BR/>");
	}

	protected abstract void generateContent(PrintStream ps) throws IOException;

	protected void generateFooter(PrintStream ps) throws IOException
	{
		ps.println("</P>");
	}

	protected GenesisIIStyledPage(String logoLocation, String title)
	{
		_logoLocation = logoLocation;
		_title = title;
	}

	@Override
	public void generatePage(PrintStream ps) throws IOException
	{
		ps.format("<HTML><TITLE>%s</TITLE><BODY>\n", _title);
		generateHeader(ps);
		generateContent(ps);
		generateFooter(ps);
		ps.println("</BODY></HTML>");
	}
}