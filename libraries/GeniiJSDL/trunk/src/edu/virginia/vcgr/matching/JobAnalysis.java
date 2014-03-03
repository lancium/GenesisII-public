package edu.virginia.vcgr.matching;

final public class JobAnalysis {
	private String _failureReason;
	private boolean _pass;

	private JobAnalysis(String failureReason) {
		_failureReason = failureReason;
		_pass = (failureReason == null);
	}

	final public boolean passed() {
		return _pass;
	}

	final public String failureReason() {
		if (_failureReason == null)
			return "Succeeded";
		return _failureReason;
	}

	static public JobAnalysis pass() {
		return new JobAnalysis(null);
	}

	static public JobAnalysis fail(String reason) {
		if (reason == null)
			reason = "Unknown reason";

		return new JobAnalysis(reason);
	}
}