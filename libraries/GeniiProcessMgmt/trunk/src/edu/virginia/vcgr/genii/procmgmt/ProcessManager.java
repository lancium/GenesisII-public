package edu.virginia.vcgr.genii.procmgmt;

import java.io.IOException;

public class ProcessManager {
	static {
		System.err.println("Loading library.");
		System.loadLibrary("GeniiProcessMgmt");
		System.err.println("Done loading library.");
	}

	native static public void kill(Process process) throws IOException;
}