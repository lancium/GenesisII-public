package edu.virginia.vcgr.genii.client.byteio;

/**
 * A simple enum to identify the origin of a seek operation in streamable byteio transfer
 * operations.
 * 
 * @author mmm2a
 */
public enum SeekOrigin {
	SEEK_BEGINNING, SEEK_CURRENT, SEEK_END
}