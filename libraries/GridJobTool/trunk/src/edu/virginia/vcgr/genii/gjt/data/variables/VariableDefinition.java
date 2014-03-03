package edu.virginia.vcgr.genii.gjt.data.variables;

import javax.xml.bind.annotation.XmlSeeAlso;

import edu.virginia.vcgr.genii.gjt.data.Describer;
import edu.virginia.vcgr.genii.gjt.data.variables.doubleloop.DoubleLoopVariableDefinition;
import edu.virginia.vcgr.genii.gjt.data.variables.intloop.IntegerLoopVariableDefinition;
import edu.virginia.vcgr.genii.gjt.data.variables.list.ValueListVariableDefinition;
import edu.virginia.vcgr.genii.gjt.data.variables.undef.UndefinedVariableDefinition;
import edu.virginia.vcgr.jsdl.sweep.SweepFunction;

@XmlSeeAlso({ UndefinedVariableDefinition.class,
		ValueListVariableDefinition.class, IntegerLoopVariableDefinition.class,
		DoubleLoopVariableDefinition.class })
public interface VariableDefinition {
	public int size();

	public Describer<? extends VariableDefinition> describer();

	public VariableDefinitionType type();

	public SweepFunction generateFunction();
}