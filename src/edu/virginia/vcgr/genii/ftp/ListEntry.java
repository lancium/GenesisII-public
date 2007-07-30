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

import java.util.Date;

public class ListEntry
{
	static private final long _180Days = 
		1000 * 60 * 60 * 24 * 180;
	
	private boolean _isDirectory;
	private int _hardLinkCount;
	private FilePermissions _permissions;
	private String _user;
	private String _group;
	private long _size;
	private Date _lastMod;
	private String _name;
	
	public ListEntry(String name, Date lastMod, long size,
		String user, String group, FilePermissions permissions,
		int hardLinkCount, boolean isDirectory)
	{
		_isDirectory = isDirectory;
		_hardLinkCount = hardLinkCount;
		_permissions = permissions;
		_user = user;
		_group = group;
		_size = size;
		_lastMod = lastMod;
		_name = name;
	}
	
	public String toString()
	{
		String dateString;
		
		Date now = new Date();
		
		if (now.getTime() - _lastMod.getTime() > _180Days)
		{
			// long ago
			dateString = String.format("%1$tb %1$te %1$tY", _lastMod);
		} else
		{
			// recent
			dateString = String.format("%1$tb %1$te %1$tR", _lastMod);
		}
		
		return String.format("%1$s%2$s %3$d %4$s %5$s %6$d %7$s %8$s",
			(_isDirectory ? "d" : "-"),
			_permissions,
			_hardLinkCount,
			_user,
			_group,
			_size,
			dateString,
			_name);
	}
	
	static public void main(String []args)
	{
		System.out.println(
			new ListEntry("Mark", new Date(
				new Date().getTime() - 2 * _180Days), 1024,
				"morgan", "vcgr", new FilePermissions(0x7, 0x5, 0x5),
				0, false));
		System.out.println(
			new ListEntry("Matt", new Date(
				new Date().getTime() - 5000), 1024,
				"morgan", "vcgr", new FilePermissions(0x6, 0x0, 0x0),
				0, true));
	}
}
