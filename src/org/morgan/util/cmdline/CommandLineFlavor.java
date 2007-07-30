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
package org.morgan.util.cmdline;

import java.util.ArrayList;

/**
 * This file isn't really implemented yet.
 *
 * @author Mark Morgan (mark@mark-morgan.org)
 */
public class CommandLineFlavor
{
	static final public int INFINITE_LIMIT = -1;
	
	private String _flavorName;
	private int _minimumArguments;
	private int _maximumArguments;
	private ArrayList<String> _requiredFlags = new ArrayList<String>();
	private ArrayList<String> _requiredOptions = new ArrayList<String>();
	
	public CommandLineFlavor(String flavorName, int minimumArgs, int maximumArgs)
	{
		if (minimumArgs == INFINITE_LIMIT)
			throw new IllegalArgumentException(
				"Cannot have an infinite lower limit.");
		
		_flavorName = flavorName;
		_minimumArguments = minimumArgs;
		_maximumArguments = maximumArgs;
	}
	
	public CommandLineFlavor(String flavorName, int exactArgs)
	{
		this(flavorName, exactArgs, exactArgs);
	}
	
	public CommandLineFlavor(String flavorName)
	{
		this(flavorName, 0, INFINITE_LIMIT);
	}
	
	public void addRequiredFlag(String flagName)
	{
		_requiredFlags.add(flagName);
	}
	
	public void addRequiredOption(String optionName)
	{
		_requiredOptions.add(optionName);
	}
	
	public boolean matches(CommandLine cline)
	{
		int numArgs = cline.numArguments();
		if (numArgs < _minimumArguments)
			return false;
		if ((numArgs > _maximumArguments) && 
			(_maximumArguments != INFINITE_LIMIT))
			return false;
		
		for (String flag : _requiredFlags)
		{
			if (!cline.hasFlag(flag))
				return false;
		}
		
		for (String option : _requiredOptions)
		{
			if (!cline.hasOption(option))
				return false;
		}
		
		return true;
	}
	
	public String getFlavorName()
	{
		return _flavorName;
	}
}
