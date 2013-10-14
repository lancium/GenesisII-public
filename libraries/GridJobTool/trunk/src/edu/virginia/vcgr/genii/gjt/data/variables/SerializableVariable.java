package edu.virginia.vcgr.genii.gjt.data.variables;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;

import edu.virginia.vcgr.genii.gjt.data.JobDocumentConstants;
import edu.virginia.vcgr.genii.gjt.data.variables.doubleloop.DoubleLoopVariableDefinition;
import edu.virginia.vcgr.genii.gjt.data.variables.intloop.IntegerLoopVariableDefinition;
import edu.virginia.vcgr.genii.gjt.data.variables.list.ValueListVariableDefinition;
import edu.virginia.vcgr.genii.gjt.data.variables.undef.UndefinedVariableDefinition;

public class SerializableVariable
{
	@XmlAttribute(name = "name")
	private String _name;

	@XmlElements({
		@XmlElement(namespace = JobDocumentConstants.DOCUMENT_NAMESPACE, name = "undefined",
			type = UndefinedVariableDefinition.class),
		@XmlElement(namespace = JobDocumentConstants.DOCUMENT_NAMESPACE, name = "integer-loop",
			type = IntegerLoopVariableDefinition.class),
		@XmlElement(namespace = JobDocumentConstants.DOCUMENT_NAMESPACE, name = "double-loop",
			type = DoubleLoopVariableDefinition.class),
		@XmlElement(namespace = JobDocumentConstants.DOCUMENT_NAMESPACE, name = "value-list",
			type = ValueListVariableDefinition.class) })
	private VariableDefinition _definition;

	public SerializableVariable(String name, VariableDefinition varDef)
	{
		_name = name;
		_definition = varDef;
	}

	public SerializableVariable()
	{
		this(null, null);
	}

	public String name()
	{
		return _name;
	}

	public VariableDefinition definition()
	{
		return _definition;
	}
}