package edu.virginia.g3.fsview.cifs;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

final class CifsFSViewInformationPanel extends JPanel
{
	static final long serialVersionUID = 0L;

	static final private int HOST_FIELD = 0;
	static final private int SHARE_FIELD = 1;

	private CifsFSViewInformationModel _model;

	private JTextField _hostname = new JTextField(32);
	private JTextField _shareName = new JTextField(32);

	CifsFSViewInformationPanel(CifsFSViewInformationModel model)
	{
		super(new GridBagLayout());

		_model = model;

		add(new JLabel("Host"), new GridBagConstraints(0, 0, 1, 1, 0.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
			new Insets(5, 5, 5, 5), 5, 5));
		add(_hostname, new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
			new Insets(5, 5, 5, 5), 5, 5));

		add(new JLabel("Share Name"), new GridBagConstraints(0, 1, 1, 1, 0.0, 1.0, GridBagConstraints.WEST,
			GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));
		add(_shareName, new GridBagConstraints(1, 1, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
			new Insets(5, 5, 5, 5), 5, 5));

		_hostname.addCaretListener(new CaretListenerImpl(HOST_FIELD));
		_shareName.addCaretListener(new CaretListenerImpl(SHARE_FIELD));
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

				case SHARE_FIELD:
					_model.shareName(_shareName.getText());
					break;
			}
		}
	}
}