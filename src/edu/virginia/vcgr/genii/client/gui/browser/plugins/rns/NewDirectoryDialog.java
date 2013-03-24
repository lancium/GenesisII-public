package edu.virginia.vcgr.genii.client.gui.browser.plugins.rns;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.DefaultFormatter;

import edu.virginia.vcgr.genii.client.gui.GuiUtils;

/**
 * This dialog box is displayed when the user has requested that a new directory be created. It asks
 * the user to give a name to that new directory.
 * 
 * @author mmm2a
 */
class NewDirectoryDialog extends JDialog
{
	static final long serialVersionUID = 0L;

	private JFormattedTextField _input;

	/**
	 * Create a new directory dialog with the given parent frame.
	 * 
	 * @param parent
	 *            The parent frame that owns this dialog.
	 */
	private NewDirectoryDialog(JFrame parent)
	{
		super(parent);

		setTitle("New Directory Name");

		/*
		 * Don't allow the user to click on "X" in the corner to close this box. They MUST click
		 * cancel or OK.
		 */
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		Container contentPane = getContentPane();
		contentPane.setLayout(new GridBagLayout());

		contentPane.add(new JLabel("New Directory Name?"), new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
			GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));

		DefaultFormatter formatter = new DefaultFormatter();
		formatter.setAllowsInvalid(false);
		formatter.setCommitsOnValidEdit(true);
		formatter.setValueClass(NewDirectoryName.class);

		contentPane.add(_input = new JFormattedTextField(formatter), new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 5, 5));

		JPanel bottomPanel = new JPanel(new GridBagLayout());
		OKAction action = new OKAction();
		_input.addCaretListener(action);
		bottomPanel.add(new JButton(action), new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER,
			GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));
		JButton cancel = new JButton("Cancel");
		cancel.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				_input.setText("");
				setVisible(false);
			}
		});
		bottomPanel.add(cancel, new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER,
			GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));
		contentPane.add(bottomPanel, new GridBagConstraints(0, 1, 2, 1, 1.0, 1.0, GridBagConstraints.SOUTH,
			GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 5, 5));

		Dimension d = _input.getPreferredSize();
		d.width = 150;
		_input.setPreferredSize(d);

		setResizable(false);
	}

	/**
	 * The action that the OK button has to either perform actions or disable the button when the
	 * text in the text field is not appropriate to be a new directory name.
	 * 
	 * @author mmm2a
	 */
	private class OKAction extends AbstractAction implements CaretListener
	{
		static final long serialVersionUID = 0L;

		/**
		 * Create a new OK action.
		 */
		public OKAction()
		{
			super("OK");

			setEnabled(false);
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			setVisible(false);
		}

		@Override
		public void caretUpdate(CaretEvent e)
		{
			String input = _input.getText();
			setEnabled(input != null && input.length() > 0);
		}
	}

	static private NewDirectoryDialog _dialog = null;

	/**
	 * A helper method that creates the dialog box and uses it to get user input.
	 * 
	 * @param owner
	 *            The frame that should own the new dialog.
	 * @return The string name of the directory entered by the user, or null if the user cancelled
	 *         the dialog.
	 */
	static public String getDirectoryName(JFrame owner)
	{
		if (_dialog == null) {
			_dialog = new NewDirectoryDialog(owner);
			_dialog.setModalityType(ModalityType.APPLICATION_MODAL);
			_dialog.pack();
			GuiUtils.centerComponent(_dialog);
		}

		_dialog._input.setText("");
		_dialog.setVisible(true);

		String text = _dialog._input.getText();
		if (text == null || text.length() == 0)
			return null;

		return text;
	}
}