package edu.virginia.vcgr.genii.gjt.data.stage;

import java.awt.Window;

public interface StageFactory
{
	public StageProtocol protocol();

	public StageEditor<? extends StageData> createEditor(Window owner);
}