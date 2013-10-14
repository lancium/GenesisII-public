package edu.virginia.vcgr.xscript.scriptlang;

import javax.script.ScriptException;

import edu.virginia.vcgr.xscript.ParseStatement;
import edu.virginia.vcgr.xscript.XScriptContext;

public interface ConditionExpression extends ParseStatement
{
	public boolean evaluateCondition(XScriptContext context) throws ScriptException;
}