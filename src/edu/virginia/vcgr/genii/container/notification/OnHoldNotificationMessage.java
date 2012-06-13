package edu.virginia.vcgr.genii.container.notification;

import java.util.Date;

import org.apache.axis.message.MessageElement;
import org.oasis_open.wsn.base.NotificationMessageHolderType;

public class OnHoldNotificationMessage {
	
	private Date messagePublicationTime;
	private NotificationMessageHolderType holderType;
	private MessageElement[] additionalAttributes;
	
	public Date getMessagePublicationTime() {
		return messagePublicationTime;
	}

	public void setMessagePublicationTime(Date messagePublicationTime) {
		this.messagePublicationTime = messagePublicationTime;
	}

	public NotificationMessageHolderType getHolderType() {
		return holderType;
	}
	
	public void setHolderType(NotificationMessageHolderType holderType) {
		this.holderType = holderType;
	}
	
	public MessageElement[] getAdditionalAttributes() {
		return additionalAttributes;
	}
	
	public void setAdditionalAttributes(MessageElement[] additionalAttributes) {
		this.additionalAttributes = additionalAttributes;
	}
}
