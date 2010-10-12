package edu.virginia.vcgr.genii.container.cservices.history;

import java.io.Closeable;
import java.util.Iterator;

public interface CloseableIterator<Type>
	extends Closeable, Iterator<Type>
{
}