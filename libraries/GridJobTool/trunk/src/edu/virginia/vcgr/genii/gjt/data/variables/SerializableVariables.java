package edu.virginia.vcgr.genii.gjt.data.variables;

import java.util.Vector;

import javax.xml.bind.annotation.XmlElement;

import edu.virginia.vcgr.genii.gjt.data.JobDocumentConstants;

public class SerializableVariables {
	@XmlElement(namespace = JobDocumentConstants.DOCUMENT_NAMESPACE, name = "variable")
	private Vector<SerializableVariable> _variables;

	public SerializableVariables() {
		_variables = new Vector<SerializableVariable>();
	}

	public void add(SerializableVariable var) {
		_variables.add(var);
	}

	public Vector<SerializableVariable> vars() {
		return _variables;
	}
}