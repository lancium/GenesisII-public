/*
 * Copyright 2006 University of Virginia
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package edu.virginia.vcgr.genii.client.io;

import java.util.ArrayList;
import java.util.HashMap;

import org.morgan.util.cmdline.ICommandLine;

public class ReducedCommandLine implements ICommandLine
{
	private ICommandLine _parentCommandLine;
	private int _firstArgument;
	
	public ReducedCommandLine(ICommandLine parentCommandLine, int firstArgument)
	{
		_parentCommandLine = parentCommandLine;
		_firstArgument = firstArgument;
	}
	
	/**
	 * Returns true if the command line has no arguments, options, or flags
	 */
	public boolean isEmpty() 
	{
		return _parentCommandLine.isEmpty();
	}
	
	public int numArguments()
	{
		return _parentCommandLine.numArguments() - _firstArgument;
	}

	public String getArgument(int number)
	{
		return _parentCommandLine.getArgument(number + _firstArgument);
	}

	public String[] getArguments()
	{
		String []ret = new String[numArguments()];
		String []tmp = _parentCommandLine.getArguments();
		System.arraycopy(tmp, _firstArgument, ret, 0, ret.length);
		
		return ret;
	}

	public boolean hasFlag(String flagName)
	{
		return _parentCommandLine.hasFlag(flagName);
	}

	public String[] getFlags()
	{
		return _parentCommandLine.getFlags();
	}

	public boolean hasOption(String optionName)
	{
		return _parentCommandLine.hasOption(optionName);
	}

	public HashMap<String, ArrayList<String>> getOptions()
	{
		return _parentCommandLine.getOptions();
	}

	public String getOptionValue(String optionName)
	{
		return _parentCommandLine.getOptionValue(optionName);
	}

	public String[] getOptionValues(String optionName)
	{
		return _parentCommandLine.getOptionValues(optionName);
	}
}
