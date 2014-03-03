package edu.virginia.g3.fsview;

import java.io.Closeable;
import java.io.IOException;

public interface FSViewSession extends Closeable {
	public FSViewFactory factory();

	public FSViewEntry root() throws IOException;

	public FSViewEntry lookup(String path) throws IOException;

	public boolean isOpen();

	public boolean isReadOnly();
}