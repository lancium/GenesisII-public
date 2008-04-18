package edu.virginia.vcgr.genii.client.notification;

/**
 * This class is a placeholder for constants which represent the notification topics of
 * interest considered well-known within the system.
 * 
 * @author mmm2a
 */
public class WellknownTopics
{
	/** This topic is raised by all resources when they are terminated */
	static public final String TERMINATED =
		"edu.virginia.vcgr.genii.common.lifetime.terminated";
	
	/** This topic is raised by some RNS implementations when a new entry is 
	 * added to their list. */
	static public final String RNS_ENTRY_ADDED =
		"edu.virginia.vcgr.genii.rns.entry-added";
	
	/** This topic is raised by an SByteIO instance when it is about to be <I>closed</I>
	 * or terminated.
	 */
	static public final String SBYTEIO_INSTANCE_DYING =
		"edu.virginia.vcgr.genii.byteio.sbyteio.instance-dying";
	
	/** This topic is raised when rbyteio operation occurs */
	static public final String RANDOM_BYTEIO_OP = 
		"edu.virginia.vcgr.genii.byteio.rbyteio.op";
}