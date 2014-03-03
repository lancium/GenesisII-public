package edu.virginia.vcgr.genii.gjt.data.variables.list;

import java.awt.Window;

import edu.virginia.vcgr.genii.gjt.data.variables.VariableDefinition;
import edu.virginia.vcgr.genii.gjt.data.variables.VariableDefinitionEditor;
import edu.virginia.vcgr.genii.gjt.data.variables.VariableDefinitionEditorFactory;

public class ValueListVariableDefinitionEditorFactory implements
		VariableDefinitionEditorFactory {
	@Override
	public VariableDefinitionEditor<? extends VariableDefinition> createEditor(
			Window owner) {
		return new ValueListVariableDefinitionEditor(owner);
	}
}
