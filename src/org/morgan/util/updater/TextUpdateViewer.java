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
package org.morgan.util.updater;

import java.io.IOException;
import java.io.PrintStream;

import org.morgan.util.Version;
import org.morgan.util.io.GuaranteedDirectory;

/**
 * @author Mark Morgan (mark@mark-morgan.org)
 */
public class TextUpdateViewer implements IUpdateListener
{
	private PrintStream _out;
	private int _total;
	private int _nextFile = 1;
	
	public TextUpdateViewer(PrintStream out)
	{
		_out = out;
	}
	
	public TextUpdateViewer()
	{
		this(System.out);
	}
	
	public void exceptionOccurred(String msg, IOException ioe)
	{
		_out.println(msg);
		_out.flush();
	}

	public void startingUpdate(int filesToUpdate)
	{
		_nextFile = 1;
		_total = filesToUpdate;
		_out.println("Updating " + filesToUpdate + " files.");
		_out.flush();
	}

	public void finishedUpdate()
	{
		_out.println("Finished!");
		_out.flush();
	}

	public void startingFileUpdate(String fileName, Version oldVersion,
			Version newVersion)
	{
		_out.print("Updating [" + _nextFile + "/" + _total + "] \"" + fileName + "\"");
		if (oldVersion != null)
			_out.print(" from Version " + oldVersion);
		_out.println(" to Version " + newVersion);
		_out.flush();
	}

	public void finishedFileUpdate(String fileName)
	{
		_nextFile++;
	}

	public void startingCommit()
	{
		_out.println("Committing...");
		_out.flush();
	}

	public void finishedCommit()
	{
	}
	
	static public void main(String []args) throws Exception
	{
		if (args.length != 4)
		{
			System.err.println(
				"USAGE:  TextUpdateViewer <inst-path> <base-url> <project> <instance>");
			System.exit(1);
		}
		
		UpdateManager man = new UpdateManager(new GuaranteedDirectory(args[0]),
			args[1], args[2], args[3]);
		man.addUpdateListener(new TextUpdateViewer());
		man.update();
	}
}
