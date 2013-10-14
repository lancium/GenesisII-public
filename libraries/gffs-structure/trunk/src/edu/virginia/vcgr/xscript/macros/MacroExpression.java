package edu.virginia.vcgr.xscript.macros;

import javax.script.ScriptContext;

public interface MacroExpression
{
	public String toString(ScriptContext variables);
}