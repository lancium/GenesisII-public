package edu.virginia.g3.fsview.ssh;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

final class SSHFSViewInformationPanel extends JPanel
{
	static final long serialVersionUID = 0L;

	static final private int HOST_FIELD = 0;
	static final private int PATH_FIELD = 1;

	private SSHFSViewInformationModel _model;

	private JTextField _hostname = new JTextField(32);
	private JSpinner _port = new JSpinner(new SpinnerNumberModel(22, 1, Short.MAX_VALUE, 1));
	private JTextField _path = new JTextField(32);

	SSHFSViewInformationPanel(SSHFSViewInformationModel model)
	{
		super(new GridBagLayout());

		_model = model;

		add(new JLabel("Host"), new GridBagConstraints(0, 0, 1, 1, 0.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
			new Insets(5, 5, 5, 5), 5, 5));
		add(_hostname, new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
			new Insets(5, 5, 5, 5), 5, 5));

		add(new JLabel("Port"), new GridBagConstraints(2, 0, 1, 1, 0.0, 1.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
			new Insets(5, 5, 5, 5), 5, 5));
		add(_port, new GridBagConstraints(3, 0, 1, 1, 0.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
			new Insets(5, 5, 5, 5), 5, 5));

		add(new JLabel("Path"), new GridBagConstraints(0, 1, 1, 1, 0.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
			new Insets(5, 5, 5, 5), 5, 5));
		add(_path, new GridBagConstraints(1, 1, 3, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
			new Insets(5, 5, 5, 5), 5, 5));

		_hostname.addCaretListener(new CaretListenerImpl(HOST_FIELD));
		_port.addChangeListener(new PortChangeListener());
		_path.addCaretListener(new CaretListenerImpl(PATH_FIELD));
	}

	private class CaretListenerImpl implements CaretListener
	{
		private int _field;

		private CaretListenerImpl(int field)
		{
			_field = field;
		}

		@Override
		final public void caretUpdate(CaretEvent e)
		{
			switch (_field) {
				case HOST_FIELD:
					_model.hostname(_hostname.getText());
					break;

				case PATH_FIELD:
					_model.path(_path.getText());
					break;
			}
		}
	}

	private class PortChangeListener implements ChangeListener
	{
		@Override
		final public void stateChanged(ChangeEvent e)
		{
			_model.port(((Integer) _port.getValue()).intValue());
		}
	}
}