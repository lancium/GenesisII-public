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

/**
 * This interface represents pre-parsed informmation about a command line including
 * all of its options and arguments.
 * 
 * @author Mark Morgan (mark@mark-morgan.org)
 */
public interface ICommandLine
{
	/**
	 * Retrieve the number of arguments stored within this command line.  This number
	 * <B>does not</B> include the name of the command (or the 0th argument).
	 * 
	 * @return The number of arguments stored in this command line.
	 */
	public int numArguments();
	
	/**
	 * Get the argument indicated by the given number.  The index is 0-based, but does
	 * not include the command name.  In otherwords, in the command line<BR>
	 * <CODE>command arg0 arg1 arg2</CODE><BR>
	 * argument number 0 would be arg0 in the example command line invocation.
	 * 
	 * @param number The argument number to retrieve.
	 * @return The argument stored at the indicated position.
	 */
	public String getArgument(int number);
	
	/**
	 * Retrieve all of the arguments (except the command name) stored in this instance.
	 * 
	 * @return An array of arguments stored in this command line instance.
	 */
	public String []getArguments();
	
	/**
	 * Determine whether or not a flag is set in this command line.  A flag is defined
	 * as any command line entry which starts with exactly one dash (-) and is a single
	 * letter long, or starts with two dashes (--) and is a long name consisting of 1 or
	 * more letters, but in both cases flags must not have any values associated with
	 * them.  For example, in the command line<BR>
	 * <CODE>command -abc=foo --string1 --string2=value arg0 arg1</CODE><BR>
	 * a and b would be flags while c would be an option (having the value of <B>foo</B>).
	 * string1 would be a flag and string2 would be an option with the value of 
	 * <B>value</B>.
	 * 
	 * @param flagName The string name of the flag to check for.
	 * @return true if the flag was given on the command line, false otherwise.
	 */
	public boolean hasFlag(String flagName);
	
	/**
	 * Retrieve all of the flags for this command line instance.  A flag is defined
	 * as any command line entry which starts with exactly one dash (-) and is a single
	 * letter long, or starts with two dashes (--) and is a long name consisting of 1 or
	 * more letters, but in both cases flags must not have any values associated with
	 * them.  For example, in the command line<BR>
	 * <CODE>command -abc=foo --string1 --string2=value arg0 arg1</CODE><BR>
	 * a and b would be flags while c would be an option (having the value of <B>foo</B>).
	 * string1 would be a flag and string2 would be an option with the value of 
	 * <B>value</B>.
	 * 
	 * @return An array of all of the flags for this commandline.
	 */
	public String []getFlags();
	
	/**
	 * Determine whether or not a given option is included in this command line.  
	 * An option is defined as any command line entry which starts with exactly one dash 
	 * (-) and is a single letter long, or starts with two dashes (--) and is a long name
	 * consisting of 1 or more letters, but in both cases options must have values 
	 * associated with them.  For example, in the command line<BR>
	 * <CODE>command -abc=foo --string1 --string2=value arg0 arg1</CODE><BR>
	 * a and b would be flags while c would be an option (having the value of <B>foo</B>).
	 * string1 would be a flag and string2 would be an option with the value of 
	 * <B>value</B>.
	 * 
	 * @param optionName The name of the option to check for.
	 * @return True if the indicated options exists in this command line, false otherwise.
	 */
	public boolean hasOption(String optionName);
	
	/**
	 * Retrieve all of the options associated with this command line.  
	 * An option is defined as any command line entry which starts with exactly one dash 
	 * (-) and is a single letter long, or starts with two dashes (--) and is a long name
	 * consisting of 1 or more letters, but in both cases options must have values 
	 * associated with them.  For example, in the command line<BR>
	 * <CODE>command -abc=foo --string1 --string2=value arg0 arg1</CODE><BR>
	 * a and b would be flags while c would be an option (having the value of <B>foo</B>).
	 * string1 would be a flag and string2 would be an option with the value of 
	 * <B>value</B>.
	 * 
	 * @return A map of all of the options stored in this command line.  Notice that the
	 * map maps option names to arrays of strings.  This is because command lines may
	 * have more then one value and those values are all returned in this map.
	 */
	public HashMap<String, ArrayList<String> > getOptions();
	
	/**
	 * Retrieve the first value associated with a given option name.  
	 * An option is defined as any command line entry which starts with exactly one dash 
	 * (-) and is a single letter long, or starts with two dashes (--) and is a long name
	 * consisting of 1 or more letters, but in both cases options must have values 
	 * associated with them.  For example, in the command line<BR>
	 * <CODE>command -abc=foo --string1 --string2=value arg0 arg1</CODE><BR>
	 * a and b would be flags while c would be an option (having the value of <B>foo</B>).
	 * string1 would be a flag and string2 would be an option with the value of 
	 * <B>value</B>.
	 * 
	 * @param optionName The name of the option to return a value for.
	 * @return The first option value associated with this option name.
	 */
	public String getOptionValue(String optionName);
	
	/**
	 * Retrieve all of the values associated with a given option name.  
	 * An option is defined as any command line entry which starts with exactly one dash 
	 * (-) and is a single letter long, or starts with two dashes (--) and is a long name
	 * consisting of 1 or more letters, but in both cases options must have values 
	 * associated with them.  For example, in the command line<BR>
	 * <CODE>command -abc=foo --string1 --string2=value arg0 arg1</CODE><BR>
	 * a and b would be flags while c would be an option (having the value of <B>foo</B>).
	 * string1 would be a flag and string2 would be an option with the value of 
	 * <B>value</B>.
	 * 
	 * @param optionName The name of the option to return values for.
	 * @return An array of all of the option values stored in this command line.
	 */
	public String[] getOptionValues(String optionName);
}
