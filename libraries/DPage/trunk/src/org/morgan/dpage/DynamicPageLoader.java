package org.morgan.dpage;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.ContextHandler;

public class DynamicPageLoader {
	/*
	 * for newer jetty. static public void addHandler(Server jettyServer,
	 * AbstractHandler newHandler) { ArrayList<Handler> handlerList = new
	 * ArrayList<Handler>();
	 * handlerList.addAll(Arrays.asList(jettyServer.getHandlers()));
	 * handlerList.add(0, newHandler); HandlerList newHandlers = new
	 * HandlerList(); newHandlers.setHandlers((Handler[])handlerList.toArray());
	 * jettyServer.setHandler(newHandlers); }
	 * 
	 * static public void addHandler(ContextHandler contexty, AbstractHandler
	 * newHandler) { ArrayList<Handler> handlerList = new ArrayList<Handler>();
	 * handlerList.addAll(Arrays.asList(contexty.getHandlers()));
	 * handlerList.add(0, newHandler); HandlerList newHandlers = new
	 * HandlerList(); newHandlers.setHandlers((Handler[])handlerList.toArray());
	 * contexty.setHandler(newHandlers); }
	 */

	static public void addDynamicPages(Server jettyServer,
			ScratchSpaceManager scratchManager, File sourceDPagePackage)
			throws IOException {
		DynamicPagePackage dpagePackage = new DynamicPagePackage(
				scratchManager, sourceDPagePackage);

		Map<String, PageContextDescription> pageDescriptions = dpagePackage
				.pageContexts();
		for (String context : pageDescriptions.keySet()) {
			PageContextDescription pageDescription = pageDescriptions
					.get(context);

			ContextHandler cHandler = new ContextHandler(context);
			cHandler.addHandler(new DynamicPageContext(dpagePackage
					.classLoader(), pageDescription.resourceBase(),
					pageDescription.injectionHandlerFactory()));
			jettyServer.addHandler(cHandler);
		}
		dpagePackage.close();
	}
}