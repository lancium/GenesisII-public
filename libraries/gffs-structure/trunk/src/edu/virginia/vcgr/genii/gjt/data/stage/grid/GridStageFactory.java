package edu.virginia.vcgr.genii.gjt.data.stage.grid;

import java.awt.Window;

import edu.virginia.vcgr.genii.gjt.data.stage.AbstractStageFactory;
import edu.virginia.vcgr.genii.gjt.data.stage.StageData;
import edu.virginia.vcgr.genii.gjt.data.stage.StageEditor;
import edu.virginia.vcgr.genii.gjt.data.stage.StageProtocol;

public class GridStageFactory extends AbstractStageFactory
{
	public GridStageFactory()
	{
		super(StageProtocol.grid);
	}

	@Override
	public StageEditor<? extends StageData> createEditor(Window owner)
	{
		return new GridStageEditor(owner);
	}
}