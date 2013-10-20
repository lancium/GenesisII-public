package edu.virginia.g3.fsview.gui;

import java.awt.Color;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.Comparator;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import edu.virginia.g3.fsview.FSViewConnectionInformation;
import edu.virginia.g3.fsview.FSViewFactories;
import edu.virginia.g3.fsview.FSViewFactory;

final public class FSViewCreatorDialog extends JDialog
{
	static final long serialVersionUID = 0L;

	private JTabbedPane _tabbedPane = new JTabbedPane();
	private JCheckBox _readOnlyButton = new JCheckBox("Read Only", false);
	private JLabel _errorLabel = new JLabel("No Errors");

	private boolean _cancelled = true;

	public FSViewCreatorDialog(Window owner)
	{
		super(owner);
		setTitle("FSView Creator");
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setResizable(false);

		Container content = getContentPane();
		content.setLayout(new GridBagLayout());

		OKAction ok = new OKAction();
		FSViewFactory[] factories = FSViewFactories.factories();
		Arrays.sort(factories, new Comparator<FSViewFactory>()
		{
			@Override
			final public int compare(FSViewFactory o1, FSViewFactory o2)
			{
				return o1.toString().compareTo(o2.toString());
			}
		});

		for (FSViewFactory factory : factories) {
			FSViewCombinedInformationModel model = new FSViewCombinedInformationModel(factory);
			_tabbedPane.add(model.createGuiComponent());
			model.addInformationListener(ok);
		}
		_tabbedPane.setSelectedIndex(0);

		ok.evalutateState();

		content.add(_tabbedPane, new GridBagConstraints(0, 0, 2, 1, 1.0, 1.0, GridBagConstraints.CENTER,
			GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 5, 5));
		content.add(_readOnlyButton, new GridBagConstraints(0, 1, 2, 1, 1.0, 0.0, GridBagConstraints.WEST,
			GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));

		content.add(new JButton(ok), new GridBagConstraints(0, 2, 1, 1, 1.0, 0.0, GridBagConstraints.WEST,
			GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));
		content.add(new JButton(new CancelAction()), new GridBagConstraints(0, 2, 1, 1, 1.0, 0.0, GridBagConstraints.EAST,
			GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));
		/* VANA added 'Help' button */
		content.add(new JButton(new HelpAction()), new GridBagConstraints(1, 2, 1, 1, 1.0, 0.0, GridBagConstraints.EAST,
			GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));
		/* VANA added 'Help' button */

		JPanel errorPanel = new JPanel(new GridBagLayout());
		errorPanel.setBorder(BorderFactory.createLoweredBevelBorder());
		errorPanel.add(_errorLabel, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.WEST,
			GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));

		content.add(errorPanel, new GridBagConstraints(0, 3, 2, 1, 1.0, 0.0, GridBagConstraints.CENTER,
			GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 5, 5));

		_tabbedPane.addChangeListener(ok);
	}

	public FSViewConnectionInformation connectionInformation()
	{
		if (_cancelled)
			return null;

		FSViewCombinedInformationPanel panel = (FSViewCombinedInformationPanel) _tabbedPane.getSelectedComponent();

		FSViewConnectionInformation info = panel.model().wrap();
		info.readOnly(_readOnlyButton.isSelected());
		return info;
	}

	private class OKAction extends AbstractAction implements FSViewInformationListener<FSViewConnectionInformation>,
		ChangeListener
	{
		static final long serialVersionUID = 0L;

		private void evalutateState()
		{
			FSViewCombinedInformationPanel panel = (FSViewCombinedInformationPanel) _tabbedPane.getSelectedComponent();

			AcceptabilityState state = panel.model().isAcceptable();

			setEnabled(state.isAcceptable());
			if (state.isAcceptable()) {
				_errorLabel.setText("No Errors");
				_errorLabel.setForeground(Color.lightGray);
			} else {
				_errorLabel.setText(state.toString());
				_errorLabel.setForeground(Color.red);
			}
		}

		private OKAction()
		{
			super("OK");
		}

		@Override
		final public void actionPerformed(ActionEvent e)
		{
			_cancelled = false;
			dispose();
		}

		@Override
		final public void contentsChanged(FSViewInformationModel<FSViewConnectionInformation> model)
		{
			evalutateState();
		}

		@Override
		final public void stateChanged(ChangeEvent e)
		{
			evalutateState();
		}
	}

	private class CancelAction extends AbstractAction
	{
		static final long serialVersionUID = 0L;

		private CancelAction()
		{
			super("Cancel");
		}

		@Override
		final public void actionPerformed(ActionEvent e)
		{
			_cancelled = true;
			dispose();
		}
	}

	/* VANA Help button functionality */
	private class HelpAction extends AbstractAction
	{
		static final long serialVersionUID = 0L;

		private HelpAction()
		{
			super("Help");
		}

		@Override
		final public void actionPerformed(ActionEvent e)
		{
			FSViewCombinedInformationPanel panel = (FSViewCombinedInformationPanel) _tabbedPane.getSelectedComponent();
			String message = null;
			if (panel.model().modelName() == "Filesystem") {
				message =
					"Enter Filesytem path."
						+ "\n"
						+ "This path should be a shared filesystem path on remote machine, and the path must be visible to the Container you choose in next steps."
						+ "\n" + "User MUST have access rights to this path.";
				JOptionPane.showMessageDialog(panel, message);
			} else if (panel.model().modelName() == "SSH Access") {
				message =
					"Enter Hostname of the remote machine." + "\n"
						+ "Enter Filesystem path on the remote machine. User should have access rights to this path" + "\n"
						+ "Path must be visible to the Container you choose in next steps" + "\n"
						+ "Enter Username and Password to SSH access the remote machine.";
				JOptionPane.showMessageDialog(panel, message);
			} else if (panel.model().modelName() == "Windows Share") {
				message =
					"Enter Windows machine Hostname." + "\n" + "Enter the Windows share name" + "\n"
						+ "Enter Domain name of your windows machine" + "\n"
						+ "Enter Username and Password to access windows share";
				JOptionPane.showMessageDialog(panel, message);
			}
		}
	}

	/* VANA Help button functionality */

	static public void main(String[] args)
	{
		FSViewCreatorDialog dialog = new FSViewCreatorDialog(null);
		dialog.setModalityType(ModalityType.APPLICATION_MODAL);
		dialog.pack();
		dialog.setVisible(true);

		FSViewConnectionInformation info = dialog.connectionInformation();
		if (info != null)
			System.out.println(info);
	}
}
