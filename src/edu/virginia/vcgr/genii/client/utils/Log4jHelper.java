package edu.virginia.vcgr.genii.client.utils;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;

//////////////
// Name   : Log4jHelper
// Author : Chris Koeritz
// Rights : Copyright (c) 2012-$now By University of Virginia
//////////////
// This file is free software; you can modify/redistribute it under the terms
// of the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
// Feel free to send updates to: [ koeritz@virginia.edu ]
//////////////

public class Log4jHelper
{
	private static String log4jFileStatic = null;

	@SuppressWarnings("unchecked")
	static public List<String> queryAppenders()
	{
		ArrayList<String> ted = new ArrayList<String>();
		Enumeration<Appender> apps = Logger.getRootLogger().getAllAppenders();
		while (apps.hasMoreElements()) {
			Appender a = (Appender) apps.nextElement();
			ted.add(a.getName());
		}
		return ted;
	}

	// retrieves the file used for logging on the appender called "appenderName".
	// this only works if the appenderName exists, and if it is derived from FileAppender.
	static public String queryLog4jFile(String appenderName)
	{
		// re-use the answer from last time, if there was an answer.  the log file will
		// currently not change during runtime.
		if (log4jFileStatic == null) {
			Appender appender = Logger.getRootLogger().getAppender(appenderName);
			if (appender instanceof FileAppender)
				log4jFileStatic = ((FileAppender) appender).getFile();
		}
		return log4jFileStatic;
	}

}
