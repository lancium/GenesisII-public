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
import java.util.HashMap;
import java.util.HashSet;
import java.util.StringTokenizer;

/**
 * @author Mark Morgan (mark@mark-morgan.org)
 */
public class CommandLine implements ICommandLine
{
	private HashSet<String> _flags = new HashSet<String>();;
	private HashMap<String, ArrayList<String> > _options =
		new HashMap<String, ArrayList<String> >();
	private ArrayList<String> _arguments = new ArrayList<String>();
	
	private void parseFlags(String flags)
	{
		for (int lcv = 0; lcv < flags.length(); lcv++)
		{
			_flags.add(flags.substring(lcv, lcv + 1));
		}
	}
	
	private void parseOptionFlag(String line)
	{
		int index = line.indexOf("=");
		if (index < 0)
		{
			_flags.add(line);
			return;
		}
		
		String name = line.substring(0, index);
		ArrayList<String> values = _options.get(name);
		if (values == null)
		{
			values = new ArrayList<String>();
			_options.put(name, values);
		}
		
		String []vals = line.substring(index + 1).split(",");
		for (String val : vals)
		{
			if (val.length() == 0)
				continue;
			
			values.add(val);
		}
	}
	
	private void addArgument(String arg)
	{
		_arguments.add(arg);
	}
	
	public CommandLine(String commandline)
	{
		this(commandline, true);
	}

	public CommandLine(String commandline, boolean allowSingleDashes)
	{
		if (commandline == null)
			return;
		
		// needs to be fixed to accommodate escaped quotes
		
		StringTokenizer t = new StringTokenizer(commandline, " \t'\"", true);
		int tokenMode = 0;
		ArrayList<String> parts = new ArrayList<String>();
		String currentToken = "";
		while (t.hasMoreTokens()) {
			String token = t.nextToken();
			if (tokenMode == 0) {
				if (!token.equals(" ") && !token.equals("\t") && !token.equals("\"") && !token.equals("'")) {
					parts.add(token);
				} else if (token.equals("\"")) {
					tokenMode = 1;
				} else if (token.equals("'")) {
					tokenMode = 2;
				}
			} else if (tokenMode == 1) {
				if (!token.equals("\"")) {
					currentToken += token;
				} else {
					parts.add(currentToken);
					currentToken = "";
					tokenMode = 0;
				}
			} else if (tokenMode == 2) {
				if (!token.equals("'")) {
					currentToken += token;
				} else {
					parts.add(currentToken);
					currentToken = "";
					tokenMode = 0;
				}
			}
		}
	
		for (String arg : parts)
		{
			if (arg.startsWith("--"))
				parseOptionFlag(arg.substring(2));
			else if (allowSingleDashes && arg.startsWith("-"))
				parseFlags(arg.substring(1));
			else
				addArgument(arg);
		}
	}

	public CommandLine(String []args) {
		this(args, true);
	}

	public CommandLine(String []args, boolean allowSingleDashes)
	{
		for (String arg : args)
		{
			if (arg.startsWith("--"))
				parseOptionFlag(arg.substring(2));
			else if (allowSingleDashes && arg.startsWith("-"))
				parseFlags(arg.substring(1));
			else
				addArgument(arg);
		}
	}
	
	public int numArguments()
	{
		return _arguments.size();
	}
	
	public String getArgument(int number)
	{
		if (number >= _arguments.size())
			return null;

		return _arguments.get(number);
	}
	
	public String []getArguments()
	{
		String []args = new String[_arguments.size()];
		_arguments.toArray(args);
		return args;
	}
	
	public boolean hasFlag(String flagName)
	{
		return _flags.contains(flagName);
	}
	
	public String []getFlags()
	{
		String []flags = new String[_flags.size()];
		_flags.toArray(flags);
		return flags;
	}
	
	public boolean hasOption(String optionName)
	{
		return _options.containsKey(optionName);
	}
	
	public HashMap<String, ArrayList<String> > getOptions()
	{
		return _options;
	}
	
	public String getOptionValue(String optionName)
	{
		ArrayList<String> list = _options.get(optionName);
		if (list == null || list.size() == 0)
			return null;
		
		return list.get(0);
	}
	
	public String[] getOptionValues(String optionName)
	{
		ArrayList<String> list = _options.get(optionName);
		String []ret;
		
		if (list == null)
			return new String[0];
		
		ret = new String[list.size()];
		list.toArray(ret);
		return ret;
	}
}
