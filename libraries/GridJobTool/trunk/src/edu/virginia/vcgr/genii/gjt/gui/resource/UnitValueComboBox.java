package edu.virginia.vcgr.genii.gjt.gui.resource;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

import edu.virginia.vcgr.genii.gjt.data.UnitValue;

@SuppressWarnings("rawtypes")
abstract class UnitValueComboBox<U extends Enum<U>> extends JComboBox
{
	static final long serialVersionUID = 0L;

	private UnitValue<Long, U> _value;

	protected abstract U[] getItems();

	@SuppressWarnings("unchecked")
	protected UnitValueComboBox(UnitValue<Long, U> initialValue)
	{
		setModel(new DefaultComboBoxModel(getItems()));
		_value = initialValue;

		setSelectedItem(_value.units());
		addItemListener(new ItemListenerImpl());
	}

	@SuppressWarnings("unchecked")
	private class ItemListenerImpl implements ItemListener
	{
		@Override
		public void itemStateChanged(ItemEvent e)
		{
			if (e.getStateChange() == ItemEvent.SELECTED) {
				_value.units((U) e.getItem());
			}
		}
	}
}