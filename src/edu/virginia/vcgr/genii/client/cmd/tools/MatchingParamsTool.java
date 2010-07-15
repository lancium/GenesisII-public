package edu.virginia.vcgr.genii.client.cmd.tools;

import java.util.Collection;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.io.FileResource;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.gpath.*;
import edu.virginia.vcgr.genii.common.GeniiCommon;
import edu.virginia.vcgr.genii.common.MatchingParameter;
import edu.virginia.vcgr.genii.container.q2.matching.MatchingParamEnum;

public class MatchingParamsTool extends BaseGridTool
{
	static final private String _DESCRIPTION =
		"Allows a user to manipulate a target's matching parameters.";
	static final private FileResource _USAGE =
		new FileResource("edu/virginia/vcgr/genii/client/cmd/tools/resources/matchp-tool-usage.txt");
	
	static private final Pattern ADD_REMOVE_PATTERN = Pattern.compile(
		"^\\s*((?:add)|(?:remove))\\s*\\(([^),]+),([^)]+)\\)\\s*$");
	
	private Collection<String> _targets = new LinkedList<String>();
	private Collection<MatchingParameter> _adds =
		new LinkedList<MatchingParameter>();
	private Collection<MatchingParameter> _removes =
		new LinkedList<MatchingParameter>();
	
	private void verifyParameterName(String name) throws InvalidToolUsageException
	{
		try
		{
			int index = name.indexOf(':');
			if (index > 0)
				MatchingParamEnum.valueOf(name.substring(0, index));
		}
		catch (IllegalArgumentException e)
		{
			throw new InvalidToolUsageException(String.format(
				"Matching parameter name %s is not valid (requirment indicator must be %s, or %s).",
				name, MatchingParamEnum.requires, MatchingParamEnum.supports));
		}
	}
	
	public MatchingParamsTool()
	{
		super(_DESCRIPTION, _USAGE, false);
	}
	
	@Override
	protected int runCommand() throws Throwable
	{
		RNSPath current = RNSPath.getCurrent();
		MatchingParameter []addParameters;
		MatchingParameter []removeParameters;
		
		addParameters = new MatchingParameter[_adds.size()];
		removeParameters = new MatchingParameter[_removes.size()];
		
		addParameters = _adds.toArray(addParameters);
		removeParameters = _removes.toArray(removeParameters);
		
		for (String target : _targets)
		{
			for (RNSPath t : current.expand(target))
			{
				GeniiCommon stub = ClientUtils.createProxy(
					GeniiCommon.class, t.getEndpoint());
				
				if (addParameters.length > 0)
				{
					stdout.format("Adding values to %s\n", t.pwd());
					stub.addMatchingParameter(addParameters);
				}
				
				if (removeParameters.length > 0)
				{
					stdout.format("Removing values from %s\n", t.pwd());
					stub.removeMatchingParameter(removeParameters);
				}
			}
		}
		
		return 0;
	}

	@Override
	protected void verify() throws ToolException
	{
		if (_targets.size() < 1)
			throw new InvalidToolUsageException();
	}
	
	@Override
	public void addArgument(String argument) throws ToolException
	{
		Matcher matcher = ADD_REMOVE_PATTERN.matcher(argument);
		if (matcher.matches())
		{
			String addRemove = matcher.group(1).trim();
			String name = matcher.group(2).trim();
			String value = matcher.group(3).trim();
			
			verifyParameterName(name);
			
			if (addRemove.equals("add"))
				_adds.add(new MatchingParameter(name, value));
			else if (addRemove.equals("remove"))
				_removes.add(new MatchingParameter(name, value));
			else
				throw new InvalidToolUsageException(
					"Unknown error occurred in tool.");
		} else
		{
			GeniiPath gPath = new GeniiPath(argument);
			if(gPath.pathType() != GeniiPathType.Grid)
				throw new InvalidToolUsageException("<target> must be a grid path. ");
			_targets.add(argument);
		}
	}
}