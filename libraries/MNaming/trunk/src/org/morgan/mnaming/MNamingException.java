package org.morgan.mnaming;

public class MNamingException extends Exception {
	static final long serialVersionUID = 0L;

	public MNamingException(String msg) {
		super(msg);
	}

	public MNamingException(String msg, Throwable cause) {
		super(msg, cause);
	}
}