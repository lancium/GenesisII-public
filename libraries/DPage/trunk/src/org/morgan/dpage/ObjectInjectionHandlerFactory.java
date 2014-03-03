package org.morgan.dpage;

import javax.servlet.http.HttpServletRequest;

public interface ObjectInjectionHandlerFactory
{
	public ObjectInjectionHandler createHandler(String target, HttpServletRequest request) throws InjectionException;
}