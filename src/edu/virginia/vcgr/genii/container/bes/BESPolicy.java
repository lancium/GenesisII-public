package edu.virginia.vcgr.genii.container.bes;

import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;

import org.apache.axis.message.MessageElement;

import edu.virginia.vcgr.genii.client.bes.BESConstants;

public class BESPolicy
{
	private BESPolicyActions _userLoggedInAction;
	private BESPolicyActions _screenSaverInactiveAction;
	
	public BESPolicy(BESPolicyActions userLoggedInAction, 
		BESPolicyActions screenSaverInactiveAction)
	{
		_userLoggedInAction = userLoggedInAction;
		_screenSaverInactiveAction = screenSaverInactiveAction;
	}

	public BESPolicyActions getUserLoggedInAction()
	{
		return _userLoggedInAction;
	}

	public void setUserLoggedInAction(BESPolicyActions loggedInAction)
	{
		_userLoggedInAction = loggedInAction;
	}

	public BESPolicyActions getScreenSaverInactiveAction()
	{
		return _screenSaverInactiveAction;
	}

	public void setScreenSaverInactiveAction(BESPolicyActions saverInactiveAction)
	{
		_screenSaverInactiveAction = saverInactiveAction;
	}
	
	public BESPolicyActions getCurrentAction(
		Boolean userLoggedIn, Boolean screenSaverActive)
	{
		boolean uli = (userLoggedIn == null) ? 
			false : userLoggedIn.booleanValue();
		boolean ssia = (screenSaverActive == null) ?
			false : !screenSaverActive.booleanValue();
		
		BESPolicyActions uliAction = BESPolicyActions.NOACTION;
		BESPolicyActions ssiaAction = BESPolicyActions.NOACTION;
		
		if (uli)
			uliAction = _userLoggedInAction;
		if (ssia)
			ssiaAction = _screenSaverInactiveAction;
		
		int comp = uliAction.compareTo(ssiaAction);
		if (comp > 0)
			return uliAction;
		
		return ssiaAction;
	}
	
	public MessageElement toMessageElement(QName elementName)
	{
		MessageElement ret = new MessageElement(elementName);
			
		MessageElement loggedIn = new MessageElement(
			new QName(BESConstants.GENII_BES_NS, "user-logged-in-action"),
			getUserLoggedInAction().toString());
		MessageElement screenSaverInactive = new MessageElement(
			new QName(BESConstants.GENII_BES_NS, "screensaver-inactive-action"),
			getScreenSaverInactiveAction().toString());
			
		try
		{
			ret.addChild(loggedIn);
			ret.addChild(screenSaverInactive);
		}
		catch (SOAPException se)
		{
			throw new RuntimeException(
				"Unexpected exception thrown while packageing policy.");
		}
			
		return ret;
	}
	
	public String toString()
	{
		return String.format("User Logged In Action:  %s\nScreensaver Inactive Action:  %s",
			_userLoggedInAction, _screenSaverInactiveAction);
	}
	
	static public BESPolicy fromMessageElement(MessageElement element)
	{
		BESPolicyActions userLoggedInAction = BESPolicyActions.NOACTION;
		BESPolicyActions screenSaverInactiveAction = BESPolicyActions.NOACTION;
		
		Iterator<?> iter = element.getChildElements();
		while (iter.hasNext())
		{
			MessageElement child = (MessageElement)iter.next();
			QName childName = child.getQName();
			
			if (childName.equals(new QName(
				BESConstants.GENII_BES_NS, "user-logged-in-action")))
			{
				userLoggedInAction = BESPolicyActions.valueOf(child.getValue());
			} else if (childName.equals(new QName(
				BESConstants.GENII_BES_NS, "screensaver-inactive-action")))
			{
				screenSaverInactiveAction = BESPolicyActions.valueOf(
					child.getValue());
			}
		}
		
		return new BESPolicy(userLoggedInAction, screenSaverInactiveAction);
	}
}