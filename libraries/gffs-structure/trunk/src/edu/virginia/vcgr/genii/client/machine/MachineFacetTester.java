package edu.virginia.vcgr.genii.client.machine;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Date;

public class MachineFacetTester
{
	static public void main(String[] args) throws Throwable
	{
		if (args.length != 1) {
			System.err.println("USAGE:  MachineFacetTester <output-file>");
			System.exit(1);
		}

		MyMachineListener listener = new MyMachineListener(new File(args[0]));
		MachineFacetUpdater updater = new MachineFacetUpdater(1000 * 30, 1000 * 30);
		updater.addMachineListener(listener);
		updater.start();

		while (true) {
			Thread.sleep(1000 * 1000);
		}
	}

	static private class MyMachineListener implements MachineListener
	{
		private PrintStream _ps = null;

		public MyMachineListener(File outputFile) throws IOException
		{
			_ps = new PrintStream(outputFile);
		}

		@Override
		public void screenSaverActivated(boolean activated)
		{
			_ps.println("[" + new Date() + "] -- " + (activated ? "Screen Saver Activated" : "Screen Saver DeActivated"));
			_ps.flush();
		}

		@Override
		public void userLoggedIn(boolean loggedIn)
		{
			_ps.println("[" + new Date() + "] -- " + (loggedIn ? "User Logged In" : "User Logged Out"));
			_ps.flush();
		}
	}
}