package edu.virginia.vcgr.xscript;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;

public class XScriptEngineFactory 
	implements ScriptEngineFactory, XScriptConstants
{	
	private List<String> _extensions;
	private List<String> _mimeTypes;
	private List<String> _shortNames;
	
	private Map<String, Object> _factoryParameters;
	
	public XScriptEngineFactory()
	{
		_extensions = new ArrayList<String>(
			FILE_EXTENSIONS.length);
		for (String extension : FILE_EXTENSIONS)
			_extensions.add(extension);
		
		_mimeTypes = new ArrayList<String>(
			MIME_TYPES.length);
		for (String mimeType : MIME_TYPES)
			_mimeTypes.add(mimeType);
		
		_shortNames = new ArrayList<String>(
			SHORT_NAMES.length);
		for (String shortName: SHORT_NAMES)
			_shortNames.add(shortName);
		
		_factoryParameters = new HashMap<String, Object>();
		_factoryParameters.put(ScriptEngine.ENGINE, ENGINE_NAME);
		_factoryParameters.put(ScriptEngine.ENGINE_VERSION, ENGINE_VERSION);
		_factoryParameters.put(ScriptEngine.NAME, _shortNames);
		_factoryParameters.put(ScriptEngine.LANGUAGE, LANGUAGE_NAME);
		_factoryParameters.put(ScriptEngine.LANGUAGE_VERSION, 
			LANGUAGE_VERSION);
	}
	
	@Override
	public String getEngineName()
	{
		return ENGINE_NAME;
	}

	@Override
	public String getEngineVersion()
	{
		return ENGINE_VERSION;
	}

	@Override
	public List<String> getExtensions()
	{
		return _extensions;
	}

	@Override
	public String getLanguageName()
	{
		return LANGUAGE_NAME;
	}

	@Override
	public String getLanguageVersion()
	{
		return LANGUAGE_VERSION;
	}

	@Override
	public String getMethodCallSyntax(String obj, String m, String... args)
	{
		StringBuilder builder = new StringBuilder();
		
		builder.append(String.format(
			"<gsh:call xmlns:gsh=\"%s\" function=\"%s\">\n", XSCRIPT_NS, m));
		for (String arg : args)
			builder.append(String.format(
				"\t<gsh:parameter>%s</gsh:parameter>\n", arg));
		builder.append("</gsh:call>");
		
		return builder.toString();
	}

	@Override
	public List<String> getMimeTypes()
	{
		return _mimeTypes;
	}

	@Override
	public List<String> getNames()
	{
		return _shortNames;
	}

	@Override
	public String getOutputStatement(String toDisplay)
	{
		return String.format("<gsh:echo xmlns:gsh=\"%s\" message=\"%s\"/>", 
			XSCRIPT_NS, toDisplay);
	}

	@Override
	public Object getParameter(String key)
	{
		return _factoryParameters.get(key);
	}

	@Override
	public String getProgram(String... statements)
	{
		StringBuilder builder = new StringBuilder();
		
		builder.append(String.format(
			"<gsh:script xmlns:gsh=\"%s\">\n", XSCRIPT_NS));
		
		for (String statement : statements)
			builder.append(statement);
		
		builder.append("\n</gsh:script>");
		
		return builder.toString();
	}

	@Override
	public ScriptEngine getScriptEngine()
	{
		return new XScriptEngine(this);
	}
}