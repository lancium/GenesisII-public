package edu.virginia.vcgr.xscript;

import javax.script.ScriptException;

import org.w3c.dom.Element;

public interface ParseHandler
{
	public ParseStatement parse(ParseContext context, Element element) throws ScriptException;
}