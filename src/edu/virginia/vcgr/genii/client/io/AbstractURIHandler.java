package edu.virginia.vcgr.genii.client.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.client.security.gamlauthz.identity.UsernamePasswordIdentity;

public abstract class AbstractURIHandler implements IURIHandler
{
	static public final int NUM_RETRIES = 5;
	static public final long BACKOFF = 8000L;
	
	public abstract InputStream openInputStream(
		URI source, UsernamePasswordIdentity credential)
		throws IOException;
	public abstract OutputStream openOutputStream(
		URI target, UsernamePasswordIdentity credential)
		throws IOException;

	@Override
	final public void get(URI source, File target, 
		UsernamePasswordIdentity credential) throws IOException
	{
		IOException lastException = null;
		
		int attempt = 0;
		
		for (attempt = 0; attempt < NUM_RETRIES; attempt++)
		{
			try
			{
				getInternal(source, target, credential);
				
				return;
			}
			catch (IOException ioe)
			{
				lastException = ioe;
			}
			
			try { Thread.sleep(BACKOFF << attempt); } 
				catch (Throwable cause) {}
		}
		
		throw lastException;
	}
	
	@Override
	final public void put(File source, URI target, 
		UsernamePasswordIdentity credential) throws IOException
	{
		IOException lastException = null;
		
		int attempt = 0;
		
		for (attempt = 0; attempt < NUM_RETRIES; attempt++)
		{
			try
			{
				putInternal(source, target, credential);
				
				return;
			}
			catch (IOException ioe)
			{
				lastException = ioe;
			}
			
			try { Thread.sleep(BACKOFF << attempt); } 
				catch (Throwable cause) {}
		}
		
		throw lastException;
	}
	
	protected void getInternal(URI source, File target, 
		UsernamePasswordIdentity credential) throws IOException
	{
		FileOutputStream fos = null;
		InputStream in = null;
		
		try
		{
			fos = new FileOutputStream(target);
			in = openInputStream(source, credential);
			StreamUtils.copyStream(in, fos);
		}
		finally
		{
			StreamUtils.close(fos);
			StreamUtils.close(in);
		}
	}

	protected void putInternal(File source, URI target,
		UsernamePasswordIdentity credential) throws IOException
	{
		FileInputStream fin = null;
		OutputStream out = null;
		
		try
		{
			fin = new FileInputStream(source);
			out = openOutputStream(target, credential);
			StreamUtils.copyStream(fin, out);
		}
		finally
		{
			StreamUtils.close(fin);
			StreamUtils.close(out);
		}
	}
}
