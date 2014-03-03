package edu.virginia.vcgr.genii.gjt.data.stage.scp;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import edu.virginia.vcgr.genii.gjt.data.stage.StageEditor;
import edu.virginia.vcgr.genii.gjt.gui.util.ButtonPanel;
import edu.virginia.vcgr.genii.gjt.gui.util.TitledPanel;

class ScpStageEditor extends StageEditor<ScpStageData> {
	static final long serialVersionUID = 0L;

	private JTextField _hostname = new JTextField(32);
	private JSpinner _port = new JSpinner(new SpinnerNumberModel(
			ScpStageData.DEFAULT_SCP_PORT, 1, Integer.MAX_VALUE, 1));
	private JTextField _path = new JTextField(32);
	private JTextField _username = new JTextField(16);
	private JPasswordField _password = new JPasswordField(16);
	private JRadioButton _scp = new JRadioButton("Use SCP");
	private JRadioButton _sftp = new JRadioButton("Use SFTP");

	@Override
	protected ScpStageData getStageDataImpl() {
		ScpStageData ret = new ScpStageData();

		ret.host(_hostname.getText());
		ret.path(_path.getText());
		ret.port(((Integer) _port.getValue()).intValue());
		ret.username(_username.getText());
		ret.password(new String(_password.getPassword()));
		ret.isSFTP(_sftp.isSelected());

		return ret;
	}

	ScpStageEditor(Window owner) {
		super(owner, "SCP Stage Editor");

		Container container = getContentPane();
		container.setLayout(new GridBagLayout());

		container.add(new JLabel("Hostname"), new GridBagConstraints(0, 0, 1,
				1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(5, 5, 5, 5), 5, 5));
		container.add(_hostname, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(5, 5, 5, 5), 5, 5));
		container.add(new JLabel("Port"), new GridBagConstraints(2, 0, 1, 1,
				0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(5, 5, 5, 5), 5, 5));
		container.add(_port, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(5, 5, 5, 5), 5, 5));

		container.add(new JLabel("Path"), new GridBagConstraints(0, 1, 1, 1,
				0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(5, 5, 5, 5), 5, 5));
		container.add(_path, new GridBagConstraints(1, 1, 3, 1, 1.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(5, 5, 5, 5), 5, 5));

		container.add(new JLabel("Username"), new GridBagConstraints(0, 2, 1,
				1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(5, 5, 5, 5), 5, 5));
		container.add(_username, new GridBagConstraints(1, 2, 1, 1, 1.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(5, 5, 5, 5), 5, 5));
		container.add(new JLabel("Password"), new GridBagConstraints(2, 2, 1,
				1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(5, 5, 5, 5), 5, 5));
		container.add(_password, new GridBagConstraints(3, 2, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(5, 5, 5, 5), 5, 5));

		ButtonGroup scp_sftpGroup = new ButtonGroup();
		scp_sftpGroup.add(_scp);
		scp_sftpGroup.add(_sftp);

		JPanel scp_sftpPanel = new TitledPanel("SCP/SFTP Protocol",
				new GridBagLayout());
		scp_sftpPanel.add(_scp, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5,
						5, 5, 5), 5, 5));
		scp_sftpPanel.add(_sftp, new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5,
						5, 5, 5), 5, 5));
		container.add(scp_sftpPanel, new GridBagConstraints(0, 3, 4, 1, 1.0,
				0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(5, 5, 5, 5), 5, 5));

		JButton okButton = new JButton(createDefaultOKAction());
		getRootPane().setDefaultButton(okButton);
		container.add(ButtonPanel.createHorizontalPanel(okButton,
				createDefaultCancelAction()), new GridBagConstraints(0, 4, 4,
				1, 1.0, 1.0, GridBagConstraints.SOUTH,
				GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 5, 5));
	}

	@Override
	public void setInitialData(ScpStageData stageData) {
		_hostname.setText(stageData.host());
		_port.setValue(new Integer(stageData.port()));
		_path.setText(stageData.path());
		_username.setText(stageData.username());
		_password.setText(stageData.password());
		_scp.setSelected(!stageData.isSFTP());
		_sftp.setSelected(stageData.isSFTP());
	}
}