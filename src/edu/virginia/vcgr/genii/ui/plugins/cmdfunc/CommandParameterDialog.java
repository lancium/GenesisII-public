package edu.virginia.vcgr.genii.ui.plugins.cmdfunc;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.morgan.utils.gui.ButtonPanel;
import org.morgan.utils.gui.GUIUtils;

import edu.virginia.vcgr.genii.client.resource.JavaCommandFunction;
import edu.virginia.vcgr.genii.client.resource.JavaCommandParameter;

class CommandParameterDialog extends JDialog
{
	static final long serialVersionUID = 0L;

	private JComponent[] _answerComponents;
	private String[] _answers = null;

	private CommandParameterDialog(Component ownerComponent, JavaCommandFunction function)
	{
		super(SwingUtilities.getWindowAncestor(ownerComponent), "Command Function Parameters");

		Container content = getContentPane();
		content.setLayout(new GridBagLayout());

		content.add(new JLabel(String.format("Parameters for Command Function \"%s\":", function)), new GridBagConstraints(0,
			0, 2, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));

		_answerComponents = new JComponent[function.parameters().length];
		int count = 0;
		for (JavaCommandParameter parameter : function.parameters()) {
			if (parameter.typeString().equals("int"))
				_answerComponents[count] = new JSpinner();
			else
				_answerComponents[count] = new JTextField(16);

			count++;

			JLabel label = new JLabel(parameter.toString());
			content.add(label, new GridBagConstraints(0, count, 1, 1, 0.0, 0.0, GridBagConstraints.EAST,
				GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));

			content.add(_answerComponents[count - 1], new GridBagConstraints(1, count, 1, 1, 1.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 5, 5));

			String desc = parameter.description();
			if (desc != null) {
				label.setToolTipText(desc);
				_answerComponents[count - 1].setToolTipText(desc);
			}
		}

		content.add(ButtonPanel.createHorizontalButtonPanel(new ExecuteAction(), new CancelAction()), new GridBagConstraints(0,
			count + 1, 2, 1, 1.0, 1.0, GridBagConstraints.SOUTH, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 5, 5));

		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
	}

	private class ExecuteAction extends AbstractAction
	{
		static final long serialVersionUID = 0L;

		private ExecuteAction()
		{
			super("Execute Function");
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			_answers = new String[_answerComponents.length];
			for (int lcv = 0; lcv < _answers.length; lcv++) {
				JComponent jc = _answerComponents[lcv];
				if (jc instanceof JSpinner)
					_answers[lcv] = ((JSpinner) jc).getValue().toString();
				else
					_answers[lcv] = ((JTextField) jc).getText();
			}

			dispose();
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
		public void actionPerformed(ActionEvent e)
		{
			_answers = null;
			dispose();
		}
	}

	static String[] fillInParameters(Component ownerComponent, JavaCommandFunction function)
	{
		CommandParameterDialog dialog = new CommandParameterDialog(ownerComponent, function);
		dialog.setModalityType(ModalityType.DOCUMENT_MODAL);
		dialog.pack();
		GUIUtils.centerWindow(dialog);
		dialog.setVisible(true);

		return dialog._answers;
	}
}