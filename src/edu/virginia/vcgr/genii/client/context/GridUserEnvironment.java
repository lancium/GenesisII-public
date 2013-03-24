package edu.virginia.vcgr.genii.client.context;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class GridUserEnvironment
{
	static private final String CALLING_CONTEXT_PROPERTY = "edu.virginia.vcgr.genii.client.context.grid-user-env";

	static private Log _logger = LogFactory.getLog(GridUserEnvironment.class);

	@SuppressWarnings("unchecked")
	static public Map<String, String> getGridUserEnvironment()
	{
		Map<String, String> ret = null;
		ICallingContext ctxt = null;

		try {
			ctxt = ContextManager.getCurrentContext();
			if (ctxt != null)
				ret = (Map<String, String>) ctxt.getSingleValueProperty(CALLING_CONTEXT_PROPERTY);
		} catch (IOException ioe) {
			_logger.warn("Unable to load grid user environment.", ioe);
		}

		if (ret == null) {
			ret = new HashMap<String, String>();
			if (ctxt != null)
				ctxt.setSingleValueProperty(CALLING_CONTEXT_PROPERTY, (Serializable) ret);
		}

		return ret;
	}

	static public void clearGridUserEnvironment() throws FileNotFoundException, IOException
	{
		ICallingContext ctxt = ContextManager.getExistingContext();
		if (ctxt != null)
			ctxt.removeProperty(CALLING_CONTEXT_PROPERTY);
	}

	static private String findVariable(String original, StringBuilder target)
	{
		boolean escaped = false;
		for (int lcv = 0; lcv < original.length(); lcv++) {
			char c = original.charAt(lcv);

			if (escaped) {
				escaped = false;
				if (c != '$')
					target.append('\\');
				target.append(c);
			} else {
				switch (c) {
					case '\\':
						escaped = true;
						break;

					case '$':
						return original.substring(lcv);

					default:
						target.append(c);
						break;
				}
			}
		}

		if (escaped)
			target.append('\\');
		return "";
	}

	static private String handleVariable(String original, StringBuilder target, Map<String, String> environment)
	{
		String variable = null;

		if (original.length() > 1) {
			if (original.charAt(1) == '{') {
				int index = original.indexOf('}');
				if (index > 0) {
					variable = original.substring(2, index);
					original = original.substring(index + 1);
				}
			} else {
				int lcv;
				for (lcv = 1; lcv < original.length(); lcv++) {
					if (!Character.isLetterOrDigit(original.charAt(lcv)))
						break;
				}

				variable = original.substring(1, lcv);
				original = original.substring(lcv);
			}
		}

		if (variable == null) {
			target.append(original);
			return "";
		}

		variable = environment.get(variable);
		if (variable == null)
			variable = "";

		target.append(variable);
		return original;
	}

	static public String replaceVariables(Map<String, String> environment, String original)
	{
		if (original == null)
			return null;

		StringBuilder result = new StringBuilder();

		while (original.length() > 0) {
			original = findVariable(original, result);
			original = handleVariable(original, result, environment);
		}

		return result.toString();
	}

	static private void doTest(Map<String, String> env, String orig)
	{
		System.out.format("\"%s\" -> \"%s\"\n", orig, replaceVariables(env, orig));
	}

	static public void main(String[] args)
	{
		Map<String, String> test = new HashMap<String, String>();
		test.put("NAME", "Mark");
		test.put("AGE", "35");

		doTest(test, "Hello, $NAME");
		doTest(test, "$NAME was here.");
		doTest(test, "$NAME is $AGE years old.");

		doTest(test, "Hello, ${NAME}");
		doTest(test, "${NAME} was here.");
		doTest(test, "${NAME} is ${AGE} years old.");
	}
}