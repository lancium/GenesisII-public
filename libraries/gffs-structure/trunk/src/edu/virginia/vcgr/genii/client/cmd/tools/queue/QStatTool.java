package edu.virginia.vcgr.genii.client.cmd.tools.queue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.TimeZone;

import org.apache.axis.message.MessageElement;
import org.ggf.bes.factory.ActivityStatusType;
import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ReloadShellException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.cmd.tools.BaseGridTool;
import edu.virginia.vcgr.genii.client.cmd.tools.Option;
import edu.virginia.vcgr.genii.client.cmd.tools.ToolCategory;
import edu.virginia.vcgr.genii.client.dialog.UserCancelException;
import edu.virginia.vcgr.genii.client.queue.JobInformation;
import edu.virginia.vcgr.genii.client.queue.JobTicket;
import edu.virginia.vcgr.genii.client.queue.QueueManipulator;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rp.ResourcePropertyException;
import edu.virginia.vcgr.genii.client.security.axis.AuthZSecurityException;
import edu.virginia.vcgr.genii.client.gpath.*;
import edu.virginia.vcgr.genii.client.io.LoadFileResource;

public class QStatTool extends BaseGridTool
{
	static final private String _DESCRIPTION = "config/tooldocs/description/dqstat";
	static final private String _USAGE = "config/tooldocs/usage/uqstat";
	static final private String _MANPAGE = "config/tooldocs/man/qstat";

	private boolean _full = false;
	private boolean _vm = false;

	public QStatTool()
	{
		super(new LoadFileResource(_DESCRIPTION), new LoadFileResource(_USAGE), false, ToolCategory.EXECUTION);
		addManPage(new LoadFileResource(_MANPAGE));
	}

	@Option({ "f", "full" })
	public void setFull()
	{
		_full = true;
	}

	@Option({ "v", "vm_info" })
	public void setVMInfo()
	{
		_vm = true;
	}

	@Override
	protected int runCommand() throws ReloadShellException, ToolException, UserCancelException, RNSException, AuthZSecurityException,
		IOException, ResourcePropertyException
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
			printJobInformation(info.next(), gPath);
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
		if(!_vm)
			stdout.println(String.format("%1$-40s   %2$-21s   %3$-4s   %4$-8s", "Ticket", "Submit Time", "Tries", "State"));
	}

	static private final String _FORMAT = "%1$-40s   %2$tH:%2$tM %2$tZ %2$td %2$tb %2$tY   %3$-4d   %4$s";

	private void printJobInformation(JobInformation jobInfo, GeniiPath qPath)
	{
		if(_vm)
		{
			printVMJobInformation(jobInfo, qPath);
			return;
		}

		String stateString = jobInfo.getScheduledOn();
		if (stateString != null)
			stateString = String.format("On %s", stateString);
		else
			stateString = String.format("%s", jobInfo.getJobState());

		TimeZone tz = TimeZone.getDefault();
		Calendar submitTime = jobInfo.getSubmitTime();
		submitTime.setTimeZone(tz);

		stdout.println(String.format(_FORMAT, jobInfo.getTicket(), submitTime, jobInfo.getFailedAttempts(), stateString));

		if (_full) {
			stdout.format("\tJob Name:  %s\n", jobInfo.jobName());
			ActivityStatusType ast = jobInfo.besActivityStatus();
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
	}

	//LAK 2020 Aug 20: The VM flag instead outputs a less human friendly, but more information along with easier parsing for a program.
	private void printVMJobInformation(JobInformation jobInfo, GeniiPath qPath)
	{
		String scheduledOn = jobInfo.getScheduledOn();
		String stateString = String.format("%s", jobInfo.getJobState());
		String ipString = "IP: None";

		TimeZone tz = TimeZone.getDefault();
		Calendar submitTime = jobInfo.getSubmitTime();
		submitTime.setTimeZone(tz);

		if(scheduledOn != null)
		{
			String jobDir = qPath.path() + "/resources/" + scheduledOn + "/activities/" + jobInfo.jobName() + "/working-dir/";

			String ipAddrFile = readFile(jobDir + ".IPADDR");
			if(ipAddrFile != null)
				ipString = String.format("IP: %s", ipAddrFile.replaceAll(System.lineSeparator(),","));
		}

		stdout.println(String.format("Job: %1s %2s %3$tH:%3$tM %3$tZ %3$td %3$tb %3$tY %4$d %5$s %6$s", jobInfo.jobName(), jobInfo.getTicket(), submitTime, jobInfo.getFailedAttempts(), stateString, ipString));
	}

	//LAK 2020 Aug 20: Added this helper method to read some grid files. We use this for the VM information flag requests.
	private String readFile(String filePath)
	{
		ByteArrayOutputStream result = new ByteArrayOutputStream();
		InputStream inputStream = null;
		byte[] buffer = new byte[1024];
		int length;

		try
		{
			GeniiPath path = new GeniiPath(filePath);
			inputStream = path.openInputStream();
			while ((length = inputStream.read(buffer)) != -1) {
			    result.write(buffer, 0, length);
			}
		}
		catch(IOException e)
		{
			return null;
		}
		finally
		{
			//LAK: We do not need to close the ByteArrayOutputStream (see source file)
			StreamUtils.close(inputStream);
		}

		// StandardCharsets.UTF_8.name() > JDK 7
		try {
			return result.toString("UTF-8");
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}
}
