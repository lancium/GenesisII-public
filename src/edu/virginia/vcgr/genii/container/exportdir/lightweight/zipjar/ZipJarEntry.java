package edu.virginia.vcgr.genii.container.exportdir.lightweight.zipjar;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Calendar;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.client.io.fslock.FSLock;
import edu.virginia.vcgr.genii.client.io.fslock.FSLockManager;
import edu.virginia.vcgr.genii.container.exportdir.lightweight.AbstractVExportEntry;
import edu.virginia.vcgr.genii.container.exportdir.lightweight.VExportDir;
import edu.virginia.vcgr.genii.container.exportdir.lightweight.VExportEntry;
import edu.virginia.vcgr.genii.container.exportdir.lightweight.VExportFile;

class ZipJarEntry extends AbstractVExportEntry implements VExportDir, VExportFile
{
	static private FSLockManager _lockManager = new FSLockManager();

	private Map<String, Map<String, ZipEntry>> _directoryMap;
	private ZipFile _zipFile;
	private File _zipFileTarget;
	private Map<String, ZipEntry> _directory;
	private ZipEntry _file;
	private String _forkPath;

	static private String getParent(String forkPath)
	{
		int index = forkPath.lastIndexOf('/');
		if (index < 0)
			return "";

		return forkPath.substring(0, index);
	}

	static private String getName(String forkPath)
	{
		int index = forkPath.lastIndexOf('/');
		if (index < 0)
			return forkPath;

		return forkPath.substring(index + 1);
	}

	static private boolean isDirectory(Map<String, Map<String, ZipEntry>> directoryMap, String path)
	{
		if (path.length() == 0)
			return true;

		return directoryMap.containsKey(path);
	}

	final private String formPath(String child)
	{
		if (_forkPath.length() == 0)
			return child;

		return String.format("%s/%s", _forkPath, child);
	}

	private Map<String, ZipEntry> getDirectory() throws IOException
	{
		if (_directory == null)
			throw new IOException(String.format("Couldn't find directory \"%s\".", getName()));
		return _directory;
	}

	private ZipEntry getFile() throws IOException
	{
		if (_file == null)
			throw new IOException(String.format("Couldn't find file \"%s\".", getName()));
		return _file;
	}

	ZipJarEntry(File zipFileTarget, ZipFile zipFile, Map<String, Map<String, ZipEntry>> directoryMap, String forkPath)
		throws IOException
	{
		super(getName(forkPath), isDirectory(directoryMap, forkPath));

		_zipFile = zipFile;
		_directoryMap = directoryMap;
		_zipFileTarget = zipFileTarget;
		_forkPath = forkPath;

		if (isDirectory())
			_directory = directoryMap.get(forkPath);
		else {
			_directory = directoryMap.get(getParent(forkPath));
			if (_directory == null)
				throw new FileNotFoundException(String.format("Unable to find entry \"%s\".", forkPath));
			_file = _directory.get(getName());
		}
	}

	@Override
	public boolean createFile(String newFileName) throws IOException
	{
		throw new IOException("Not allowed to create new files inside of a Zip/Jar export.");
	}

	@Override
	public Collection<VExportEntry> list(String name) throws IOException
	{
		Collection<VExportEntry> entries = new LinkedList<VExportEntry>();
		Map<String, ZipEntry> dir = getDirectory();
		for (String entryName : dir.keySet()) {
			if (name == null || name.equals(entryName)) {
				entries.add(new ZipJarEntry(_zipFileTarget, _zipFile, _directoryMap, formPath(entryName)));
			}
		}

		return entries;
	}

	@Override
	public boolean mkdir(String newDirName) throws IOException
	{
		throw new IOException("Not allowed to make new directories inside of a Zip/Jar export.");
	}

	@Override
	public boolean remove(String entryName) throws IOException
	{
		throw new IOException("Not allowed to remove entries from a Zip/Jar export.");
	}

	@Override
	public Calendar accessTime() throws IOException
	{
		return Calendar.getInstance();
	}

	@Override
	public void accessTime(Calendar c) throws IOException
	{
		throw new IOException("Not allowed to modify entries in a Zip/Jar export.");
	}

	@Override
	public Calendar createTime() throws IOException
	{
		return Calendar.getInstance();
	}

	@Override
	public Calendar modificationTime() throws IOException
	{
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(getFile().getTime());
		return c;
	}

	@Override
	public void modificationTime(Calendar c) throws IOException
	{
		throw new IOException("Not allowed to modify entries in a Zip/Jar export.");
	}

	@Override
	public void read(long offset, ByteBuffer target) throws IOException
	{
		FSLock lock = null;

		byte[] data = new byte[1024 * 8];
		int read;
		ZipEntry entry = getFile();
		InputStream in = null;

		try {
			lock = _lockManager.acquire(_zipFileTarget);

			in = _zipFile.getInputStream(entry);
			in.skip(offset);
			while (target.hasRemaining()) {
				read = target.remaining();
				if (read > data.length)
					read = data.length;
				read = in.read(data, 0, read);
				if (read <= 0)
					break;
				target.put(data, 0, read);
			}
		} finally {
			StreamUtils.close(in);
			if (lock != null)
				lock.release();
		}
	}

	@Override
	public boolean readable() throws IOException
	{
		return _zipFileTarget.canRead();
	}

	@Override
	public long size() throws IOException
	{
		return getFile().getSize();
	}

	@Override
	public void truncAppend(long offset, ByteBuffer source) throws IOException
	{
		throw new IOException("Not allowed to modify entries in a Zip/Jar export.");
	}

	@Override
	public boolean writable() throws IOException
	{
		return false;
	}

	@Override
	public void write(long offset, ByteBuffer source) throws IOException
	{
		throw new IOException("Not allowed to modify entries in a Zip/Jar export.");
	}

	@Override
	public Collection<VExportEntry> list() throws IOException
	{
		return list(null);
	}

}