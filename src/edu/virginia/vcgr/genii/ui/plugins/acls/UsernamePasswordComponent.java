package edu.virginia.vcgr.genii.ui.plugins.acls;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.datatransfer.Transferable;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.TransferHandler;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import edu.virginia.vcgr.genii.security.credentials.identity.UsernamePasswordIdentity;
import edu.virginia.vcgr.genii.ui.UIContext;

class UsernamePasswordComponent extends JPanel
{
	static final long serialVersionUID = 0L;
	
	private UIContext _uiContext;
	
	private JTextField _username = new JTextField(16);
	private JPasswordField _password = new JPasswordField(32);
	private PersonComponent _person = new PersonComponent();
	
	private void resetDraggable()
	{
		if ( (_username.getText().length() > 0) &&
			(_password.getPassword().length > 0) )
			_person.setEnabled(true);
		else
			_person.setEnabled(false);
	}
	
	UsernamePasswordComponent(UIContext context)
	{
		super(new GridBagLayout());
		
		_uiContext = context;
		
		add(new JLabel("Username"), new GridBagConstraints(
			0, 0, 1, 1, 0.0, 1.0,
			GridBagConstraints.EAST, GridBagConstraints.NONE,
			new Insets(5, 5, 5, 5), 5, 5));
		add(_username, new GridBagConstraints(
			1, 0, 1, 1, 1.0, 1.0,
			GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
			new Insets(5, 5, 5, 5), 5, 5));
		add(new JLabel("Password"), new GridBagConstraints(
			0, 1, 1, 1, 0.0, 1.0,
			GridBagConstraints.EAST, GridBagConstraints.NONE,
			new Insets(5, 5, 5, 5), 5, 5));
		add(_password, new GridBagConstraints(
			1, 1, 1, 1, 1.0, 1.0,
			GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
			new Insets(5, 5, 5, 5), 5, 5));
		
		add(_person, new GridBagConstraints(
			0, 2, 2, 1, 1.0, 1.0,
			GridBagConstraints.EAST, GridBagConstraints.NONE,
			new Insets(5, 5, 5, 5), 5, 5));
		
		CaretListenerImpl listener = new CaretListenerImpl();
		_username.addCaretListener(listener);
		_password.addCaretListener(listener);
		
		Dimension d = getPreferredSize();
		d.width = 200;
		
		setPreferredSize(d);
		setMinimumSize(d);
		
		_person.setTransferHandler(new UsernamePasswordTransferHandler());
		
		resetDraggable();
	}
	
	private class CaretListenerImpl implements CaretListener
	{
		static final long serialVersionUID = 0L;
		
		@Override
		public void caretUpdate(CaretEvent e)
		{
			resetDraggable();
		}
	}
	
	private class UsernamePasswordTransferHandler extends TransferHandler
	{
		static final long serialVersionUID = 0L;
		
		@Override
		protected Transferable createTransferable(JComponent c)
		{
			return new ACLTransferable(new ACLEntryWrapperTransferData(
				null, new ACLEntryWrapper(_uiContext,
				new UsernamePasswordIdentity(
					_username.getText(),
					new String(_password.getPassword()), true))));
		}

		@Override
		public int getSourceActions(JComponent c)
		{
			return LINK;
		}
	}
}