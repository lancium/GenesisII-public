package edu.virginia.vcgr.genii.client.security.gamlauthz;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PermissionDeniedException extends AuthZSecurityException
{
	static final long serialVersionUID = 0L;
	
	static final private String PERMISSION_DENIED_MESSAGE_FORMAT =
		"Access denied for method %s.";
	
	static final private Pattern EXTRACTOR_PATTERN = Pattern.compile(
		"^.*Access denied for method ([^ .]+)\\..*$", Pattern.DOTALL);
	
	public PermissionDeniedException(String methodName)
	{
		super(String.format(PERMISSION_DENIED_MESSAGE_FORMAT, methodName));
	}
	
	static public String extractMethodName(String message)
	{
		Matcher matcher = EXTRACTOR_PATTERN.matcher(message);
		if (matcher.matches())
			return matcher.group(1);
		
		return null;
	}
}