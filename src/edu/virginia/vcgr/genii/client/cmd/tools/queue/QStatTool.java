package edu.virginia.vcgr.genii.client.cmd.tools.queue;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.TimeZone;

import org.apache.axis.message.MessageElement;
import org.ggf.bes.factory.ActivityStatusType;

import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.cmd.tools.BaseGridTool;
import edu.virginia.vcgr.genii.client.cmd.tools.Option;
import edu.virginia.vcgr.genii.client.cmd.tools.ToolCategory;
import edu.virginia.vcgr.genii.client.queue.JobInformation;
import edu.virginia.vcgr.genii.client.queue.JobTicket;
import edu.virginia.vcgr.genii.client.queue.QueueManipulator;
import edu.virginia.vcgr.genii.client.gpath.*;
import edu.virginia.vcgr.genii.client.io.FileResource;

public class QStatTool extends BaseGridTool
{
	static final private String _DESCRIPTION = "edu/virginia/vcgr/genii/client/cmd/tools/description/dqstat";
	static final private String _USAGE = "edu/virginia/vcgr/genii/client/cmd/tools/usage/uqstat";
	static final private String _MANPAGE = "edu/virginia/vcgr/genii/client/cmd/tools/man/qstat";

	private boolean _full = false;

	public QStatTool()
	{
		super(new FileResource(_DESCRIPTION), new FileResource(_USAGE), false, ToolCategory.EXECUTION);
		addManPage(new FileResource(_MANPAGE));
	}

	@Option({ "f", "full" })
	public void setFull()
	{
		_full = true;
	}

	@Override
	protected int runCommand() throws Throwable
	{
		GeniiPath gPath = new GeniiPath(getArgument(0));
		if (gPath.pathType() != GeniiPathType.Grid)
			throw new InvalidToolUsageException("<queue-path> must be a grid path. ");
		ArrayList<JobTicket> tickets;
		QueueManipulator manipulator = new QueueManipulator(gPath.path());

		if (numArguments() > 1) {
			tickets = new ArrayList<JobTicket>(numArguments() - 1);
			for (String arg : getArguments().subList(1, numArguments())) {
				tickets.add(new JobTicket(arg));
			}
		} else
			tickets = null;

		Iterator<JobInformation> info = manipulator.status(tickets);
		printHeader();
		while (info.hasNext()) {
			printJobInformation(info.next());
		}

		return 0;
	}

	@Override
	protected void verify() throws ToolException
	{
		if (numArguments() < 1)
			throw new InvalidToolUsageException("Must supply a queue path.");
	}

	private void printHeader()
	{
		stdout.println(String.format("%1$-36s   %2$-21s   %3$-4s   %4$-8s", "Ticket", "Submit Time", "Tries", "State"));
	}

	static private final String _FORMAT = "%1$-36s   %2$tH:%2$tM %2$tZ %2$td %2$tb %2$tY   %3$-4d   %4$s";

	private void printJobInformation(JobInformation jobInfo)
	{
		String stateString = jobInfo.getScheduledOn();
		if (stateString != null)
			stateString = String.format("On %s", stateString);
		else
			stateString = String.format("%s", jobInfo.getJobState());

		TimeZone tz = TimeZone.getDefault();
		Calendar submitTime = jobInfo.getSubmitTime();
		submitTime.setTimeZone(tz);

		stdout.println(String.format(_FORMAT, jobInfo.getTicket(), submitTime, jobInfo.getFailedAttempts(), stateString));
		ActivityStatusType ast = jobInfo.besActivityStatus();
		if (_full) {
			stdout.format("\tJob Name:  %s\n", jobInfo.jobName());
			if (ast != null) {
				stdout.format("\tBES Status:  %s\n", ast.getState().getValue());
				MessageElement[] any = ast.get_any();
				if (any != null) {
					for (MessageElement e : any) {
						stdout.format("\t%s\n", e);
					}
				}
			}
		}
		stdout.println();
	}
}
