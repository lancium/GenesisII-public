package edu.virginia.vcgr.genii.gjt.data;

public class ParameterizableStringListDescriber implements
		Describer<ParameterizableStringList> {
	@Override
	public int maximumVerbosity() {
		return 2;
	}

	@Override
	public String describe(ParameterizableStringList type, int verbosity) {
		if (type.size() == 0)
			return "";
		if (type.size() == 1)
			return type.get(0);

		if (verbosity <= 0)
			return "...";
		else if (verbosity == 1)
			return String.format("%s, %s, ... %s", type.get(0), type.get(1),
					type.get(type.size() - 1));
		else {
			StringBuilder builder = new StringBuilder();
			for (String value : type) {
				if (builder.length() > 0)
					builder.append(", ");
				builder.append(value);
			}

			return builder.toString();
		}
	}
}
