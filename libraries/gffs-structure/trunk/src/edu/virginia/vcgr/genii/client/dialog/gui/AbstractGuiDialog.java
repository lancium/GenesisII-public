package edu.virginia.vcgr.genii.client.dialog.gui;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.morgan.util.GraphicsUtils;

import edu.virginia.vcgr.genii.client.dialog.Dialog;
import edu.virginia.vcgr.genii.client.dialog.DialogException;
import edu.virginia.vcgr.genii.client.dialog.TextContent;
import edu.virginia.vcgr.genii.client.dialog.UserCancelException;

abstract class AbstractGuiDialog extends JDialog implements Dialog
{
	static final long serialVersionUID = 0L;

	private TextContent _help;
	private JButton _helpButton;

	protected JComponent _body;
	protected boolean _cancelled = true;

	protected Action _okAction;
	protected Action _cancelAction;

	protected AbstractGuiDialog(String title, Object... bodyParameters)
	{
		setTitle(title);

		Container container = getContentPane();
		container.setLayout(new GridBagLayout());

		_helpButton = new HelpButton();
		_helpButton.setEnabled(false);
		_helpButton.addActionListener(new HelpButtonActionListener());

		container.add(_body = createBody(bodyParameters), new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 5, 5));
		container.add(createButtonBar(), new GridBagConstraints(0, 2, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER,
			GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 5, 5));
		container.add(_helpButton, new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0, GridBagConstraints.EAST,
			GridBagConstraints.NONE, new Insets(5, 0, 0, 0), 0, 0));

		setModalityType(ModalityType.APPLICATION_MODAL);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	}

	protected abstract JComponent createBody(Object[] parameters);

	protected BottomButtonBar createButtonBar()
	{
		return new BottomButtonBar(_okAction = new OKAction(), _cancelAction = new CancelAction());
	}

	protected void okCalled()
	{
	}

	protected void cancelCalled()
	{
	}

	@Override
	public TextContent getHelp()
	{
		return _help;
	}

	@Override
	public void setHelp(TextContent helpContent)
	{
		_help = helpContent;
		toggleHelpButton();
	}

	private void toggleHelpButton()
	{
		try {
			if (!SwingUtilities.isEventDispatchThread()) {
				SwingUtilities.invokeAndWait(new Runnable()
				{
					public void run()
					{
						toggleHelpButton();
					}
				});
			}

			_helpButton.setEnabled(_help != null);
		} catch (InvocationTargetException ite) {
			throw new RuntimeException("Unexpected exception.", ite.getCause());
		} catch (InterruptedException ie) {
			throw new RuntimeException("Unexpectedly interrupted.", ie);
		}
	}

	protected void displayHelp()
	{
		JOptionPane.showMessageDialog(_helpButton, _help, "Help", JOptionPane.INFORMATION_MESSAGE, HelpButton.getHelpIcon());
	}

	private class HelpButtonActionListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent event)
		{
			displayHelp();
		}
	}

	protected class OKAction extends AbstractAction
	{
		static final long serialVersionUID = 0L;

		public OKAction()
		{
			super("OK");
		}

		@Override
		public void actionPerformed(ActionEvent event)
		{
			_cancelled = false;
			okCalled();
			dispose();
		}
	}

	protected class CancelAction extends AbstractAction
	{
		static final long serialVersionUID = 0L;

		public CancelAction()
		{
			super("Cancel");
		}

		@Override
		public void actionPerformed(ActionEvent event)
		{
			_cancelled = true;
			cancelCalled();
			dispose();
		}
	}

	@Override
	public void showDialog() throws DialogException, UserCancelException
	{
		pack();
		GraphicsUtils.centerWindow(this);
		setVisible(true);

		if (_cancelled)
			throw new UserCancelException();
	}
}