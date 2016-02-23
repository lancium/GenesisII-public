package edu.virginia.vcgr.genii.client.security;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.security.axis.AuthZSecurityException;

public class PermissionDeniedException extends AuthZSecurityException
{
	static final long serialVersionUID = 0L;

	static final private String PERMISSION_DENIED_MESSAGE_FORMAT = GenesisIIConstants.ACCESS_DENIED_SENTINEL + " on %s (in method %s).";

	static final private Pattern METHOD_EXTRACTOR_PATTERN = Pattern.compile(
		"^.*" + GenesisIIConstants.ACCESS_DENIED_SENTINEL + " on .*.in method ([^ )]+).\\..*$", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
	static final private Pattern ASSET_EXTRACTOR_PATTERN = Pattern.compile(
		"^.*" + GenesisIIConstants.ACCESS_DENIED_SENTINEL + " on (.+) .in method .*\\..*$", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

	public PermissionDeniedException(String methodName, String assetDenied)
	{
		super(String.format(PERMISSION_DENIED_MESSAGE_FORMAT, assetDenied, methodName));
	}

	static public String extractMethodName(String message)
	{
		// Added 8/14/2015 by ASG
		if (message == null)
			return null;
		Matcher matcher = METHOD_EXTRACTOR_PATTERN.matcher(message);
		if (matcher.matches())
			return matcher.group(1);
		return null;
	}

	static public String extractAssetDenied(String message)
	{
		// Added 8/14/2015 by ASG
		if (message == null)
			return null;
		Matcher matcher = ASSET_EXTRACTOR_PATTERN.matcher(message);
		if (matcher.matches())
			return matcher.group(1);
		return null;
	}
}