package edu.virginia.vcgr.genii.client.wsrf.wsn;

public abstract class AbstractNotificationHandler<ContentsType extends NotificationMessageContents>
	implements NotificationHandler<ContentsType>
{
	private Class<ContentsType> _contentsType;
	
	protected AbstractNotificationHandler(Class<ContentsType> contentsType)
	{
		_contentsType = contentsType;
	}
	
	@Override
	final public Class<ContentsType> contentsType()
	{
		return _contentsType;
	}
}