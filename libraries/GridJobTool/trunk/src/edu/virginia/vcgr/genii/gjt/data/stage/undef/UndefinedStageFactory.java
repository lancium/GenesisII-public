package edu.virginia.vcgr.genii.gjt.data.stage.undef;

import java.awt.Window;

import edu.virginia.vcgr.genii.gjt.data.stage.AbstractStageFactory;
import edu.virginia.vcgr.genii.gjt.data.stage.StageData;
import edu.virginia.vcgr.genii.gjt.data.stage.StageEditor;
import edu.virginia.vcgr.genii.gjt.data.stage.StageProtocol;

public class UndefinedStageFactory extends AbstractStageFactory {
	@Override
	public StageEditor<? extends StageData> createEditor(Window owner) {
		return null;
	}

	public UndefinedStageFactory() {
		super(StageProtocol.undefined);
	}
}
