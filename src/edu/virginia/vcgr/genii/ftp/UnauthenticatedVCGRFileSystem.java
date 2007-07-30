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

import java.io.InputStream;
import java.io.OutputStream;

import edu.virginia.vcgr.genii.client.context.ICallingContext;

public class UnauthenticatedVCGRFileSystem implements IFTPFileSystem
{
	protected ICallingContext _context;
	
	public UnauthenticatedVCGRFileSystem(ICallingContext context)
	{
		_context = context;
	}
	
	public String getGreeting()
	{
		return "VCGR HTC Grid FTP Server";
	}

	public String pwd() throws FTPException
	{
		throw new UnauthenticatedException();
	}

	public void cwd(String path) throws FTPException
	{
		throw new UnauthenticatedException();
	}

	public long size(String path) throws FTPException
	{
		throw new UnauthenticatedException();
	}

	public ListEntry[] list() throws FTPException
	{
		throw new UnauthenticatedException();
	}

	public String mkdir(String newDir) throws FTPException
	{
		throw new UnauthenticatedException();
	}

	public void removeDirectory(String directory) throws FTPException
	{
		throw new UnauthenticatedException();
	}

	public IFTPFileSystem authenticate(String user, String password)
			throws FTPException
	{
		if (password == null)
			return null;
		return new VCGRHTCFileSystem(_context);
	}

	public void delete(String entry) throws FTPException
	{
		throw new UnauthenticatedException();
	}

	public void rename(String oldEntry, String newEntry) throws FTPException
	{
		throw new UnauthenticatedException();
	}

	public void retrieve(String entry, OutputStream out) throws FTPException
	{
		throw new UnauthenticatedException();
	}

	public void store(String entry, InputStream in) throws FTPException
	{
		throw new UnauthenticatedException();
	}

	public boolean exists(String entry) throws FTPException
	{
		throw new UnauthenticatedException();
	}
}
