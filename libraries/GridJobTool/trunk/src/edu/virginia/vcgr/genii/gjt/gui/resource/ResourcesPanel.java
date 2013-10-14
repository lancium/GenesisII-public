package edu.virginia.vcgr.genii.gjt.gui.resource;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JPanel;

import edu.virginia.vcgr.genii.gjt.JobDocumentContext;

public class ResourcesPanel extends JPanel
{
	static final long serialVersionUID = 0L;

	public ResourcesPanel(JobDocumentContext context)
	{
		super(new GridBagLayout());
		setName("Resources");

		add(new OperatingSystemPanel(context), new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER,
			GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 5, 5));
		add(new ProcessorArchitecturePanel(context), new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER,
			GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 5, 5));

		add(new MemoryPanel(context), new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER,
			GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 5, 5));
		add(new WallclockPanel(context), new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER,
			GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 5, 5));

		add(new MatchingParameterPanel(context.jobDocument().matchingParameters()), new GridBagConstraints(0, 2, 2, 1, 1.0,
			1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 5, 5));

		add(new SPMDPanel(context), new GridBagConstraints(0, 3, 2, 1, 1.0, 0.0, GridBagConstraints.CENTER,
			GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 5, 5));
	}
}