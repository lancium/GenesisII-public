package edu.virginia.vcgr.genii.gjt.gui.stage;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;

import edu.virginia.vcgr.genii.gjt.data.JobDocument;
import edu.virginia.vcgr.genii.gjt.gui.fs.FilesystemCombo;
import edu.virginia.vcgr.genii.gjt.gui.util.ParameterizableStringField;
import edu.virginia.vcgr.genii.gjt.gui.util.TitledPanel;

class RedirectionPanel extends TitledPanel
{
	static final long serialVersionUID = 0L;

	RedirectionPanel(JobDocument document)
	{
		super("Stream Redirection", new GridBagLayout());

		add(new JLabel("Standard Input"), new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
			GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));
		add(new ParameterizableStringField(document.standardInput(), 16), new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 5, 5));
		add(new FilesystemCombo(document.filesystemMap(), document.standardInput()), new GridBagConstraints(2, 0, 1, 1, 0.0,
			0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 5, 5));

		add(new JLabel("Standard Output"), new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
			GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));
		add(new ParameterizableStringField(document.standardOutput(), 16), new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 5, 5));
		add(new FilesystemCombo(document.filesystemMap(), document.standardOutput()), new GridBagConstraints(2, 1, 1, 1, 0.0,
			0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 5, 5));

		add(new JLabel("Standard Error"), new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
			GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));
		add(new ParameterizableStringField(document.standardError(), 16), new GridBagConstraints(1, 2, 1, 1, 1.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 5, 5));
		add(new FilesystemCombo(document.filesystemMap(), document.standardError()), new GridBagConstraints(2, 2, 1, 1, 0.0,
			0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 5, 5));
	}
}