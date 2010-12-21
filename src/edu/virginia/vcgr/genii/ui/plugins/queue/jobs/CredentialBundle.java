package edu.virginia.vcgr.genii.ui.plugins.queue.jobs;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.client.security.SecurityUtils;
import edu.virginia.vcgr.genii.client.security.VerbosityLevel;
import edu.virginia.vcgr.genii.client.security.credentials.identity.Identity;
import edu.virginia.vcgr.genii.client.security.credentials.identity.UsernamePasswordIdentity;

class CredentialBundle implements Comparable<CredentialBundle>
{
	private String _value;
	private String _tooltip;
	
	static final private Pattern CN_PATTERN = Pattern.compile(
		"CN\\s*=\\s*([^\\s,]+)[\\s,]");
	
	static private String toToolTip(Collection<Identity> owners)
	{
		StringWriter writer = new StringWriter();
		PrintWriter pw = new PrintWriter(writer);
		boolean first = true;
		
		pw.println("<html><body>");
		for (Identity id : owners)
		{
			if (!first)
				pw.println("<BR>");
			first = false;
			
			pw.println(id.describe(VerbosityLevel.HIGH));
		}
		pw.println("</body></html>");
		
		StreamUtils.close(pw);
		StreamUtils.close(writer);
		
		return writer.toString();
	}

	static private String getUsefulCN(String text)
	{
		List<String> badMatches = new LinkedList<String>();
		List<String> goodMatches = new LinkedList<String>();
		String []toRet;
		
		Matcher matcher = CN_PATTERN.matcher(text);
		while (matcher.find())
		{
			String value = matcher.group(1);
			if (value.contains("PortType"))
				badMatches.add(value);
			else
				goodMatches.add(value);
		}
		
		if (goodMatches.size() > 0)
			toRet = goodMatches.toArray(new String[goodMatches.size()]);
		else if (badMatches.size() > 0)
			toRet = badMatches.toArray(new String[badMatches.size()]);
		else
			return "<unknown-identity>";
		
		Arrays.sort(toRet);
		StringBuilder builder = new StringBuilder();
		for (String value : toRet)
		{
			if (builder.length() > 0)
				builder.append(", ");
			builder.append(value);
		}
		
		return builder.toString();
	}
	
	static private String toOwnerString(Identity owner)
	{
		if (owner instanceof UsernamePasswordIdentity)
		{
			return String.format("%s/*****",
				((UsernamePasswordIdentity)owner).getUserName());
		} else
		{
			String text = owner.describe(VerbosityLevel.HIGH);
			return getUsefulCN(text);
		}
	}
	
	CredentialBundle(Collection<Identity> identities)
	{
		Collection<Identity> owners = SecurityUtils.filterCredentials(
			identities, SecurityUtils.GROUP_TOKEN_PATTERN,
			SecurityUtils.CLIENT_IDENTITY_PATTERN);
		Set<String> ownerSet = new HashSet<String>();
		
		for (Identity owner : owners)
			ownerSet.add(toOwnerString(owner));
		
		String []ownerStrings = ownerSet.toArray(new String[ownerSet.size()]);
		
		Arrays.sort(ownerStrings);
		StringBuilder ret = new StringBuilder();
		for (String oString : ownerStrings)
		{
			if (ret.length() > 0)
				ret.append("; ");
			ret.append(oString);
		}
		
		_value = ret.toString();
		_tooltip = toToolTip(owners);
	}
	
	final String tooltipText()
	{
		return _tooltip;
	}
	
	@Override
	final public String toString()
	{
		return _value;
	}

	@Override
	final public int compareTo(CredentialBundle o)
	{
		return toString().compareTo(o.toString());
	}
}