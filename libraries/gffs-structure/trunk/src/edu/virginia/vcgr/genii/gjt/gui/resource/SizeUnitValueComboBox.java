package edu.virginia.vcgr.genii.gjt.gui.resource;

import edu.virginia.vcgr.genii.gjt.data.UnitValue;
import edu.virginia.vcgr.genii.gjt.units.SizeUnit;

public class SizeUnitValueComboBox extends UnitValueComboBox<SizeUnit>
{
	static final long serialVersionUID = 0L;

	@Override
	protected SizeUnit[] getItems()
	{
		return SizeUnit.values();
	}

	public SizeUnitValueComboBox(UnitValue<Long, SizeUnit> initialValue)
	{
		super(initialValue);
	}
}
