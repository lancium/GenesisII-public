package edu.virginia.vcgr.genii.gjt.data.variables;

import javax.xml.bind.annotation.XmlTransient;

import edu.virginia.vcgr.genii.gjt.data.Describer;

public abstract class AbstractVariableDefinition implements VariableDefinition {
	@XmlTransient
	private Describer<? extends VariableDefinition> _describer;

	@XmlTransient
	private VariableDefinitionType _type;

	@SuppressWarnings("unused")
	private AbstractVariableDefinition() {
		_describer = null;
		_type = null;
	}

	protected AbstractVariableDefinition(VariableDefinitionType type,
			Describer<? extends VariableDefinition> describer) {
		_describer = describer;
		_type = type;
	}

	@Override
	final public Describer<? extends VariableDefinition> describer() {
		return _describer;
	}

	@Override
	final public VariableDefinitionType type() {
		return _type;
	}
}