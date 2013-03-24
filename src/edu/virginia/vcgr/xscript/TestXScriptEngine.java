package edu.virginia.vcgr.xscript;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

public class TestXScriptEngine
{
	static public void main(String[] args) throws Throwable
	{
		if (args.length != 1) {
			System.err.println("USAGE:  TestXScriptEngine <input-file>");
			System.exit(1);
		}

		String extension = "";
		File script = new File(args[0]);
		int index = args[0].lastIndexOf('.');
		if (index > 0)
			extension = args[0].substring(index + 1);

		ScriptEngineManager manager = new ScriptEngineManager();
		ScriptEngine engine = manager.getEngineByExtension(extension);

		Reader reader = null;
		try {
			reader = new FileReader(script);
			Object result = engine.eval(reader);
			System.err.println("Result:  " + result);
		} finally {
			reader.close();
		}
	}
}