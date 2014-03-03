package edu.virginia.vcgr.genii.gjt.data.stage.scp;

import java.awt.Window;

import edu.virginia.vcgr.genii.gjt.data.stage.AbstractStageFactory;
import edu.virginia.vcgr.genii.gjt.data.stage.StageData;
import edu.virginia.vcgr.genii.gjt.data.stage.StageEditor;
import edu.virginia.vcgr.genii.gjt.data.stage.StageProtocol;

public class ScpStageFactory extends AbstractStageFactory
{
	public ScpStageFactory()
	{
		super(StageProtocol.scp);
	}

	@Override
	public StageEditor<? extends StageData> createEditor(Window owner)
	{
		return new ScpStageEditor(owner);
	}
}