package edu.virginia.vcgr.xscript.scriptlang;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Pattern;

import javax.script.ScriptException;

import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathAlreadyExistsException;
import edu.virginia.vcgr.genii.client.rns.RNSPathDoesNotExistException;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;
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

	public ForeachStatement(String paramName, String filter, String sourceDir, String sourceFile, String sourceRNS,
		ParseStatement innerStatement)
	{
		_paramName = paramName;
		_filter = filter;
		_sourceDir = sourceDir;
		_sourceFile = sourceFile;
		_sourceRNS = sourceRNS;
		_innerStatement = innerStatement;
	}

	private Object evaluateSourceDir(XScriptContext context, String paramName, Pattern filter, String sourceDir)
		throws ScriptException, EarlyExitException, ReturnFromFunctionException
	{
		File dir = new File(sourceDir);
		if (!dir.exists())
			return null;
		if (!dir.isDirectory())
			return null;

		Object ret = null;

		for (File entry : dir.listFiles()) {
			if (filter == null || filter.matcher(entry.getName()).matches()) {
				context.setAttribute(paramName, entry.getName());
				ret = _innerStatement.evaluate(context);
			}
		}

		return ret;
	}

	private Object evaluateSourceFile(XScriptContext context, String paramName, Pattern filter, String sourceFile)
		throws ScriptException, EarlyExitException, ReturnFromFunctionException
	{
		String line;
		BufferedReader reader = null;

		try {
			Object ret = null;
			reader = new BufferedReader(new FileReader(sourceFile));
			while ((line = reader.readLine()) != null) {
				if (filter == null || filter.matcher(line).matches()) {
					context.setAttribute(paramName, line);
					ret = _innerStatement.evaluate(context);
				}
			}

			return ret;
		} catch (IOException ioe) {
			return null;
		} finally {
			if (reader != null)
				try {
					reader.close();
				} catch (Throwable cause) {
				}
		}
	}

	private Object evaluateSourceRNS(XScriptContext context, String paramName, Pattern filter, String sourceRNS)
		throws ScriptException, EarlyExitException, ReturnFromFunctionException
	{
		Object ret = null;

		RNSPath targetPath;
		try {
			targetPath = RNSPath.getCurrent().lookup(sourceRNS, RNSPathQueryFlags.MUST_EXIST);

			for (RNSPath entry : targetPath.listContents()) {
				if ((filter == null) || (filter.matcher(entry.getName()).matches())) {
					context.setAttribute(paramName, entry.getName());
					ret = _innerStatement.evaluate(context);
				}
			}
		} catch (RNSPathDoesNotExistException e) {
			return null;
		} catch (RNSPathAlreadyExistsException e) {
			throw new ScriptException(e);
		} catch (RNSException rne) {
			return null;
		}

		return ret;
	}

	@Override
	public Object evaluate(XScriptContext context) throws ScriptException, EarlyExitException, ReturnFromFunctionException
	{
		Pattern pattern = null;

		if (_filter != null)
			pattern = Pattern.compile(MacroReplacer.replaceMacros(context, _filter));
		String paramName = MacroReplacer.replaceMacros(context, _paramName);

		if (_sourceDir != null)
			return evaluateSourceDir(context, paramName, pattern, MacroReplacer.replaceMacros(context, _sourceDir));
		else if (_sourceFile != null)
			return evaluateSourceFile(context, paramName, pattern, MacroReplacer.replaceMacros(context, _sourceFile));
		else
			return evaluateSourceRNS(context, paramName, pattern, MacroReplacer.replaceMacros(context, _sourceRNS));
	}
}