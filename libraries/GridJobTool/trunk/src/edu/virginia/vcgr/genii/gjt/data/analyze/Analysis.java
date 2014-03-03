package edu.virginia.vcgr.genii.gjt.data.analyze;

import java.util.Collection;
import java.util.LinkedList;

public class Analysis {
	private Collection<String> _warnings = new LinkedList<String>();
	private Collection<String> _errors = new LinkedList<String>();

	final public boolean hasWarnings() {
		return !_warnings.isEmpty();
	}

	final public boolean hasErrors() {
		return !_errors.isEmpty();
	}

	public Collection<String> warnings() {
		return _warnings;
	}

	public Collection<String> errors() {
		return _errors;
	}

	public void addWarning(String format, Object... arguments) {
		_warnings.add(String.format(format, arguments));
	}

	public void addError(String format, Object... arguments) {
		_errors.add(String.format(format, arguments));
	}
}