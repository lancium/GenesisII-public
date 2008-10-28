package edu.virginia.vcgr.xscript;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;
import org.xml.sax.SAXException;

public class XScriptEngine 
	implements ScriptEngine, Compilable, Invocable
{
	static private Log _logger = LogFactory.getLog(XScriptEngine.class);
	
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

	@Override
	public <T> T getInterface(Class<T> clasz)
	{
		try
		{
			if (!clasz.isInterface())
				return null;
			
			return clasz.cast(Proxy.newProxyInstance(
				Thread.currentThread().getContextClassLoader(), 
				new Class<?>[] { clasz }, 
				new ScriptFunctionInvoker(clasz)));
		} 
		catch (IllegalArgumentException e)
		{
			_logger.warn("Error trying to match interface to script function.",
				e);
			return null;
		}
		catch (NoSuchMethodException e)
		{
			_logger.warn("Error trying to match interface to script function.",
				e);
			return null;
		}
	}

	@Override
	public <T> T getInterface(Object thiz, Class<T> clasz)
	{
		return null;
	}

	@Override
	public Object invokeFunction(String name, Object... args)
			throws ScriptException, NoSuchMethodException
	{
		return invokeFunction(findFunction(name), args);
	}

	@Override
	public Object invokeMethod(Object thiz, String name, Object... args)
			throws ScriptException, NoSuchMethodException
	{
		throw new NoSuchMethodException(String.format(
			"Method \"%s\" not available.", name));
	}
	
	private ParseStatement findFunction(String functionName)
		throws NoSuchMethodException
	{
		Object functionObj = _context.getAttribute(functionName, 
			ScriptContext.GLOBAL_SCOPE);
		
		if ( (functionObj != null) && (functionObj instanceof ParseStatement) )
		{
			return (ParseStatement)functionObj;
		} else
		{
			throw new NoSuchMethodException(String.format(
				"Method \"%s\" is not available.", functionName));
		}
	}
	
	private Object invokeFunction(ParseStatement function, Object []args)
		throws ScriptException
	{
		_context.push();
		try
		{
			_context.setAttribute("ARGV", args);
			return function.evaluate(_context);
		}
		catch (ReturnFromFunctionException rffe)
		{
			return rffe.getResult();
		}
		catch (EarlyExitException eee)
		{
			return eee.getExitCode();
		}
		finally
		{
			_context.pop();
		}
	}
	
	private class ScriptFunctionInvoker implements InvocationHandler
	{
		private Map<String, ParseStatement> _functions =
			new HashMap<String, ParseStatement>();
		
		public ScriptFunctionInvoker(Class<?> iface)
			throws NoSuchMethodException
		{
			for (Method m : iface.getMethods())
			{
				String mName = m.getName();
				ParseStatement stmt = _functions.get(m);
				if (stmt != null)
					throw new NoSuchMethodException(String.format(
						"Unable to find unique function \"%s\".", mName));
				_functions.put(mName, findFunction(mName));
			}
		}
		
		@Override
		public Object invoke(Object proxy, Method method, Object[] args)
				throws Throwable
		{
			ParseStatement stmt = _functions.get(method.getName());
			return invokeFunction(stmt, args);
		}
	}
}