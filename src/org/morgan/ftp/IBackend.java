package org.morgan.ftp;

import java.io.InputStream;
import java.io.OutputStream;

public interface IBackend
{
	// password can be null.
	public boolean authenticate(String username, String password) throws FTPException;
	
	public String getGreeting();
	public String pwd() throws FTPException;
	public long size(String path) throws FTPException;
	public void cwd(String path) throws FTPException;
	
	public ListEntry[] list() throws FTPException;
	
	public String mkdir(String newDir) throws FTPException;
	public void removeDirectory(String directory) throws FTPException;
	
	public void delete(String entry) throws FTPException;
	
	public void rename(String oldEntry, String newEntry)
		throws FTPException;
	
	public InputStream retrieve(String entry) throws FTPException;
	public OutputStream store(String entry) throws FTPException;
	
	public boolean exists(String entry) throws FTPException;
}