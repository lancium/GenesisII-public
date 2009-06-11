package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.IOException;
import java.text.ParseException;

import edu.virginia.vcgr.genii.client.WellKnownPortTypes;
import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.dialog.DialogException;
import edu.virginia.vcgr.genii.client.dialog.DialogFactory;
import edu.virginia.vcgr.genii.client.dialog.DialogProvider;
import edu.virginia.vcgr.genii.client.dialog.InputDialog;
import edu.virginia.vcgr.genii.client.dialog.TextContent;
import edu.virginia.vcgr.genii.client.dialog.UserCancelException;
import edu.virginia.vcgr.genii.client.dialog.validators.ChainedInputValidator;
import edu.virginia.vcgr.genii.client.dialog.validators.ContainsTextValidator;
import edu.virginia.vcgr.genii.client.dialog.validators.NonEmptyValidator;
import edu.virginia.vcgr.genii.client.io.FileResource;
import edu.virginia.vcgr.genii.client.resource.PortType;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;
import edu.virginia.vcgr.genii.client.rns.RNSUtilities;
import edu.virginia.vcgr.genii.client.utils.units.Duration;

/**
 * This is a rather complicated tool that acts as a front end to the idp tool.
 * It's purpose is to make the job of creating a new user from whole cloth
 * easier for the user.  It does this by making calls out to other tools like
 * the idp tool and the chmod tool.
 *  
 * @author mmm2a
 */
public class CreateUserTool extends BaseGridTool
{
	protected String _loginName = null;
	protected String _password = null;
	protected String _durationString = null;
	protected Duration _duration = null;
	
	/**
	 * Construct a new create-user tool
	 */
	public CreateUserTool()
	{
		this("Creates a new user using the IDP authentication service.",
			new FileResource(
				"edu/virginia/vcgr/genii/client/cmd/tools/resources/create-user-usage.txt"), 
			false);
	}
	
	/**
	 * Construct a new create-user tool.
	 * 
	 * @param description The description to give this tool
	 * @param usageResource The resource that contains the usage for this tool.
	 * @param isHidden Whether or not this tool should be hidden from users.
	 */
	protected CreateUserTool(String description, FileResource usageResource, 
		boolean isHidden)
	{
		super(description, usageResource, isHidden);
	}
	
	/**
	 * Construct a new create-user tool.
	 * 
	 * @param description The description to give this tool
	 * @param usage The usage for this tool.
	 * @param isHidden Whether or not this tool should be hidden from users.
	 */
	protected CreateUserTool(String description, String usage, 
		boolean isHidden)
	{
		super(description, usage, isHidden);
	}
	
	/**
	 * Set the login name for the new IDP service instance.
	 * @param loginName The new login name.
	 */
	public void setLogin_name(String loginName)
	{
		_loginName = loginName;
	}
	
	/**
	 * Set the password for the new IDP instance.
	 * 
	 * @param password The new password
	 */
	public void setLogin_password(String password)
	{
		_password = password;
	}
	
	/**
	 * Set the valid duration of the certificate for the new IDP instance.
	 * 
	 * @param validDuration The valid duration for this tool.  This string is
	 * a formatted duration string.  See Genesis II wiki page for a 
	 * description.
	 */
	public void setValidDuration(String validDuration)
	{
		_durationString = validDuration;
	}
	
	@Override
	protected int runCommand() throws Throwable
	{
		DialogProvider twp = DialogFactory.getProvider(stdout, stderr, stdin, useGui());
		
		String idpServicePath = getArgument(0);
		String idpName = getArgument(1);
		
		RNSPath idpServiceRNS;
		
		if (idpServicePath == null)
		{
			idpServiceRNS = getIDPServicePathFromUser(twp);
			if (idpServiceRNS == null)
				return 0;
		} else
			idpServiceRNS = RNSPath.getCurrent().lookup(
				idpServicePath, RNSPathQueryFlags.MUST_EXIST);
		if (idpName == null)
		{
			idpName = getIDPNameFromUser(twp, idpServiceRNS);
			if (idpName == null)
				return 0;
		}
		
		if (_loginName == null)
		{
			_loginName = getLoginNameFromUser(twp);
			if (_loginName == null)
				return 0;
		}
		
		if (_password == null)
		{
			_password = getLoginPasswordFromUser(twp);
			if (_password == null)
				return 0;
		}
		
		stdout.println("Going to create an IDP instance:\n");
		stdout.println("\tIDP Service:  " + idpServiceRNS.pwd());
		stdout.println("\tIDP Name:     " + idpName);
		stdout.println("\tLogin Name:   " + _loginName);
		stdout.println("\tLogin Pass:   *******");
		
		enactCreation(null, null, idpServiceRNS.pwd(), idpName);
		
		return 0;
	}

	@Override
	protected void verify() throws ToolException
	{
		int numArgs = numArguments();
		if (numArgs > 2)
			throw new InvalidToolUsageException("Too many arguments.");
		
		if (_durationString != null)
		{
			try
			{
				_duration = Duration.parse(_durationString);
			}
			catch (ParseException pe)
			{
				throw new ToolException("Unable to parse duration string.", pe);
			}
		}
	}
	
	/**
	 * Enact the steps necessary to create the new IDP instance.
	 * 
	 * @param storeType THe store type to get a source credential from, or NULL
	 * @param sourcePath the Source URI to get a source credential from, or NULL
	 * @param idpServicePath The path to the IDP service to use.
	 * @param idpName The new name to give the IDP instance inside the 
	 * IDP service.
	 * 
	 * @throws Throwable
	 */
	protected void enactCreation(String storeType, String sourcePath, 
		String idpServicePath, String idpName)
			throws Throwable
	{
		IdpTool idpTool = new IdpTool();
		if (storeType != null)
			idpTool.setStoretype(storeType);
		if (sourcePath != null)
			idpTool.addArgument(sourcePath);
		idpTool.addArgument(idpServicePath);
		idpTool.addArgument(idpName);
		if (_duration != null)
			idpTool.setValidDuration(_durationString);
		idpTool.run(stdout, stderr, stdin);
		
		String fullPath = idpServicePath + "/" + idpName;
		RNSPath.clearCacheEntry(fullPath);
		
		try
		{
			GamlChmodTool chmodTool = new GamlChmodTool();
			chmodTool.addArgument(fullPath);
			chmodTool.addArgument("+x");
			chmodTool.addArgument("--username=" + _loginName);
			chmodTool.addArgument("--password=" + _password);
			
			chmodTool.run(stdout, stderr, stdin);
		}
		catch (Throwable cause)
		{
			// Couldn't chmod the object, try to clean up
			stderr.println("Unable to set permissions on newly created idp " +
				"instance.  Attempting to clean up.");
			
			RmTool rm = new RmTool();
			rm.setForce();
			rm.addArgument(fullPath);
			rm.run(stdout, stderr, stdin);
			
			throw cause;
		}
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
	protected RNSPath getIDPServicePathFromUser(DialogProvider wp)
		throws DialogException, IOException, UserCancelException
	{
		InputDialog input = wp.createInputDialog("IDP Service Path", "IDP service to use?");
		input.setDefaultAnswer("");
		input.setHelp(new TextContent(new FileResource(
			"edu/virginia/vcgr/genii/client/cmd/tools/resources/create-user-idp-path-help.txt")));
		
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
			catch (Throwable cause)
			{
				wp.createErrorDialog("Unable to Locate IDP Service",
					new TextContent("Unable to locate IDP service.",
						"Please try again or type \"Cancel\" to quit.")).showDialog();
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
	protected String getIDPNameFromUser(DialogProvider wp, RNSPath idpServicePath)
		throws DialogException, UserCancelException
	{
		InputDialog input = wp.createInputDialog("New Instance IDP Name", 
			"New IDP instance name?");
		input.setHelp(new TextContent(
			"The IDP name is the new name inside the IDP service by",
			"which this instance will be known."));
		input.setInputValidator(new ChainedInputValidator(
			new NonEmptyValidator("You must supply a non-empty IDP instance name."),
			ContainsTextValidator.mustNotContainText("/", 
				"IDP instance names cannot contain the '/' character.")));
		
		while (true)
		{
			try
			{
				input.showDialog();
				String answer = input.getAnswer();
				
				RNSPath namePath = idpServicePath.lookup(answer);
				if (namePath.exists())
				{
					wp.createErrorDialog("Bad IDP Instance Name",
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
				wp.createErrorDialog("Bad IDP Service",
					new TextContent("Unable to locate IDP service.",
					"Please try again.")).showDialog();
			}
		}
	}
	
	/**
	 * Get the new login name from the user.
	 * 
	 * @param wp The widget provider to create dialog widgets from.
	 * @return The new login name.
	 * 
	 * @throws DialogException
	 */
	protected String getLoginNameFromUser(DialogProvider wp)
		throws DialogException, UserCancelException
	{
		InputDialog input = wp.createInputDialog("Login User Name", 
			"Login user name?");
		input.setHelp(new TextContent(
			"The login user name is the user name used when authenticating to",
			"the new IDP service to log in to it."));
		input.setInputValidator(new NonEmptyValidator(
			"You must supply a non-empty login name."));
		
		while (true)
		{
			input.showDialog();
			String answer = input.getAnswer();
			
			return answer;
		}
	}
	
	/**
	 * Get the new login password from the user.
	 * 
	 * @param wp The widget provider to create dialog widgets from.
	 * @return The new login password.
	 * 
	 * @throws DialogException
	 */
	protected String getLoginPasswordFromUser(DialogProvider wp)
		throws DialogException, UserCancelException
	{
		InputDialog input = wp.createHiddenInputDialog(
			"Login Password", "Login password?");
		input.setHelp(new TextContent(
			"The login password is the password used when authenticating to",
			"the new IDP service to log in to it."));
		input.setInputValidator(new NonEmptyValidator(
			"You must supply a non-empty password."));
		
		while (true)
		{
			input.showDialog();
			String answer = input.getAnswer();
			
			return answer;
		}
	}
}