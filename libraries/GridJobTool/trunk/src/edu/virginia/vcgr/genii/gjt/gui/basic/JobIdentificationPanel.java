package edu.virginia.vcgr.genii.gjt.gui.basic;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;

import edu.virginia.vcgr.genii.gjt.data.JobDocument;
import edu.virginia.vcgr.genii.gjt.data.ParameterizableStringList;
import edu.virginia.vcgr.genii.gjt.data.ParameterizableStringListDescriber;
import edu.virginia.vcgr.genii.gjt.gui.util.DescribedField;
import edu.virginia.vcgr.genii.gjt.gui.util.ParameterizableStringField;
import edu.virginia.vcgr.genii.gjt.gui.util.ParameterizableStringListEditor;
import edu.virginia.vcgr.genii.gjt.gui.util.TitledPanel;

class JobIdentificationPanel extends TitledPanel
{
	static final long serialVersionUID = 0L;

	JobIdentificationPanel(JobDocument document)
	{
		super("Job Identification", new GridBagLayout());

		add(new JLabel("Job Name"), new GridBagConstraints(0, 0, 1, 1, 0.0, 1.0, GridBagConstraints.WEST,
			GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));
		add(new ParameterizableStringField(document.jobName(), 16), new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0,
			GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 5, 5));

		add(new JLabel("Job Annotations"), new GridBagConstraints(2, 0, 1, 1, 0.0, 1.0, GridBagConstraints.WEST,
			GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));
		add(new DescribedField<ParameterizableStringList>(document.jobAnnotations(), new ParameterizableStringListDescriber(),
			new ParameterizableStringListEditor("Job Annotations", "Enter new job annotation"), 16), new GridBagConstraints(3,
			0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 5, 5));

		add(new JLabel("Job Projects"), new GridBagConstraints(0, 1, 1, 1, 0.0, 1.0, GridBagConstraints.WEST,
			GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));
		add(new DescribedField<ParameterizableStringList>(document.jobProjects(), new ParameterizableStringListDescriber(),
			new ParameterizableStringListEditor("Job Projects", "Enter new job project"), 16), new GridBagConstraints(1, 1, 1,
			1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 5, 5));

		add(new JLabel("Job Description"), new GridBagConstraints(2, 1, 1, 1, 0.0, 1.0, GridBagConstraints.WEST,
			GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));
		add(new ParameterizableStringField(document.jobDescription(), 16), new GridBagConstraints(3, 1, 1, 1, 1.0, 1.0,
			GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 5, 5));
	}
}