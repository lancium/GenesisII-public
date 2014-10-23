package edu.virginia.vcgr.genii.client.utils;

import org.apache.log4j.Logger;

// ////////////
// Name : StatsLogger
// Author : Chris Koeritz
// Rights : Copyright (c) 2014-$now By University of Virginia
// ////////////
// This file is free software; you can modify/redistribute it under the terms
// of the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
// Feel free to send updates to: [ koeritz@virginia.edu ]
// ////////////

/**
 * provides a logger object that will be used to record statistics about runtime activity that can
 * help in providing usage metrics for the GFFS.
 */
public class StatsLogger
{
	private static final Logger _logger = Logger.getLogger(StatsLogger.class);

	static public Logger stats()
	{
		return _logger;
	}

	static public void logStats(String toLog)
	{
		_logger.info(toLog);
	}
}
