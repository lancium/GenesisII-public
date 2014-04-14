package edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.LinkedList;

import edu.virginia.vcgr.genii.client.io.fslock.FSLock;
import edu.virginia.vcgr.genii.client.io.fslock.FSLockManager;
import edu.virginia.vcgr.genii.container.exportdir.lightweight.AbstractVExportEntry;
import edu.virginia.vcgr.genii.container.exportdir.lightweight.VExportDir;
import edu.virginia.vcgr.genii.container.exportdir.lightweight.VExportEntry;
import edu.virginia.vcgr.genii.container.exportdir.lightweight.VExportFile;
import edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk.proxyio.client.FileServerClient;
import edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk.proxyio.client.FileServerID;
import edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk.proxyio.client.request.DefaultResponse;
import edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk.proxyio.client.request.DirListResponse;
import edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk.proxyio.client.request.ReadResponse;
import edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk.proxyio.client.request.StatResponse;
import edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk.proxyio.commons.DirListing;
import edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk.proxyio.commons.ErrorCode;
import edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk.proxyio.commons.StatAttributes;
import edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk.proxyio.utils.PathType;

public class SudoDiskExportEntry extends AbstractVExportEntry implements VExportDir, VExportFile
{
	static private FSLockManager _lockManager = new FSLockManager();

	private File _target;
	private String _uname;

	public SudoDiskExportEntry(File target, String uname) throws IOException
	{
		super (target.getName(), isDir(target, uname));
		_target = target;
		_uname = uname;

		if (!doesExist(_target, _uname))
			throw new FileNotFoundException(String.format("Unable to locate file system entry \"%s\".", _target));
	}

	/**
	 * 
	 * @param target The file object which you want to check if it's a directory
	 * @param uname The local username on whose behalf the file system operation is to be
	 * performed
	 * @return true/false whether it's a directory or not
	 * @throws IOException
	 */
	public static boolean isDir(File target, String uname) throws IOException{
		FileServerID fsid = startIfNecessary(uname);
		if (fsid == null) {
			throw new IOException ("Unable to start i/o proxy");
		}
		try {
			DefaultResponse dr = FileServerClient.isDir(target.getAbsolutePath(),
					fsid.getNonce(), 
					fsid.getPort());

			if (dr.getErrorCode() == ErrorCode.SUCCESS_CODE) {
				return true;
			} else if (dr.getErrorCode() == ErrorCode.NOT_DIR_CODE) {
				return false;
			} else {
				throw new IOException (dr.getErrorMsg());
			}

		} catch (Exception e) {
			throw new IOException (e.toString());
		}

	}

	/**
	 * 
	 * @param target The File object which you want to check exists or not
	 * @param uname The local username on whose behalf the file system operation is to be
	 * performed
	 * @return true/false depending on existance of the file object
	 * @throws IOException
	 */
	public static boolean doesExist(File target, String uname) throws IOException {
		FileServerID fsid = startIfNecessary(uname);
		if (fsid == null) {
			throw new IOException ("Unable to start i/o proxy");
		}
		try {
			//the exists check can be called with pathtype of file/dir!
			DefaultResponse dr = FileServerClient.exists(target.getAbsolutePath(),
					fsid.getNonce(), fsid.getPort(), PathType.FILE);

			if (dr.getErrorCode() == ErrorCode.SUCCESS_CODE) {
				return true;
			} else if (dr.getErrorCode() == ErrorCode.FNF_CODE) {
				return false;
			} else {
				throw new IOException (dr.getErrorMsg());
			}

		} catch (Exception e) {
			throw new IOException (e.toString());
		}
	}

	@Override	
	public boolean createFile(String newFileName) throws IOException
	{
		String fullNewFileName = _target.getAbsolutePath() + 
				File.separator + newFileName;
		return (createNewFile(fullNewFileName));
	}

	private boolean createNewFile(String fullNewFileName) 
			throws IOException {

		FileServerID fsid = startIfNecessary(_uname);
		if (fsid == null) {
			throw new IOException ("Unable to start i/o proxy");
		}
		try {
			DefaultResponse dr = FileServerClient.creat(fullNewFileName,
					fsid.getNonce(), fsid.getPort());

			if (dr.getErrorCode() == ErrorCode.SUCCESS_CODE) {
				return true;
			} else if (dr.getErrorCode() == ErrorCode.CREATE_FAIL_CODE) {
				return false;
			} else {
				throw new IOException (dr.getErrorMsg());
			}

		} catch (Exception e) {
			throw new IOException (e.toString());
		}
	}

	@Override
	public Collection<VExportEntry> list(String name) throws IOException
	{
		FileServerID fsid = startIfNecessary(_uname);
		if (fsid == null) {
			throw new IOException ("Unable to start i/o proxy");
		}

		Collection<VExportEntry> entries = new LinkedList<VExportEntry>();
		try {
			DirListResponse dlr = FileServerClient.listlong(_target.getAbsolutePath(),
					fsid.getNonce(), fsid.getPort());

			if (dlr.getErrorCode() != ErrorCode.SUCCESS_CODE) {
				throw new IOException(dlr.getErrorMsg());
			} 


			DirListing dlisting = dlr.getListing();
			if (dlisting == null) {
				return entries;
			}

			ArrayList<StatAttributes> contents = dlisting.getDirListing();
			for (StatAttributes content : contents) {
				if (name == null || name.equals(content.getFileName())) {
					if (content.getType() != PathType.LINK) {
						entries.add(new SudoDiskExportEntry(
								new File(_target, content.getFileName()),
								_uname));
					}
				}

			}

		} catch (Exception e) {
			throw new IOException (e.toString());
		}

		return entries;
	}

	@Override
	public boolean mkdir(String newDirName) throws IOException
	{
		String fullNewDirName = _target.getAbsolutePath() + 
				File.separator + newDirName;
		return createDir(fullNewDirName);
	}	

	private boolean createDir(String fullNewDirName) 
			throws IOException {
		FileServerID fsid = startIfNecessary(_uname);
		if (fsid == null) {
			throw new IOException ("Unable to start i/o proxy");
		}
		try {
			DefaultResponse dr = FileServerClient.mkdir(fullNewDirName, 
					fsid.getNonce(), fsid.getPort());

			if (dr.getErrorCode() == ErrorCode.SUCCESS_CODE) {
				return true;
			} else if (dr.getErrorCode() == ErrorCode.MKDIR_FAIL_CODE) {
				return false;
			} else {
				throw new IOException (dr.getErrorMsg());
			}

		} catch (Exception e) {
			throw new IOException (e.toString());
		}
	}

	@Override
	public boolean remove(String entryName) throws IOException
	{
		String fullRmName = _target.getAbsolutePath() + 
				File.separator + entryName;
		return rm(fullRmName);
	}

	private boolean rm(String fullRmName) throws IOException{
		FileServerID fsid = startIfNecessary(_uname);
		if (fsid == null) {
			throw new IOException ("Unable to start i/o proxy");
		}
		try {
			DefaultResponse dr;
			if (isDirectory()) {
				dr = FileServerClient.rmdir(fullRmName, fsid.getNonce(),
						fsid.getPort());
			} else {
				dr = FileServerClient.rm(fullRmName, 
						fsid.getNonce(), fsid.getPort());
			}

			if (dr.getErrorCode() == ErrorCode.SUCCESS_CODE) {
				return true;
			} else if (dr.getErrorCode() == ErrorCode.DELETE_FAIL_CODE) {
				return false;
			} else {
				throw new IOException (dr.getErrorMsg());
			}

		} catch (Exception e) {
			throw new IOException (e.toString());
		}
	}

	@Override
	public Calendar accessTime() throws IOException
	{
		Calendar c = Calendar.getInstance();
		FileServerID fsid = startIfNecessary(_uname);
		if (fsid == null) {
			throw new IOException ("Unable to start i/o proxy");
		}
		try {
			StatResponse sr = FileServerClient.stat(_target.getAbsolutePath(),
					fsid.getNonce(), fsid.getPort());

			if (sr.getErrorCode() == ErrorCode.SUCCESS_CODE) {
				StatAttributes sa = sr.getStat();
				c.setTimeInMillis(sa.getLastAccessTime());
				return c;
			} else {
				throw new IOException (sr.getErrorMsg());
			}

		} catch (Exception e) {
			throw new IOException (e.toString());
		}

	}

	@Override
	public void accessTime(Calendar c) throws IOException
	{
		// Do nothing
	}

	@Override
	public Calendar createTime() throws IOException
	{
		return Calendar.getInstance();
	}

	@Override
	public Calendar modificationTime() throws IOException
	{
		return getModTime(_target.getAbsolutePath());
	}

	private Calendar getModTime(String path) throws IOException {
		
		Calendar c = Calendar.getInstance();
		FileServerID fsid = startIfNecessary(_uname);
		if (fsid == null) {
			throw new IOException ("Unable to start i/o proxy");
		}
		try {
			StatResponse sr = FileServerClient.stat(path,
					fsid.getNonce(), fsid.getPort());

			if (sr.getErrorCode() == ErrorCode.SUCCESS_CODE) {
				StatAttributes sa = sr.getStat();
				c.setTimeInMillis(sa.getLastModifiedTime());
				return c;
			} else {
				throw new IOException (sr.getErrorMsg());
			}

		} catch (Exception e) {
			throw new IOException (e.toString());
		}
	}

	@Override
	public void modificationTime(Calendar c) throws IOException
	{
		// Do nothing
	}

	@Override
	public void read(long offset, ByteBuffer target) throws IOException
	{
		FSLock lock = null;
		FileServerID fsid = startIfNecessary(_uname);
		if (fsid == null) {
			throw new IOException ("Unable to start i/o proxy");
		}

		try {
			lock = _lockManager.acquire(_target);

			ReadResponse rr = FileServerClient.read(_target.getAbsolutePath(),
					offset, target.limit(), fsid.getNonce(), fsid.getPort());

			if (rr.getErrorCode() == ErrorCode.SUCCESS_CODE) {
				target.put(rr.getReadBuf());
			} else {
				throw new IOException(rr.getErrorMsg());
			}
		} finally {
			if (lock != null)
				lock.release();
		}
	}

	@Override
	public boolean readable() throws IOException
	{
		return canRead(_target.getAbsolutePath(), _uname);
	}

	/**
	 * 
	 * @param path The file system path which you want to check if it's readable
	 * @param uname The local username on whose behalf the file system operation is to be
	 * performed
	 * @return true/false depending on whether the given file system path is readable
	 * by uname or not
	 * @throws IOException
	 */
	public static boolean canRead(String path, String uname) throws IOException {

		FileServerID fsid = startIfNecessary(uname);
		if (fsid == null) {
			throw new IOException ("Unable to start i/o proxy");
		}
		try {
			//the canRead check can be called with pathtype of file/dir!
			DefaultResponse dr = FileServerClient.canRead(path,
						fsid.getNonce(), fsid.getPort(), PathType.FILE);

			if (dr.getErrorCode() == ErrorCode.SUCCESS_CODE) {
				return true;
			} else if (dr.getErrorCode() == ErrorCode.CANNOT_READ_CODE) {
				return false;
			} else {
				throw new IOException (dr.getErrorMsg());
			}

		} catch (Exception e) {
			throw new IOException (e.toString());
		}
	}

	@Override
	public long size() throws IOException
	{
		FSLock lock = null;

		FileServerID fsid = startIfNecessary(_uname);
		if (fsid == null) {
			throw new IOException ("Unable to start i/o proxy");
		}

		try {
			lock = _lockManager.acquire(_target);
			if (isDirectory()) {
				return 0L;
			} else {
				StatResponse sr = FileServerClient.stat(_target.getAbsolutePath(),
						fsid.getNonce(), fsid.getPort());
				if (sr.getErrorCode() == ErrorCode.SUCCESS_CODE) {
					return sr.getStat().getSize();
				} else {
					throw new IOException (sr.getErrorMsg());
				}
			}

		} finally {
			if (lock != null)
				lock.release();
		}
	}

	@Override
	public void truncAppend(long offset, ByteBuffer source) throws IOException
	{

		FSLock lock = null;
		FileServerID fsid = startIfNecessary(_uname);
		if (fsid == null) {
			throw new IOException ("Unable to start i/o proxy");
		}

		try {
			lock = _lockManager.acquire(_target);

			byte[] buf = new byte[source.remaining()];
			source.get(buf);

			DefaultResponse dr = FileServerClient.truncAppend (_target.getAbsolutePath(),
					buf, offset,fsid.getNonce(), fsid.getPort());
			if (dr.getErrorCode() != ErrorCode.SUCCESS_CODE) {
				throw new IOException (dr.getErrorMsg());
			}
		} finally {
			if (lock != null)
				lock.release();
		}

	}

	@Override
	public boolean writable() throws IOException
	{
		FileServerID fsid = startIfNecessary(_uname);
		if (fsid == null) {
			throw new IOException ("Unable to start i/o proxy");
		}
		try {
			//the canWrite check can be called with pathtype of file/dir!
			DefaultResponse dr;
			if (isDirectory()) {
				dr = FileServerClient.canWrite(_target.getAbsolutePath(),
						fsid.getNonce(), fsid.getPort(), PathType.DIRECTORY);
			} else {
				dr = FileServerClient.canWrite(_target.getAbsolutePath(),
						fsid.getNonce(), fsid.getPort(), PathType.FILE);
			}

			if (dr.getErrorCode() == ErrorCode.SUCCESS_CODE) {
				return true;
			} else if (dr.getErrorCode() == ErrorCode.CANNOT_WRITE_CODE) {
				return false;
			} else {
				throw new IOException (dr.getErrorMsg());
			}

		} catch (Exception e) {
			throw new IOException (e.toString());
		}
	}

	@Override
	public void write(long offset, ByteBuffer source) throws IOException
	{
		FSLock lock = null;
		FileServerID fsid = startIfNecessary(_uname);
		if (fsid == null) {
			throw new IOException ("Unable to start i/o proxy");
		}

		try {
			lock = _lockManager.acquire(_target);

			byte[] buf = new byte[source.remaining()];
			source.get(buf);
			DefaultResponse dr = FileServerClient.write(_target.getAbsolutePath(),
					buf, offset,fsid.getNonce(), fsid.getPort());
			if (dr.getErrorCode() != ErrorCode.SUCCESS_CODE) {
				throw new IOException (dr.getErrorMsg());
			}
		} finally {
			if (lock != null)
				lock.release();
		}
	}

	public static boolean isLink(File target, String uname) throws IOException
	{

		FileServerID fsid = startIfNecessary(uname);
		if (fsid == null) {
			throw new IOException ("Unable to start i/o proxy");
		}
		try {
			StatResponse sr = FileServerClient.stat(target.getAbsolutePath(),
					fsid.getNonce(), fsid.getPort());

			if (sr.getErrorCode() == ErrorCode.SUCCESS_CODE) {
				StatAttributes sa = sr.getStat();
				if (sa.getType() == PathType.LINK) {
					return true;
				} else {
					return false;
				}
			} else {
				throw new IOException (sr.getErrorMsg());
			}

		} catch (Exception e) {
			throw new IOException (e.toString());
		}


	}

	public int getTotalSize(String name) throws IOException
	{

		int numEntries = 0;

		FileServerID fsid = startIfNecessary(_uname);
		if (fsid == null) {
			throw new IOException ("Unable to start i/o proxy");
		}

		try {
			DirListResponse dlr = FileServerClient.listlong(_target.getAbsolutePath(),
					fsid.getNonce(), fsid.getPort());

			if (dlr.getErrorCode() != ErrorCode.SUCCESS_CODE) {
				throw new IOException(dlr.getErrorMsg());
			} 

			DirListing dlisting = dlr.getListing();
			if (dlisting == null) {
				return numEntries;
			}

			ArrayList<StatAttributes> contents = dlisting.getDirListing();
			for (StatAttributes content : contents) {
				if (content.getType() != PathType.LINK) {
					numEntries++;
				}

			}

		} catch (Exception e) {
			throw new IOException (e.toString());
		}

		return numEntries;
	}

	public Collection<String> getListing() throws IOException
	{
		return null;
	}

	@Override
	public Collection<VExportEntry> list() throws IOException
	{
		return list(null);
	}

	private static synchronized FileServerID startIfNecessary(String username) throws IOException {

		FileServerID fsid = FileServerClient.start(username);
		if (fsid == null) {
			throw new IOException("Unable to start proxy io server");
		}
		return fsid;
	}
}