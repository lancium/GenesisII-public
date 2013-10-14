package edu.virginia.vcgr.genii.gjt.data.stage.mailto;

import java.awt.Window;

import edu.virginia.vcgr.genii.gjt.data.stage.AbstractStageFactory;
import edu.virginia.vcgr.genii.gjt.data.stage.StageData;
import edu.virginia.vcgr.genii.gjt.data.stage.StageEditor;
import edu.virginia.vcgr.genii.gjt.data.stage.StageProtocol;

public class MailtoStageFactory extends AbstractStageFactory
{
	public MailtoStageFactory()
	{
		super(StageProtocol.mailto);
	}

	@Override
	public StageEditor<? extends StageData> createEditor(Window owner)
	{
		return new MailtoStageEditor(owner);
	}
}