package edu.virginia.vcgr.appmgr.util;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Properties;

public class PropertyUtilities {
	static public String getRequiredProperty(Properties properties,
			String propertyName) throws IOException {
		String ret = properties.getProperty(propertyName);
		if (ret == null)
			throw new IOException(String.format(
					"Unable to get required property \"%s\".", propertyName));

		return ret;
	}

	static public Collection<String> getPropertyList(Properties properties,
			String basePropertyName) {
		Collection<String> ret = new LinkedList<String>();

		for (int lcv = 0; true; lcv++) {
			String value = properties.getProperty(String.format("%s.%d",
					basePropertyName, lcv));
			if (value == null)
				break;
			ret.add(value);
		}

		return ret;
	}
}