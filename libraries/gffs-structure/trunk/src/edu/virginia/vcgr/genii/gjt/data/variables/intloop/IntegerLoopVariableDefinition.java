package edu.virginia.vcgr.genii.gjt.data.variables.intloop;

import javax.xml.bind.annotation.XmlAttribute;

import edu.virginia.vcgr.genii.gjt.data.Describer;
import edu.virginia.vcgr.genii.gjt.data.variables.AbstractVariableDefinition;
import edu.virginia.vcgr.genii.gjt.data.variables.VariableDefinition;
import edu.virginia.vcgr.genii.gjt.data.variables.VariableDefinitionType;
import edu.virginia.vcgr.jsdl.sweep.SweepFunction;
import edu.virginia.vcgr.jsdl.sweep.functions.LoopIntegerSweepFunction;

public class IntegerLoopVariableDefinition extends AbstractVariableDefinition
{
	static final public String DESCRIPTION = "Integer Loop";

	static private Describer<? extends VariableDefinition> DESCRIBER = new Describer<IntegerLoopVariableDefinition>()
	{
		@Override
		public String describe(IntegerLoopVariableDefinition type, int verbosity)
		{
			if (type._step == 1)
				return String.format("[%d, %d]", type._start, type._end);
			else
				return String.format("[%d, %d] by %d", type._start, type._end, type._step);
		}

		@Override
		public int maximumVerbosity()
		{
			return 0;
		}
	};

	@XmlAttribute(name = "start", required = true)
	int _start;

	@XmlAttribute(name = "end", required = true)
	int _end;

	@XmlAttribute(name = "step", required = true)
	int _step;

	/* Used for XML deserialization */
	@SuppressWarnings("unused")
	private IntegerLoopVariableDefinition()
	{
		this(0, 0, 1);
	}

	IntegerLoopVariableDefinition(int start, int end, int step)
	{
		super(VariableDefinitionType.IntegerLoop, DESCRIBER);

		_start = start;
		_end = end;
		_step = step;
	}

	@Override
	final public int size()
	{
		return (_end - _start + _step) / _step;
	}

	@Override
	public SweepFunction generateFunction()
	{
		return new LoopIntegerSweepFunction(_start, _end, _step);
	}
}