package edu.virginia.vcgr.genii.client.cmd.tools;

import java.util.prefs.BackingStoreException;

import edu.virginia.vcgr.genii.client.cmd.DebugExceptionHandler;
import edu.virginia.vcgr.genii.client.cmd.IExceptionHandler;
import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.SimpleExceptionHandler;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.configuration.UserPreferences;
import edu.virginia.vcgr.genii.client.dialog.ComboBoxDialog;
import edu.virginia.vcgr.genii.client.dialog.DialogException;
import edu.virginia.vcgr.genii.client.dialog.DialogFactory;
import edu.virginia.vcgr.genii.client.dialog.DialogProvider;
import edu.virginia.vcgr.genii.client.dialog.InformationDialog;
import edu.virginia.vcgr.genii.client.dialog.MenuItem;
import edu.virginia.vcgr.genii.client.dialog.RunnableMenuItem;
import edu.virginia.vcgr.genii.client.dialog.SimpleMenuItem;
import edu.virginia.vcgr.genii.client.dialog.TextContent;
import edu.virginia.vcgr.genii.client.dialog.UserCancelException;
import edu.virginia.vcgr.genii.client.dialog.YesNoDialog;
import edu.virginia.vcgr.genii.client.dialog.YesNoSelection;

public class UserPreferencesTool extends BaseGridTool
{
	static private final String _DESCRIPTION = 
		"Manipulates global user preferences for this machine.";
	static private final String _USAGE =
		"user-preferences"; 
	
	public UserPreferencesTool()
	{
		super(_DESCRIPTION, _USAGE, false);
	}
	
	@Override
	protected int runCommand() throws Throwable
	{
		DialogProvider provider = DialogFactory.getProvider(
			stdout, stderr, stdin, useGui());
		
		MenuItem viewCurrentUserPreferencesItem = new SimpleMenuItem(
			"V", new ViewCurrentUserPreferencesHandler());
		MenuItem changeExceptionHandlerItem = new SimpleMenuItem(
			"E", new ChangeExceptionHandler());
		MenuItem changeGUIPreferencesItem = new SimpleMenuItem(
			"G", new ChangeGUIPreferencesHandler());
		MenuItem quitItem = new SimpleMenuItem(
			"Q", "Quit Preferences Tool");
		
		ComboBoxDialog menu = provider.createSingleListSelectionDialog(
			"User Preferences", 
			"What would you like to do?", quitItem,
			viewCurrentUserPreferencesItem,
			changeExceptionHandlerItem,
			changeGUIPreferencesItem,
			quitItem);
		
		while (true)
		{
			menu.showDialog();
			MenuItem item = menu.getSelectedItem();
			
			if (item.getContent() instanceof String)
				break;
			
			try
			{
				((RunnableMenuItem)item.getContent()).run(provider);
			}
			catch (UserCancelException uce)
			{
				// Do nothing
			}
		}
		
		return 0;
	}

	@Override
	protected void verify() throws ToolException
	{
		if (numArguments() != 0)
			throw new InvalidToolUsageException();
	}
	
	static private class ViewCurrentUserPreferencesHandler 
		implements RunnableMenuItem
	{
		public void run(DialogProvider provider)
			throws DialogException, UserCancelException
		{
			String handlerString;
			IExceptionHandler handler = 
				UserPreferences.preferences().getExceptionHandler();
			if (handler instanceof SimpleExceptionHandler)
				handlerString = "Simple";
			else if (handler instanceof DebugExceptionHandler)
				handlerString = "Debug";
			else
				handlerString = "Unknown";
			
			boolean preferGUI =
				UserPreferences.preferences().preferGUI();
			
			InformationDialog dialog = provider.createInformationDialog(
				"Current User Preferences",
				new TextContent(
					String.format("Exception Handler:  %s", handlerString),
					String.format("Prefer GUI?  %s", preferGUI ? "Yes" : "No")));
			dialog.showDialog();
		}
		
		public String toString()
		{
			return "View Current User Preferences";
		}
	}
	
	static private class ChangeExceptionHandler 
		implements RunnableMenuItem
	{
		public void run(DialogProvider provider)
			throws DialogException, UserCancelException
		{
			MenuItem simple = new SimpleMenuItem(
				"S", "Simple Exception Handler");
			MenuItem debug = new SimpleMenuItem(
				"D", "Debug Exception Handler");
			
			ComboBoxDialog dialog = provider.createSingleListSelectionDialog(
				"New Exception Handler", 
				"Please select the new exception handler to use:", 
				null, simple, debug);
			dialog.showDialog();
			MenuItem selected = dialog.getSelectedItem();
			
			try
			{
				if (selected == simple)
					UserPreferences.preferences().setExceptionHandler(
						new SimpleExceptionHandler());
				else
					UserPreferences.preferences().setExceptionHandler(
						new DebugExceptionHandler());
			}
			catch (BackingStoreException bse)
			{
				bse.printStackTrace(System.err);
				throw new RuntimeException(
					"Unable to store user preferences.", bse);
			}
		}
		
		public String toString()
		{
			return "Change Exception Handler";
		}
	}
	
	static private class ChangeGUIPreferencesHandler 
		implements RunnableMenuItem
	{
		public void run(DialogProvider provider)
			throws DialogException, UserCancelException
		{
			YesNoDialog dialog = provider.createYesNoDialog(
				"Prefer GUI", 
				"Do you prefer using graphical interfaces when available?",
				YesNoSelection.YES);
			dialog.showDialog();
			
			try
			{
				UserPreferences.preferences().preferGUI(dialog.isYes());
			}
			catch (BackingStoreException bse)
			{
				bse.printStackTrace(System.err);
				throw new RuntimeException(
					"Unable to store user preferences.", bse);
			}
		}
		
		public String toString()
		{
			return "Change GUI Preferences";
		}
	}
}
