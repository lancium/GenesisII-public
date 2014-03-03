package edu.virginia.vcgr.genii.gjt.gui.resource;

import java.util.Vector;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;

import edu.virginia.vcgr.genii.gjt.conf.Configuration;
import edu.virginia.vcgr.genii.gjt.conf.SPMDVariation;

@SuppressWarnings("rawtypes")
class SPMDVariationComboModel extends AbstractListModel implements
		ComboBoxModel {
	static final long serialVersionUID = 0L;

	private SPMDVariation _selection;
	private Vector<SPMDVariation> _elements;

	SPMDVariationComboModel() {
		_selection = null;
		_elements = new Vector<SPMDVariation>(Configuration.configuration
				.spmdVariations().values());
		_elements.add(0, null);
	}

	@Override
	public Object getSelectedItem() {
		return _selection;
	}

	@Override
	public void setSelectedItem(Object anItem) {
		_selection = (SPMDVariation) anItem;
	}

	@Override
	public Object getElementAt(int index) {
		return _elements.get(index);
	}

	@Override
	public int getSize() {
		return _elements.size();
	}
}