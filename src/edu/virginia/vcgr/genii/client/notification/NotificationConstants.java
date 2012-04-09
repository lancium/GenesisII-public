package edu.virginia.vcgr.genii.client.notification;

import org.apache.axis.types.URI;

public class NotificationConstants
{
	/**
	 * UNPROCESSED - there was no registered handler for this notification.
	 */
	public static final String UNPROCESSED =
		"http://vcgr.cs.virginia.edu/genii/2010/08/notification/unprocessed";

	/**
	 * OK - the notification was processed and the internal state was updated.
	 */
	public static final String OK =
		"http://vcgr.cs.virginia.edu/genii/2010/08/notification/ok";

	/**
	 * FAIL - any of:
	 * - A parameter was illegal, for example a negative integer for a parameter that
	 *   requires a positive integer.
	 * - A parameter conflicted with the local state, for example a request to modify
	 *   byte number 11 of a file with only 10 bytes.
	 * - An internal error, for example a disk error.
	 * - No message was found in the notification request.
	 */
	public static final String FAIL =
		"http://vcgr.cs.virginia.edu/genii/2010/08/notification/fail";

	/**
	 * TRYAGAIN - if the notification is persistent, then the sender should wait briefly
	 * (use exponential fallback) and then resend the notification.
	 */
	public static final String TRYAGAIN =
		"http://vcgr.cs.virginia.edu/genii/2010/08/notification/tryagain";
	
	/**
	 * String -> URI
	 */
	public static URI toURI(String status)
	{
		try
		{
			return new URI(status);
		}
		catch (Exception exception)
		{
			try
			{
				return new URI(FAIL);
			}
			catch (Exception secondException)
			{
				return new URI();
			}
		}
	}
}
