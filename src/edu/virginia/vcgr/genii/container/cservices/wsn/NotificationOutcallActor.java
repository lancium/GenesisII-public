package edu.virginia.vcgr.genii.container.cservices.wsn;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

import org.oasis_open.wsn.base.NotificationMessageHolderType;
import org.oasis_open.wsn.base.Notify;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.wsrf.wsn.notification.NotificationMessageHolder;
import edu.virginia.vcgr.genii.common.GeniiCommon;
import edu.virginia.vcgr.genii.container.cservices.percall.OutcallActor;

public class NotificationOutcallActor implements OutcallActor
{
	static final long serialVersionUID = 0L;
	
	private ICallingContext _callingContext;
	private Collection<NotificationMessageOutcallContent> _contents =
		new LinkedList<NotificationMessageOutcallContent>();
	
	public NotificationOutcallActor(
		NotificationMessageOutcallContent...contents) 
			throws FileNotFoundException, IOException
	{
		_callingContext = ContextManager.getCurrentContext().deriveNewContext();
		
		for (NotificationMessageOutcallContent content : contents)
			_contents.add(content);
	}
	
	final public void add(NotificationMessageOutcallContent content)
	{
		_contents.add(content);
	}
	
	@Override
	public boolean enactOutcall(ICallingContext callingContext,
		EndpointReferenceType target) throws Throwable
	{
		Collection<NotificationMessageHolderType> holders =
			new ArrayList<NotificationMessageHolderType>(
				_contents.size());
		for (NotificationMessageOutcallContent content : _contents)
		{
			NotificationMessageHolder holder = new NotificationMessageHolder(
				content.subscriptionReference(), content.publisher(),
				content.topic(), content.contents());
			holders.add(holder.toAxisType());
		}
		
		Notify notify = new Notify(
			holders.toArray(new NotificationMessageHolderType[holders.size()]),
			null);
		
		GeniiCommon common = ClientUtils.createProxy(
			GeniiCommon.class, target,
			(callingContext == null) ? _callingContext : callingContext);
		common.notify(notify);
		return true;
	}
}