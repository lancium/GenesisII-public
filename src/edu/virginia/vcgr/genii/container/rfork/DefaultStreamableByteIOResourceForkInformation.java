package edu.virginia.vcgr.genii.container.rfork;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.StreamCorruptedException;

import edu.virginia.vcgr.genii.client.resource.ResourceException;

public class DefaultStreamableByteIOResourceForkInformation implements ResourceForkInformation
{
	static final long serialVersionUID = 0L;

	private boolean _destroyOnClose;
	private boolean _doNotify;
	private String _forkPath;
	private ResourceForkInformation _dependentForkInfo;
	transient private StreamableByteIOFactoryResourceFork _dependentFork = null;
	private String _containerFilename;

	public DefaultStreamableByteIOResourceForkInformation(String forkPath, StreamableByteIOFactoryResourceFork dependentFork,
		String containerFilename, boolean destroyOnClose, boolean doNotify)
	{
		_destroyOnClose = destroyOnClose;
		_doNotify = doNotify;
		_forkPath = forkPath;
		_dependentFork = dependentFork;
		_dependentForkInfo = dependentFork.describe();
		_containerFilename = containerFilename;
	}

	@Override
	public ResourceFork instantiateFork(ResourceForkService forkService) throws ResourceException
	{
		if (_dependentFork == null)
			_dependentFork = (StreamableByteIOFactoryResourceFork) _dependentForkInfo.instantiateFork(forkService);

		try {
			return new DefaultStreamableByteIOResourceFork(forkService, _forkPath, _destroyOnClose, _doNotify, _dependentFork,
				_containerFilename);
		} catch (IOException ioe) {
			throw new ResourceException("Unable to instantiate fork.", ioe);
		}
	}

	@Override
	public Class<? extends ResourceFork> forkClass()
	{
		return _dependentForkInfo.forkClass();
	}

	private void writeObject(ObjectOutputStream out) throws IOException
	{
		out.writeBoolean(_destroyOnClose);
		out.writeBoolean(_doNotify);
		out.writeUTF(_forkPath);
		out.writeObject(_dependentForkInfo);
		out.writeUTF(_containerFilename);
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		_dependentFork = null;
		_destroyOnClose = in.readBoolean();
		_doNotify = in.readBoolean();
		_forkPath = in.readUTF();
		_dependentForkInfo = (ResourceForkInformation) in.readObject();
		_containerFilename = in.readUTF();
	}

	@SuppressWarnings("unused")
	private void readObjectNoData() throws ObjectStreamException
	{
		throw new StreamCorruptedException("Unable to deserialize resource fork information.");
	}

	@Override
	public String forkPath()
	{
		return _forkPath;
	}
}