package edu.virginia.vcgr.genii.gjt.data.variables.undef;

import edu.virginia.vcgr.genii.gjt.data.Describer;
import edu.virginia.vcgr.genii.gjt.data.variables.AbstractVariableDefinition;
import edu.virginia.vcgr.genii.gjt.data.variables.VariableDefinition;
import edu.virginia.vcgr.genii.gjt.data.variables.VariableDefinitionType;
import edu.virginia.vcgr.jsdl.sweep.SweepFunction;

public class UndefinedVariableDefinition extends AbstractVariableDefinition {
	static final public String DESCRIPTION = "";

	static private Describer<? extends VariableDefinition> DESCRIBER = new Describer<VariableDefinition>() {
		@Override
		public String describe(VariableDefinition type, int verbosity) {
			return "";
		}

		@Override
		public int maximumVerbosity() {
			return 0;
		}
	};

	public UndefinedVariableDefinition() {
		super(VariableDefinitionType.UndefinedVariable, DESCRIBER);
	}

	@Override
	public int size() {
		return 0;
	}

	@Override
	public SweepFunction generateFunction() {
		throw new UnsupportedOperationException(
				"Attempt to generate a sweep function from the undefined variable definition type.");
	}
}