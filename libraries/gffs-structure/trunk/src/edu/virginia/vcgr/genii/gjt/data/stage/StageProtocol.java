package edu.virginia.vcgr.genii.gjt.data.stage;

import edu.virginia.vcgr.genii.gjt.data.stage.ftp.FtpStageFactory;
import edu.virginia.vcgr.genii.gjt.data.stage.grid.GridStageFactory;
import edu.virginia.vcgr.genii.gjt.data.stage.gsiftp.GsiFtpStageFactory;
import edu.virginia.vcgr.genii.gjt.data.stage.http.HttpStageFactory;
import edu.virginia.vcgr.genii.gjt.data.stage.mailto.MailtoStageFactory;
import edu.virginia.vcgr.genii.gjt.data.stage.scp.ScpStageFactory;
import edu.virginia.vcgr.genii.gjt.data.stage.undef.UndefinedStageFactory;

public enum StageProtocol {
	undefined("undefined", new UndefinedStageFactory(), StageDirection.readAndWrite),
	http("http", new HttpStageFactory(), StageDirection.readAndWrite),
	grid("grid", new GridStageFactory(), StageDirection.readAndWrite),
	ftp("ftp", new FtpStageFactory(), StageDirection.readAndWrite),
	scp("scp/sftp", new ScpStageFactory(), StageDirection.readAndWrite),
	mailto("mailto", new MailtoStageFactory(), StageDirection.write),
	gsiftp("gsiftp", new GsiFtpStageFactory(), StageDirection.readAndWrite);

	private StageDirection _stageDirection;
	private String _label;
	private StageFactory _factory;

	private StageProtocol(String label, StageFactory factory, StageDirection stageDirection)
	{
		_label = label;
		_factory = factory;
		_stageDirection = stageDirection;
	}

	final public StageFactory factory()
	{
		return _factory;
	}

	final public StageDirection stageDirection()
	{
		return _stageDirection;
	}

	@Override
	public String toString()
	{
		return _label;
	}
}