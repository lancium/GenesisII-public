package edu.virginia.vcgr.genii.gjt.data.variables;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import edu.virginia.vcgr.genii.gjt.util.Duple;

public class VariableManager implements ParameterizableListener {
	static private Logger _logger = Logger.getLogger(VariableManager.class);

	static final private Pattern VARIABLE_PATTERN = Pattern
			.compile("\\$\\{([^}]+)\\}");

	static final public Duple<String, List<VariableInformation>> findVariables(
			String text) {
		int shift = 0;
		int lastEnd = 0;
		StringBuilder newString = new StringBuilder();
		List<VariableInformation> info = new Vector<VariableInformation>();

		Matcher matcher = VARIABLE_PATTERN.matcher(text);
		while (matcher.find()) {
			newString.append(text.substring(lastEnd, matcher.start()));
			lastEnd = matcher.end();
			info.add(new VariableInformation(matcher.group(1), matcher.start()
					- shift));
			shift += 3;
			newString.append(matcher.group(1));
		}
		newString.append(text.substring(lastEnd));

		return new Duple<String, List<VariableInformation>>(
				newString.toString(), info);
	}

	private Map<String, VariableCounter> _counters = new HashMap<String, VariableCounter>();

	private Collection<VariableListener> _listeners = new LinkedList<VariableListener>();

	final private void fireVariableAdded(String variableName) {
		Collection<VariableListener> listeners;

		synchronized (_listeners) {
			listeners = new Vector<VariableListener>(_listeners);
		}

		for (VariableListener listener : listeners)
			listener.variableAdded(this, variableName);
	}

	final private void fireVariableRemoved(String variableName) {
		Collection<VariableListener> listeners;

		synchronized (_listeners) {
			listeners = new Vector<VariableListener>(_listeners);
		}

		for (VariableListener listener : listeners)
			listener.variableRemoved(this, variableName);
	}

	final private void modifyCounts(String text,
			Map<String, VariableCounter> counts, int multiplier) {
		Matcher matcher = VARIABLE_PATTERN.matcher(text);
		while (matcher.find()) {
			String name = matcher.group(1);
			VariableCounter counter = counts.get(name);
			if (counter == null)
				counts.put(name, counter = new VariableCounter(0));
			counter.modify(multiplier);
		}
	}

	final public void addVariableListener(VariableListener listener) {
		synchronized (_listeners) {
			_listeners.add(listener);
		}
	}

	final public void removeVariableListener(VariableListener listener) {
		synchronized (_listeners) {
			_listeners.remove(listener);
		}
	}

	final public Set<String> variables() {
		Set<String> ret = new HashSet<String>();
		for (Map.Entry<String, VariableCounter> variable : _counters.entrySet()) {
			if (variable.getValue().get() > 0)
				ret.add(variable.getKey());
		}

		return ret;
	}

	final void modifyVariableCount(String variableName, int delta) {
		boolean added = false;
		boolean removed = false;

		if (delta == 0)
			return;

		synchronized (_counters) {
			VariableCounter counter = _counters.get(variableName);
			if (delta > 0) {
				if (counter == null) {
					_counters.put(variableName, counter = new VariableCounter(
							delta));
					added = true;
				} else
					counter.modify(delta);
			} else {
				if (counter == null) {
					_logger.warn(String.format(
							"Variable manager asked to decrement a variable "
									+ "that doesn't exist (%s).", variableName));
				} else {
					if (counter.modify(delta) < 0) {
						_logger.warn(String.format(
								"Variable manager asked to decrement a variable "
										+ "below a count of 0 (%s).",
								variableName));
						counter.set(0);
					}

					if (counter.get() == 0) {
						_counters.remove(variableName);
						removed = true;
					}
				}
			}

			_logger.debug(String.format("Variable %s has a count of %d\n",
					variableName, (counter == null) ? delta : counter.get()));
		}

		if (added)
			fireVariableAdded(variableName);
		else if (removed)
			fireVariableRemoved(variableName);
	}

	final void handleStringReplacement(String oldValue, String newValue) {
		Map<String, VariableCounter> tmpCounter = new HashMap<String, VariableCounter>();

		if (oldValue == null)
			oldValue = "";
		if (newValue == null)
			newValue = "";

		modifyCounts(oldValue, tmpCounter, -1);
		modifyCounts(newValue, tmpCounter, 1);

		for (Map.Entry<String, VariableCounter> entry : tmpCounter.entrySet()) {
			int delta = entry.getValue().get();
			if (delta != 0)
				modifyVariableCount(entry.getKey(), delta);
		}
	}

	@Override
	public void parameterizableStringModified(String oldValue, String newValue) {
		handleStringReplacement(oldValue, newValue);
	}
}