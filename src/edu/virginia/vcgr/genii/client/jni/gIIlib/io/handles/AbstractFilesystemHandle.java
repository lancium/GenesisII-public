package edu.virginia.vcgr.genii.client.jni.gIIlib.io.handles;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.fsii.FSFilesystem;
import edu.virginia.vcgr.fsii.exceptions.FSException;
import edu.virginia.vcgr.fsii.path.UnixFilesystemPathRepresentation;

abstract class AbstractFilesystemHandle implements FilesystemHandle
{
	static private Log _logger = LogFactory.getLog(AbstractFilesystemHandle.class);

	protected String[] _path;
	protected FSFilesystem _fs;

	protected AbstractFilesystemHandle(FSFilesystem fs, String[] path)
	{
		_path = path;
		_fs = fs;
	}

	@Override
	public void delete() throws FSException
	{
		if (_logger.isTraceEnabled())
			_logger.trace(String.format("AbstractFilesystemHandle::delete(%s)",
				UnixFilesystemPathRepresentation.INSTANCE.toString(_path)));

		_fs.unlink(_path);
	}

	@Override
	public boolean renameTo(String[] target)
	{
		if (_logger.isTraceEnabled())
			_logger.trace(String.format("AbstractFilesystemHandle::renameTo(%s, %s)",
				UnixFilesystemPathRepresentation.INSTANCE.toString(_path),
				UnixFilesystemPathRepresentation.INSTANCE.toString(target)));

		try {
			_fs.rename(_path, target);
			return true;
		} catch (Throwable cause) {
			return false;
		}
	}
}