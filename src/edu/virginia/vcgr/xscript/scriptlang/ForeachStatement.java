package edu.virginia.vcgr.xscript.scriptlang;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Pattern;

import javax.script.ScriptException;

import edu.virginia.vcgr.xscript.EarlyExitException;
import edu.virginia.vcgr.xscript.ParseStatement;
import edu.virginia.vcgr.xscript.ReturnFromFunctionException;
import edu.virginia.vcgr.xscript.XScriptContext;
import edu.virginia.vcgr.xscript.macros.MacroReplacer;

public class ForeachStatement implements ParseStatement
{
	private String _paramName;
	private String _filter;
	private String _sourceDir;
	private String _sourceFile;
	private String _sourceRNS;
	private ParseStatement _innerStatement;
	
	public ForeachStatement(String paramName, String filter,
		String sourceDir, String sourceFile, String sourceRNS,
		ParseStatement innerStatement)
	{
		_paramName = paramName;
		_filter = filter;
		_sourceDir = sourceDir;
		_sourceFile = sourceFile;
		_sourceRNS = sourceRNS;
		_innerStatement = innerStatement;
	}
	
	private Object evaluateSourceDir(XScriptContext context,
		String paramName, Pattern filter, String sourceDir) 
			throws ScriptException,	EarlyExitException, 
				ReturnFromFunctionException
	{
		File dir = new File(sourceDir);
		if (!dir.exists())
			throw new ScriptException(String.format(
				"Source directory \"%s\" does not exist.", sourceDir));
		if (!dir.isDirectory())
			throw new ScriptException(String.format(
				"Source directory \"%s\" is not a directory.", sourceDir));
		
		Object ret = null;
		
		for (String path : dir.list())
		{
			if (filter == null || filter.matcher(path).matches())
			{
				context.setAttribute(paramName, path);
				ret = _innerStatement.evaluate(context);
			}
		}
		
		return ret;
	}
	
	private Object evaluateSourceFile(XScriptContext context,
		String paramName, Pattern filter, String sourceFile) 
			throws ScriptException,	EarlyExitException, 
				ReturnFromFunctionException
	{
		String line;
		BufferedReader reader = null;
		
		try
		{
			Object ret = null;
			reader = new BufferedReader(new FileReader(sourceFile));
			while ( (line = reader.readLine()) != null)
			{
				if (filter == null || filter.matcher(line).matches())
				{
					context.setAttribute(paramName, line);
					ret = _innerStatement.evaluate(context);
				}
			}
			
			return ret;
		}
		catch (IOException ioe)
		{
			throw new ScriptException(ioe);
		}
		finally
		{
			if (reader != null)
				try { reader.close(); } catch (Throwable cause) {}
		}
	}
	
	private Object evaluateSourceRNS(XScriptContext context,
		String paramName, Pattern filter, String sourceRNS) 
			throws ScriptException,	EarlyExitException, 
				ReturnFromFunctionException
	{
		throw new ScriptException("Foreach of RNS space not implemented yet.");
	}

	@Override
	public Object evaluate(XScriptContext context) throws ScriptException,
			EarlyExitException, ReturnFromFunctionException
	{
		Pattern pattern = null;
		
		if (_filter != null)
			pattern = Pattern.compile(MacroReplacer.replaceMacros(
				context, _filter));
		String paramName = MacroReplacer.replaceMacros(context, _paramName);
		
		if (_sourceDir != null)
			return evaluateSourceDir(context, paramName, pattern,
				MacroReplacer.replaceMacros(context, _sourceDir));
		else if (_sourceFile != null)
			return evaluateSourceFile(context, paramName, pattern,
				MacroReplacer.replaceMacros(context, _sourceFile));
		else
			return evaluateSourceRNS(context, paramName, pattern,
				MacroReplacer.replaceMacros(context, _sourceRNS));
	}
}