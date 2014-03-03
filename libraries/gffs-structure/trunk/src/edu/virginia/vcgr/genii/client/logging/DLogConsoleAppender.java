package edu.virginia.vcgr.genii.client.logging;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.MDC;
import org.apache.log4j.spi.LoggingEvent;

public class DLogConsoleAppender extends ConsoleAppender {

	@Override
	public void append(LoggingEvent event) {
		String rpcid = DLogUtils.getRPCID();
		if (rpcid == null) {
			MDC.put("RPCID", "NULL ID");
		} else {
			MDC.put("RPCID", rpcid);
		}
		super.append(event);
	}

}
