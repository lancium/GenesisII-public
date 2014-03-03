package edu.virginia.vcgr.genii.ui.login;

import java.awt.event.ActionEvent;

import javax.swing.JMenu;

import edu.virginia.vcgr.genii.ui.UIContext;

public class CredentialManagementJMenu extends JMenu
{
	static final long serialVersionUID = 0L;

	static private class CredentialManagementActionImpl extends CredentialManagementAction
	{
		static final long serialVersionUID = 0L;

		private CredentialManagementActionImpl(CredentialManagementContext context)
		{
			super(context);
		}

		@Override
		final public void actionPerformed(ActionEvent event)
		{
			// Nothing to do.
		}
	}

	CredentialManagementJMenu(CredentialManagementContext context)
	{
		super(new CredentialManagementActionImpl(context));

		add(context.loginAction());
		add(context.logoutAction());
		add(context.logoutAllAction());
	}

	public CredentialManagementJMenu(UIContext context)
	{
		this(new CredentialManagementContext(context));
	}
}
