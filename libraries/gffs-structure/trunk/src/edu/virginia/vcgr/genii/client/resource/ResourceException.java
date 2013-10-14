package edu.virginia.vcgr.genii.client.resource;

import java.util.Calendar;

import org.oasis_open.wsrf.basefaults.BaseFaultType;
import org.oasis_open.wsrf.basefaults.BaseFaultTypeDescription;

public class ResourceException extends BaseFaultType
{
	static final long serialVersionUID = 0;

	protected Throwable _myCause = null;

	public ResourceException(String msg)
	{
		super(null, Calendar.getInstance(), null, null, new BaseFaultTypeDescription[] { new BaseFaultTypeDescription(msg) },
			null);
		this.setFaultString(msg);
	}

	public ResourceException(String msg, Throwable cause)
	{
		super(null, Calendar.getInstance(), null, null, getDescriptions(msg, cause), null);
		_myCause = cause;
		this.setFaultString(msg);
	}

	public Throwable getCause()
	{
		return _myCause;
	}

	public String getMessage()
	{
		BaseFaultTypeDescription[] descs = getDescription();
		if ((descs != null) && (descs.length > 0)) {
			return getDescription(0).get_value();
		}
		return super.getMessage();
	}

	static private BaseFaultTypeDescription[] getDescriptions(String msg, Throwable cause)
	{
		if (msg == null)
			msg = cause.toString();

		return new BaseFaultTypeDescription[] { new BaseFaultTypeDescription(msg), new BaseFaultTypeDescription("Caused by:"),
			new BaseFaultTypeDescription(cause.toString()) };
	}
}