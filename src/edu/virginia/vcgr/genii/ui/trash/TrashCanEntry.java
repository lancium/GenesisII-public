package edu.virginia.vcgr.genii.ui.trash;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.ui.persist.Persistable;
import edu.virginia.vcgr.genii.ui.persist.PersistenceKey;

class TrashCanEntry implements Persistable
{
	private RNSPath _path;
	private ICallingContext _context;

	TrashCanEntry(ICallingContext context, RNSPath path)
	{
		_context = context;
		_path = path;
	}

	TrashCanEntry(PersistenceKey key) throws IOException, ClassNotFoundException
	{
		InputStream in = null;

		try {
			in = key.open();
			ObjectInputStream ois = new ObjectInputStream(in);
			ois.readUTF();
			_path = (RNSPath) ois.readObject();
			_context = (ICallingContext) ois.readObject();
		} finally {
			StreamUtils.close(in);
		}
	}

	final RNSPath path()
	{
		return _path;
	}

	final ICallingContext callingContext()
	{
		return _context;
	}

	@Override
	public boolean persist(ObjectOutputStream oos) throws IOException
	{
		oos.writeUTF(_path.pwd());
		oos.writeObject(_path);
		oos.writeObject(_context);
		return true;
	}

	static public String readPath(PersistenceKey key) throws IOException
	{
		InputStream in = null;

		try {
			in = key.open();
			ObjectInputStream ois = new ObjectInputStream(in);
			return ois.readUTF();
		} finally {
			StreamUtils.close(in);
		}
	}
}