package org.morgan.ftp;

import java.io.PrintStream;

import org.apache.log4j.Logger;

public class FTPException extends Exception
{
	static final long serialVersionUID = 0L;
	
	private String []_ftpLines;
	
	public FTPException(String []ftpLines)
	{
		_ftpLines = ftpLines;
	}
	
	public FTPException(String line)
	{
		this (new String [] { line });
	}
	
	public FTPException(int number, String line)
	{
		this(String.format("%1$d %2$s", number, line));
	}
	
	public void communicate(PrintStream ps)
	{
		for (String line : _ftpLines)
		{
			ps.println(line);
		}
		
		ps.flush();
	}
	
	public void communicate(Logger logger)
	{
		StringBuilder builder = new StringBuilder();
		
		builder.append("FTP Exception:\n");
		for (String line : _ftpLines)
		{
			builder.append("\t" + line);
		}
		
		logger.warn(builder.toString());
	}
}