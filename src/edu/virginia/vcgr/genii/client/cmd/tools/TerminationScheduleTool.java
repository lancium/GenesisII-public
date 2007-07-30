package edu.virginia.vcgr.genii.client.cmd.tools;

import java.rmi.RemoteException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.axis.message.MessageElement;
import org.morgan.util.configuration.ConfigurationException;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.io.FileResource;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathDoesNotExistException;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;
import edu.virginia.vcgr.genii.common.GeniiCommon;
import edu.virginia.vcgr.genii.common.rattrs.SetAttributes;

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
	
	static private Pattern _DELTA_PAT = Pattern.compile(
		"([0-9]+)((?:ms)|(?:[smhd]))");
	
	static private Date parseTargetTime(String targetTime)
		throws ParseException
	{
		if (targetTime.startsWith("+"))
		{
			Matcher m = _DELTA_PAT.matcher(targetTime.substring(1));
			if (!m.matches())
				throw new ParseException("Delta timestamp \""
						+ targetTime + "\" unrecognized.", -1);
			long value = Long.parseLong(m.group(1));
			String type = m.group(2);
			
			if (type.equals("s"))
			{
				value *= 1000;
			} else if (type.equals("m"))
			{
				value *= (60 * 1000);
			} else if (type.equals("h"))
			{
				value *= (60 * 60 * 1000);
			} else if (type.equals("d"))
			{
				value *= (24 * 60 * 60 * 1000);
			}
			
			return new Date(new Date().getTime() + value);
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
		
		SetAttributes set = new SetAttributes(
			new MessageElement [] {
				new MessageElement(
						GenesisIIConstants.SCHED_TERM_TIME_QNAME, c) });
				
		common.setAttributes(set);
	}

	@Override
	protected void verify() throws ToolException
	{
		if (numArguments() != 2)
			throw new InvalidToolUsageException();
	}
}