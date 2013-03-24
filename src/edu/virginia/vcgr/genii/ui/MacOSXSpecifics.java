package edu.virginia.vcgr.genii.ui;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class MacOSXSpecifics
{
	static private Class<?> getApplicationListenerInterface() throws ClassNotFoundException
	{
		return Class.forName("com.apple.eawt.ApplicationListener");
	}

	static private Object getApplicationObject() throws ClassNotFoundException, SecurityException, NoSuchMethodException,
		IllegalArgumentException, IllegalAccessException, InvocationTargetException
	{
		Class<?> appClass = Class.forName("com.apple.eawt.Application");
		Method getterMethod = appClass.getMethod("getApplication");
		return getterMethod.invoke(null);
	}

	static private Object getApplicationAdapterObject() throws ClassNotFoundException, InstantiationException,
		IllegalAccessException
	{
		Class<?> appClass = Class.forName("com.apple.eawt.ApplicationAdapter");
		return appClass.newInstance();
	}

	static private void callVoidMethod(Object instance, String methodName, Class<?>[] types, Object[] parameters)
		throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException,
		InvocationTargetException
	{
		Class<?> cl = instance.getClass();
		Method m = cl.getDeclaredMethod(methodName, types);
		m.invoke(instance, parameters);
	}

	static public void setupMacOSApplication(ApplicationContext context)
	{
		try {
			Object application = getApplicationObject();

			callVoidMethod(application, "addAboutMenuItem", new Class<?>[0], new Object[0]);

			callVoidMethod(application, "addPreferencesMenuItem", new Class<?>[0], new Object[0]);
			callVoidMethod(application, "setEnabledAboutMenu", new Class<?>[] { boolean.class }, new Object[] { true });
			callVoidMethod(application, "setEnabledPreferencesMenu", new Class<?>[] { boolean.class }, new Object[] { true });

			callVoidMethod(application, "addApplicationListener", new Class<?>[] { getApplicationListenerInterface() },
				new Object[] { Proxy.newProxyInstance(MacOSXSpecifics.class.getClassLoader(),
					new Class<?>[] { getApplicationListenerInterface() }, new ApplicationListenerInvoker(context)) });
		} catch (RuntimeException re) {
			throw re;
		} catch (Throwable cause) {
			throw new RuntimeException("Internal Error -- we think we are Mac OS X, but we aren't.", cause);
		}
	}

	static private class ApplicationListenerInvoker implements InvocationHandler
	{
		private ApplicationContext _context;
		private Object _applicationAdapter;

		static private void setApplicationEventHandled(Object eventObject, boolean result) throws SecurityException,
			IllegalArgumentException, NoSuchMethodException, IllegalAccessException, InvocationTargetException
		{
			callVoidMethod(eventObject, "setHandled", new Class<?>[] { boolean.class }, new Object[] { result });
		}

		private ApplicationListenerInvoker(ApplicationContext context) throws ClassNotFoundException, InstantiationException,
			IllegalAccessException
		{
			_context = context;
			_applicationAdapter = getApplicationAdapterObject();
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
		{
			if (method.getName().equals("handleAbout")) {
				_context.fireApplicationAboutRequested();
				setApplicationEventHandled(args[0], true);
			} else if (method.getName().equals("handlePreferences")) {
				_context.fireApplicationPreferencesRequested();
				setApplicationEventHandled(args[0], true);
			} else if (method.getName().equals("handleQuit")) {
				setApplicationEventHandled(args[0], _context.fireApplicationQuitRequested());
			} else
				return method.invoke(_applicationAdapter, args);

			return null;
		}
	}
}