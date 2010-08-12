package edu.virginia.vcgr.genii.client.cmd.tools;

import java.util.LinkedList;

import edu.virginia.vcgr.appmgr.os.OperatingSystemType;
import edu.virginia.vcgr.genii.client.WellKnownPortTypes;
import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.dialog.ComboBoxDialog;
import edu.virginia.vcgr.genii.client.dialog.DialogException;
import edu.virginia.vcgr.genii.client.dialog.DialogFactory;
import edu.virginia.vcgr.genii.client.dialog.DialogProvider;
import edu.virginia.vcgr.genii.client.dialog.InputDialog;
import edu.virginia.vcgr.genii.client.dialog.MenuItem;
import edu.virginia.vcgr.genii.client.dialog.SimpleMenuItem;
import edu.virginia.vcgr.genii.client.dialog.TextContent;
import edu.virginia.vcgr.genii.client.dialog.UserCancelException;
import edu.virginia.vcgr.genii.client.dialog.validators.NonEmptyValidator;
import edu.virginia.vcgr.genii.client.io.FileResource;
import edu.virginia.vcgr.genii.client.resource.PortType;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSUtilities;
import edu.virginia.vcgr.genii.client.utils.units.Duration;

/**
 * This is almost the same tool as the create-user tool, except that it creates
 * a new IDP instance by delegating an existing credential to it.
 * 
 * @author mmm2a
 */
public class CreateUserDelegateTool extends CreateUserTool
{
	protected String _storeType = null;
	
	/**
	 * Construct a new create-user-delegate tool.
	 */
	public CreateUserDelegateTool()
	{
		super("Creates a new IDP instance based off of an existing credential.",
			new FileResource("edu/virginia/vcgr/genii/client/cmd/tools/resources/create-user-delegate-usage.txt"),
			false);
	}
	
	/**
	 * Set the store type to use as a source for this new idp instance.
	 * 
	 * @param storeType The new store type (one of WIN, JKS, PKCS12).
	 */
	@Option({"storetype"})
	public void setStoretype(String storeType)
	{
		_storeType = storeType;
	}
	
	@Override
	protected void verify() throws ToolException
	{
		int numArgs = numArguments();
		if (numArgs > 3)
			throw new InvalidToolUsageException("Too many arguments.");
		
		if (_storeType != null)
		{
			if (!_storeType.equalsIgnoreCase("WIN") &&
				!_storeType.equalsIgnoreCase("JKS") &&
				!_storeType.equalsIgnoreCase("PKCS12"))
				throw new InvalidToolUsageException();
				
		}
		
		if (_durationString != null)
		{
			try
			{
				_duration = new Duration(_durationString);
			}
			catch (IllegalArgumentException pe)
			{
				throw new ToolException("Unable to parse duration string.", pe);
			}
		}
	}
	
	@Override
	protected int runCommand() throws Throwable
	{
		DialogProvider provider = DialogFactory.getProvider(
			stdout, stderr, stdin, useGui());
		LinkedList<String> args = new LinkedList<String>(getArguments());
		
		String sourceURI = null;
		
		if (_storeType == null)
		{
			_storeType = getStoreTypeFromUser(provider);
			if (_storeType == null)
				return 0;
			if (!_storeType.equalsIgnoreCase("WIN"))
			{
				sourceURI = getSourceURIFromUser(provider);
				if (sourceURI == null)
					return 0;
			}
		} else
		{
			if (!_storeType.equalsIgnoreCase("WIN"))
			{
				if (args.isEmpty())
					throw new InvalidToolUsageException();
				sourceURI = args.pop();
			}
		}
		
		String idpServicePath = null;
		String idpName = null;
		
		if (!args.isEmpty())
		{
			idpServicePath = args.pop();
			if (!args.isEmpty())
				idpName = args.pop();
		}
		
		RNSPath idpServiceRNS;
		
		if (idpServicePath == null)
		{
			idpServiceRNS = getIDPServicePathFromUser(provider);
			if (idpServiceRNS == null)
				return 0;
		} else
			idpServiceRNS = RNSUtilities.findService(
				"/containers/BootstrapContainer", "X509AuthnPortType", 
				new PortType[] { WellKnownPortTypes.X509_AUTHN_SERVICE_PORT_TYPE },
				idpServicePath);
		
		if (idpName == null)
		{
			idpName = getIDPNameFromUser(provider, idpServiceRNS);
			if (idpName == null)
				return 0;
		}
		
		if (_loginName == null)
		{
			_loginName = getLoginNameFromUser(provider);
			if (_loginName == null)
				return 0;
		}
		
		if (_password == null)
		{
			_password = getLoginPasswordFromUser(provider);
			if (_password == null)
				return 0;
		}
		
		stdout.println("Going to create an IDP instance:\n");
		stdout.println("\tIDP Service:  " + idpServiceRNS.pwd());
		stdout.println("\tIDP Name:     " + idpName);
		stdout.println("\tLogin Name:   " + _loginName);
		stdout.println("\tLogin Pass:   *******");
		
		enactCreation(_storeType, sourceURI, idpServiceRNS.pwd(), idpName);
		
		return 0;
	}
	
	/**
	 * Ask the user to give us the store type to use.
	 * 
	 * @param wp The widget provider from which to create dialog widgets.
	 * @return The store type (or null for a cancel).
	 * 
	 * @throws DialogException
	 */
	private String getStoreTypeFromUser(DialogProvider wp)
		throws DialogException, UserCancelException
	{
		ComboBoxDialog menu;
		
		boolean isWindows = OperatingSystemType.getCurrent().isWindows();
		
		MenuItem pkcs12 = new SimpleMenuItem("P", "PKCS12");
		MenuItem jks = new SimpleMenuItem("J", "JKS");
		MenuItem win = new SimpleMenuItem("W", "WIN");
		
		if (isWindows)
			menu = wp.createComboBoxDialog("Store Type Selection", 
				"Source keystore format?", pkcs12,
				pkcs12, jks, win);
		else
			menu = wp.createComboBoxDialog("Store Type Selection", 
				"Source keystore format?", pkcs12,
				pkcs12, jks);
		
		menu.setHelp(new TextContent(
			"The source keystore format is the format of the keystore from",
			"which to retrieve the certificate that will be delegate to",
			"the new IDP instance."));
		
		menu.showDialog();
		String answer = menu.getSelectedItem().toString();
		
		return answer;
	}
	
	/**
	 * Retrieve the source certificate URI from the user.
	 * 
	 * @param wp The widget provider from which to create dialog widgets.
	 * @return The source URI from the user (or null to cancel).
	 * 
	 * @throws DialogException
	 */
	private String getSourceURIFromUser(DialogProvider wp)
		throws DialogException, UserCancelException
	{
		InputDialog input = wp.createInputDialog("Source URI", 
			"Source keystore URI?");
		input.setHelp(new TextContent(
			"The Source URI is the path to the source keystore from which",
			"a certificate will be delegated."));
		input.setInputValidator(new NonEmptyValidator("You must enter a source URI!"));
		
		while (true)
		{
			input.showDialog();
			String answer = input.getAnswer();
			
			return answer;
		}
	}
}