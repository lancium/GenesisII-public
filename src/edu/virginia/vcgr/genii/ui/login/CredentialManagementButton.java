package edu.virginia.vcgr.genii.ui.login;

import javax.swing.JButton;

import edu.virginia.vcgr.genii.ui.UIContext;

public class CredentialManagementButton extends JButton
{
	static final long serialVersionUID = 0L;
	
	public CredentialManagementButton(UIContext context)
	{
		super(new PopupTriggerCredentialManagementAction(context));
	}
}