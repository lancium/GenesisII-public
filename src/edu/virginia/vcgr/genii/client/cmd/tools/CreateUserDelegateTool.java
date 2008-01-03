package edu.virginia.vcgr.genii.client.cmd.tools;

import java.text.ParseException;

import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.io.FileResource;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;
import edu.virginia.vcgr.genii.client.utils.dialog.DialogException;
import edu.virginia.vcgr.genii.client.utils.dialog.GenericQuestionWidget;
import edu.virginia.vcgr.genii.client.utils.dialog.MenuChoice;
import edu.virginia.vcgr.genii.client.utils.dialog.MenuWidget;
import edu.virginia.vcgr.genii.client.utils.dialog.WidgetProvider;
import edu.virginia.vcgr.genii.client.utils.dialog.text.TextWidgetProvider;
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
				_duration = Duration.parse(_durationString);
			}
			catch (ParseException pe)
			{
				throw new ToolException("Unable to parse duration string.", pe);
			}
		}
	}
	
	@Override
	protected int runCommand() throws Throwable
	{
		TextWidgetProvider twp = new TextWidgetProvider(stdout, stderr, stdin);
		
		String sourceURI = null;
		int start = 0;
		
		if (_storeType == null)
			_storeType = getStoreTypeFromUser(twp);
		
		if (_storeType == null)
			return 0;
		
		if (_storeType.equalsIgnoreCase("WIN"))
			start = 0;
		else
		{
			sourceURI = getArgument(0);
			if (sourceURI == null)
			{
				sourceURI = getSourceURIFromUser(twp);
				if (sourceURI == null)
					return 0;
			}
			
			start = 1;
		}
		String idpServicePath = getArgument(start);
		String idpName = getArgument(start + 1);
		
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
	private String getStoreTypeFromUser(WidgetProvider wp)
		throws DialogException
	{
		MenuWidget menu = wp.createMenuDialog("Store Type Selection");
		menu.setDetailedHelp(
			"The source keystore format is the format of the keystore from\n"
			+ "which to retrieve the certificate that will be delegate to\n"
			+ "the new IDP instance.");
		
		boolean isWindows = System.getProperty("os.name").contains("Windows");
		if (isWindows)
			menu.setChoices(
				new MenuChoice("P", "PKCS12"), 
				new MenuChoice("J", "JKS"),
				new MenuChoice("W", "WIN"),
				new MenuChoice("x", "Cancel"));
		else
			menu.setChoices(
				new MenuChoice("P", "PKCS12"), 
				new MenuChoice("J", "JKS"),
				new MenuChoice("x", "Cancel"));
		
		menu.setPrompt("Source keystore format?");
		
		menu.showWidget();
		String answer = menu.getSelectedChoice().toString();
		if (answer.equalsIgnoreCase("Cancel"))
			return null;
		
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
	private String getSourceURIFromUser(WidgetProvider wp)
		throws DialogException
	{
		GenericQuestionWidget widget = wp.createGenericQuestionDialog(
			"Source URI");
		widget.setDetailedHelp(
			"The Source URI is the path to the source keystore from which\n"
			+ "a certificate will be delegated.");
		widget.setPrompt("Source keystore URI?");
		
		while (true)
		{
			widget.showWidget();
			String answer = widget.getAnswer();
			
			if (answer == null || answer.isEmpty())
			{
				widget.showErrorMessage(
					"Please enter the uri (path) for source certificate store (or Cancel to quit).");
				continue;
			}
			
			if (answer.equalsIgnoreCase("Cancel"))
				return null;
			
			return answer;
		}
	}
}