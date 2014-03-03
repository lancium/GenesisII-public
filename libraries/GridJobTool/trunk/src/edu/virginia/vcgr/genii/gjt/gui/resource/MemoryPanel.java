package edu.virginia.vcgr.genii.gjt.gui.resource;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JSpinner;

import edu.virginia.vcgr.genii.gjt.JobDocumentContext;
import edu.virginia.vcgr.genii.gjt.data.SizeValue;
import edu.virginia.vcgr.genii.gjt.gui.util.TitledPanel;
import edu.virginia.vcgr.genii.gjt.units.SizeUnit;

class MemoryPanel extends TitledPanel {
	static final long serialVersionUID = 0L;

	MemoryPanel(JobDocumentContext context) {
		super("Memory Restrictions", new GridBagLayout());

		SizeValue currentMem = context.jobDocument().memoryUpperBound();
		JSpinner upperBound = new UnitValueSpinner(
				new UnitValueSpinnerModel<SizeUnit>(currentMem, 1,
						Long.MAX_VALUE, 1));
		SizeUnitValueComboBox upperBoundUnit = new SizeUnitValueComboBox(
				currentMem);

		add(new JLabel("Upper Bound"), new GridBagConstraints(0, 0, 1, 1, 0.0,
				1.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
				new Insets(5, 5, 5, 5), 5, 5));
		add(upperBound, new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0,
				GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL,
				new Insets(5, 5, 5, 5), 5, 5));
		add(upperBoundUnit, new GridBagConstraints(2, 0, 1, 1, 0.0, 1.0,
				GridBagConstraints.NORTHEAST, GridBagConstraints.NONE,
				new Insets(5, 5, 5, 5), 5, 5));
	}
}