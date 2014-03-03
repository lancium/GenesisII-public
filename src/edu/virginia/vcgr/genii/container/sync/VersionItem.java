package edu.virginia.vcgr.genii.container.sync;

import java.io.Serializable;

/**
 * This is a compatibility class for deserialization.
 */
public class VersionItem implements Serializable {
	private static final long serialVersionUID = 1L;

	public int uid;
	public int version;
}
