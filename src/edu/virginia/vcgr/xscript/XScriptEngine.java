package edu.virginia.vcgr.xscript;

import java.io.IOException;
import java.io.Reader;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

public class XScriptEngine 
	implements ScriptEngine, Compilable
{
	private XScriptEngineFactory _factory;
	private XScriptContext _context;
	
	XScriptEngine(XScriptEngineFactory factory)
	{
		_factory = factory;
		_context = new XScriptContext();
	}

	@Override
	public Bindings createBindings()
	{
		return new SimpleBindings();
	}

	@Override
	public Object eval(String script) throws ScriptException
	{
		return eval(script, _context);
	}

	@Override
	public Object eval(Reader reader) throws ScriptException
	{
		return eval(reader, _context);
	}

	@Override
	public Object eval(String script, ScriptContext context)
			throws ScriptException
	{
		CompiledScript cScript = compile(script);
		return cScript.eval(context);
	}

	@Override
	public Object eval(Reader reader, ScriptContext context)
			throws ScriptException
	{
		CompiledScript cScript = compile(reader);
		return cScript.eval(context);
	}

	@Override
	public Object eval(String script, Bindings n) throws ScriptException
	{
		XScriptContext ctxt = getScriptContext(n);
		return eval(script, ctxt);
	}

	@Override
	public Object eval(Reader reader, Bindings n) throws ScriptException
	{
		XScriptContext ctxt = getScriptContext(n);
		return eval(reader, ctxt);
	}

	@Override
	public Object get(String key)
	{
		return getBindings(ScriptContext.ENGINE_SCOPE).get(key);
	}

	@Override
	public Bindings getBindings(int scope)
	{
		return _context.getBindings(scope);
	}

	@Override
	public ScriptContext getContext()
	{
		return _context;
	}

	@Override
	public ScriptEngineFactory getFactory()
	{
		return _factory;
	}

	@Override
	public void put(String key, Object value)
	{
		getBindings(ScriptContext.ENGINE_SCOPE).put(key, value);
	}

	@Override
	public void setBindings(Bindings bindings, int scope)
	{
		_context.setBindings(bindings, scope);
	}

	@Override
	public void setContext(ScriptContext context)
	{
		_context = (XScriptContext)context;
	}

	@Override
	public CompiledScript compile(String script) throws ScriptException
	{
		try
		{
			return XScriptParser.parse(this, script);
		}
		catch (IOException ioe)
		{
			throw new ScriptException(ioe);
		} 
		catch (ParserConfigurationException e)
		{
			throw new ScriptException(e);
		} catch (SAXException e)
		{
			throw new ScriptException(e);
		}
	}

	@Override
	public CompiledScript compile(Reader script) throws ScriptException
	{
		try
		{
			return XScriptParser.parse(this, script);
		}
		catch (IOException ioe)
		{
			throw new ScriptException(ioe);
		} 
		catch (ParserConfigurationException e)
		{
			throw new ScriptException(e);
		} catch (SAXException e)
		{
			throw new ScriptException(e);
		}
	}
	
	protected XScriptContext getScriptContext(Bindings nn) 
	{
		XScriptContext ctxt = new XScriptContext();
		Bindings gs = getBindings(ScriptContext.GLOBAL_SCOPE);
		
		if (gs != null) 
			ctxt.setBindings(gs, ScriptContext.GLOBAL_SCOPE);
		
		if (nn != null) 
		{
			ctxt.setBindings(nn,
			ScriptContext.ENGINE_SCOPE);
		} else
			throw new NullPointerException("Engine scope Bindings may not be null.");
		
		ctxt.setReader(_context.getReader());
		ctxt.setWriter(_context.getWriter());
		ctxt.setErrorWriter(_context.getErrorWriter());
		
		return ctxt;
	}
}