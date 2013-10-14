package edu.virginia.vcgr.appmgr.patch;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.appmgr.io.IOUtils;

class FileMoverPatchOperationTransaction implements PatchOperationTransaction
{
	static private Log _logger = LogFactory.getLog(FileMoverPatchOperationTransaction.class);

	private File _original;
	private File _backup;

	FileMoverPatchOperationTransaction(File original, File backup)
	{
		_original = original;
		_backup = backup;
	}

	@Override
	public void commit()
	{
		if (_backup != null)
			IOUtils.recursiveDelete(_backup);
	}

	@Override
	public void rollback()
	{
		IOUtils.recursiveDelete(_original);
		if (_backup != null) {
			if (!_backup.renameTo(_original))
				_logger.error(String.format("Unable to rollback file \"%s\".\n", _original));
		}
	}
}