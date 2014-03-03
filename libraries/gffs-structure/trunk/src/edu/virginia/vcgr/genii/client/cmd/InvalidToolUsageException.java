package edu.virginia.vcgr.genii.client.cmd;

import edu.virginia.vcgr.genii.algorithm.application.ProgramTools;

public class InvalidToolUsageException extends ToolException {
	static final long serialVersionUID = 0L;

	static final private String _INVALID_USAGE_MSG = "Invalid tool usage.";

	public InvalidToolUsageException() {
		super(_INVALID_USAGE_MSG + "...  caller was "
				+ ProgramTools.getStackFrame(1));
	}

	public InvalidToolUsageException(String additionalMessage) {
		super(_INVALID_USAGE_MSG + ": " + additionalMessage
				+ "...  caller was " + ProgramTools.getStackFrame(1));
	}
}