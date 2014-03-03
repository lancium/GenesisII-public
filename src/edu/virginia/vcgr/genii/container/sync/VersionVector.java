package edu.virginia.vcgr.genii.container.sync;

import java.io.ObjectStreamException;

/**
 * This is a compatibility class for deserialization.
 */
public class VersionVector extends edu.virginia.vcgr.genii.client.sync.VersionVector
{
	private static final long serialVersionUID = 1035761061083900662L;

	public VersionItem[] vector;

	private Object readResolve() throws ObjectStreamException
	{
		edu.virginia.vcgr.genii.client.sync.VersionItem[] realVector =
			new edu.virginia.vcgr.genii.client.sync.VersionItem[vector.length];
		for (int i = 0; i < vector.length; i++) {
			realVector[i] = new edu.virginia.vcgr.genii.client.sync.VersionItem(vector[i].uid, vector[i].version);
		}
		return new edu.virginia.vcgr.genii.client.sync.VersionVector(realVector);
	}

	private Object writeReplace() throws ObjectStreamException
	{
		return readResolve();
	}
}
