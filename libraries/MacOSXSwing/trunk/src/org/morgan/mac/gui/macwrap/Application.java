package org.morgan.mac.gui.macwrap;

import java.awt.Image;
import java.awt.PopupMenu;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.log4j.Logger;

public class Application {
	static private Logger _logger = Logger.getLogger(Application.class);

	static private Class<?> REAL_APPLICATION_CLASS;

	static private Method ADD_ABOUT_MENU_ITEM;
	static private Method ADD_APPLICATION_LISTENER;
	static private Method ADD_PREFERENCES_MENU_ITEM;
	static private Method GET_APPLICATION;
	static private Method GET_DOCK_ICON_IMAGE;
	static private Method GET_DOCK_MENU;
	static private Method GET_ENABLED_ABOUT_MENU;
	static private Method GET_ENABLED_PREFERENCES_MENU;
	static private Method IS_ABOUT_MENU_ITEM_PRESENT;
	static private Method IS_PREFERENCES_MENU_ITEM_PRESENT;
	static private Method OPEN_HELP_VIEWER;
	static private Method REMOVE_ABOUT_MENU_ITEM;
	static private Method REMOVE_APPLICATION_LISTENER;
	static private Method REMOVE_PREFERENCES_MENU_ITEM;
	static private Method SET_DOCK_ICON_BADGE;
	static private Method SET_DOCK_ICON_IMAGE;
	static private Method SET_DOCK_MENU;
	static private Method SET_ENABLED_ABOUT_MENU;
	static private Method SET_ENABLED_PREFERENCES_MENU;

	static {
		try {
			REAL_APPLICATION_CLASS = Class
					.forName("com.apple.eawt.Application");

			ADD_ABOUT_MENU_ITEM = REAL_APPLICATION_CLASS
					.getDeclaredMethod("addAboutMenuItem");
			ADD_APPLICATION_LISTENER = REAL_APPLICATION_CLASS
					.getDeclaredMethod(
							"addApplicationListener",
							ApplicationListenerWrapper.REAL_APPLICATION_LISTENER_CLASS);
			ADD_PREFERENCES_MENU_ITEM = REAL_APPLICATION_CLASS
					.getDeclaredMethod("addPreferencesMenuItem");
			GET_APPLICATION = REAL_APPLICATION_CLASS
					.getDeclaredMethod("getApplication");
			GET_DOCK_ICON_IMAGE = REAL_APPLICATION_CLASS
					.getDeclaredMethod("getDockIconImage");
			GET_DOCK_MENU = REAL_APPLICATION_CLASS
					.getDeclaredMethod("getDockMenu");
			GET_ENABLED_ABOUT_MENU = REAL_APPLICATION_CLASS
					.getDeclaredMethod("getEnabledAboutMenu");
			GET_ENABLED_PREFERENCES_MENU = REAL_APPLICATION_CLASS
					.getDeclaredMethod("getEnabledPreferencesMenu");
			IS_ABOUT_MENU_ITEM_PRESENT = REAL_APPLICATION_CLASS
					.getDeclaredMethod("isAboutMenuItemPresent");
			IS_PREFERENCES_MENU_ITEM_PRESENT = REAL_APPLICATION_CLASS
					.getDeclaredMethod("isPreferencesMenuItemPresent");
			OPEN_HELP_VIEWER = REAL_APPLICATION_CLASS
					.getDeclaredMethod("openHelpViewer");
			REMOVE_ABOUT_MENU_ITEM = REAL_APPLICATION_CLASS
					.getDeclaredMethod("removeAboutMenuItem");
			REMOVE_APPLICATION_LISTENER = REAL_APPLICATION_CLASS
					.getDeclaredMethod(
							"removeApplicationListener",
							ApplicationListenerWrapper.REAL_APPLICATION_LISTENER_CLASS);
			SET_DOCK_ICON_BADGE = REAL_APPLICATION_CLASS.getDeclaredMethod(
					"setDockIconBadge", String.class);
			SET_DOCK_ICON_IMAGE = REAL_APPLICATION_CLASS.getDeclaredMethod(
					"setDockIconImage", Image.class);
			SET_DOCK_MENU = REAL_APPLICATION_CLASS.getDeclaredMethod(
					"setDockMenu", PopupMenu.class);
			SET_ENABLED_ABOUT_MENU = REAL_APPLICATION_CLASS.getDeclaredMethod(
					"setEnabledAboutMenu", boolean.class);
			SET_ENABLED_PREFERENCES_MENU = REAL_APPLICATION_CLASS
					.getDeclaredMethod("setEnabledPreferencesMenu",
							boolean.class);
		} catch (ClassNotFoundException e) {
			_logger.fatal("Unable to load Mac Application class.", e);
			System.exit(1);
		} catch (SecurityException e) {
			_logger.fatal("Unable to load method for Mac Application class.", e);
			System.exit(1);
		} catch (NoSuchMethodException e) {
			_logger.fatal("Unable to load method for Mac Application class.", e);
			System.exit(1);
		}
	}

	static private Object doExceptionlessInvoke(Method method, Object instance,
			Object... args) {
		try {
			return method.invoke(instance, args);
		} catch (InvocationTargetException ite) {
			Throwable cause = ite.getCause();
			if (cause instanceof RuntimeException)
				throw (RuntimeException) cause;

			throw new RuntimeException("Unable to call method.", cause);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException("Unable to call method.", e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Unable to call method.", e);
		}
	}

	static public Application getApplication() {
		return new Application(doExceptionlessInvoke(GET_APPLICATION, null));
	}

	private Object _applicationObject;

	private Application(Object applicationObject) {
		_applicationObject = applicationObject;
	}

	public void addAboutMenuItem() {
		doExceptionlessInvoke(ADD_ABOUT_MENU_ITEM, _applicationObject);
	}

	public void addApplicationListener(ApplicationListener listener) {
		doExceptionlessInvoke(ADD_APPLICATION_LISTENER, _applicationObject,
				ApplicationListenerWrapper.wrapApplicationListener(listener));
	}

	public void addPreferencesMenuItem() {
		doExceptionlessInvoke(ADD_PREFERENCES_MENU_ITEM, _applicationObject);
	}

	public Image getDockIconImage() {
		return (Image) doExceptionlessInvoke(GET_DOCK_ICON_IMAGE,
				_applicationObject);
	}

	public PopupMenu getDockMenu() {
		return (PopupMenu) doExceptionlessInvoke(GET_DOCK_MENU,
				_applicationObject);
	}

	public boolean getEnabledAboutMenu() {
		return (Boolean) doExceptionlessInvoke(GET_ENABLED_ABOUT_MENU,
				_applicationObject);
	}

	public boolean getEnabledPreferencesMenu() {
		return (Boolean) doExceptionlessInvoke(GET_ENABLED_PREFERENCES_MENU,
				_applicationObject);
	}

	public boolean isAboutMenuItemPresent() {
		return (Boolean) doExceptionlessInvoke(IS_ABOUT_MENU_ITEM_PRESENT,
				_applicationObject);
	}

	public boolean isPreferencesMenuItemPresent() {
		return (Boolean) doExceptionlessInvoke(
				IS_PREFERENCES_MENU_ITEM_PRESENT, _applicationObject);
	}

	public void openHelpViewer() {
		doExceptionlessInvoke(OPEN_HELP_VIEWER, _applicationObject);
	}

	public void removeAboutMenuItem() {
		doExceptionlessInvoke(REMOVE_ABOUT_MENU_ITEM, _applicationObject);
	}

	public void removeApplicationListener(ApplicationListener listener) {
		doExceptionlessInvoke(REMOVE_APPLICATION_LISTENER, _applicationObject,
				ApplicationListenerWrapper.wrapApplicationListener(listener));
	}

	public void removePreferencesMenuItem() {
		doExceptionlessInvoke(REMOVE_PREFERENCES_MENU_ITEM, _applicationObject);
	}

	public void setDockIconBadge(String badge) {
		doExceptionlessInvoke(SET_DOCK_ICON_BADGE, _applicationObject, badge);
	}

	public void setDockIconImage(Image image) {
		doExceptionlessInvoke(SET_DOCK_ICON_IMAGE, _applicationObject, image);
	}

	public void setDockMenu(PopupMenu menu) {
		doExceptionlessInvoke(SET_DOCK_MENU, _applicationObject, menu);
	}

	public void setEnabledAboutMenu(boolean enable) {
		doExceptionlessInvoke(SET_ENABLED_ABOUT_MENU, _applicationObject,
				new Boolean(enable));
	}

	public void setEnabledPreferencesMenu(boolean enable) {
		doExceptionlessInvoke(SET_ENABLED_PREFERENCES_MENU, _applicationObject,
				new Boolean(enable));
	}
}