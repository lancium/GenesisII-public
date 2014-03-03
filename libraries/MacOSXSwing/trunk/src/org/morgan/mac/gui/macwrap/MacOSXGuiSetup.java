package org.morgan.mac.gui.macwrap;

public class MacOSXGuiSetup {
	static final public String ABOUT_NAME_PROPERTY = "com.apple.mrj.application.apple.menu.about.name";
	static final public String GROWBOX_INTRUDES_PROPERTY = "com.apple.mrj.application.growbox.intrudes";
	static final public String LIVE_RESIZE_PROPERTY = "com.apple.mrj.application.live-resize";
	static final public String SMALL_TABS_PROPERTY = "com.apple.macos.smallTabs";
	static final public String USE_SCREEN_MENU_BAR_PROPERTY = "apple.laf.useScreenMenuBar";

	static public boolean isMacOSX() {
		return System.getProperty("os.name").equalsIgnoreCase("Mac OS X");
	}

	static public void setupMacOSXGuiApplication(String aboutName,
			boolean useScreenMenuBar, boolean growboxIntrudes,
			boolean liveResize, boolean smallTabs) {
		setAboutName(aboutName);

		setUseScreenMenuBar(useScreenMenuBar);
		setGrowboxIntrudes(growboxIntrudes);
		setLiveResize(liveResize);
		setSmallTabs(smallTabs);
	}

	static public void setAboutName(String aboutName) {
		if (aboutName != null)
			System.setProperty(ABOUT_NAME_PROPERTY, aboutName);
		else
			System.getProperties().remove(ABOUT_NAME_PROPERTY);
	}

	static public void setGrowboxIntrudes(boolean growboxIntrudes) {
		System.setProperty(GROWBOX_INTRUDES_PROPERTY,
				Boolean.toString(growboxIntrudes));
	}

	static public void setLiveResize(boolean liveResize) {
		System.setProperty(LIVE_RESIZE_PROPERTY, Boolean.toString(liveResize));
	}

	static public void setSmallTabs(boolean smallTabs) {
		System.setProperty(SMALL_TABS_PROPERTY, Boolean.toString(smallTabs));
	}

	static public void setUseScreenMenuBar(boolean useScreenMenuBar) {
		System.setProperty(USE_SCREEN_MENU_BAR_PROPERTY,
				Boolean.toString(useScreenMenuBar));
	}
}