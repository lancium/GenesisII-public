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
	public abstract InputStream openInputStream(
		URI source, UsernamePasswordIdentity credential)
		throws IOException;
	public abstract OutputStream openOutputStream(
		URI target, UsernamePasswordIdentity credential)
		throws IOException;
	
	@Override
	public void get(URI source, File target, UsernamePasswordIdentity credential)
			throws IOException
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

	@Override
	public void put(File source, URI target, UsernamePasswordIdentity credential)
			throws IOException
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
