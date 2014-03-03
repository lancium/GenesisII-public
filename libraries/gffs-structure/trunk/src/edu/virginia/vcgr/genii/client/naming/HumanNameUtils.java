package edu.virginia.vcgr.genii.client.naming;

import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HumanNameUtils
{
	static public String generateUniqueName(String desiredName, Collection<String> takenNames)
	{
		int nextNumber = 0;

		Pattern regex = Pattern.compile("^" + Pattern.quote(desiredName) + "(?: \\(((?:0)|(?:[1-9][0-9]*))\\))?$");

		for (String taken : takenNames) {
			Matcher matcher = regex.matcher(taken);
			if (matcher.matches()) {
				String num = matcher.group(1);
				if (num == null && nextNumber < 1)
					nextNumber = 1;
				else if (num != null) {
					int number = Integer.parseInt(num);
					if (nextNumber <= number)
						nextNumber = number + 1;
				}
			}
		}

		if (nextNumber != 0)
			desiredName = desiredName + " (" + nextNumber + ")";

		return desiredName;
	}

	static public void main(String[] args)
	{
		ArrayList<String> taken = new ArrayList<String>();

		/*
		 * taken.add("Mark Morgan"); taken.add("Mark Morgan 1"); taken.add("Mark Morgan (1)");
		 * taken.add("Mark Morgan (3)");
		 */

		System.err.println("Unique name is \"" + generateUniqueName("Mark Morgan", taken) + "\".");
	}
}