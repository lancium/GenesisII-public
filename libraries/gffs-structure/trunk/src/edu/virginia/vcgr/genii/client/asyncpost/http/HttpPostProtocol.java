package edu.virginia.vcgr.genii.client.asyncpost.http;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;

import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.client.asyncpost.AbstractPostProtocol;
import edu.virginia.vcgr.genii.client.asyncpost.PostProtocol;

public class HttpPostProtocol extends AbstractPostProtocol implements PostProtocol
{
	static private final String[] HANDLED_PROTOCOLS = new String[] { "http" };

	static private String convert(byte[] content) throws UnsupportedEncodingException
	{
		return String.format("%s=%s", URLEncoder.encode("content", "UTF-8"), URLEncoder.encode(new String(content), "UTF-8"));
	}

	public HttpPostProtocol()
	{
		super(HANDLED_PROTOCOLS);
	}

	@Override
	protected void doPost(URI target, byte[] content) throws Throwable
	{
		HttpURLConnection connection = null;
		OutputStream out = null;
		InputStream in = null;

		try {
			connection = (HttpURLConnection) (target.toURL().openConnection());
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setUseCaches(false);
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			connection.setRequestMethod("POST");
			connection.connect();
			out = null;
			in = null;

			out = connection.getOutputStream();
			out.write(convert(content).getBytes());
			out.flush();
			out.close();
			out = null;

			in = connection.getInputStream();
			while (in.read(content) > 0)
				;
		} finally {
			StreamUtils.close(out);
			StreamUtils.close(in);
			if (connection != null) {
				try {
					connection.disconnect();
				} catch (Throwable cause) {
				}
			}
		}
	}
}