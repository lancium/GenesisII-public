package org.morgan.ftp;

import java.io.OutputStream;
import java.io.PrintStream;

public class FTPPrintStream extends PrintStream
{
	public FTPPrintStream(OutputStream out, boolean autoFlush)
	{
		super(out, autoFlush);
	}
	
	public void println()
	{
		print("\r\n");
	}
	
	public void println(boolean x)
	{
		print(x);
		print("\r\n");
	}
	
	public void println(char x)
	{
		print(x);
		print("\r\n");
	}
	
	public void println(char[] x)
	{
		print(x);
		print("\r\n");
	}
	
	public void println(double x)
	{
		print(x);
		print("\r\n");
	}
	
	public void println(float x)
	{
		print(x);
		print("\r\n");
	}
	
	public void println(int x)
	{
		print(x);
		print("\r\n");
	}
	
	public void println(long x)
	{
		print(x);
		print("\r\n");
	}
	
	public void println(Object x)
	{
		print(x);
		print("\r\n");
	}
	
	public void println(String x)
	{
		print(x);
		print("\r\n");
	}
}
