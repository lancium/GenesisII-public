package edu.virginia.vcgr.genii.gjt.data.stage.undef;

import edu.virginia.vcgr.genii.gjt.data.analyze.Analysis;
import edu.virginia.vcgr.genii.gjt.data.stage.AbstractStageData;
import edu.virginia.vcgr.genii.gjt.data.stage.StageProtocol;

public class UndefinedStageData extends AbstractStageData {
	@Override
	protected void activateImpl() {
		// Nothing to do
	}

	@Override
	protected void deactivateImpl() {
		// Nothing to do
	}

	public UndefinedStageData() {
		super(StageProtocol.undefined);
	}

	@Override
	public void analyze(String filename, Analysis analysis) {
		analysis.addError("A data stage for file \"%s\" is not defined.",
				filename);
	}

	@Override
	public String getJSDLURI() {
		throw new UnsupportedOperationException(
				"Can't get the JSDL URI for an undefined data stage type.");
	}
}
