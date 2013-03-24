package edu.virginia.vcgr.genii.container.sync;

import java.io.Serializable;

public class VersionVector implements Serializable
{
	private static final long serialVersionUID = 1035761061083900662L;

	/**
	 * Version vectors are very short (usually one to four items) so we use a linear search. They
	 * are made to be sent across a network, so we use a compact data structure.
	 */
	public VersionItem[] vector;

	public VersionVector()
	{
		this.vector = new VersionItem[0];
	}

	public int getLocalID()
	{
		return vector[0].uid;
	}

	public int getLocalVersion()
	{
		return vector[0].version;
	}

	public int getVersion(int uid)
	{
		for (VersionItem item : vector) {
			if (item.uid == uid)
				return item.version;
		}
		return 0;
	}

	public void setVersion(int uid, int version)
	{
		VersionItem[] newVector = new VersionItem[vector.length + 1];
		int idx;
		for (idx = 0; idx < vector.length; idx++) {
			VersionItem item = vector[idx];
			if (item.uid == uid) {
				item.version = version;
				return;
			}
			newVector[idx] = item;
		}
		if ((version > 0) || (vector.length == 0)) {
			newVector[idx] = new VersionItem(uid, version);
			this.vector = newVector;
		}
	}

	public void copy(VersionVector that)
	{
		for (VersionItem item : that.vector) {
			this.setVersion(item.uid, item.version);
		}
	}

	public String toString()
	{
		String result = "";
		for (VersionItem item : vector) {
			if (result.length() > 0)
				result += ",";
			result += "" + item.uid + ":" + item.version;
		}
		return result;
	}

	public static VersionVector fromString(String text)
	{
		VersionVector vector = new VersionVector();
		String[] tokenList = text.split(",");
		for (String token : tokenList) {
			String[] pair = token.split(":");
			int uid = Integer.parseInt(pair[0]);
			int version = Integer.parseInt(pair[1]);
			vector.setVersion(uid, version);
		}
		return vector;
	}
}
