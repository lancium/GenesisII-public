package edu.virginia.vcgr.genii.gjt.data.variables;

public interface VariableListener {
	public void variableAdded(VariableManager manager, String variableName);

	public void variableRemoved(VariableManager manager, String variableName);
}