package edu.virginia.vcgr.genii.gjt.data;

import java.util.List;
import java.util.Map;
import java.util.Vector;

import edu.virginia.vcgr.genii.gjt.data.fs.FilesystemType;
import edu.virginia.vcgr.genii.gjt.data.variables.VariableInformation;
import edu.virginia.vcgr.genii.gjt.data.variables.VariableManager;
import edu.virginia.vcgr.genii.gjt.data.xpath.DefaultXPathAttributeNode;
import edu.virginia.vcgr.genii.gjt.data.xpath.XPathBuilder;
import edu.virginia.vcgr.genii.gjt.util.Duple;
import edu.virginia.vcgr.genii.gjt.util.Triple;
import edu.virginia.vcgr.jsdl.sweep.SweepParameter;

public class JSDLGenerator {
	static public boolean indicatesFilesystem(FilesystemType filesystemType) {
		return (filesystemType != null)
				&& (filesystemType != FilesystemType.Default);
	}

	static public String generate(ParameterizableString string,
			XPathBuilder builder, Map<String, List<SweepParameter>> variables) {
		if (string == null)
			return null;

		String value = string.get();
		if (value == null)
			return null;

		if (value.equals(""))
			return null;

		Duple<String, List<VariableInformation>> results = VariableManager
				.findVariables(value);
		for (VariableInformation info : results.second()) {
			List<SweepParameter> parameters = variables.get(info.variable());
			if (parameters == null)
				variables.put(info.variable(),
						parameters = new Vector<SweepParameter>());

			parameters.add(builder.toSubstringParameter(info.offset() + 1, info
					.variable().length()));
		}

		return results.first();
	}

	static public Duple<String, FilesystemType> generate(
			FilesystemAssociatedString source, XPathBuilder builder,
			Map<String, List<SweepParameter>> variables) {
		if (source == null)
			return null;

		String stringReturn = generate((ParameterizableString) source, builder,
				variables);
		if (stringReturn == null)
			return null;
		if (stringReturn.equals(""))
			return null;

		return new Duple<String, FilesystemType>(stringReturn,
				source.getFilesystemType());
	}

	static public List<String> generate(ParameterizableStringList strings,
			XPathBuilder builder, Map<String, List<SweepParameter>> variables) {
		if (strings == null)
			return null;

		List<String> values = new Vector<String>();

		for (String value : strings) {
			Duple<String, List<VariableInformation>> results = VariableManager
					.findVariables(value);

			values.add(results.first());

			for (VariableInformation info : results.second()) {
				List<SweepParameter> parameters = variables
						.get(info.variable());
				if (parameters == null)
					variables.put(info.variable(),
							parameters = new Vector<SweepParameter>());

				parameters.add(builder.toSubstringParameter(info.offset() + 1,
						info.variable().length()));
				builder.iterate();
			}
		}

		return values;
	}

	static public List<Duple<String, FilesystemType>> generate(
			FilesystemAssociatedStringList list, XPathBuilder builder,
			Map<String, List<SweepParameter>> variables) {
		if (list == null)
			return null;

		List<Duple<String, FilesystemType>> ret = new Vector<Duple<String, FilesystemType>>();

		for (StringFilesystemPair item : list) {
			Duple<String, List<VariableInformation>> results = VariableManager
					.findVariables(item.get());

			ret.add(new Duple<String, FilesystemType>(results.first(), item
					.getFilesystemType()));

			for (VariableInformation info : results.second()) {
				List<SweepParameter> parameters = variables
						.get(info.variable());
				if (parameters == null)
					variables.put(info.variable(),
							parameters = new Vector<SweepParameter>());

				parameters.add(builder.toSubstringParameter(info.offset() + 1,
						info.variable().length()));
			}

			builder.iterate();
		}

		return ret;
	}

	static public List<Triple<String, String, FilesystemType>> generate(
			EnvironmentList list, XPathBuilder builder,
			Map<String, List<SweepParameter>> variables) {
		if (list == null)
			return null;

		List<Triple<String, String, FilesystemType>> ret = new Vector<Triple<String, String, FilesystemType>>();

		for (StringStringFilesystemTriple item : list) {
			builder.setAttribute(new DefaultXPathAttributeNode(null, "name"));
			Duple<String, List<VariableInformation>> keyResults = VariableManager
					.findVariables(item.getKey());
			for (VariableInformation info : keyResults.second()) {
				List<SweepParameter> parameters = variables
						.get(info.variable());
				if (parameters == null)
					variables.put(info.variable(),
							parameters = new Vector<SweepParameter>());

				parameters.add(builder.toSubstringParameter(info.offset() + 1,
						info.variable().length()));
			}
			builder.clearAttribute();

			Duple<String, List<VariableInformation>> valueResults = VariableManager
					.findVariables(item.getValue());
			for (VariableInformation info : valueResults.second()) {
				List<SweepParameter> parameters = variables
						.get(info.variable());
				if (parameters == null)
					variables.put(info.variable(),
							parameters = new Vector<SweepParameter>());

				parameters.add(builder.toSubstringParameter(info.offset() + 1,
						info.variable().length()));
			}

			ret.add(new Triple<String, String, FilesystemType>(keyResults
					.first(), valueResults.first(), item.getFilesystemType()));

			builder.iterate();
		}

		return ret;
	}
}