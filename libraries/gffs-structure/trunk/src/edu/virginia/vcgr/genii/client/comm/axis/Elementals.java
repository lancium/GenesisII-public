package edu.virginia.vcgr.genii.client.comm.axis;

import java.util.Collection;

import org.apache.axis.message.MessageElement;

import edu.virginia.vcgr.genii.client.ser.AnyHelper;

/**
 * helpers for the MessageElement class provided by Apache Axis.
 * 
 * @author Chris Koeritz
 * @copyright Copyright (c) 2014-$now By University of Virginia
 * @license This file is free software; you can modify and redistribute it under the terms of the
 *          Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
public class Elementals
{
	/**
	 * a frequently used pattern, where we want to pass axis a list of message elements with zero
	 * length.
	 */
	public static MessageElement[] getEmptyArray()
	{
		return new MessageElement[0];
	}

	/**
	 * returns a list with exactly one axis version of the MessageElement in it. the message element
	 * comes from conversion of the object passed in.
	 */
	public static MessageElement[] objectToArray(Object toElement)
	{
		MessageElement[] result = new MessageElement[1];
		result[0] = AnyHelper.toAny(toElement);
		return result;
	}

	/**
	 * a simple helper method that spits out an array containing the one element that is passed in.
	 */
	public static MessageElement[] unitaryArray(MessageElement toEncapsulate)
	{
		return new MessageElement[] { toEncapsulate };
	}

	/**
	 * converts a collection of our message elements into an array of them.
	 */
	static public MessageElement[] toArray(Collection<MessageElement> toConvert)
	{
		if (toConvert == null)
			return null;
		if (toConvert.size() == 0)
			return new MessageElement[0];
		MessageElement[] toReturn = new MessageElement[toConvert.size()];
		int indy = 0;
		for (MessageElement e : toConvert) {
			toReturn[indy++] = e;
		}
		return toReturn;
	}

	/**
	 * converts a collection of clean message elements into the axis equivalent.
	 */
	static public org.apache.axis.message.MessageElement[] toAxisArray(Collection<MessageElement> toConvert)
	{
		if (toConvert == null)
			return null;
		if (toConvert.size() == 0)
			return new org.apache.axis.message.MessageElement[0];
		org.apache.axis.message.MessageElement[] toReturn = new org.apache.axis.message.MessageElement[toConvert.size()];
		int indy = 0;
		for (MessageElement e : toConvert) {
			toReturn[indy++] = e;
		}
		return toReturn;
	}

	/**
	 * slightly different from original getChildren() which returns a List (without generic
	 * arguments); we switch to an array automatically since we mainly use that.
	 */
	static public org.apache.axis.message.MessageElement[] getOurChildren(MessageElement parent)
	{
		if (parent.getChildren() == null)
			return null;
		return (org.apache.axis.message.MessageElement[]) (parent.getChildren().toArray());
	}
}
