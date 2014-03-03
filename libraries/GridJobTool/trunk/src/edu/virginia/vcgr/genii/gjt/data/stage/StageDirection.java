package edu.virginia.vcgr.genii.gjt.data.stage;

public enum StageDirection {
	read(true, false),
	write(false, true),
	readAndWrite(true, true);

	private boolean _canRead;
	private boolean _canWrite;

	private StageDirection(boolean canRead, boolean canWrite)
	{
		_canRead = canRead;
		_canWrite = canWrite;
	}

	final public boolean canRead()
	{
		return _canRead;
	}

	final public boolean canWrite()
	{
		return _canWrite;
	}
}