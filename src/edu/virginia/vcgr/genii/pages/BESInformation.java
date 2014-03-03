package edu.virginia.vcgr.genii.pages;

import java.io.IOException;
import java.io.PrintStream;

import org.morgan.dpage.InjectParameter;

import edu.virginia.vcgr.genii.container.bes.BES;
import edu.virginia.vcgr.genii.container.bes.activity.BESActivity;
import edu.virginia.vcgr.genii.container.dynpages.templates.GenesisIIStyledPage;

public class BESInformation extends GenesisIIStyledPage
{
	static final private String PAGE_TITLE = "BES Information";

	@InjectParameter("besID")
	private String _besID;

	private void generateAllBESPage(PrintStream ps) throws IOException
	{
		ps.println("<H2>BES Containers Available</H2>");
		ps.println("<UL>");
		for (String besID : BES.listBESs()) {
			ps.format("<LI><A HREF=\"bes-info.html?besID=%s\">BES %s</A></LI>", besID, besID);
		}
		ps.println("</UL>");
	}

	private int getFaultCount(BESActivity activity)
	{
		try {
			return activity.getFaults().size();
		} catch (Throwable cause) {
			return -1;
		}
	}

	private void generateBESPage(PrintStream ps, String besID) throws IOException
	{
		ps.format("<H2>Information for BES Container %s</H2>", besID);

		BES bes = BES.getBES(besID);
		if (bes == null) {
			ps.println("<BOLD>None Available</BOLD>");
			return;
		}

		ps.println("<TABLE border=\"1\">");
		ps.println("<CAPTION>BES Activities</CAPTION>");
		ps.println("<TR bgcolor=\"#7FFFD4\"><TH>Activity Name</TH><TH>Activity CWD</TH><TH>State</TH><TH>Num. Faults</TH></TR>");
		for (BESActivity activity : bes.getContainedActivities()) {
			ps.format("<TR><TD>%s</TD><TD>%s</TD><TD>%s</TD><TD>%s</TD></TR>", activity.getJobName(),
				activity.getActivityCWD(), activity.getState(), getFaultCount(activity));
		}
		ps.println("</TABLE>");
	}

	@Override
	protected void generateContent(PrintStream ps) throws IOException
	{
		if (_besID == null)
			generateAllBESPage(ps);
		else
			generateBESPage(ps, _besID);
	}

	public BESInformation()
	{
		super("images/grid_logo_medium.jpg", PAGE_TITLE);
	}
}
