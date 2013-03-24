package edu.virginia.vcgr.genii.container.q2;

import java.sql.SQLException;

import org.morgan.inject.InjectionException;
import org.morgan.inject.MInject;
import org.morgan.inject.MInjectFactory;

import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.container.resource.StringResourceIdentifier;

class QueueManagerInjectionFactory implements MInjectFactory
{
	@MInject
	private StringResourceIdentifier _resourceID;

	@Override
	public <Type> Type resolve(MInject arg0, Class<Type> targetType) throws InjectionException
	{
		try {
			return targetType.cast(QueueManager.getManager(_resourceID.key()));
		} catch (ResourceException e) {
			throw new InjectionException(String.format("Cannot inject QueueManager for queue %s.", _resourceID), e);
		} catch (SQLException e) {
			throw new InjectionException(String.format("Cannot inject QueueManager for queue %s.", _resourceID), e);
		}
	}
}