package org.morgan.mac.gui.macwrap;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.log4j.Logger;

public class ApplicationEvent
{
	static private Logger _logger = Logger.getLogger(ApplicationEvent.class);

	static private Class<?> REAL_APPLICATION_EVENT_CLASS;
	static private Method GET_FILENAME_METHOD;
	static private Method IS_HANDLED_METHOD;
	static private Method SET_HANDLED_METHOD;

	static {
		try {
			REAL_APPLICATION_EVENT_CLASS = Class.forName("com.apple.eawt.ApplicationEvent");

			GET_FILENAME_METHOD = REAL_APPLICATION_EVENT_CLASS.getDeclaredMethod("getFilename");
			IS_HANDLED_METHOD = REAL_APPLICATION_EVENT_CLASS.getDeclaredMethod("isHandled");
			SET_HANDLED_METHOD = REAL_APPLICATION_EVENT_CLASS.getDeclaredMethod("setHandled", boolean.class);
		} catch (ClassNotFoundException e) {
			_logger.fatal("Unable to find Mac ApplicationEvent class.", e);
			System.exit(1);
		} catch (SecurityException e) {
			_logger.fatal("Unable to load Mac ApplicationEvent method.", e);
			System.exit(1);
		} catch (NoSuchMethodException e) {
			_logger.fatal("Unable to load Mac ApplicationEvent method.", e);
			System.exit(1);
		}
	}

	private Object _applicationEvent;

	ApplicationEvent(Object applicationEventObject)
	{
		_applicationEvent = applicationEventObject;
	}

	final public String getFilename()
	{
		try {
			return (String) GET_FILENAME_METHOD.invoke(_applicationEvent);
		} catch (IllegalArgumentException e) {
			_logger.fatal("Unable to call Mac getFilename method on ApplicationEvent.", e);
		} catch (IllegalAccessException e) {
			_logger.fatal("Unable to call Mac getFilename method on ApplicationEvent.", e);
		} catch (InvocationTargetException e) {
			_logger.fatal("Unable to call Mac getFilename method on ApplicationEvent.", e);
		}

		System.exit(1);
		return null;
	}

	final public boolean isHandled()
	{
		try {
			return (Boolean) IS_HANDLED_METHOD.invoke(_applicationEvent);
		} catch (IllegalArgumentException e) {
			_logger.fatal("Unable to call Mac isHandled method on ApplicationEvent.", e);
		} catch (IllegalAccessException e) {
			_logger.fatal("Unable to call Mac isHandled method on ApplicationEvent.", e);
		} catch (InvocationTargetException e) {
			_logger.fatal("Unable to call Mac isHandled method on ApplicationEvent.", e);
		}

		System.exit(1);
		return false;
	}

	final public void setHandled(boolean state)
	{
		try {
			SET_HANDLED_METHOD.invoke(_applicationEvent, state);
			return;
		} catch (IllegalArgumentException e) {
			_logger.fatal("Unable to call Mac setHandled method on ApplicationEvent.", e);
		} catch (IllegalAccessException e) {
			_logger.fatal("Unable to call Mac setHandled method on ApplicationEvent.", e);
		} catch (InvocationTargetException e) {
			_logger.fatal("Unable to call Mac setHandled method on ApplicationEvent.", e);
		}

		System.exit(1);
	}
}