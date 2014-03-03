package edu.virginia.vcgr.genii.gjt.data.variables.list;

import java.util.Collection;
import java.util.Vector;

import javax.xml.bind.annotation.XmlElement;

import edu.virginia.vcgr.genii.gjt.data.Describer;
import edu.virginia.vcgr.genii.gjt.data.JobDocumentConstants;
import edu.virginia.vcgr.genii.gjt.data.variables.AbstractVariableDefinition;
import edu.virginia.vcgr.genii.gjt.data.variables.VariableDefinition;
import edu.virginia.vcgr.genii.gjt.data.variables.VariableDefinitionType;
import edu.virginia.vcgr.jsdl.sweep.SweepFunction;
import edu.virginia.vcgr.jsdl.sweep.functions.ValuesSweepFunction;

public class ValueListVariableDefinition extends AbstractVariableDefinition implements VariableDefinition
{
	static final public String DESCRIPTION = "Value List";

	static private Describer<? extends VariableDefinition> DESCRIBER = new Describer<ValueListVariableDefinition>()
	{
		@Override
		public String describe(ValueListVariableDefinition type, int verbosity)
		{
			if (type.size() == 0)
				return "";
			if (type.size() == 1)
				return type._values.get(0);

			if (verbosity <= 0)
				return "...";
			else if (verbosity == 1)
				return String.format("%s, %s, ... %s", type._values.get(0), type._values.get(1),
					type._values.get(type.size() - 1));
			else {
				StringBuilder builder = new StringBuilder();
				for (String value : type._values) {
					if (builder.length() > 0)
						builder.append(", ");
					builder.append(value);
				}

				return builder.toString();
			}
		}

		@Override
		public int maximumVerbosity()
		{
			return 2;
		}
	};

	@XmlElement(namespace = JobDocumentConstants.DOCUMENT_NAMESPACE, name = "value")
	Vector<String> _values;

	/* Used for XML deserialization */
	@SuppressWarnings("unused")
	private ValueListVariableDefinition()
	{
		this(new Vector<String>());
	}

	public ValueListVariableDefinition(Collection<String> values)
	{
		super(VariableDefinitionType.ValueList, DESCRIBER);

		_values = new Vector<String>(values);
	}

	@Override
	final public int size()
	{
		return _values.size();
	}

	@Override
	public SweepFunction generateFunction()
	{
		ValuesSweepFunction ret = new ValuesSweepFunction();
		for (String value : _values)
			ret.addValue(value);

		return ret;
	}
}