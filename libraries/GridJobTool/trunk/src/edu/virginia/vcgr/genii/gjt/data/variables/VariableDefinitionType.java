package edu.virginia.vcgr.genii.gjt.data.variables;

import edu.virginia.vcgr.genii.gjt.data.variables.doubleloop.DoubleLoopVariableDefinition;
import edu.virginia.vcgr.genii.gjt.data.variables.doubleloop.DoubleLoopVariableDefinitionEditorFactory;
import edu.virginia.vcgr.genii.gjt.data.variables.intloop.IntegerLoopVariableDefinition;
import edu.virginia.vcgr.genii.gjt.data.variables.intloop.IntegerLoopVariableDefinitionEditorFactory;
import edu.virginia.vcgr.genii.gjt.data.variables.list.ValueListVariableDefinition;
import edu.virginia.vcgr.genii.gjt.data.variables.list.ValueListVariableDefinitionEditorFactory;
import edu.virginia.vcgr.genii.gjt.data.variables.undef.UndefinedVariableDefinition;

public enum VariableDefinitionType {
	UndefinedVariable(UndefinedVariableDefinition.DESCRIPTION, null), IntegerLoop(
			IntegerLoopVariableDefinition.DESCRIPTION,
			new IntegerLoopVariableDefinitionEditorFactory()), DoubleLoop(
			DoubleLoopVariableDefinition.DESCRIPTION,
			new DoubleLoopVariableDefinitionEditorFactory()), ValueList(
			ValueListVariableDefinition.DESCRIPTION,
			new ValueListVariableDefinitionEditorFactory());

	private String _description;
	private VariableDefinitionEditorFactory _editorFactory;

	private VariableDefinitionType(String description,
			VariableDefinitionEditorFactory editorFactory) {
		_description = description;
		_editorFactory = editorFactory;
	}

	@Override
	public String toString() {
		return _description;
	}

	public VariableDefinitionEditorFactory editorFactory() {
		return _editorFactory;
	}
}