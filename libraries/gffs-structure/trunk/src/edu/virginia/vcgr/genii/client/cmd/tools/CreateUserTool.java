package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.algorithm.time.TimeHelpers;
import edu.virginia.vcgr.genii.client.WellKnownPortTypes;
import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ReloadShellException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.configuration.DeploymentName;
import edu.virginia.vcgr.genii.client.configuration.Installation;
import edu.virginia.vcgr.genii.client.configuration.NamespaceDefinitions;
import edu.virginia.vcgr.genii.client.dialog.DialogException;
import edu.virginia.vcgr.genii.client.dialog.DialogFactory;
import edu.virginia.vcgr.genii.client.dialog.DialogProvider;
import edu.virginia.vcgr.genii.client.dialog.InputDialog;
import edu.virginia.vcgr.genii.client.dialog.TextContent;
import edu.virginia.vcgr.genii.client.dialog.UserCancelException;
import edu.virginia.vcgr.genii.client.dialog.validators.ChainedInputValidator;
import edu.virginia.vcgr.genii.client.dialog.validators.ContainsTextValidator;
import edu.virginia.vcgr.genii.client.dialog.validators.NonEmptyValidator;
import edu.virginia.vcgr.genii.client.io.LoadFileResource;
import edu.virginia.vcgr.genii.client.rcreate.CreationException;
import edu.virginia.vcgr.genii.client.resource.PortType;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;
import edu.virginia.vcgr.genii.client.rns.RNSUtilities;
import edu.virginia.vcgr.genii.client.rp.ResourcePropertyException;
import edu.virginia.vcgr.genii.client.security.axis.AuthZSecurityException;
import edu.virginia.vcgr.genii.client.utils.units.Duration;
import edu.virginia.vcgr.genii.security.identity.IdentityType;

/**
 * This is a rather complicated tool that acts as a front end to the idp tool. It's purpose is to
 * make the job of creating a new user from whole cloth easier for the user. It does this by making
 * calls out to other tools like the idp tool and the chmod tool.
 * 
 * @author mmm2a
 */
public class CreateUserTool extends BaseGridTool
{
	static private Log _logger = LogFactory.getLog(CreateUserTool.class);

	protected String _loginName = null;
	protected String _password = null;
	protected String _durationString = null;
	protected Duration _duration = null;

	static final private String _DESCRIPTION = "config/tooldocs/description/dcreate-user";
	static final private String _USAGE = "config/tooldocs/usage/ucreate-user";

	/**
	 * Construct a new create-user tool
	 */
	public CreateUserTool()
	{
		this(new LoadFileResource(_DESCRIPTION), new LoadFileResource(_USAGE), false);
	}

	/**
	 * Construct a new create-user tool.
	 * 
	 * @param description
	 *            The description to give this tool
	 * @param usageResource
	 *            The resource that contains the usage for this tool.
	 * @param isHidden
	 *            Whether or not this tool should be hidden from users.
	 */
	protected CreateUserTool(LoadFileResource description, LoadFileResource usageResource, boolean isHidden)
	{
		super(description, usageResource, isHidden, ToolCategory.SECURITY);
	}

	/**
	 * Set the login name for the new IDP service instance.
	 * 
	 * @param loginName
	 *            The new login name.
	 */
	@Option({ "login-name" })
	public void setLogin_name(String loginName)
	{
		_loginName = loginName;
	}

	/**
	 * Set the password for the new IDP instance.
	 * 
	 * @param password
	 *            The new password
	 */
	@Option({ "login-password" })
	public void setLogin_password(String password)
	{
		_password = password;
	}

	/**
	 * Set the valid duration of the certificate for the new IDP instance.
	 * 
	 * @param validDuration
	 *            The valid duration for this tool. This string is a formatted duration string. See
	 *            Genesis II wiki page for a description.
	 */
	@Option({ "validDuration" })
	public void setValidDuration(String validDuration)
	{
		_durationString = validDuration;
	}

	@Override
	protected int runCommand() throws ReloadShellException, ToolException, UserCancelException, RNSException,
		AuthZSecurityException, IOException, ResourcePropertyException, CreationException, InvalidToolUsageException,
		ClassNotFoundException, DialogException
	{
		DialogProvider twp = DialogFactory.getProvider(stdout, stderr, stdin, useGui());

		String idpServicePath = getArgument(0);
		String idpName = getArgument(1);

		RNSPath idpServiceRNS;

		if (idpServicePath == null) {
			idpServiceRNS = getIDPServicePathFromUser(twp);
			if (idpServiceRNS == null)
				return 0;
		} else
			idpServiceRNS = RNSPath.getCurrent().lookup(idpServicePath, RNSPathQueryFlags.MUST_EXIST);
		if (idpName == null) {
			idpName = getIDPNameFromUser(twp, idpServiceRNS);
			if (idpName == null)
				return 0;
		}

		if (_loginName == null) {
			_loginName = getLoginNameFromUser(twp);
			if (_loginName == null)
				return 0;
		}

		if (_password == null) {
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

		if (_durationString != null) {
			try {
				_duration = new Duration(_durationString);
			} catch (IllegalArgumentException pe) {
				throw new ToolException("Unable to parse duration string.", pe);
			}
		}
	}

	/**
	 * Enact the steps necessary to create the new IDP instance.
	 * 
	 * @param storeType
	 *            THe store type to get a source credential from, or NULL
	 * @param sourcePath
	 *            the Source URI to get a source credential from, or NULL
	 * @param idpServicePath
	 *            The path to the IDP service to use.
	 * @param idpName
	 *            The new name to give the IDP instance inside the IDP service.
	 * @throws ToolException
	 * @throws ReloadShellException
	 * @throws AuthZSecurityException
	 * 
	 * @throws Throwable
	 */
	protected void enactCreation(String storeType, String sourcePath, String idpServicePath, String idpName)
		throws RNSException, ToolException, ReloadShellException, AuthZSecurityException
	{
		IdpTool idpTool = new IdpTool();
		if (storeType != null)
			idpTool.setStoretype(storeType);
		if (sourcePath != null)
			idpTool.addArgument(sourcePath);

		// Set as user type
		idpTool.setCredentialType(IdentityType.USER);

		idpTool.addArgument(idpServicePath);
		idpTool.addArgument(idpName);
		if (_duration != null)
			idpTool.setValidDuration(_durationString);
		int retVal = idpTool.run(stdout, stderr, stdin);
		if (retVal != 0) {
			String msg = "failure during IDP creation: return value=" + retVal;
			_logger.error(msg);
			throw new AuthZSecurityException(msg);
		}

		stdout.println(String.format("\tLifetime:     %.2f days / %.2f years.",
			TimeHelpers.millisToDays(BaseGridTool.getValidMillis()), TimeHelpers.millisToYears(BaseGridTool.getValidMillis())));

		String fullPath = idpServicePath + "/" + idpName;

		try {
			ChmodTool chmodTool = new ChmodTool();
			chmodTool.addArgument(fullPath);
			chmodTool.addArgument("+x");
			chmodTool.addArgument("--username=" + _loginName);
			chmodTool.addArgument("--password=" + _password);

			retVal = chmodTool.run(stdout, stderr, stdin);
			if (retVal != 0) {
				String msg = "failure during chmod: return value=" + retVal;
				// no logging here, since exc. handler will print the error.
				throw new AuthZSecurityException(msg);
			}

		} catch (Throwable cause) {
			// Couldn't chmod the object, try to clean up
			String msg = "Unable to set permissions on newly created idp instance.  Attempting to clean up.";
			stderr.println(msg);

			RmTool rm = new RmTool();
			rm.setForce();
			rm.addArgument(fullPath);
			retVal = rm.run(stdout, stderr, stdin);
			if (retVal != 0) {
				String msg2 = "failure during cleanup rm: return value=" + retVal;
				_logger.error(msg2);
			}

			throw new ToolException(msg + ": " + cause.getLocalizedMessage(), cause);
		}
	}

	/**
	 * Get the user to input the IDP service to use.
	 * 
	 * @param wp
	 *            The widge provider to create dialog widgets from.
	 * @return The RNSPath to the IDP service to use.
	 * 
	 * @throws DialogException
	 * @throws IOException
	 */
	protected RNSPath getIDPServicePathFromUser(DialogProvider wp) throws DialogException, IOException, UserCancelException
	{
		InputDialog input = wp.createInputDialog("IDP Service Path", "IDP service to use?");
		input.setDefaultAnswer("");
		input.setHelp(new TextContent(new LoadFileResource("config/tooldocs/usage/ucreate-user-idp-path-help")));

		while (true) {
			try {
				input.showDialog();
				String answer = input.getAnswer();
				NamespaceDefinitions nsd = Installation.getDeployment(new DeploymentName()).namespace();
				RNSPath idpService =
					RNSUtilities.findService(nsd.getRootContainer(), "X509AuthnPortType",
						new PortType[] { WellKnownPortTypes.X509_AUTHN_SERVICE_PORT_TYPE() }, answer);
				return idpService;
			} catch (UserCancelException uce) {
				// they cancelled the entry.
				return null;
			} catch (DialogException de) {
				throw de;
			} catch (Throwable cause) {
				wp.createErrorDialog("Unable to Locate IDP Service",
					new TextContent("Unable to locate IDP service.", "Please try again or type \"Cancel\" to quit."))
					.showDialog();
			}
		}
	}

	/**
	 * Get the name of the IDP instance from the user.
	 * 
	 * @param wp
	 *            The widget provider from which to create dialog widgets.
	 * @param idpServicePath
	 *            The path to the IDP service that the instance will be created inside of.
	 * 
	 * @return The new IDP name.
	 * 
	 * @throws DialogException
	 */
	protected String getIDPNameFromUser(DialogProvider wp, RNSPath idpServicePath) throws DialogException, UserCancelException
	{
		InputDialog input = wp.createInputDialog("New Instance IDP Name", "New IDP instance name?");
		input.setHelp(new TextContent("The IDP name is the new name inside the IDP service by",
			"which this instance will be known."));
		input.setInputValidator(new ChainedInputValidator(new NonEmptyValidator(
			"You must supply a non-empty IDP instance name."), ContainsTextValidator.mustNotContainText("/",
			"IDP instance names cannot contain the '/' character.")));

		while (true) {
			try {
				input.showDialog();
				String answer = input.getAnswer();

				RNSPath namePath = idpServicePath.lookup(answer);
				if (namePath.exists()) {
					wp.createErrorDialog("Bad IDP Instance Name",
						new TextContent("Name \"" + answer + "\" already exists in the IDP service.")).showDialog();
					continue;
				}

				return answer;
			} catch (DialogException de) {
				throw de;
			} catch (UserCancelException uce) {
				throw uce;
			} catch (Throwable cause) {
				wp.createErrorDialog("Bad IDP Service", new TextContent("Unable to locate IDP service.", "Please try again."))
					.showDialog();
			}
		}
	}

	/**
	 * Get the new login name from the user.
	 * 
	 * @param wp
	 *            The widget provider to create dialog widgets from.
	 * @return The new login name.
	 * 
	 * @throws DialogException
	 */
	protected String getLoginNameFromUser(DialogProvider wp) throws DialogException, UserCancelException
	{
		InputDialog input = wp.createInputDialog("Login User Name", "Login user name?");
		input.setHelp(new TextContent("The login user name is the user name used when authenticating to",
			"the new IDP service to log in to it."));
		input.setInputValidator(new NonEmptyValidator("You must supply a non-empty login name."));

		while (true) {
			input.showDialog();
			String answer = input.getAnswer();

			return answer;
		}
	}

	/**
	 * Get the new login password from the user.
	 * 
	 * @param wp
	 *            The widget provider to create dialog widgets from.
	 * @return The new login password.
	 * 
	 * @throws DialogException
	 */
	protected String getLoginPasswordFromUser(DialogProvider wp) throws DialogException, UserCancelException
	{
		InputDialog input = wp.createHiddenInputDialog("Login Password", "Login password?");
		input.setHelp(new TextContent("The login password is the password used when authenticating to",
			"the new IDP service to log in to it."));
		input.setInputValidator(new NonEmptyValidator("You must supply a non-empty password."));

		while (true) {
			input.showDialog();
			String answer = input.getAnswer();

			return answer;
		}
	}
}
