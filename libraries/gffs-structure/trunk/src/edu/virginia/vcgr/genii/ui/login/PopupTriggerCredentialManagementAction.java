package edu.virginia.vcgr.genii.ui.login;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;

import javax.swing.JPopupMenu;

import edu.virginia.vcgr.genii.ui.UIContext;

public class PopupTriggerCredentialManagementAction extends CredentialManagementAction
{
	static final long serialVersionUID = 0L;

	private JPopupMenu _popupMenu;

	PopupTriggerCredentialManagementAction(CredentialManagementContext context)
	{
		super(context);

		_popupMenu = new JPopupMenu("Credential Management");
		_popupMenu.add(context.loginAction());
		_popupMenu.add(context.logoutAction());
		_popupMenu.add(context.logoutAllAction());
	}

	public PopupTriggerCredentialManagementAction(UIContext context)
	{
		this(new CredentialManagementContext(context));
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		Component invoker = (Component) e.getSource();
		Point mousePosition = invoker.getMousePosition();
		if (mousePosition == null)
			mousePosition = new Point(0, 0);
		_popupMenu.show(invoker, mousePosition.x, mousePosition.y);
	}
}