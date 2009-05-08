package edu.virginia.vcgr.genii.client.security;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.security.auth.x500.X500Principal;

public class X500PrincipalUtilities
{
	static final private Pattern cnExtractor = Pattern.compile(
		"^.*[cC][nN]=([^,]+),.*$");
	
	static private String getCN(X500Principal principal)
	{
		Matcher matcher = cnExtractor.matcher(principal.getName(
			X500Principal.RFC1779));
		if (!matcher.matches())
			return principal.getName(X500Principal.RFC1779);
		
		return matcher.group(1);
	}
	
	static public String describe(X500Principal principal, 
		VerbosityLevel verbosity)
	{
		if (principal == null)
			return "(no principal)";
		
		if (verbosity.compareTo(VerbosityLevel.HIGH) >= 0)
			return principal.toString();
		else
			return String.format("\"%s\"",
				getCN(principal));
	}
}