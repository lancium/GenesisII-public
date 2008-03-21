package edu.virginia.vcgr.genii.client.cmd.tools;

import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.bes.BESRP;
import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.io.FileResource;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;
import edu.virginia.vcgr.genii.client.rp.ResourcePropertyManager;
import edu.virginia.vcgr.genii.client.utils.dialog.MenuWidget;
import edu.virginia.vcgr.genii.client.utils.dialog.text.TextWidgetProvider;
import edu.virginia.vcgr.genii.container.bes.BESPolicy;
import edu.virginia.vcgr.genii.container.bes.BESPolicyActions;

public class BesPolicyTool extends BaseGridTool
{
	static final private String _DESCRIPTION =
		"Gets and sets the bes process policy.";
	static final private FileResource _USAGE =
		new FileResource("edu/virginia/vcgr/genii/client/cmd/tools/resources/bes-policy-usage.txt");
	
	private BESPolicyActions _userLoggedInAction = null;
	private BESPolicyActions _screenSaverInactiveAction = null;
	private boolean _query = false;
	
	public BesPolicyTool()
	{
		super(_DESCRIPTION, _USAGE, false);
	}
	
	public void setSet_user_logged_in(String action)
	{
		_userLoggedInAction = BESPolicyActions.valueOf(action);
	}
	
	public void setSet_screensaver_inactive(String action)
	{
		_screenSaverInactiveAction = BESPolicyActions.valueOf(action);
	}
	
	public void setQuery()
	{
		_query = true;
	}
	
	@Override
	protected int runCommand() throws Throwable
	{
		RNSPath bes = RNSPath.getCurrent().lookup(getArgument(0), 
			RNSPathQueryFlags.MUST_EXIST);
		
		if (_query)
			query(bes.getEndpoint());
		else if (_userLoggedInAction != null && _screenSaverInactiveAction != null)
			setPolicy(bes.getEndpoint(), 
				_userLoggedInAction, _screenSaverInactiveAction);
		else
		{
			TextWidgetProvider twp = new TextWidgetProvider(
				stdout, stderr, stdin);
			
			MenuWidget menu = twp.createMenuDialog("User Logged In Action");
			menu.setDetailedHelp(
				"Please select the action to take when a user logs in to the target computer.");
			menu.setPrompt("User Logged In Action?");
			menu.setChoices(BESPolicyActions.NOACTION,
				BESPolicyActions.SUSPEND, BESPolicyActions.SUSPENDORKILL,
				BESPolicyActions.KILL);
			menu.showWidget();
			_userLoggedInAction = (BESPolicyActions)menu.getSelectedChoice();
			if (_userLoggedInAction == null)
				return 0;
			
			menu = twp.createMenuDialog("Screensaver Inactive Action");
			menu.setDetailedHelp(
				"Please select the action to take when the screensaver is de-activated on the target computer.");
			menu.setPrompt("Screensaver Inactive Action?");
			menu.setChoices(BESPolicyActions.NOACTION,
				BESPolicyActions.SUSPEND, BESPolicyActions.SUSPENDORKILL,
				BESPolicyActions.KILL);
			menu.showWidget();
			_screenSaverInactiveAction = (BESPolicyActions)menu.getSelectedChoice();
			if (_screenSaverInactiveAction == null)
				return 0;
			
			setPolicy(bes.getEndpoint(),
				_userLoggedInAction, _screenSaverInactiveAction);
		}
		
		return 0;
	}
	
	@Override
	protected void verify() throws ToolException
	{
		if (numArguments() != 1)
			throw new InvalidToolUsageException();
		
		if (_query && (_userLoggedInAction != null || _screenSaverInactiveAction != null))
			throw new InvalidToolUsageException();
		
		boolean uL = (_userLoggedInAction == null);
		boolean sL = (_screenSaverInactiveAction == null);
		
		if (uL != sL)
			throw new InvalidToolUsageException();
	}
	
	private void query(EndpointReferenceType bes)
		throws Throwable
	{
		BESRP rp = (BESRP)ResourcePropertyManager.createRPInterface(
			bes, BESRP.class);
		
		BESPolicy policy = rp.getPolicy();
		stdout.println(policy.toString());
	}
	
	private void setPolicy(EndpointReferenceType bes,
		BESPolicyActions userLoggedInAction, 
		BESPolicyActions screenSaverInactiveAction)
		throws Throwable
	{
		BESRP rp = (BESRP)ResourcePropertyManager.createRPInterface(
			bes, BESRP.class);
		
		rp.setPolicy(new BESPolicy(userLoggedInAction, screenSaverInactiveAction));
	}
}