package edu.virginia.vcgr.genii.ui.errors;

public class ClassifiedError
{
	private ErrorClassification _classification;
	private String _reportTitle;
	private String _reportMessage;
	private Throwable _cause;

	ClassifiedError(ErrorClassification classification, String reportTitle, String reportMessage, Throwable cause)
	{
		_classification = classification;
		_reportTitle = reportTitle;
		_reportMessage = reportMessage;
		_cause = cause;
	}

	final public ErrorClassification classification()
	{
		return _classification;
	}

	final public String reportTitle()
	{
		return _reportTitle;
	}

	final public String reportMessage()
	{
		return _reportMessage;
	}

	final public Throwable cause()
	{
		return _cause;
	}
}