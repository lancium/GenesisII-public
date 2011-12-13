/*
 * Copyright 2006 University of Virginia
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package edu.virginia.vcgr.genii.client.security.authz.acl;

import java.io.*;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.GeneralSecurityException;

import javax.security.auth.x500.X500Principal;

import org.morgan.util.cmdline.*;
import org.morgan.util.configuration.ConfigurationException;
import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.common.security.*;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;
import edu.virginia.vcgr.genii.client.byteio.ByteIOStreamFactory;
import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.naming.ResolverUtils;

import edu.virginia.vcgr.genii.client.security.authz.AuthZSecurityException;
import edu.virginia.vcgr.genii.client.security.authz.AuthZSubTool;
import edu.virginia.vcgr.genii.client.security.credentials.identity.*;

import org.ws.addressing.EndpointReferenceType;

/**
 * Implementation of the AuthZSubTool for the GII ACL authz provider.
 * 
 * @author dgm4d
 *
 */
public class AclAuthZClientTool implements AuthZSubTool
{

	static private final int NOT_READ = 32;
	static private final int NOT_WRITE = 16;
	static private final int NOT_EXECUTE = 8;
	static private final int READ = 4;
	static private final int WRITE = 2;
	static private final int EXECUTE = 1;

	static public final String CHMOD_SYNTAX =
			"<change-command> <acl-entry>\n" +
			"\n" +
			"\t where <change-command> is one of:\n" + 
			"\n" + 
			"\t\t<[<+|->r][<+|->w][<+|->x]> \n" + 
			"\n" + 
			"\t\t<octal mode> \n" +  			
			"\n" + 
			"\t and where <acl-entry> is one of:\n" + 
			"\n" + 
			"\t\t[--local-src] <cert-file> \n" + 
			"\n" + 
			"\t\t[--local-src] <ca-cert-file> --pattern=<DN-pattern> \n" + 
			"\t\t\t where <DN-pattern> is a (possibly-empty) set of \n" + 
			"\t\t\t comma-separated X.500 distinguished-name \n" + 
			"\t\t\t <attribute>=<value> components \n" + 
			"\n" + 
			"\t\t--everyone \n" + 
			"\n" + 
			"\t\t--username=<username> --password=<password>";

	public AuthZConfig getEmptyAuthZConfig() throws AuthZSecurityException
	{
		return Acl.encodeAcl(new Acl());
	}

	public void displayAuthZConfig(AuthZConfig config, PrintWriter out,
		PrintWriter err, BufferedReader in) throws AuthZSecurityException
	{

		if (config == null)
		{
			return;
		}

		Acl acl = Acl.decodeAcl(config);

		out.println("  Requires message-level encryption: "
				+ acl.requireEncryption);
		out.println();
		out.println("  Read-authorized trust certificates: ");
		int i = 0;
		for (AclEntry entry : acl.readAcl) 
		{
			if (entry == null)
			{
				out.println("    [" + i + "] EVERYONE");
			}
			else
			{
				out.println("    [" + i + "] " + entry);
			}
			i++;
		}
		out.println();
		out.println("  Write-authorized trust certificates: ");
		i = 0;
		for (AclEntry entry : acl.writeAcl) 
		{
			if (entry == null)
			{
				out.println("    [" + i + "] EVERYONE");
			}
			else
			{
				out.println("    [" + i + "] " + entry);
			}
			i++;
		}
		out.println();
		out.println("  Execute-authorized trust certificates: ");
		i = 0;
		for (AclEntry entry : acl.executeAcl) 
		{
			if (entry == null)
			{
				out.println("    [" + i + "] EVERYONE");
			}
			else
			{
				out.println("    [" + i + "] " + entry);
			}
			i++;
		}
	}

	public static int parseMode(String modeString)
			throws IllegalArgumentException
	{

		try
		{
			// this is precicely what they want: everything is either on or off
			return Integer.parseInt(modeString) | NOT_READ | NOT_WRITE
					| NOT_EXECUTE;
		}
		catch (NumberFormatException e)
		{
		}

		if (modeString.length() / 2 < 1)
		{
			throw new IllegalArgumentException();
		}

		int retval = 0;

		while (true)
		{
			int borderIndex;
			int plusIndex = modeString.indexOf('+', 1);
			int minusIndex = modeString.indexOf('-', 1);
			if (plusIndex == -1 && minusIndex == -1)
				borderIndex = modeString.length();
			else if (plusIndex > minusIndex)
				borderIndex = plusIndex;
			else
				borderIndex = minusIndex;

			switch (modeString.charAt(0))
			{
			case '+':
				for (int i = 1; i < borderIndex; i++)
				{
					if (modeString.charAt(i) == 'r')
						retval |= READ;
					else if (modeString.charAt(i) == 'w')
						retval |= WRITE;
					else if (modeString.charAt(i) == 'x')
						retval |= EXECUTE;
				}
				break;

			case '-':
				for (int i = 1; i < borderIndex; i++)
				{
					if (modeString.charAt(i) == 'r')
						retval |= NOT_READ;
					else if (modeString.charAt(i) == 'w')
						retval |= NOT_WRITE;
					else if (modeString.charAt(i) == 'x')
						retval |= NOT_EXECUTE;
				}
				break;

			default:
				throw new IllegalArgumentException();
			}
			if (borderIndex == (modeString.length()))
			{
				break;
			}
			else
			{
				modeString = modeString.substring(borderIndex);
			}
		}
		return retval;
	}

	public static X509Identity downloadIdentity(String sourcePath, boolean isLocalSource)
			throws ConfigurationException, FileNotFoundException, IOException,
			RNSException, GeneralSecurityException
	{

		if (isLocalSource)
		{
			InputStream in = new FileInputStream(sourcePath);
			;
			try
			{
				CertificateFactory cf = CertificateFactory.getInstance("X.509");
				X509Certificate cert =
						(X509Certificate) cf.generateCertificate(in);
				X509Certificate[] chain = { cert };
				return new X509Identity(chain);
			}
			finally
			{
				StreamUtils.close(in);
			}
		}

		RNSPath current = RNSPath.getCurrent();
		RNSPath path = current.lookup(sourcePath, RNSPathQueryFlags.MUST_EXIST);

		if (new TypeInformation(path.getEndpoint()).isByteIO())
		{
			// read the file as an encoded X.509 .cer file
			InputStream in = ByteIOStreamFactory.createInputStream(path);
			try
			{
				CertificateFactory cf = CertificateFactory.getInstance("X.509");
				X509Certificate cert =
						(X509Certificate) cf.generateCertificate(in);
				X509Certificate[] chain = { cert };
				return new X509Identity(chain);
			}
			finally
			{
				StreamUtils.close(in);
			}

		}
		else
		{
			// get the identity of the resource

			// make sure it's not an unbound epr
			EndpointReferenceType epr = path.getEndpoint();
			if (EPRUtils.isUnboundEPR(epr))
			{
				epr = ResolverUtils.resolve(epr);
			}

			X509Certificate[] chain = EPRUtils.extractCertChain(epr);
			return new X509Identity(chain);
		}

	}

	public AuthZConfig modifyAuthZConfig(
			AuthZConfig config, 
			PrintWriter out,
			PrintWriter err, 
			BufferedReader in) 
		throws IOException,	AuthZSecurityException
	{

		boolean chosen = false;
		while (!chosen)
		{
			out.println("\nOptions:");
			out.println("  [1] Toggle message-level encryption requirement");
			out.println("  [2] Modify access control lists");
			out.println("  [3] Cancel");
			out.print("Please make a selection: ");
			out.flush();

			String input = in.readLine();
			out.println();
			if (input == null)
				return null;
			
			int choice = 0;
			try
			{
				choice = Integer.parseInt(input);
			}
			catch (NumberFormatException e)
			{
				out.println("Invalid choice.");
				continue;
			}

			switch (choice)
			{
			case 1:
				Acl acl = Acl.decodeAcl(config);
				acl.requireEncryption = !acl.requireEncryption;
				config = Acl.encodeAcl(acl);
				chosen = true;
				break;
			case 2:
				CommandLine cLine;
				while (true)
				{
					out.println("Modification syntax: " + CHMOD_SYNTAX + "\n");
					out.print(">");
					out.flush();

					cLine = new CommandLine(in.readLine(), false);
					if (!validateChmodSyntax(cLine))
					{
						out.println("Invalid syntax");
						continue;
					}
					break;
				}
				
				if (cLine.isEmpty())
				{
					break;
				}
				
				config = chmod(cLine, config);
				
				chosen = true;
				break;
			case 3:
				chosen = true;
			}
		}

		return config;
	}

	public boolean validateChmodSyntax(ICommandLine cLine)
	{
		if (cLine.isEmpty()) 
		{
			return true;
		}
		
		// make sure we have the new perms and the cert file
		if (cLine.hasFlag("everyone"))
		{
			if ((cLine.numArguments() != 1) || (cLine.hasOption("local-src")))
			{
				return false;
			}
		}
		else if (cLine.hasOption("username"))
		{
			// make sure password also supplied
			if (!(cLine.hasOption("password") || cLine.hasOption("hashedpass")))
			{
				return false;
			}
		}
		else
		{
			if (cLine.numArguments() != 2)
			{
				return false;
			}
		}

		// make sure the new perms parses
		try
		{
			parseMode(cLine.getArgument(0));
		}
		catch (IllegalArgumentException e)
		{
			return false;
		}

		return true;
	}

	/**
	 * Parses the given command line and applies the indicated authz changes to
	 * the specified authz configuration. Assumes that the syntax is valid
	 * (having been checked with validateChmodSyntax())
	 */
	public AuthZConfig chmod(
			ICommandLine cLine,
			AuthZConfig config)		
		throws IOException, AuthZSecurityException
	{
		if (config.get_any() == null)
		{
			return config;
		}

		AclEntry newEntry = null;

		if (cLine.hasFlag("everyone")) 
		{
			// everyone (null)
		} 
		else if (cLine.hasOption("username")) 
		{
			// username password
			String password = cLine.getOptionValue("password");
			boolean hash = true;
			if (password == null){
				password = cLine.getOptionValue("hashedpass");
				hash = false;
			}

			newEntry = new UsernamePasswordIdentity(
					cLine.getOptionValue("username"), 
					password, hash);
		}
		else
		{
			// X.509 explicit or pattern
			try
			{
				X509Identity identity = downloadIdentity(
						cLine.getArgument(1),
						cLine.hasFlag("local-src"));

				if (cLine.hasOption("pattern")) 
				{
					// create a X509PatternAclEntry

					String pattern = cLine.getOptionValue("pattern");
					if (pattern == null) 
					{
						// handle case if pattern is empty
						newEntry = new X509PatternAclEntry(
								identity,
								null);
					} 
					else 
					{
						newEntry = new X509PatternAclEntry(
								identity,
								new X500Principal(pattern));
					}
				} 
				else 
				{
					// simply use the X509Identity explicitly as the acl entry
					
					newEntry = identity;
				}
			}
			catch (ConfigurationException e)
			{
				throw new AuthZSecurityException(
						"Could not load certificate file.", e);
			}
			catch (FileNotFoundException e)
			{
				throw new AuthZSecurityException(
						"Could not load certificate file.", e);
			}
			catch (RNSException e)
			{
				throw new AuthZSecurityException(
						"Could not load certificate file.", e);
			}
			catch (GeneralSecurityException e)
			{
				throw new AuthZSecurityException(
						"Could not load certificate file.", e);
			}
		}

		return chmod(config, cLine.getArgument(0), newEntry);
	}


	/**
	 * Parses the given command line and applies the indicated authz changes to
	 * the specified authz configuration. Assumes that the syntax is valid
	 * (having been checked with validateChmodSyntax())
	 */
	public AuthZConfig chmod(
			AuthZConfig config, 		
			String permission,
			AclEntry newEntry) 
		throws IOException, AuthZSecurityException
	{			
		Acl acl = Acl.decodeAcl(config);

		int mode = parseMode(permission);

		if ((mode & READ) > 0)
		{
			if (!acl.readAcl.contains(newEntry))
				acl.readAcl.add(newEntry);
		}
		else if ((mode & NOT_READ) > 0)
		{
			acl.readAcl.remove(newEntry);
		}

		if ((mode & WRITE) > 0)
		{
			if (!acl.writeAcl.contains(newEntry))
				acl.writeAcl.add(newEntry);
		}
		else if ((mode & NOT_WRITE) > 0)
		{
			acl.writeAcl.remove(newEntry);
		}

		if ((mode & EXECUTE) > 0)
		{
			if (!acl.executeAcl.contains(newEntry))
				acl.executeAcl.add(newEntry);
		}
		else if ((mode & NOT_EXECUTE) > 0)
		{
			acl.executeAcl.remove(newEntry);
		}

		return Acl.encodeAcl(acl);
	}
}