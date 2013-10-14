package edu.virginia.vcgr.genii.client.dialog.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;

import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import edu.virginia.vcgr.genii.client.dialog.InputDialog;
import edu.virginia.vcgr.genii.client.dialog.InputValidator;

public class GuiInputDialog extends AbstractGuiDialog implements InputDialog, ActionListener
{

	static private final String _OK_ACTION = "OK";

	@Override
	protected void okCalled()
	{
		_answer = _field.getText();

		super.okCalled();
	}

	static final long serialVersionUID = 0L;

	private String _defaultAnswer;
	private JLabel _label;
	private JTextField _field;
	private String _answer;
	private InputValidator _validator;

	public GuiInputDialog(String title, String prompt)
	{
		super(title, prompt);

		_validator = null;
		_answer = null;
		_defaultAnswer = null;

	}

	protected JTextField createTextField()
	{
		JTextField field = new JTextField();
		Dimension dim = field.getMinimumSize();
		dim.width = 100;
		field.setMinimumSize(dim);
		dim = field.getPreferredSize();
		dim.width = 100;
		field.setPreferredSize(dim);

		field.setActionCommand(_OK_ACTION);
		field.addActionListener(this);

		return field;
	}

	@Override
	protected JComponent createBody(Object[] bodyParameters)
	{
		_label = new JLabel((String) bodyParameters[0]);
		_field = createTextField();

		_field.setInputVerifier(new InternalInputVerifier());

		JPanel panel = new JPanel(new GridBagLayout());

		panel.add(_label, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
			new Insets(5, 5, 5, 5), 5, 5));
		panel.add(_field, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
			new Insets(5, 5, 5, 5), 5, 5));

		return panel;
	}

	@Override
	public String getAnswer()
	{
		return _answer;
	}

	@Override
	public String getDefaultAnswer()
	{
		return _defaultAnswer;
	}

	@Override
	public InputValidator getInputValidator()
	{
		return _validator;
	}

	@Override
	public void setDefaultAnswer(String defaultAnswer)
	{
		_defaultAnswer = defaultAnswer;
		setDefaultValue();
	}

	private void setDefaultValue()
	{
		try {
			if (!SwingUtilities.isEventDispatchThread()) {
				SwingUtilities.invokeAndWait(new Runnable()
				{
					public void run()
					{
						setDefaultValue();
					}
				});
			}

			_field.setText(_defaultAnswer);
		} catch (InvocationTargetException ite) {
			throw new RuntimeException("Unexpected exception.", ite.getCause());
		} catch (InterruptedException ie) {
			throw new RuntimeException("Unexpectedly interrupted.", ie);
		}
	}

	@Override
	public void setInputValidator(InputValidator validator)
	{
		_validator = validator;
	}

	public void actionPerformed(ActionEvent arg0)
	{
		super._okAction.actionPerformed(arg0);
	}

	private class InternalInputVerifier extends InputVerifier
	{
		@Override
		public boolean verify(JComponent input)
		{
			String text = _field.getText();
			if (_validator != null) {
				String msg = _validator.validateInput(text);
				if (msg != null) {
					JOptionPane.showMessageDialog(_field, msg, "Input Validation Failed", JOptionPane.ERROR_MESSAGE);
					return false;
				}
			}

			return true;
		}
	}
}