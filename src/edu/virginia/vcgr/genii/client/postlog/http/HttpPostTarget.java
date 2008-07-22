package edu.virginia.vcgr.genii.client.postlog.http;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.client.postlog.PostEvent;
import edu.virginia.vcgr.genii.client.postlog.PostTarget;

public class HttpPostTarget implements PostTarget
{
	static private Log _logger = LogFactory.getLog(HttpPostTarget.class);
	
	static private final String URL_PROPERTY =
		"edu.virginia.vcgr.genii.client.postlog.http-post-target.url";
	
	static private final int MAXIMUM_THREADS = 8;
	static private final long DEFAULT_THREAD_TIMEOUT_MS = 1000L * 60;
	
	static private ThreadPoolExecutor _executor =
		new ThreadPoolExecutor(0, MAXIMUM_THREADS, 
			DEFAULT_THREAD_TIMEOUT_MS, TimeUnit.MILLISECONDS, 
			new LinkedBlockingQueue<Runnable>());
	
	private URL _url;
	
	static private String createContent(
		PostEvent event)
	{
		StringBuilder builder = new StringBuilder();
		boolean seenFirst = false;
		
		for (Map.Entry<String, String> entry : event.content().entrySet())
		{
			if (seenFirst)
				builder.append("&");
			
			try
			{
				String value = entry.getValue();
				if (value != null)
				{
					builder.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
					builder.append('=');
					builder.append(URLEncoder.encode(value, "UTF-8"));
					seenFirst = true;
				}
			}
			catch (UnsupportedEncodingException uee)
			{
				_logger.warn("Got an exception that really shouldn't happen.", 
					uee);
			}
		}
		
		return builder.toString();
	}
	
	static private HttpURLConnection setupConnection(URL url) throws IOException
	{
		HttpURLConnection conn = (HttpURLConnection)url.openConnection();
		conn.setDoOutput(true);
		conn.setDoInput(true);
		conn.setUseCaches(false);
		conn.setRequestProperty("Content-Type", 
			"application/x-www-form-urlencoded");
		
		return conn;
	}
	
	public HttpPostTarget(URL url)
	{
		_url = url;
	}
	
	public HttpPostTarget(String postURL)
		throws MalformedURLException
	{
		if (postURL == null)
			_url = null;
		else
			_url = new URL(postURL);
	}
	
	public HttpPostTarget(Properties postProperties)
		throws MalformedURLException
	{
		this(postProperties.getProperty(URL_PROPERTY));
	}
	
	@Override
	public void post(PostEvent event)
	{
		if (_url == null)
			return;
		
		_executor.execute(new Poster(event));
	}
	
	private class Poster implements Runnable
	{
		private PostEvent _event;
		
		public Poster(PostEvent event)
		{
			_event = event;
		}
		
		@Override
		public void run()
		{
			OutputStream out = null;
			InputStream in = null;
			HttpURLConnection connection = null;
			
			try
			{
				String content = createContent(_event);
				connection = setupConnection(_url);
				
				out = connection.getOutputStream();
				DataOutputStream dos = new DataOutputStream(out);
				dos.writeBytes(content);
				dos.flush();
				dos.close();
				out = null;
				
				in = connection.getInputStream();
				byte []data = new byte[1024 * 8];
				while ( (in.read(data)) > 0 );
			}
			catch (Throwable t)
			{
				_logger.warn("Unable to log information to web server.",
					t);
			}
			finally
			{
				StreamUtils.close(out);
				StreamUtils.close(in);
				
				if (connection != null)
				{
					try
					{
						connection.disconnect();
					}
					catch (Throwable cause)
					{
						// do nothing.
					}
				}
			}
		}
	}
}