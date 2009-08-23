package edu.virginia.vcgr.genii.container.dynpages;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.io.StreamUtils;
import org.mortbay.jetty.handler.AbstractHandler;

public class DynamicPageHandler extends AbstractHandler
{
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
}