package edu.virginia.vcgr.genii.container.rfork;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.StreamCorruptedException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import edu.virginia.vcgr.genii.client.resource.ResourceException;

public class DefaultResourceForkInformation implements ResourceForkInformation
{
	static final long serialVersionUID = 0L;

	private Constructor<? extends ResourceFork> _constructor = null;
	private Class<? extends ResourceFork> _resourceForkClass;
	private String _forkPath;

	public DefaultResourceForkInformation(Class<? extends ResourceFork> resourceForkClass, String forkPath)
	{
		_resourceForkClass = resourceForkClass;
		_forkPath = forkPath.replaceAll("/{2,}", "/");
	}

	@Override
	public ResourceFork instantiateFork(ResourceForkService forkService) throws ResourceException
	{
		try {
			Constructor<? extends ResourceFork> constructor;
			synchronized (_resourceForkClass) {
				if (_constructor == null) {
					_constructor = _resourceForkClass.getConstructor(ResourceForkService.class, String.class);
				}

				constructor = _constructor;
			}

			return constructor.newInstance(forkService, _forkPath);
		} catch (NoSuchMethodException nsme) {
			throw new ResourceException("Unable to find appropriate constructor for resource fork.", nsme);
		} catch (IllegalArgumentException e) {
			throw new ResourceException("Unable to instantiate resource fork.", e);
		} catch (InstantiationException e) {
			throw new ResourceException("Unable to instantiate resource fork.", e);
		} catch (IllegalAccessException e) {
			throw new ResourceException("Unable to instantiate resource fork.", e);
		} catch (InvocationTargetException e) {
			Throwable cause = e.getCause();
			if (cause == null)
				cause = e;
			if (cause instanceof ResourceException)
				throw ((ResourceException) cause);

			throw new ResourceException("Unable to instantiate resource fork.", cause);
		}
	}

	@Override
	public Class<? extends ResourceFork> forkClass()
	{
		return _resourceForkClass;
	}

	private void writeObject(ObjectOutputStream out) throws IOException
	{
		out.writeUTF(_resourceForkClass.getName());
		out.writeUTF(_forkPath);
	}

	@SuppressWarnings("unchecked")
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		_constructor = null;
		_resourceForkClass = (Class<? extends ResourceFork>) Class.forName(in.readUTF());
		_forkPath = in.readUTF().replaceAll("/{2,}", "/");
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