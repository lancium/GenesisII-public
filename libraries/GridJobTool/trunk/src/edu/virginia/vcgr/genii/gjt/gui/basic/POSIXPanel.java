package edu.virginia.vcgr.genii.gjt.gui.basic;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;

import edu.virginia.vcgr.genii.gjt.data.JobDocument;
import edu.virginia.vcgr.genii.gjt.gui.args.ArgumentsPanel;
import edu.virginia.vcgr.genii.gjt.gui.env.EnvironmentPanel;
import edu.virginia.vcgr.genii.gjt.gui.fs.FilesystemCombo;
import edu.virginia.vcgr.genii.gjt.gui.util.ParameterizableStringField;
import edu.virginia.vcgr.genii.gjt.gui.util.TitledPanel;

public class POSIXPanel extends TitledPanel
{
	static final long serialVersionUID = 0L;

	POSIXPanel(JobDocument document)
	{
		super("Job Identification", new GridBagLayout());

		add(new JLabel("Executable"), new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
			GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));
		add(new ParameterizableStringField(document.executable(), 16), new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 5, 5));
		add(new FilesystemCombo(document.filesystemMap(), document.executable()), new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 5, 5));
		add(new ArgumentsPanel(document.filesystemMap(), document.arguments()), new GridBagConstraints(0, 1, 3, 1, 1.0, 1.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 5, 5));
		add(new EnvironmentPanel(document.filesystemMap(), document.environment()), new GridBagConstraints(0, 2, 3, 1, 1.0,
			1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 5, 5));
	}
}