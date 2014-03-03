package edu.virginia.vcgr.genii.gjt.data.stage.mailto;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;

import edu.virginia.vcgr.genii.gjt.data.stage.StageEditor;
import edu.virginia.vcgr.genii.gjt.gui.util.ButtonPanel;

class MailtoStageEditor extends StageEditor<MailtoStageData> {
	static final long serialVersionUID = 0L;

	private JTextField _emailAddress = new JTextField(32);
	private JTextField _subject = new JTextField(32);
	private JTextField _attachmentName = new JTextField(16);

	@Override
	protected MailtoStageData getStageDataImpl() {
		MailtoStageData ret = new MailtoStageData();

		ret.emailAddress(_emailAddress.getText());
		ret.subject(_subject.getText());
		ret.attachmentName(_attachmentName.getText());

		return ret;
	}

	MailtoStageEditor(Window owner) {
		super(owner, "Mailto Editor");

		Container content = getContentPane();
		content.setLayout(new GridBagLayout());

		content.add(new JLabel("Email Address"), new GridBagConstraints(0, 0,
				1, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));
		content.add(_emailAddress, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(5, 5, 5, 5), 5, 5));

		content.add(new JLabel("Email Subject"), new GridBagConstraints(0, 1,
				1, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));
		content.add(_subject, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(5, 5, 5, 5), 5, 5));

		content.add(new JLabel("Attachment Filename"), new GridBagConstraints(
				0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));
		content.add(_attachmentName, new GridBagConstraints(1, 2, 1, 1, 1.0,
				0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(5, 5, 5, 5), 5, 5));

		JButton okButton = new JButton(createDefaultOKAction());
		getRootPane().setDefaultButton(okButton);
		content.add(ButtonPanel.createHorizontalPanel(okButton,
				createDefaultCancelAction()), new GridBagConstraints(0, 3, 2,
				1, 1.0, 1.0, GridBagConstraints.SOUTH,
				GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 5, 5));
	}

	@Override
	public void setInitialData(MailtoStageData stageData) {
		_emailAddress.setText(stageData.emailAddress());
		_subject.setText(stageData.subject());
		_attachmentName.setText(stageData.attachmentName());
	}
}