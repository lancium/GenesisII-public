package edu.virginia.vcgr.genii.client.utils;

import org.apache.log4j.Logger;

//////////////
// Name   : DetailedLogger
// Author : Chris Koeritz
// Rights : Copyright (c) 2012-$now By University of Virginia
//////////////
// This file is free software; you can modify/redistribute it under the terms
// of the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
// Feel free to send updates to: [ koeritz@virginia.edu ]
//////////////

// provides a logger object for lengthy detail, such as full stack traces of exceptions.
// the logger still supports all the different logging levels (debug, warn, etc), although
// the main log4j configuration dictates whether any of those are visible or not.
// note that the log entries will always specify DetailedLogger as the logging class, so it
// may be wise to include additional information about the true caller.
public class DetailedLogger
{
	private static final Logger _logger = Logger.getLogger(DetailedLogger.class);

	DetailedLogger()
	{
	}

	public Logger detailed()
	{
		return _logger;
	}
}
