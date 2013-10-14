package edu.virginia.vcgr.genii.gjt.gui.resource;

import javax.swing.JSpinner;

public class UnitValueSpinner extends JSpinner
{
	static final long serialVersionUID = 0L;

	public UnitValueSpinner(UnitValueSpinnerModel<?> model)
	{
		super(model);

		setEditor(new NullableNumberSpinnerEditor(this));
	}
}
