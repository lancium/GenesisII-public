package edu.virginia.vcgr.genii.client.logging;

import org.apache.log4j.MDC;
import org.apache.log4j.RollingFileAppender;
import org.apache.log4j.spi.LoggingEvent;

public class DLogRollingFileAppender extends RollingFileAppender
{
	@Override
	public void append(LoggingEvent event)
	{
		String rpcid = DLogUtils.getRPCID();
		if (rpcid != null) {
			MDC.put("RPCID", rpcid);
		} else {
			MDC.put("RPCID", "NULL RPCID");
		}

		super.append(event);
	}
}
