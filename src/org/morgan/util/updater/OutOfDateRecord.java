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

import java.io.File;

import org.morgan.util.Version;

/**
 * @author Mark Morgan (mark@mark-morgan.org)
 */
class OutOfDateRecord
{
	private String _relativeName;
	private File _tmpFile;
	private Version _oldVersion;
	private Version _newVersion;
	
	public OutOfDateRecord(String relativeName, File updateDir, Version oldVersion,
		Version newVersion)
	{
		_relativeName = relativeName;
		_tmpFile = new File(updateDir, relativeName + ".tmp");
		_oldVersion = oldVersion;
		_newVersion = newVersion;
	}
	
	public String getRelativeName()
	{
		return _relativeName;
	}
	
	public File getTmpFile()
	{
		return _tmpFile;
	}
	
	public Version getOldVersion()
	{
		return _oldVersion;
	}
	
	public Version getNewVersion()
	{
		return _newVersion;
	}
}
