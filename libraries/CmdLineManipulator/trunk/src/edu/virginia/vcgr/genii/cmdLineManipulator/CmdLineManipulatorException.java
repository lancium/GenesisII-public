package edu.virginia.vcgr.genii.cmdLineManipulator;

public class CmdLineManipulatorException extends Exception {
	static final long serialVersionUID = 0L;

	public CmdLineManipulatorException(String msg) {
		super(msg);
	}

	public CmdLineManipulatorException(String msg, Throwable cause) {
		super(msg, cause);
	}
}