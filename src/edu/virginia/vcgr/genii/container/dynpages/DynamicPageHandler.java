package edu.virginia.vcgr.genii.container.dynpages;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.io.StreamUtils;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.AbstractHandler;
import org.mortbay.jetty.handler.ContextHandler;

import edu.virginia.vcgr.genii.client.configuration.DeploymentName;
import edu.virginia.vcgr.genii.client.configuration.Installation;

public class DynamicPageHandler extends AbstractHandler
{
	static final private String DYNAMIC_PAGE_CONFIGURATION_FILENAME =
		"dynamic-pages.conf";
	
	static private Log _logger = LogFactory.getLog(DynamicPageHandler.class);
	
	private String _resourceBase;
	
	public DynamicPageHandler(String resourceBase)
	{
		_resourceBase = resourceBase;
	}
	
	@Override
	public void handle(String target, HttpServletRequest request,
		HttpServletResponse response, int dispatch) 
			throws IOException, ServletException
	{
		OutputStream out = null;
		String targetDirectory;
		String targetName;
		InjectionContext context = null;
		
		int lcv = target.lastIndexOf('/');
		targetDirectory = target.substring(0, lcv);
		targetName = target.substring(lcv + 1);
		
		if (targetDirectory.length() == 0)
			targetDirectory = "/";
		if (targetName.length() == 0)
			targetName = "index.html";
		
		try
		{
			out = response.getOutputStream();
			PrintStream ps = new PrintStream(out);
			
			PageMap map = new PageMap(null,
				_resourceBase + targetDirectory);

			DynamicPage page = map.loadPage(targetName);
			context = new InjectionContext(request);
			PageInjector.inject(page, context);
			page.generate(ps);
			
			ps.flush();
		}
		catch (FileNotFoundException fnfe)
		{
			response.sendError(HttpServletResponse.SC_NOT_FOUND, String.format(
				fnfe.getLocalizedMessage()));
		}
		catch (Throwable cause)
		{
			_logger.warn("Unable to generate page content.", cause);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
				cause.getLocalizedMessage());
		}
		finally
		{
			StreamUtils.close(context);
			StreamUtils.close(out);
		}
	}
	
	static public void addDynamicPages(Server server)
	{
		ContextHandler context;
		File confFile =
			Installation.getDeployment(new DeploymentName()).getConfigurationFile(
				DYNAMIC_PAGE_CONFIGURATION_FILENAME);
		if (!confFile.exists())
			return;
		
		FileReader fReader = null;
		try
		{
			fReader = new FileReader(confFile);
			BufferedReader reader = new BufferedReader(fReader);
			String line;
			while ( (line = reader.readLine()) != null)
			{
				int index = line.indexOf('#');
				if (index >= 0)
					line = line.substring(0, index);
				line = line.trim();
				index = line.indexOf('=');
				if (index < 0)
					throw new IOException(
						"Unable to parse dynamic page configuration file.");
				
				context = new ContextHandler(line.substring(0, index));
				server.addHandler(context);
				context.addHandler(new DynamicPageHandler(line.substring(
					index + 1)));
			}
		}
		catch (Throwable cause)
		{
			_logger.warn("Unable to load dynamic page handler.", cause);
		}
		finally
		{
			StreamUtils.close(fReader);
		}
	}
}