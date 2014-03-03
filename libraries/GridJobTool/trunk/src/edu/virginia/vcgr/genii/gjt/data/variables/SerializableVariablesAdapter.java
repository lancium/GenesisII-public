package edu.virginia.vcgr.genii.gjt.data.variables;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class SerializableVariablesAdapter extends
		XmlAdapter<SerializableVariables, Map<String, VariableDefinition>> {
	@Override
	public SerializableVariables marshal(Map<String, VariableDefinition> v)
			throws Exception {
		SerializableVariables ret = new SerializableVariables();
		for (Map.Entry<String, VariableDefinition> entry : v.entrySet())
			ret.add(new SerializableVariable(entry.getKey(), entry.getValue()));

		return ret;
	}

	@Override
	public Map<String, VariableDefinition> unmarshal(SerializableVariables v)
			throws Exception {
		Map<String, VariableDefinition> ret = new HashMap<String, VariableDefinition>();
		for (SerializableVariable var : v.vars())
			ret.put(var.name(), var.definition());

		return ret;
	}
}