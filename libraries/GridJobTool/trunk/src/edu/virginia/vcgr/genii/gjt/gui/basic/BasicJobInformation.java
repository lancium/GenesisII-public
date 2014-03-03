package edu.virginia.vcgr.genii.gjt.gui.basic;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JPanel;

import edu.virginia.vcgr.genii.gjt.JobDocumentContext;

public class BasicJobInformation extends JPanel {
	static final long serialVersionUID = 0L;

	public BasicJobInformation(JobDocumentContext documentContext) {
		super(new GridBagLayout());

		setName("Basic Job Information");

		add(new JobIdentificationPanel(documentContext.jobDocument()),
				new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0,
						GridBagConstraints.CENTER,
						GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
						5, 5));

		add(new POSIXPanel(documentContext.jobDocument()),
				new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(5, 5, 5, 5), 5, 5));
	}
}