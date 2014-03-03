package edu.virginia.vcgr.xscript;

import javax.script.ScriptException;

public interface ParseContext
{
	public ParseHandler findHandler(String namespace) throws ScriptException;
}