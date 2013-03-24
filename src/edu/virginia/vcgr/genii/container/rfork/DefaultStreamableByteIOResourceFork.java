package edu.virginia.vcgr.genii.container.rfork;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Calendar;

import org.morgan.util.io.GuaranteedDirectory;
import org.morgan.util.io.StreamUtils;
import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;

import edu.virginia.vcgr.genii.client.byteio.SeekOrigin;
import edu.virginia.vcgr.genii.client.io.MarkableFileInputStream;
import edu.virginia.vcgr.genii.client.resource.AddressingParameters;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.container.Container;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.ResourceManager;
import edu.virginia.vcgr.genii.security.RWXCategory;
import edu.virginia.vcgr.genii.security.rwx.MappingResolver;
import edu.virginia.vcgr.genii.security.rwx.RWXManager;
import edu.virginia.vcgr.genii.security.rwx.RWXMappingException;
import edu.virginia.vcgr.genii.security.rwx.RWXMappingResolver;

public class DefaultStreamableByteIOResourceFork extends AbstractStreamableByteIOResourceFork
{
	static public class DependentStreamableMappingResolver implements MappingResolver
	{
		@Override
		public RWXCategory resolve(Class<?> serviceClass, Method operation)
		{
			try {
				ResourceKey rKey = ResourceManager.getCurrentResource();
				AddressingParameters ap = rKey.getAddressingParameters();
				if (ap != null) {
					ResourceForkInformation info = (ResourceForkInformation) ap.getResourceForkInformation();
					if (info != null) {
						DefaultStreamableByteIOResourceFork fork = (DefaultStreamableByteIOResourceFork) info
							.instantiateFork(null);
						Class<? extends StreamableByteIOFactoryResourceFork> dependentForkClass = fork._dependentFork
							.getClass();

						Method targetMethod;

						if (operation.getName().equals("seekRead")) {
							targetMethod = dependentForkClass.getMethod("snapshotState", OutputStream.class);
						} else if (operation.getName().equals("seekWrite")) {
							targetMethod = dependentForkClass.getMethod("modifyState", InputStream.class);
						} else if (operation.getName().equals("destroy")) {
							return RWXCategory.OPEN;
						} else
							throw new RWXMappingException("Target method \"" + operation.getName()
								+ "\" is not one of seekRead or seekWrite.");

						return RWXManager.lookup(dependentForkClass, targetMethod);
					}
				}
			} catch (ResourceException re) {
				throw new RWXMappingException("Unable to find RWXCategory for target operation.", re);
			} catch (ResourceUnknownFaultType e) {
				throw new RWXMappingException("Unable to find RWXCategory for target operation.", e);
			} catch (SecurityException e) {
				throw new RWXMappingException("Unable to find RWXCategory for target operation.", e);
			} catch (NoSuchMethodException e) {
				throw new RWXMappingException("Unable to find RWXCategory for target operation.", e);
			}

			throw new RWXMappingException("Unable to find target fork.");
		}
	}

	private boolean _doNotify;
	private boolean _destroyOnClose;
	private File _targetFile;
	private StreamableByteIOFactoryResourceFork _dependentFork;

	public DefaultStreamableByteIOResourceFork(ResourceForkService service, String forkPath, boolean destroyOnClose,
		boolean doNotify, StreamableByteIOFactoryResourceFork dependentFork) throws IOException
	{
		super(service, forkPath);

		_destroyOnClose = destroyOnClose;
		_doNotify = doNotify;
		_dependentFork = dependentFork;

		_targetFile = Container.getConfigurationManager().getUserDirectory();
		_targetFile = new GuaranteedDirectory(_targetFile, "sbyteio-forks");
		_targetFile = File.createTempFile("sbyteio", ".dat", _targetFile);

		OutputStream out = null;
		try {
			out = new FileOutputStream(_targetFile);
			dependentFork.snapshotState(out);
		} finally {
			StreamUtils.close(out);
		}
	}

	public DefaultStreamableByteIOResourceFork(ResourceForkService service, String forkPath, boolean destroyOnClose,
		boolean doNotify, StreamableByteIOFactoryResourceFork dependentFork, String containerFilename) throws IOException
	{
		super(service, forkPath);

		_destroyOnClose = destroyOnClose;
		_doNotify = doNotify;
		_dependentFork = dependentFork;

		_targetFile = Container.getConfigurationManager().getUserDirectory();
		_targetFile = new GuaranteedDirectory(_targetFile, "sbyteio-forks");
		_targetFile = new File(_targetFile, containerFilename);
	}

	@Override
	@RWXMappingResolver(DependentStreamableMappingResolver.class)
	public void destroy() throws ResourceException
	{
		try {
			if (_doNotify && isDirty()) {
				InputStream input = null;
				try {
					input = new MarkableFileInputStream(_targetFile);
					_dependentFork.modifyState(input);
				} catch (IOException e) {
					throw new ResourceException("Unable to modify dependent state.", e);
				} finally {
					StreamUtils.close(input);
				}
			}
		} finally {
			_targetFile.delete();
			super.destroy();
		}
	}

	@Override
	public boolean getDestroyOnClose()
	{
		return _destroyOnClose;
	}

	@Override
	public boolean getEndOfStream()
	{
		long position = getPosition();
		return position >= _targetFile.length();
	}

	@Override
	public boolean getSeekable()
	{
		return true;
	}

	private void seek(SeekOrigin origin, long seekOffset, RandomAccessFile raf) throws IOException
	{
		long offset = 0;

		if (origin == SeekOrigin.SEEK_BEGINNING)
			offset = seekOffset;
		else if (origin == SeekOrigin.SEEK_CURRENT)
			offset = getPosition() + seekOffset;
		else if (origin == SeekOrigin.SEEK_END)
			offset = raf.length() + seekOffset;

		raf.seek(offset);
	}

	@Override
	@RWXMappingResolver(DependentStreamableMappingResolver.class)
	public void seekRead(SeekOrigin origin, long seekOffset, ByteBuffer destination) throws IOException
	{
		RandomAccessFile raf = null;

		try {
			raf = new RandomAccessFile(_targetFile, "r");
			seek(origin, seekOffset, raf);
			FileChannel channel = raf.getChannel();

			while (destination.hasRemaining()) {
				if (channel.read(destination) < 0)
					break;
			}
		} finally {
			setPosition(raf.getFilePointer());
			StreamUtils.close(raf);
		}
	}

	@Override
	@RWXMappingResolver(DependentStreamableMappingResolver.class)
	public void seekWrite(SeekOrigin origin, long seekOffset, ByteBuffer source) throws IOException
	{
		RandomAccessFile raf = null;

		try {
			raf = new RandomAccessFile(_targetFile, "rw");
			seek(origin, seekOffset, raf);
			FileChannel channel = raf.getChannel();

			while (source.hasRemaining()) {
				channel.write(source);
			}
		} finally {
			setDirty();
			setPosition(raf.getFilePointer());
			StreamUtils.close(raf);
		}
	}

	@Override
	public Calendar accessTime()
	{
		return Calendar.getInstance();
	}

	@Override
	public void accessTime(Calendar newTime)
	{
		// Do nothing
	}

	@Override
	public Calendar createTime()
	{
		return Calendar.getInstance();
	}

	@Override
	public Calendar modificationTime()
	{
		Calendar ret = Calendar.getInstance();
		ret.setTimeInMillis(_targetFile.lastModified());
		return ret;
	}

	@Override
	public void modificationTime(Calendar newTime)
	{
		_targetFile.setLastModified(newTime.getTimeInMillis());
	}

	@Override
	public boolean readable()
	{
		return true;
	}

	@Override
	public long size()
	{
		return _targetFile.length();
	}

	@Override
	public boolean writable()
	{
		return true;
	}

	@Override
	public ResourceForkInformation describe()
	{
		return new DefaultStreamableByteIOResourceForkInformation(getForkPath(), _dependentFork, _targetFile.getName(),
			_destroyOnClose, _doNotify);
	}
}