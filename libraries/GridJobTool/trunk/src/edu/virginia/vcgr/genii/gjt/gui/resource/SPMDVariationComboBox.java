package edu.virginia.vcgr.genii.gjt.gui.resource;

import javax.swing.JComboBox;

@SuppressWarnings("rawtypes")
public class SPMDVariationComboBox extends JComboBox {
	static final long serialVersionUID = 0l;

	@SuppressWarnings("unchecked")
	public SPMDVariationComboBox() {
		super(new SPMDVariationComboModel());

		setEditable(false);
	}
}
