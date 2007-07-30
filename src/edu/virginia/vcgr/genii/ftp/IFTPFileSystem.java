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

public interface IFTPFileSystem
{
	public String getGreeting();
	public String pwd() throws FTPException;
	public long size(String path) throws FTPException;
	public void cwd(String path) throws FTPException;
	public ListEntry[] list() throws FTPException;
	public String mkdir(String newDir) throws FTPException;
	public void removeDirectory(String directory) throws FTPException;
	
	// Password can be null
	public IFTPFileSystem authenticate(String user, String password) 
		throws FTPException;
	
	public void delete(String entry) throws FTPException;
	public void rename(String oldEntry, String newEntry) 
		throws FTPException;
	
	public void retrieve(String entry, OutputStream out)
		throws FTPException;
	public void store(String entry, InputStream in) throws FTPException;
	
	public boolean exists(String entry) throws FTPException;
}
