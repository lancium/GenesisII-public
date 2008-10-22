package edu.virginia.vcgr.xscript;

import java.util.List;
import java.util.Stack;

import javax.script.Bindings;
import javax.script.SimpleBindings;
import javax.script.SimpleScriptContext;

public class XScriptContext 
	extends SimpleScriptContext implements XScriptConstants
{
	private Stack<Bindings> _bindings = new Stack<Bindings>();
	
	private Throwable _lastException = null;
	
	public void setException(Throwable e)
	{
		_lastException = e;
	}
	
	public Throwable getLastException()
	{
		try
		{
			return _lastException;
		}
		finally
		{
			_lastException = null;
		}
	}
	
	@Override
	public Object getAttribute(String name, int scope)
	{
		switch (scope)
		{
			case ENGINE_SCOPE :
			case GLOBAL_SCOPE :
				return super.getAttribute(name, scope);
			default :
				return _bindings.get(scope - BOTTOM_SCOPE).get(name);
		}
	}

	@Override
	public Object getAttribute(String name)
	{
		for (int lcv = _bindings.size() - 1; lcv >= 0; lcv--)
		{
			Object ret = _bindings.get(lcv).get(name);
			if (ret != null)
				return ret;
		}
		
		return super.getAttribute(name);
	}

	@Override
	public int getAttributesScope(String name)
	{
		for (int lcv = _bindings.size() - 1; lcv >= 0; lcv--)
		{
			Object ret = _bindings.get(lcv).get(name);
			if (ret != null)
				return lcv + BOTTOM_SCOPE;
		}
		
		return super.getAttributesScope(name);
	}

	@Override
	public Bindings getBindings(int scope)
	{
		switch (scope)
		{
			case ENGINE_SCOPE :
			case GLOBAL_SCOPE :
				return super.getBindings(scope);
			default :
				return _bindings.get(scope - BOTTOM_SCOPE);	
		}
	}

	@Override
	public List<Integer> getScopes()
	{
		List<Integer> ret = super.getScopes();
		
		for (int lcv = 0; lcv < _bindings.size(); lcv++)
			ret.add(lcv + BOTTOM_SCOPE);
		
		return ret;
	}

	@Override
	public Object removeAttribute(String name, int scope)
	{
		switch (scope)
		{
			case ENGINE_SCOPE :
			case GLOBAL_SCOPE :
				return super.removeAttribute(name, scope);
			default :
				Bindings b = _bindings.get(scope - BOTTOM_SCOPE);
				return b.remove(name);
		}
	}

	public void setAttribute(String name, Object value)
	{
		_bindings.peek().put(name, value);
	}
	
	@Override
	public void setAttribute(String name, Object value, int scope)
	{
		switch (scope)
		{
			case ENGINE_SCOPE :
			case GLOBAL_SCOPE :
				super.setAttribute(name, value, scope);
				break;
			default :
				_bindings.get(scope - BOTTOM_SCOPE).put(name, value);
		}
	}

	@Override
	public void setBindings(Bindings bindings, int scope)
	{
		switch (scope)
		{
			case ENGINE_SCOPE :
			case GLOBAL_SCOPE :
				super.setBindings(bindings, scope);
				break;
			default :
				_bindings.set(scope - BOTTOM_SCOPE, bindings);
		}
	}
	
	public void push()
	{
		_bindings.push(new SimpleBindings());
	}
	
	public void pop()
	{
		_bindings.pop();
	}
}