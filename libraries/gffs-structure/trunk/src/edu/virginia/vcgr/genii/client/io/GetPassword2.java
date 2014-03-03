package edu.virginia.vcgr.genii.client.io;

import java.io.Console;

public class GetPassword2
{

	static public String getPassword(String prompt)
	{
		Console console = System.console();
		char[] password = null;
		if (console != null) {
			password = console.readPassword(prompt);
		}
		if (password != null) {
			return new String(password);
		}
		return null;
	}
}
