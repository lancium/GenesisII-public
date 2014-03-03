package edu.virginia.vcgr.genii.gjt.data.variables;

import java.awt.Window;

import javax.swing.JDialog;

public abstract class VariableDefinitionEditor<GenericType extends VariableDefinition>
		extends JDialog {
	static final long serialVersionUID = 0L;

	private boolean _cancelled = true;

	public abstract GenericType getVariableDefinitionImpl();

	public VariableDefinitionEditor(Window owner, String title) {
		super(owner);
		setTitle(title);

		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
	}

	public abstract void setFromVariableDefinition(
			GenericType variableDefinition);

	final public void cancel() {
		_cancelled = true;
		dispose();
	}

	final public void accept() {
		_cancelled = false;
		dispose();
	}

	public GenericType getVariableDefinition() {
		if (_cancelled)
			return null;

		return getVariableDefinitionImpl();
	}
}
