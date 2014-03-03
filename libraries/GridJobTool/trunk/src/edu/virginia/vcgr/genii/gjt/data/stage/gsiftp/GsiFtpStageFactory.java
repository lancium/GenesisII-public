package edu.virginia.vcgr.genii.gjt.data.stage.gsiftp;

import java.awt.Window;

import edu.virginia.vcgr.genii.gjt.data.stage.AbstractStageFactory;
import edu.virginia.vcgr.genii.gjt.data.stage.StageData;
import edu.virginia.vcgr.genii.gjt.data.stage.StageEditor;
import edu.virginia.vcgr.genii.gjt.data.stage.StageProtocol;

public class GsiFtpStageFactory extends AbstractStageFactory
{
	public GsiFtpStageFactory()
	{
		super(StageProtocol.gsiftp);
	}

	@Override
	public StageEditor<? extends StageData> createEditor(Window owner)
	{
		return new GsiFtpStageEditor(owner);
	}
}