package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.IOException;
import java.text.ParseException;

import javax.xml.namespace.QName;

import edu.virginia.vcgr.genii.client.WellKnownPortTypes;
import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.io.FileResource;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;
import edu.virginia.vcgr.genii.client.rns.RNSUtilities;
import edu.virginia.vcgr.genii.client.utils.dialog.DialogException;
import edu.virginia.vcgr.genii.client.utils.dialog.GenericQuestionWidget;
import edu.virginia.vcgr.genii.client.utils.dialog.PasswordWidget;
import edu.virginia.vcgr.genii.client.utils.dialog.WidgetProvider;
import edu.virginia.vcgr.genii.client.utils.dialog.text.TextWidgetProvider;
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
	public void setLogin_Name(String loginName)
	{
		_loginName = loginName;
	}
	
	/**
	 * Set the password for the new IDP instance.
	 * 
	 * @param password The new password
	 */
	public void setLogin_Password(String password)
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
		TextWidgetProvider twp = new TextWidgetProvider(stdout, stderr, stdin);
		
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
		
		try
		{
			GamlChmodTool chmodTool = new GamlChmodTool();
			chmodTool.addArgument(fullPath);
			chmodTool.addArgument("+x");
			chmodTool.setUsername(_loginName);
			chmodTool.setPassword(_password);
			
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
	protected RNSPath getIDPServicePathFromUser(WidgetProvider wp)
		throws DialogException, IOException
	{
		GenericQuestionWidget widget = wp.createGenericQuestionDialog(
			"IDP Service Path");
		widget.setDetailedHelp(new FileResource(
			"edu/virginia/vcgr/genii/client/cmd/tools/resources/create-user-idp-path-help.txt"));
		widget.setPrompt("IDP service to use?");
		
		while (true)
		{
			try
			{
				widget.showWidget();
				String answer = widget.getAnswer();
				if (answer.equalsIgnoreCase("CANCEL"))
					return null;
				
				RNSPath idpService = RNSUtilities.findService(
					"/containers/BootstrapContainer",
					"X509AuthnPortType",
					new QName[] { WellKnownPortTypes.X509_AUTHN_SERVICE_PORT_TYPE },
					answer);
				return idpService;
			}
			catch (DialogException de)
			{
				throw de;
			}
			catch (Throwable cause)
			{
				widget.showErrorMessage("Unable to locate IDP service.  " +
					"Please try again or type \"Cancel\" to quit.");
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
	protected String getIDPNameFromUser(WidgetProvider wp, RNSPath idpServicePath)
		throws DialogException
	{
		GenericQuestionWidget widget = wp.createGenericQuestionDialog(
			"New Instance IDP Name");
		widget.setDetailedHelp(
			"The IDP name is the new name inside the IDP service by \n" +
			"which this instance will be known.");
		widget.setPrompt("New IDP Instance Name?");
		
		while (true)
		{
			try
			{
				widget.showWidget();
				String answer = widget.getAnswer();
				if (answer == null || answer.length() == 0)
				{
					widget.showErrorMessage(
						"Please enter a new name to give the IDP service (or Cancel to quit).");
					continue;
				}
				
				if (answer.equalsIgnoreCase("CANCEL"))
					return null;
				
				RNSPath namePath = idpServicePath.lookup(answer, RNSPathQueryFlags.DONT_CARE);
				if (namePath.exists())
				{
					widget.showErrorMessage("Name \"" + answer + 
						"\" already exists in the IDP service.");
					continue;
				}
				
				return answer;
			}
			catch (DialogException de)
			{
				throw de;
			}
			catch (Throwable cause)
			{
				widget.showErrorMessage("Unable to locate IDP service.  " +
					"Please try again or type \"Cancel\" to quit.");
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
	protected String getLoginNameFromUser(WidgetProvider wp)
		throws DialogException
	{
		GenericQuestionWidget widget = wp.createGenericQuestionDialog(
		"Login user name");
		widget.setDetailedHelp(
			"The login user name is the user name used when authenticating to\n"
			+ "the new IDP service to log in to it.");
		widget.setPrompt("Login user name?");
		
		while (true)
		{
			widget.showWidget();
			String answer = widget.getAnswer();
			if (answer == null || answer.length() == 0)
			{
				widget.showErrorMessage(
					"Please supply a login user name (or type in Cancel to quit).");
				continue;
			}
			
			if (answer.equalsIgnoreCase("Cancel"))
				return null;
			
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
	protected String getLoginPasswordFromUser(WidgetProvider wp)
		throws DialogException
	{
		PasswordWidget widget = wp.createPasswordDialog("Login Password");
		widget.setDetailedHelp(
			"The login password is the password used when authenticating to\n"
			+ "the new IDP service to log in to it.");
		widget.setPrompt("Login password?");
		
		while (true)
		{
			widget.showWidget();
			String answer = widget.getAnswer();
			if (answer == null || answer.length() == 0)
			{
				widget.showErrorMessage(
					"Please supply a login password (or type in Cancel to quit).");
				continue;
			}
			
			if (answer.equalsIgnoreCase("Cancel"))
				return null;
			
			return answer;
		}
	}
}