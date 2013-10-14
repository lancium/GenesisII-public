package edu.virginia.vcgr.genii.client.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Random;

import org.morgan.util.io.DataTransferStatistics;
import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.security.credentials.identity.UsernamePasswordIdentity;

public abstract class AbstractURIHandler implements IURIHandler
{
	static public final int NUM_RETRIES = 5;
	static public final long BACKOFF = 8000L;

	static private Random GENERATOR = new Random();

	public abstract InputStream openInputStream(URI source, UsernamePasswordIdentity credential) throws IOException;

	public abstract OutputStream openOutputStream(URI target, UsernamePasswordIdentity credential) throws IOException;

	static private long generateBackoff(int attempt)
	{
		long twitter = (long) ((GENERATOR.nextFloat() - 0.5) * (BACKOFF << attempt));
		return (BACKOFF << attempt) + twitter;
	}

	@Override
	final public DataTransferStatistics get(URI source, File target, UsernamePasswordIdentity credential) throws IOException
	{
		IOException lastException = null;

		int attempt = 0;

		for (attempt = 0; attempt < NUM_RETRIES; attempt++) {
			try {
				return getInternal(source, target, credential);
			} catch (FileNotFoundException fnfe) {
				lastException = fnfe;
				break;
			} catch (IOException ioe) {
				lastException = ioe;
			}

			try {
				Thread.sleep(generateBackoff(attempt));
			} catch (Throwable cause) {
			}
		}

		throw lastException;
	}

	@Override
	final public DataTransferStatistics put(File source, URI target, UsernamePasswordIdentity credential) throws IOException
	{
		IOException lastException = null;

		int attempt = 0;

		for (attempt = 0; attempt < NUM_RETRIES; attempt++) {
			try {
				return putInternal(source, target, credential);
			} catch (IOException ioe) {
				lastException = ioe;
			}

			try {
				Thread.sleep(generateBackoff(attempt));
			} catch (Throwable cause) {
			}
		}

		throw lastException;
	}

	protected DataTransferStatistics getInternal(URI source, File target, UsernamePasswordIdentity credential)
		throws IOException
	{
		FileOutputStream fos = null;
		InputStream in = null;

		try {
			fos = new FileOutputStream(target);
			in = openInputStream(source, credential);
			return StreamUtils.copyStream(in, fos);
		} finally {
			StreamUtils.close(fos);
			StreamUtils.close(in);
		}
	}

	protected DataTransferStatistics putInternal(File source, URI target, UsernamePasswordIdentity credential)
		throws IOException
	{
		FileInputStream fin = null;
		OutputStream out = null;

		try {
			fin = new FileInputStream(source);
			out = openOutputStream(target, credential);
			return StreamUtils.copyStream(fin, out);
		} finally {
			StreamUtils.close(fin);
			StreamUtils.close(out);
		}
	}
}
