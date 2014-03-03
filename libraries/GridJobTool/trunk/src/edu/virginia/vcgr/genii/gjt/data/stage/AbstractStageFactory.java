package edu.virginia.vcgr.genii.gjt.data.stage;

public abstract class AbstractStageFactory implements StageFactory
{
	private StageProtocol _protocol;

	protected AbstractStageFactory(StageProtocol protocol)
	{
		_protocol = protocol;
	}

	@Override
	final public StageProtocol protocol()
	{
		return _protocol;
	}
}