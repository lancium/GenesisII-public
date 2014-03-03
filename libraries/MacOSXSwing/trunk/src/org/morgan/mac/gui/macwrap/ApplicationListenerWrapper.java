package org.morgan.mac.gui.macwrap;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.apache.log4j.Logger;

class ApplicationListenerWrapper implements InvocationHandler
{
	static private Logger _logger = Logger.getLogger(ApplicationListenerWrapper.class);

	static Class<?> REAL_APPLICATION_LISTENER_CLASS;

	static {
		try {
			REAL_APPLICATION_LISTENER_CLASS = Class.forName("com.apple.eawt.ApplicationListener");
		} catch (ClassNotFoundException e) {
			_logger.fatal("Unable to load ApplicationListener class.", e);
			System.exit(1);
		}
	}
	private ApplicationListener _newListener;

	private ApplicationListenerWrapper(ApplicationListener newListener)
	{
		_newListener = newListener;
	}

	@Override
	final public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
	{
		ApplicationEvent event = new ApplicationEvent(args[0]);

		if (method.getName().equals("handleAbout"))
			_newListener.handleAbout(event);
		else if (method.getName().equals("handleOpenApplication"))
			_newListener.handleOpenApplication(event);
		else if (method.getName().equals("handleOpenFile"))
			_newListener.handleOpenFile(event);
		else if (method.getName().equals("handlePreferences"))
			_newListener.handlePreferences(event);
		else if (method.getName().equals("handlePrintFile"))
			_newListener.handlePrintFile(event);
		else if (method.getName().equals("handleQuit"))
			_newListener.handleQuit(event);
		else if (method.getName().equals("handleReOpenApplication"))
			_newListener.handleReOpenApplication(event);
		else if (method.getName().equals("equals"))
			return _newListener.equals(((ApplicationListenerWrapper) Proxy.getInvocationHandler(args[0]))._newListener);

		return null;
	}

	static Object wrapApplicationListener(ApplicationListener listener)
	{
		return Proxy.newProxyInstance(null, new Class<?>[] { REAL_APPLICATION_LISTENER_CLASS }, new ApplicationListenerWrapper(
			listener));
	}
}