package edu.virginia.vcgr.appmgr.version;

import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.appmgr.launcher.ApplicationDescription;

public class VersionManager
{
	static private Log _logger = LogFactory.getLog(VersionManager.class);

	static private final String VERSION_FILENAME = "current.version";

	private Version _currentVersion;
	private File _versionFile;

	public VersionManager()
	{
		_logger.debug("into version manager");
		_versionFile = new File(ApplicationDescription.getInstallationDirectory(), VERSION_FILENAME);
		_currentVersion = null;
	}

	public Version getCurrentVersion() throws IOException
	{
		if (_currentVersion == null) {
			// we go with the installer scheme to start with, where there's a current.version in the top-level.
			if (!_versionFile.exists()) {
				// try failing over to the source code's version of the file inside the installer directory.
				_versionFile = new File(ApplicationDescription.getInstallationDirectory(), "installer/" + VERSION_FILENAME);
				if (!_versionFile.exists()) {
					_currentVersion = Version.EMPTY_VERSION;
				}
			}
			if (_versionFile.exists()) {
				_currentVersion = new Version(_versionFile);
			}
		}

		return _currentVersion;
	}
}
