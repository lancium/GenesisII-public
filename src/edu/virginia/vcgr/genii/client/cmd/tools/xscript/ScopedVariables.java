package edu.virginia.vcgr.genii.client.cmd.tools.xscript;

import java.util.HashMap;

public class ScopedVariables
{
	private HashMap<String, String> _variables =
		new HashMap<String, String>();
	private ScopedVariables _parent = null;
	
	public ScopedVariables()
	{
		this(null);
	}
	
	private ScopedVariables(ScopedVariables parent)
	{
		_parent = parent;
	}
	
	public ScopedVariables deriveSubScope()
	{
		return new ScopedVariables(this); 
	}
	
	public String getValue(String variable)
	{
		String val = _variables.get(variable);
		if (val == null && _parent != null)
			return _parent.getValue(variable);
			
		return val;
	}
	
	public void setValue(String variable, String value)
	{
		_variables.put(variable, value);
	}
}