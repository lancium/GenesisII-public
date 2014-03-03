package edu.virginia.vcgr.genii.gjt.data.variables;

public class ParameterizableBroker extends BasicParameterizable implements Parameterizable, ParameterizableListener
{
	@Override
	public void parameterizableStringModified(String oldValue, String newValue)
	{
		fireParameterizableStringModified(oldValue, newValue);
	}
}