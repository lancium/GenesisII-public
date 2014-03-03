package edu.virginia.vcgr.genii.gjt.gui.stage;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JPanel;

import edu.virginia.vcgr.genii.gjt.JobDocumentContext;
import edu.virginia.vcgr.genii.gjt.gui.util.TitledPanel;

public class DataTab extends JPanel {
	static final long serialVersionUID = 0L;

	private JPanel createDataStagingPanel(JobDocumentContext documentContext) {
		TitledPanel stagingPanel = new TitledPanel("Data Staging",
				new GridBagLayout());

		stagingPanel.add(new DataStagePanel(documentContext.jobDocument()
				.filesystemMap(), "Input Stages", documentContext.jobDocument()
				.stageIns(), true), new GridBagConstraints(0, 0, 1, 1, 1.0,
				1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(5, 5, 5, 5), 5, 5));

		stagingPanel.add(new DataStagePanel(documentContext.jobDocument()
				.filesystemMap(), "Output Stages", documentContext
				.jobDocument().stageOuts(), false), new GridBagConstraints(0,
				1, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER,
				GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 5, 5));

		return stagingPanel;
	}

	public DataTab(JobDocumentContext documentContext) {
		super(new GridBagLayout());

		setName("Data");

		add(new RedirectionPanel(documentContext.jobDocument()),
				new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0,
						GridBagConstraints.CENTER,
						GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5),
						5, 5));
		add(createDataStagingPanel(documentContext), new GridBagConstraints(0,
				1, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER,
				GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 5, 5));
	}
}