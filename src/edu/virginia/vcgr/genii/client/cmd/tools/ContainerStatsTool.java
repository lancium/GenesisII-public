package edu.virginia.vcgr.genii.client.cmd.tools;

import java.util.Map;

import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.gpath.GeniiPath;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;
import edu.virginia.vcgr.genii.client.ser.DBSerializer;
import edu.virginia.vcgr.genii.client.stats.DatabaseStatisticsReport;
import edu.virginia.vcgr.genii.client.stats.MethodStatisticsReport;
import edu.virginia.vcgr.genii.client.stats.MethodStatisticsReportPoint;
import edu.virginia.vcgr.genii.client.stats.TimeInterval;
import edu.virginia.vcgr.genii.container.ContainerStatisticsResultType;
import edu.virginia.vcgr.genii.container.VCGRContainerPortType;

public class ContainerStatsTool extends BaseGridTool
{
	static final private String _DESCRIPTION =
		"Collects usage statistics from a remote container.";
	static final private String _USAGE =
		"container-stats <container-path>";
	
	public ContainerStatsTool()
	{
		super(_DESCRIPTION, _USAGE, false);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected int runCommand() throws Throwable
	{
		RNSPath container = lookup(new GeniiPath(getArgument(0)), 
			RNSPathQueryFlags.MUST_EXIST);
		
		VCGRContainerPortType proxy = ClientUtils.createProxy(VCGRContainerPortType.class,
			container.getEndpoint());
		ContainerStatisticsResultType result = proxy.containerStatistics(null);
		
		stdout.format("Container start time:  %1tc\n", result.getContainerStartTime());
		Map<TimeInterval, DatabaseStatisticsReport> dbReport = 
			(Map<TimeInterval, DatabaseStatisticsReport>)DBSerializer.deserialize(
				result.getDbStatisticsReport());
		Map<TimeInterval, MethodStatisticsReport> methodReport =
			(Map<TimeInterval, MethodStatisticsReport>)DBSerializer.deserialize(
				result.getMethodStatisticsReport());
		stdout.format("Database Statistics:\n");
		for (TimeInterval ti : dbReport.keySet())
		{
			stdout.format("  %s:\n", ti.longDescription());
			DatabaseStatisticsReport report = dbReport.get(ti);
			stdout.format("    Num Opened = %d, Num Closed = %d, Average Duration = %d ms\n",
				report.numOpened(), report.numClosed(), report.averageDuration());
		}
		stdout.format("Method Statistics:\n");
		for (TimeInterval ti : methodReport.keySet())
		{
			stdout.format("  %s:\n", ti.longDescription());
			MethodStatisticsReport report = methodReport.get(ti);
			stdout.format("    Totals:  %s\n", report.totals());
			Map<String, MethodStatisticsReportPoint> classes = report.classTotals();
			Map<String, Map<String, MethodStatisticsReportPoint>> methods = report.methodTotals();
			for (String className : classes.keySet())
			{
				stdout.format("    Class Totals(%s):  %s\n",
					className, classes.get(className));
				Map<String, MethodStatisticsReportPoint> methodMap = methods.get(className);
				if (methodMap != null)
				{
					for (String methodName : methodMap.keySet())
					{
						stdout.format("      Method (%s):  %s\n", methodName, methodMap.get(methodName));
					}
				}
			}
		}
		
		return 0;
	}

	@Override
	protected void verify() throws ToolException
	{
		if (numArguments() != 1)
			throw new InvalidToolUsageException(
				"Missing required \"container-path\" parameter.");
	}
}