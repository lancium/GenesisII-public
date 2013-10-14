package edu.virginia.vcgr.appmgr.version;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Calendar;

import edu.virginia.vcgr.appmgr.io.IOUtils;
import edu.virginia.vcgr.appmgr.io.ScratchSpaceManager;

public class VersionManager
{
	static private final String VERSION_FILENAME = "current-version.txt";
	static private final String LAST_UPDATED_FILENAME = "last-updated.dat";

	private Calendar _lastUpdated;
	private ScratchSpaceManager _scratchMgr;
	private Version _currentVersion;
	private File _versionFile;
	private File _lastUpdatedFile;

	public VersionManager(File updateDirectory, ScratchSpaceManager scratchMgr)
	{
		_versionFile = new File(updateDirectory, VERSION_FILENAME);
		_lastUpdatedFile = new File(updateDirectory, LAST_UPDATED_FILENAME);
		_scratchMgr = scratchMgr;
		_currentVersion = null;
		_lastUpdated = null;
	}

	public Calendar getLastUpdated() throws IOException
	{
		if (_lastUpdated == null) {
			if (!_lastUpdatedFile.exists()) {
				_lastUpdated = Calendar.getInstance();
				_lastUpdated.setTimeInMillis(0L);
				setLastUpdated(_lastUpdated);
			} else {
				_lastUpdated = IOUtils.deserialize(Calendar.class, _lastUpdatedFile);
			}
		}

		return _lastUpdated;
	}

	public void setLastUpdated(Calendar lastUpdated) throws IOException
	{
		File tmpFile = _scratchMgr.createTemporaryFile();

		IOUtils.serialize(tmpFile, lastUpdated);
		_scratchMgr.move(tmpFile, _lastUpdatedFile);
		_lastUpdated = lastUpdated;
	}

	public Version getCurrentVersion() throws IOException
	{
		if (_currentVersion == null) {
			if (!_versionFile.exists())
				_currentVersion = Version.EMPTY_VERSION;
			else {
				FileReader reader = null;
				BufferedReader lineReader = new BufferedReader(reader = new FileReader(_versionFile));
				try {
					String line;
					if ((line = lineReader.readLine()) == null)
						throw new IOException("Corrupt version file:  Unexpected EOF.");
					_currentVersion = new Version(line.trim());
				} finally {
					lineReader.close();
					IOUtils.close(reader);
				}
			}
		}

		return _currentVersion;
	}

	public void setCurrentVersion(Version currentVersion) throws IOException
	{
		File tmpFile = _scratchMgr.createTemporaryFile();
		PrintStream ps = null;

		try {
			ps = new PrintStream(tmpFile);
			ps.println(currentVersion.toString());
			ps.close();
			ps = null;

			_scratchMgr.move(tmpFile, _versionFile);
			_currentVersion = currentVersion;
		} finally {
			IOUtils.close(ps);
		}
	}
}