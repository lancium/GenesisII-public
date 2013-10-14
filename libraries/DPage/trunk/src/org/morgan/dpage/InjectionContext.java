package org.morgan.dpage;

import java.io.Closeable;
import java.io.IOException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

class InjectionContext implements Closeable
{
	private HttpServletRequest _request;
	private ObjectInjectionHandler _handler;

	@Override
	protected void finalize() throws Throwable
	{

	}

	InjectionContext(HttpServletRequest request, ObjectInjectionHandler handler)
	{
		_request = request;
		_handler = handler;
	}

	final Cookie cookie(String cookieName)
	{
		for (Cookie cookie : _request.getCookies()) {
			if (cookie.getName().equals(cookieName))
				return cookie;
		}

		return null;
	}

	final String[] parameter(String parameterName)
	{
		return _request.getParameterValues(parameterName);
	}

	final ObjectInjectionHandler injectionHandler()
	{
		return _handler;
	}

	@Override
	synchronized public void close() throws IOException
	{
		try {
			if (_handler != null) {
				if (_handler instanceof Closeable)
					((Closeable) _handler).close();
			}
		} finally {
			_handler = null;
		}
	}
}