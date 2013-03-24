package edu.virginia.vcgr.genii.pages;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Map;
import java.util.Set;

import edu.virginia.vcgr.genii.client.stats.DatabaseStatistics;
import edu.virginia.vcgr.genii.client.stats.DatabaseStatisticsReport;
import edu.virginia.vcgr.genii.client.stats.MethodStatistics;
import edu.virginia.vcgr.genii.client.stats.MethodStatisticsReport;
import edu.virginia.vcgr.genii.client.stats.MethodStatisticsReportPoint;
import edu.virginia.vcgr.genii.client.stats.TimeInterval;
import edu.virginia.vcgr.genii.container.dynpages.templates.GenesisIIStyledPage;

public class ContainerStatistics extends GenesisIIStyledPage
{
	static final private String LOGO_LOCATION = "images/grid_logo_medium.jpg";
	static final private String PAGE_TITLE = "Genesis II Container Statistics";

	static private void printClassTable(PrintStream ps, Map<TimeInterval, MethodStatisticsReport> map, String className)
	{
		String classShortName = className;
		int index = classShortName.lastIndexOf('.');
		classShortName = classShortName.substring(index + 1);
		ps.format("<H3>Method Statistics for Class %s</H3>",
			String.format("<A HREF=\"class-method-stats.html?className=%s\">%s</A>", className, classShortName));
		ps.println("<TABLE border=\"1\">");
		ps.println("<TR bgcolor=\"#7FFFD4\"><TH>Interval</TH><TH>Calls Started</TH><TH>Calls Completed</TH><TH>Calls Succeeded</TH><TH>Calls Failed</TH><TH>Failure Rate</TH><TH>Average Call Duration (ms)</TH></TR>");
		for (TimeInterval ti : map.keySet()) {
			MethodStatisticsReport mReport = map.get(ti);
			MethodStatisticsReportPoint point = mReport.classTotals().get(className);
			ps.format("<TR><TD>%s</TD><TD>%d</TD><TD>%d</TD><TD>%d</TD><TD>%d</TD><TD>%.2f %%</TD><TD>%d</TD></TR>\n",
				ti.longDescription(), point.totalCallsStarted(), point.totalCompleted(), point.totalSucceeded(),
				point.totalFailed(), point.failureRate() * 100.0, point.averageDuration());
		}
		ps.println("</TABLE>");
	}

	public ContainerStatistics()
	{
		super(LOGO_LOCATION, PAGE_TITLE);
	}

	@Override
	protected void generateContent(PrintStream ps) throws IOException
	{
		edu.virginia.vcgr.genii.client.stats.ContainerStatistics stats = edu.virginia.vcgr.genii.client.stats.ContainerStatistics
			.instance();

		DatabaseStatistics dbStats = stats.getDatabaseStatistics();
		MethodStatistics mStats = stats.getMethodStatistics();

		ps.format("<H2>Database Statistics (last rejuvenated:  %tc)</H2><BR>", dbStats.databaseStartTime());
		ps.println("<TABLE BORDER=\"1\">");
		ps.println("<tr bgcolor=\"#7FFFD4\"><th>Interval</th><th>Connections Opened</TH><TH>Connections Closed</TH><TH>Average Connection Time (ms)</TH></TR>");
		Map<TimeInterval, DatabaseStatisticsReport> reports = dbStats.report();
		for (TimeInterval ti : reports.keySet()) {
			DatabaseStatisticsReport report = reports.get(ti);
			ps.format("<tr><td>%s</td><td>%d</td><td>%d</td><td>%d</td></tr>", ti.longDescription(), report.numOpened(),
				report.numClosed(), report.averageDuration());
		}
		ps.println("</TABLE>");

		ps.println("<BR>");
		ps.format("<IMG SRC=\"database-histogram.png?width=%1$d&height=%2$d\" " + "ALT=\"*\" width\"%1$d\" height=\"%2$d\"/>",
			DatabaseHistogram.WIDTH, DatabaseHistogram.HEIGHT);

		ps.println("<BR><BR>");
		ps.println("<H2>Method Statistics</H2>");
		ps.println("<H3>Container-wide Method Statistics</H3>");
		ps.println("<TABLE BORDER=\"1\">");
		ps.println("<TR bgcolor=\"#7FFFD4\"><TH>Interval</TH><TH>Calls Started</TH><TH>Calls Completed</TH><TH>Calls Succeeded</TH><TH>Calls Failed</TH><TH>Failure Rate</TH><TH>Average Call Duration (ms)</TH></TR>");
		Map<TimeInterval, MethodStatisticsReport> mReports = mStats.report();
		for (TimeInterval ti : mReports.keySet()) {
			MethodStatisticsReport mReport = mReports.get(ti);
			MethodStatisticsReportPoint point = mReport.totals();
			ps.format("<TR><TD>%s</TD><TD>%d</TD><TD>%d</TD><TD>%d</TD><TD>%d</TD><TD>%.2f %%</TD><TD>%d</TD></TR>\n",
				ti.longDescription(), point.totalCallsStarted(), point.totalCompleted(), point.totalSucceeded(),
				point.totalFailed(), point.failureRate() * 100.0, point.averageDuration());
		}
		ps.println("</TABLE>");

		ps.println("<BR");
		ps.format("<IMG SRC=\"method-histogram.png?width=%1$d&height=%2$d\" " + "ALT=\"*\" width\"%1$d\" height=\"%2$d\"/>",
			MethodHistogram.WIDTH, MethodHistogram.HEIGHT);

		Set<String> classes = mStats.report().get(TimeInterval.THIRTY_SECONDS).classTotals().keySet();
		for (String className : classes) {
			ps.println("<BR>");
			printClassTable(ps, mStats.report(), className);
		}
	}
}