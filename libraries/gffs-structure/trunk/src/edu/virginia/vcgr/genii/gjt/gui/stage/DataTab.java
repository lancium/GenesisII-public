package edu.virginia.vcgr.genii.gjt.gui.stage;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JPanel;

import edu.virginia.vcgr.genii.gjt.JobDocumentContext;
import edu.virginia.vcgr.genii.gjt.gui.util.TitledPanel;

public class DataTab extends JPanel
{
	static final long serialVersionUID = 0L;

	private JPanel createDataStagingPanel(JobDocumentContext documentContext, int index)
	{
		TitledPanel stagingPanel = new TitledPanel("Data Staging", new GridBagLayout());

		stagingPanel.add(
			new DataStagePanel(documentContext.jobRoot().jobDocument().get(index).filesystemMap(), "Input Stages",
				documentContext.jobRoot().jobDocument().get(index).stageIns(), true),
			new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 5, 5));

		stagingPanel.add(
			new DataStagePanel(documentContext.jobRoot().jobDocument().get(index).filesystemMap(), "Output Stages",
				documentContext.jobRoot().jobDocument().get(index).stageOuts(), false),
			new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 5, 5));

		return stagingPanel;
	}

	public DataTab(JobDocumentContext documentContext, int index)
	{
		super(new GridBagLayout());

		setName("Data");

		add(new RedirectionPanel(documentContext.jobRoot().jobDocument().get(index)), new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 5, 5));
		add(createDataStagingPanel(documentContext, index),
			new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 5, 5));
	}
}
