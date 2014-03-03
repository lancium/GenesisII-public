package edu.virginia.vcgr.genii.gjt.data.stage.http;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import edu.virginia.vcgr.genii.gjt.data.stage.StageEditor;
import edu.virginia.vcgr.genii.gjt.gui.util.ButtonPanel;

class HttpStageEditor extends StageEditor<HttpStageData> {
	static final long serialVersionUID = 0L;

	private JTextField _hostname = new JTextField(16);
	private JTextField _path = new JTextField(32);
	private JSpinner _port = new JSpinner(new SpinnerNumberModel(
			HttpStageData.DEFAULT_HTTP_PORT, 1, Integer.MAX_VALUE, 1));

	@Override
	protected HttpStageData getStageDataImpl() {
		HttpStageData stageData = new HttpStageData();

		stageData.hostname(_hostname.getText());
		stageData.path(_path.getText());
		stageData.port(((Integer) _port.getValue()).intValue());

		return stageData;
	}

	HttpStageEditor(Window owner) {
		super(owner, "Http Data Stage Editor");

		Container content = getContentPane();
		content.setLayout(new GridBagLayout());

		content.add(new JLabel("HTTP Host"), new GridBagConstraints(0, 0, 1, 1,
				0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(5, 5, 5, 5), 5, 5));
		content.add(_hostname, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(5, 5, 5, 5), 5, 5));

		content.add(new JLabel("HTTP Port"), new GridBagConstraints(2, 0, 1, 1,
				0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(5, 5, 5, 5), 5, 5));
		content.add(_port, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(5, 5, 5, 5), 5, 5));

		content.add(new JLabel("HTTP Path"), new GridBagConstraints(0, 1, 1, 1,
				0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(5, 5, 5, 5), 5, 5));
		content.add(_path, new GridBagConstraints(1, 1, 3, 1, 1.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(5, 5, 5, 5), 5, 5));

		JButton okButton = new JButton(createDefaultOKAction());
		getRootPane().setDefaultButton(okButton);
		content.add(ButtonPanel.createHorizontalPanel(okButton,
				createDefaultCancelAction()), new GridBagConstraints(0, 3, 4,
				1, 1.0, 1.0, GridBagConstraints.SOUTH,
				GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 5, 5));
	}

	@Override
	public void setInitialData(HttpStageData stageData) {
		_hostname.setText(stageData.hostname());
		_path.setText(stageData.path());
		_port.setValue(stageData.port());
	}
}