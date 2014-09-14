package edu.virginia.vcgr.genii.gjt.data.stage.http;

import java.awt.Window;

import edu.virginia.vcgr.genii.gjt.data.stage.AbstractStageFactory;
import edu.virginia.vcgr.genii.gjt.data.stage.StageData;
import edu.virginia.vcgr.genii.gjt.data.stage.StageEditor;
import edu.virginia.vcgr.genii.gjt.data.stage.StageProtocol;

public class HttpStageFactory extends AbstractStageFactory
{
	public HttpStageFactory()
	{
		super(StageProtocol.http);
	}

	@Override
	public StageEditor<? extends StageData> createEditor(Window owner)
	{
		return new HttpStageEditor(owner);
	}
}