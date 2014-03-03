package edu.virginia.vcgr.genii.gjt.gui.resource;

import javax.swing.JSpinner;

public class NullableNumberSpinner extends JSpinner {
	static final long serialVersionUID = 0L;

	public NullableNumberSpinner(NullableNumberSpinnerModel model) {
		super(model);

		setEditor(new NullableNumberSpinnerEditor(this));
	}
}
