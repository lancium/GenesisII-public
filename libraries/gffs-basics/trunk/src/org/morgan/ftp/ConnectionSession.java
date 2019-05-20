package org.morgan.ftp;

import java.io.Closeable;
import java.io.IOException;

public abstract class ConnectionSession implements Runnable, Closeable {

	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void run() {
		// TODO Auto-generated method stub

	}
	abstract public int getSessionID();
	abstract public long getIdleTime();
	abstract public long getIdleTimeout();

}
