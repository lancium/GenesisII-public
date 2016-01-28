package org.morgan.dpage;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerList;

public class DynamicPageLoader
{

	// hmmm: this code is remembered as non-working...
	// hmmm: !!! it has been re-done more rationally now, but still is a good thing to look at if things fail to run properly.

	static public void addHandler(Server jettyServer, AbstractHandler newHandler)
	{
		HandlerList newList = new HandlerList();

		// hmmm: do we want new handler to go in first? probably?
		newList.addHandler(newHandler);

		Handler[] handlers = jettyServer.getHandlers();
		for (Handler h : handlers) {
			newList.addHandler(h);
		}
		jettyServer.setHandler(newList);
	}

	static public void addHandler(ContextHandler contexty, AbstractHandler newHandler)
	{
		HandlerList newList = new HandlerList();

		// hmmm: do we want new handler to go in first? probably?
		newList.addHandler(newHandler);

		Handler[] handlers = contexty.getHandlers();
		for (Handler h : handlers) {
			newList.addHandler(h);
		}
		contexty.setHandler(newList);
	}

	static public void addDynamicPages(Server jettyServer, ScratchSpaceManager scratchManager, File sourceDPagePackage) throws IOException
	{
		DynamicPagePackage dpagePackage = new DynamicPagePackage(scratchManager, sourceDPagePackage);

		Map<String, PageContextDescription> pageDescriptions = dpagePackage.pageContexts();
		for (String context : pageDescriptions.keySet()) {
			PageContextDescription pageDescription = pageDescriptions.get(context);

			ContextHandler cHandler = new ContextHandler(context);
			addHandler(cHandler, new DynamicPageContext(dpagePackage.classLoader(), pageDescription.resourceBase(),
				pageDescription.injectionHandlerFactory()));
			addHandler(jettyServer, cHandler);
		}
		dpagePackage.close();
	}
}
