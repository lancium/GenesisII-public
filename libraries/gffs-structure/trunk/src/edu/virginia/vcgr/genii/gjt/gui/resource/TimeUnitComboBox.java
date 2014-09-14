package edu.virginia.vcgr.genii.gjt.gui.resource;

import edu.virginia.vcgr.genii.gjt.data.UnitValue;
import edu.virginia.vcgr.genii.gjt.units.FriendlyTimeUnit;

public class TimeUnitComboBox extends UnitValueComboBox<FriendlyTimeUnit>
{
	static final long serialVersionUID = 0L;

	@Override
	protected FriendlyTimeUnit[] getItems()
	{
		return FriendlyTimeUnit.values();
	}

	public TimeUnitComboBox(UnitValue<Long, FriendlyTimeUnit> initialValue)
	{
		super(initialValue);
	}
}
