package edu.virginia.vcgr.genii.client.utils;

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
	private static final Logger _logger = Logger.getLogger(Log4jHelper.class);
	
	// retrieves the file used for logging on the appender called "appenderName".
	// this only works if the appenderName exists, and if it is derived from FileAppender.
	static public String queryLog4jFile(String appenderName)
	{
		Appender appender = _logger.getAppender(appenderName);
		if (appender instanceof FileAppender)
			return ((FileAppender) appender).getFile();
		else
			return null;
	}

}
