package edu.virginia.vcgr.genii.client.cmd.tools;

import java.rmi.RemoteException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import org.morgan.util.configuration.ConfigurationException;
import org.oasis_open.docs.wsrf.rl_2.SetTerminationTime;

import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.io.FileResource;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathDoesNotExistException;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;
import edu.virginia.vcgr.genii.client.utils.units.Duration;
import edu.virginia.vcgr.genii.common.GeniiCommon;

public class TerminationScheduleTool extends BaseGridTool
{
	static final private String _DESCRIPTION_RESOURCE =
		"edu/virginia/vcgr/genii/client/cmd/tools/resources/termination-schedule-description.txt";
	static final private String _USAGE_RESOURCE =
		"edu/virginia/vcgr/genii/client/cmd/tools/resources/termination-schedule-usage.txt";
	
	public TerminationScheduleTool()
	{
		super(new FileResource(_DESCRIPTION_RESOURCE),
			new FileResource(_USAGE_RESOURCE), false);
	}
	
	@Override
	protected int runCommand() throws Throwable
	{
		int numArgs = numArguments();
		Date targetTime = null;
		
		targetTime = parseTargetTime(getArgument(numArgs -1));
		RNSPath path = RNSPath.getCurrent();
		
		for (int lcv = 0; lcv < (numArgs - 1); lcv++)
		{
			RNSPath []targets = path.list(getArgument(lcv),
				RNSPathQueryFlags.MUST_EXIST);
			for (RNSPath target : targets)
			{
				schedTerm(target, targetTime);
			}
		}
		
		return 0;
	}
	
	static private Date parseTargetTime(String targetTime)
		throws ParseException
	{
		if (targetTime.startsWith("+"))
		{
			Duration d = Duration.parse(targetTime.substring(1));
			return new Date(new Date().getTime() + d.getMilliseconds());
		} else
		{
			DateFormat format = DateFormat.getDateTimeInstance();
			return format.parse(targetTime);
		}
	}
	
	static public void schedTerm(RNSPath target, Date targetTime)
		throws ConfigurationException, RemoteException,
			RNSPathDoesNotExistException
	{
		GeniiCommon common = ClientUtils.createProxy(GeniiCommon.class,
			target.getEndpoint());
		
		Calendar c = Calendar.getInstance();
		c.setTime(targetTime);
		
		common.setTerminationTime(new SetTerminationTime(c, null));
	}

	@Override
	protected void verify() throws ToolException
	{
		if (numArguments() != 2)
			throw new InvalidToolUsageException();
	}
}