package edu.virginia.vcgr.genii.client.security;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.virginia.vcgr.genii.client.security.axis.AuthZSecurityException;

public class PermissionDeniedException extends AuthZSecurityException
{
	static final long serialVersionUID = 0L;

	static final private String PERMISSION_DENIED_MESSAGE_FORMAT = "Access denied on %s (in method %s).";

	static final private Pattern METHOD_EXTRACTOR_PATTERN = Pattern.compile("^.*Access denied on .*.in method ([^ )]+).\\..*$",
		Pattern.DOTALL);
	static final private Pattern ASSET_EXTRACTOR_PATTERN = Pattern.compile("^.*Access denied on (.+) .in method .*\\..*$",
		Pattern.DOTALL);

	public PermissionDeniedException(String methodName, String assetDenied)
	{
		super(String.format(PERMISSION_DENIED_MESSAGE_FORMAT, assetDenied, methodName));
	}

	static public String extractMethodName(String message)
	{
		Matcher matcher = METHOD_EXTRACTOR_PATTERN.matcher(message);
		if (matcher.matches())
			return matcher.group(1);
		return null;
	}

	static public String extractAssetDenied(String message)
	{
		Matcher matcher = ASSET_EXTRACTOR_PATTERN.matcher(message);
		if (matcher.matches())
			return matcher.group(1);
		return null;
	}
}