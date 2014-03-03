package edu.virginia.vcgr.genii.gjt.data.stage.ftp;

import java.awt.Window;

import edu.virginia.vcgr.genii.gjt.data.stage.AbstractStageFactory;
import edu.virginia.vcgr.genii.gjt.data.stage.StageData;
import edu.virginia.vcgr.genii.gjt.data.stage.StageEditor;
import edu.virginia.vcgr.genii.gjt.data.stage.StageProtocol;

public class FtpStageFactory extends AbstractStageFactory {
	public FtpStageFactory() {
		super(StageProtocol.ftp);
	}

	@Override
	public StageEditor<? extends StageData> createEditor(Window owner) {
		return new FtpStageEditor(owner);
	}
}