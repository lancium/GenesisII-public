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

import org.morgan.util.Version;

/**
 * @author Mark Morgan (mark@mark-morgan.org)
 */
public interface IUpdateListener
{
	public void exceptionOccurred(String msg, IOException ioe);
	
	public void startingUpdate(int filesToUpdate);
	public void finishedUpdate();
	
	public void startingFileUpdate(
		String fileName, Version oldVersion, Version newVersion);
	public void finishedFileUpdate(String fileName);
	
	public void startingCommit();
	public void finishedCommit();
}
