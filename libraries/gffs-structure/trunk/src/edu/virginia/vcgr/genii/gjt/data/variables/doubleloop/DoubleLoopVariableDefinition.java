package edu.virginia.vcgr.genii.gjt.data.variables.doubleloop;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;

import edu.virginia.vcgr.genii.gjt.data.Describer;
import edu.virginia.vcgr.genii.gjt.data.variables.AbstractVariableDefinition;
import edu.virginia.vcgr.genii.gjt.data.variables.VariableDefinition;
import edu.virginia.vcgr.genii.gjt.data.variables.VariableDefinitionType;
import edu.virginia.vcgr.jsdl.sweep.SweepFunction;
import edu.virginia.vcgr.jsdl.sweep.functions.LoopDoubleSweepFunction;

public class DoubleLoopVariableDefinition extends AbstractVariableDefinition
{
	static final public String DESCRIPTION = "Double Loop";

	static private Describer<? extends VariableDefinition> DESCRIBER = new Describer<DoubleLoopVariableDefinition>()
	{
		@Override
		public String describe(DoubleLoopVariableDefinition type, int verbosity)
		{
			if (type._step == 1)
				return String.format("[%.2f, %.2f]", type._start, type._end);
			else
				return String.format("[%.2f, %.2f] by %.2f", type._start, type._end, type._step);
		}

		@Override
		public int maximumVerbosity()
		{
			return 0;
		}
	};

	@XmlAttribute(name = "start", required = true)
	double _start;

	@XmlAttribute(name = "end", required = true)
	double _end;

	@XmlAttribute(name = "step", required = true)
	double _step;

	@XmlTransient
	int _size = -1;

	/* Used for XML deserialization */
	@SuppressWarnings("unused")
	private DoubleLoopVariableDefinition()
	{
		this(0.0, 0.0, 0.1);
	}

	DoubleLoopVariableDefinition(double start, double end, double step)
	{
		super(VariableDefinitionType.DoubleLoop, DESCRIBER);

		_start = start;
		_end = end;
		_step = step;
	}

	@Override
	final public int size()
	{
		if (_size < 0) {
			int count = 0;

			for (double value = _start; value <= _end; value += _step)
				count++;

			_size = count;
		}

		return _size;
	}

	@Override
	public SweepFunction generateFunction()
	{
		return new LoopDoubleSweepFunction(_start, _end, _step);
	}
}