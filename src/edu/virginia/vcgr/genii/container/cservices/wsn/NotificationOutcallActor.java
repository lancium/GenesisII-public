package edu.virginia.vcgr.genii.container.cservices.wsn;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.axis.message.MessageElement;
import org.oasis_open.wsn.base.NotificationMessageHolderType;
import org.oasis_open.wsn.base.Notify;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.comm.attachments.AttachmentType;
import edu.virginia.vcgr.genii.client.comm.attachments.GeniiAttachment;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.notification.NotificationConstants;
import edu.virginia.vcgr.genii.client.wsrf.wsn.notification.NotificationMessageHolder;
import edu.virginia.vcgr.genii.common.GeniiCommon;
import edu.virginia.vcgr.genii.common.notification.NotifyResponseType;
import edu.virginia.vcgr.genii.container.cservices.percall.OutcallActor;

public class NotificationOutcallActor implements OutcallActor
{
	static final long serialVersionUID = 0L;

	private ICallingContext _callingContext;
	private Collection<NotificationMessageOutcallContent> _contents = new LinkedList<NotificationMessageOutcallContent>();
	private boolean _persistent;

	public NotificationOutcallActor(NotificationMessageOutcallContent... contents) throws FileNotFoundException, IOException
	{
		_callingContext = ContextManager.getExistingContext().deriveNewContext();

		for (NotificationMessageOutcallContent content : contents)
			_contents.add(content);
	}

	final public void add(NotificationMessageOutcallContent content)
	{
		_contents.add(content);
	}

	public void setPersistent(boolean persistent)
	{
		_persistent = persistent;
	}

	@Override
	public boolean enactOutcall(ICallingContext callingContext, EndpointReferenceType target, GeniiAttachment attachment)
		throws Throwable
	{
		Collection<NotificationMessageHolderType> holders = new ArrayList<NotificationMessageHolderType>(_contents.size());

		List<MessageElement> messageElements = new ArrayList<MessageElement>();
		int messageIndex = 0;
		MessageElement[] additionalAttributes = null;

		for (NotificationMessageOutcallContent content : _contents) {
			NotificationMessageHolder holder = new NotificationMessageHolder(content.subscriptionReference(),
				content.publisher(), content.topic(), content.contents());
			holders.add(holder.toAxisType());
			additionalAttributes = content.contents().getAdditionalAttributes();

			// If there are additional attributes in the notification message then
			// retrieve those, add the attributes separator element, and finally
			// add all the attributes in the collections. The separator is subsequently
			// used to determine which attribute belongs to what message.
			if (additionalAttributes != null && additionalAttributes.length > 0) {
				messageElements.add(new MessageElement(GenesisIIConstants.NOTIFICATION_MESSAGE_ATTRIBUTES_SEPARATOR,
					messageIndex));
				messageElements.addAll(Arrays.asList(additionalAttributes));
			}
			messageIndex++;
		}

		Notify notify = new Notify(holders.toArray(new NotificationMessageHolderType[holders.size()]), null);
		notify.set_any(messageElements.toArray(new MessageElement[messageElements.size()]));

		GeniiCommon common = ClientUtils.createProxy(GeniiCommon.class, target, (callingContext == null) ? _callingContext
			: callingContext);
		if (attachment != null) {
			Collection<GeniiAttachment> attachments = new LinkedList<GeniiAttachment>();
			attachments.add(attachment);
			ClientUtils.setAttachments(common, attachments, AttachmentType.MTOM);
		}
		if (_persistent) {
			NotifyResponseType response = common.notifyWithResponse(notify);
			if ((response != null) && (response.getStatus() != null)
				&& (response.getStatus().toString().equals(NotificationConstants.TRYAGAIN)))
				return false;
		} else {
			common.notify(notify);
		}
		return true;
	}
}