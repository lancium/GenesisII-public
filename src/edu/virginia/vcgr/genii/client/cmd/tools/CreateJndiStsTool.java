package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.IOException;
import java.util.*;

import org.apache.axis.message.MessageElement;

import edu.virginia.vcgr.genii.client.WellKnownPortTypes;
import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.dialog.DialogException;
import edu.virginia.vcgr.genii.client.dialog.DialogFactory;
import edu.virginia.vcgr.genii.client.dialog.DialogProvider;
import edu.virginia.vcgr.genii.client.dialog.InputDialog;
import edu.virginia.vcgr.genii.client.dialog.InputValidator;
import edu.virginia.vcgr.genii.client.dialog.TextContent;
import edu.virginia.vcgr.genii.client.dialog.UserCancelException;
import edu.virginia.vcgr.genii.client.io.FileResource;
import edu.virginia.vcgr.genii.client.resource.PortType;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSUtilities;
import edu.virginia.vcgr.genii.client.security.SecurityConstants;


/**
 * This is almost the same tool as the create-user tool, except that it creates
 * a new IDP instance by delegating an existing credential to it.
 * 
 * @author mmm2a
 */
public class CreateJndiStsTool extends BaseGridTool
{
	// Type of JNDI STS to create (LDAP|NIS)
	protected String _jndiType = null;
	
	protected String _host = null;
	
	// List of X500 distinguished names to use as creation parameters
	// for identifying nodes in the LDAP tree under which we'll search 
	// for users
	protected ArrayList<String> _searchBases = new ArrayList<String>();
	
	// NIS domain use
	protected String _domain = null;
	
	/**
	 * Construct a new create-user-delegate tool.
	 */
	public CreateJndiStsTool()
	{
		super("Creates a new IDP instance based off of an existing credential.",
			new FileResource("edu/virginia/vcgr/genii/client/cmd/tools/resources/create-jndi-sts-usage.txt"),
			false);
	}
	
	public void setJnditype(String jndiType) {
		_jndiType = jndiType.toUpperCase();
	}
	
	public void setSearchBase(String searchBase) {
		_searchBases.add(searchBase);
	}
	
	public void setDomain(String domain) {
		_domain = domain;
	}
	
	public void setHost(String host) {
		_host = host;
	}	

	@Override
	protected void verify() throws ToolException
	{
		int numArgs = numArguments();
		if (numArgs > 2)
			throw new InvalidToolUsageException("Too many arguments.");
		if (numArgs < 1)
			throw new InvalidToolUsageException("Too few arguments.");
		
		if (_jndiType != null)
		{
			if ((_jndiType == null) || 
					(!_jndiType.equalsIgnoreCase("NIS") &&
					 !_jndiType.equalsIgnoreCase("LDAP")))
				throw new InvalidToolUsageException("Invalid JNDI authentication service type");
				
		}
		if (_host == null) {
			throw new InvalidToolUsageException("Host address not specified");
		}
	}
	
	@Override
	protected int runCommand() throws Throwable
	{
		DialogProvider provider = DialogFactory.getProvider(
			stdout, stderr, stdin, useGui());
		
		String jndiAuthnPortTypePath = null;
		String stsName = null;
		LinkedList<String> args = new LinkedList<String>(getArguments());
		
		if (!args.isEmpty())
		{
			jndiAuthnPortTypePath = args.pop();
			if (!args.isEmpty())
				stsName = args.pop();
		}
		
		RNSPath jndiAuthnPortTypeRNS;
		if (jndiAuthnPortTypePath == null)
		{
			jndiAuthnPortTypeRNS = getJndiAuthnPortTypePathFromUser(provider);
			if (jndiAuthnPortTypeRNS == null)
				return 0;
		} else
			jndiAuthnPortTypeRNS = RNSUtilities.findService(
				"/containers/BootstrapContainer", "JNDIAuthnPortType", 
				new PortType[] { WellKnownPortTypes.JNDI_AUTHN_SERVICE_PORT_TYPE },
				jndiAuthnPortTypePath);
		
		if (stsName == null)
		{
			stsName = getStsNameFromUser(provider, jndiAuthnPortTypeRNS);
			if (stsName == null)
				return 0;
		}
		
		if (_jndiType.equals("NIS")) {
			stdout.println("Going to create a NIS STS instance:\n");
		} else {
			stdout.println("Going to create a LDAP STS instance:\n");
		}
		stdout.println("\tJNDI AuthN Service:  " + jndiAuthnPortTypeRNS.pwd());
		stdout.println("\tSTS Name:     " + stsName);
		
		
		ArrayList<MessageElement> constructionParms = new ArrayList<MessageElement>();
		// type
		constructionParms.add(new MessageElement(
				SecurityConstants.NEW_JNDI_STS_TYPE_QNAME, _jndiType));
		// new name
		constructionParms.add(new MessageElement(
				SecurityConstants.NEW_JNDI_STS_NAME_QNAME, stsName));
		// host
		constructionParms.add(new MessageElement(
				SecurityConstants.NEW_JNDI_STS_HOST_QNAME, _host));
		// ldap searchbases
		for (String searchBase : _searchBases) {
			constructionParms.add(new MessageElement(
					SecurityConstants.NEW_JNDI_STS_SEARCHBASE_QNAME, searchBase));
			
		}
		// nis domain
		if (_domain != null) {
			constructionParms.add(new MessageElement(
					SecurityConstants.NEW_JNDI_NISDOMAIN_QNAME, _domain));
		}
		
		// create the new idp resource and link it into context space
		CreateResourceTool.createInstance(
				jndiAuthnPortTypeRNS.getEndpoint(),
				null,						// no link needed 
				constructionParms.toArray(new MessageElement[0]));

		return 0;
	}
	

	
	/**
	 * Get the user to input the IDP service to use.
	 * 
	 * @param wp The widge provider to create dialog widgets from.
	 * @return The RNSPath to the IDP service to use.
	 * 
	 * @throws DialogException
	 * @throws IOException
	 */
	protected RNSPath getJndiAuthnPortTypePathFromUser(DialogProvider wp)
		throws DialogException, UserCancelException
	{
		InputDialog input = wp.createInputDialog(
			"IDP Service Path", "IDP service to use?");
		input.setHelp(new TextContent(new FileResource(
			"edu/virginia/vcgr/genii/client/cmd/tools/resources/create-user-idp-path-help.txt")));
		input.setDefaultAnswer("");
		
		while (true)
		{
			try
			{
				input.showDialog();
				String answer = input.getAnswer();
				
				RNSPath idpService = RNSUtilities.findService(
					"/containers/BootstrapContainer",
					"X509AuthnPortType",
					new PortType[] { WellKnownPortTypes.X509_AUTHN_SERVICE_PORT_TYPE },
					answer);
				return idpService;
			}
			catch (DialogException de)
			{
				throw de;
			}
			catch (UserCancelException uce)
			{
				throw uce;
			}
			catch (Throwable cause)
			{
				wp.createErrorDialog("Bad IDP Service", new TextContent(
					"Unable to locate IDP service.", "Please try again.")).showDialog();
			}
		}
	}
	
	/**
	 * Get the name of the IDP instance from the user.
	 * 
	 * @param wp The widget provider from which to create dialog widgets.
	 * @param idpServicePath The path to the IDP service that the instance
	 * will be created inside of.
	 * 
	 * @return The new IDP name.
	 * 
	 * @throws DialogException
	 */
	protected String getStsNameFromUser(DialogProvider wp,
		RNSPath idpServicePath) throws DialogException,
			UserCancelException
	{
		InputDialog dialog = wp.createInputDialog(
			"New Instance IDP Name", "New IDP instance name?");
		dialog.setHelp(new TextContent(
			"The IDP name is the new name inside the IDP service by",
			"which this instance will be known."));
		dialog.setInputValidator(new IDPInstanceInputValidator());
		
		while (true)
		{
			try
			{
				dialog.showDialog();
				String answer = dialog.getAnswer();
				RNSPath namePath = idpServicePath.lookup(answer);
				if (namePath.exists())
				{
					wp.createErrorDialog("IDP Instance Already Exists",
						new TextContent("Name \"" + answer + 
						"\" already exists in the IDP service.")).showDialog();
					continue;
				}
				
				return answer;
			}
			catch (DialogException de)
			{
				throw de;
			}
			catch (UserCancelException uce)
			{
				throw uce;
			}
			catch (Throwable cause)
			{
				wp.createErrorDialog("IDP Service Error", 
					new TextContent("Unable to locate IDP service.",
					"Please try again or type \"Cancel\" to quit.")).showDialog();
			}
		}
	}
	
	static private class IDPInstanceInputValidator implements InputValidator
	{
		@Override
		public String validateInput(String input)
		{
			if (input.length() == 0)
				return "Invalid IDP instance name.";
			if (input.contains("/"))
				return "Invalid IDP instance name.  " +
					"Names cannot contain '/' characters.";
			
			return null;
		}	
	}
}