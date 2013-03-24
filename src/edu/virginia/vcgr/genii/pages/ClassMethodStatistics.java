package edu.virginia.vcgr.genii.pages;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Map;

import org.morgan.dpage.InjectParameter;

import edu.virginia.vcgr.genii.client.stats.MethodStatistics;
import edu.virginia.vcgr.genii.client.stats.MethodStatisticsReport;
import edu.virginia.vcgr.genii.client.stats.MethodStatisticsReportPoint;
import edu.virginia.vcgr.genii.client.stats.TimeInterval;
import edu.virginia.vcgr.genii.container.dynpages.templates.GenesisIIStyledPage;

public class ClassMethodStatistics extends GenesisIIStyledPage
{
	static final private String PAGE_TITLE = "Class Method Statistics";

	@InjectParameter("className")
	private String _className;

	public ClassMethodStatistics()
	{
		super("images/grid_logo_medium.jpg", PAGE_TITLE);
	}

	@Override
	protected void generateContent(PrintStream ps) throws IOException
	{
		String classShortName = _className;
		int index = classShortName.lastIndexOf('.');
		classShortName = classShortName.substring(index + 1);

		edu.virginia.vcgr.genii.client.stats.ContainerStatistics stats = edu.virginia.vcgr.genii.client.stats.ContainerStatistics
			.instance();

		MethodStatistics mStats = stats.getMethodStatistics();
		Map<TimeInterval, MethodStatisticsReport> report = mStats.report();

		ps.format("<H2>Method Statistics for class %s</H2>", classShortName);

		MethodStatisticsReport msr = report.get(TimeInterval.FIVE_MINUTES);
		Map<String, MethodStatisticsReportPoint> methodMap = msr.methodTotals().get(_className);

		if (methodMap == null) {
			ps.println("<BR><BOLD>None Available</BOLD>");
			return;
		}

		for (String methodName : methodMap.keySet()) {
			ps.println("<BR>");
			ps.format("<H2>Statistics for Method %s</H2><BR>", methodName);
			ps.println("<TABLE BORDER=\"1\">");
			ps.println("<TR bgcolor=\"#7FFFD4\"><TH>Interval</TH><TH>Calls Started</TH><TH>Calls Completed</TH><TH>Calls Succeeded</TH><TH>Calls Failed</TH><TH>Failure Rate</TH><TH>Average Call Duration (ms)</TH></TR>");
			for (TimeInterval ti : report.keySet()) {
				msr = report.get(ti);
				methodMap = msr.methodTotals().get(_className);
				if (methodMap != null) {
					MethodStatisticsReportPoint point = methodMap.get(methodName);
					if (point != null) {
						ps.format(
							"<TR><TD>%s</TD><TD>%d</TD><TD>%d</TD><TD>%d</TD><TD>%d</TD><TD>%.2f %%</TD><TD>%d</TD></TR>\n",
							ti.longDescription(), point.totalCallsStarted(), point.totalCompleted(), point.totalSucceeded(),
							point.totalFailed(), point.failureRate() * 100.0, point.averageDuration());
					}
				}
			}
		}
	}
}