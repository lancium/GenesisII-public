package org.morgan.dpage;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.mortbay.jetty.handler.AbstractHandler;

class DynamicPageContext extends AbstractHandler
{
	static private Logger _logger = Logger.getLogger(DynamicPageContext.class);

	private ObjectInjectionHandlerFactory _injectionHandlerFactory;
	private ClassLoader _loader;
	private String _resourceBase;

	DynamicPageContext(ClassLoader loader, String resourceBase, ObjectInjectionHandlerFactory injectionHandlerFactory)
	{
		_loader = loader;
		_resourceBase = resourceBase;
		_injectionHandlerFactory = injectionHandlerFactory;
	}

	@Override
	public void handle(String target, HttpServletRequest request, HttpServletResponse response, int dispatch)
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

		try {
			out = response.getOutputStream();
			PrintStream ps = new PrintStream(out);

			PageMap map = new PageMap(_loader, _resourceBase + targetDirectory);

			DynamicPage page = map.loadPage(targetName);
			context =
				new InjectionContext(request, _injectionHandlerFactory.createHandler(
					String.format("%s%s%s", targetDirectory, targetDirectory.endsWith("/") ? "" : "/", targetName), request));
			Injector.injectValues(page, context);
			page.generatePage(ps);

			ps.flush();
		} catch (FileNotFoundException fnfe) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND, String.format(fnfe.getLocalizedMessage()));
			_logger.warn("Unable to render page.", fnfe);
		} catch (Throwable cause) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, cause.getLocalizedMessage());

			_logger.warn("Unable to render page.", cause);
		} finally {
			StreamUtils.close(context);
			StreamUtils.close(out);
		}
	}
}