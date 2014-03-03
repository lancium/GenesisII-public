package edu.virginia.vcgr.genii.container.rfork;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractStreamableByteIOFactoryResourceFork extends AbstractResourceFork implements
	StreamableByteIOFactoryResourceFork
{
	static private class SimpleCountingStream extends OutputStream
	{
		private long _count = 0;

		public long getCount()
		{
			return _count;
		}

		@Override
		public void write(byte[] b)
		{
			_count += b.length;
		}

		@Override
		public void write(byte[] b, int off, int len)
		{
			_count += len;
		}

		@Override
		public void write(int b) throws IOException
		{
			_count++;
		}
	}

	static private AdvertisedSize discoverAdvertisedSize(Class<?> cl)
	{
		if (cl == null || cl.equals(Object.class))
			return null;

		AdvertisedSize aSize = cl.getAnnotation(AdvertisedSize.class);
		if (aSize == null) {
			if (!cl.isInterface()) {
				for (Class<?> cl2 : cl.getInterfaces()) {
					aSize = discoverAdvertisedSize(cl2);
					if (aSize != null)
						return aSize;
				}

				aSize = discoverAdvertisedSize(cl.getSuperclass());
			}
		}

		return aSize;
	}

	static private Map<Class<?>, AdvertisedSize> _sizeMap = new HashMap<Class<?>, AdvertisedSize>();

	static private AdvertisedSize getAdvertisedSize(Class<?> cl)
	{
		AdvertisedSize aSize = null;

		synchronized (_sizeMap) {
			aSize = _sizeMap.get(cl);
			if (aSize == null && !_sizeMap.containsKey(cl)) {
				aSize = discoverAdvertisedSize(cl);
				_sizeMap.put(cl, aSize);
			}
		}

		return aSize;
	}

	protected AbstractStreamableByteIOFactoryResourceFork(ResourceForkService service, String forkPath)
	{
		super(service, forkPath);
	}

	@Override
	public Calendar accessTime()
	{
		return Calendar.getInstance();
	}

	@Override
	public void accessTime(Calendar newTime)
	{
		// do nothing
	}

	@Override
	public Calendar createTime()
	{
		return Calendar.getInstance();
	}

	@Override
	public Calendar modificationTime()
	{
		return Calendar.getInstance();
	}

	@Override
	public void modificationTime(Calendar newTime)
	{
		// do nothing
	}

	@Override
	public boolean readable()
	{
		return true;
	}

	@Override
	public boolean writable()
	{
		return true;
	}

	@Override
	public long size()
	{
		AdvertisedSize aSize = getAdvertisedSize(getClass());

		if (aSize != null && aSize.value() >= 0L)
			return aSize.value();

		try {
			SimpleCountingStream stream = new SimpleCountingStream();
			snapshotState(stream);
			return stream.getCount();
		} catch (IOException ioe) {
			throw new RuntimeException("Unable to get size of stream.", ioe);
		}
	}
}