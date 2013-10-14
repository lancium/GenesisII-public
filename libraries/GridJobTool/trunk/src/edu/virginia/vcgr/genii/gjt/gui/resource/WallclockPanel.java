package edu.virginia.vcgr.genii.gjt.gui.resource;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JSpinner;

import edu.virginia.vcgr.genii.gjt.JobDocumentContext;
import edu.virginia.vcgr.genii.gjt.data.TimeValue;
import edu.virginia.vcgr.genii.gjt.gui.util.TitledPanel;
import edu.virginia.vcgr.genii.gjt.units.FriendlyTimeUnit;

public class WallclockPanel extends TitledPanel
{
	static final long serialVersionUID = 0L;

	WallclockPanel(JobDocumentContext context)
	{
		super("Wallclock Time Restrictions", new GridBagLayout());

		TimeValue currentLimit = context.jobDocument().wallclockUpperBound();
		JSpinner upperBound =
			new UnitValueSpinner(new UnitValueSpinnerModel<FriendlyTimeUnit>(currentLimit, 1, Long.MAX_VALUE, 1));
		TimeUnitComboBox upperBoundUnit = new TimeUnitComboBox(currentLimit);

		add(new JLabel("Wallclock Time Limit"), new GridBagConstraints(0, 0, 1, 1, 0.0, 1.0, GridBagConstraints.NORTHWEST,
			GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));
		add(upperBound, new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL,
			new Insets(5, 5, 5, 5), 5, 5));
		add(upperBoundUnit, new GridBagConstraints(2, 0, 1, 1, 0.0, 1.0, GridBagConstraints.NORTHEAST, GridBagConstraints.NONE,
			new Insets(5, 5, 5, 5), 5, 5));
	}
}