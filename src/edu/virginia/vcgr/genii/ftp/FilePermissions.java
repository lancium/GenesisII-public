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
package edu.virginia.vcgr.genii.ftp;

class FilePermissions
{
	static public final int EXEC_PERM = 0x1;
	static public final int WRITE_PERM = 0x2;
	static public final int READ_PERM = 0x4;
	
	private int _userPerms;
	private int _groupPerms;
	private int _worldPerms;
	
	FilePermissions(int user, int group, int world)
	{
		_userPerms = user;
		_groupPerms = group;
		_worldPerms = world;
	}
	
	private String toString(int perm)
	{
		return String.format("%1$s%2$s%3$s",
			((perm & READ_PERM) > 0) ? "r" : "-",
			((perm & WRITE_PERM) > 0) ? "w" : "-",
			((perm & EXEC_PERM) > 0) ? "x" : "-");
	}
	
	public String toString()
	{
		return toString(_userPerms) + toString(_groupPerms)
			+ toString(_worldPerms);
	}
}
