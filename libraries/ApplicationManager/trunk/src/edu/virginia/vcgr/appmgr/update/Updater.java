package edu.virginia.vcgr.appmgr.update;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URL;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.SAXException;

import edu.virginia.vcgr.appmgr.io.CommentSkipperReader;
import edu.virginia.vcgr.appmgr.io.IOUtils;
import edu.virginia.vcgr.appmgr.io.ScratchSpaceManager;
import edu.virginia.vcgr.appmgr.launcher.ApplicationDescription;
import edu.virginia.vcgr.appmgr.net.URLDownloader;
import edu.virginia.vcgr.appmgr.patch.Patcher;
import edu.virginia.vcgr.appmgr.version.Version;
import edu.virginia.vcgr.appmgr.version.VersionManager;

public class Updater
{
	static private Log _logger = LogFactory.getLog(Updater.class);

	static private void addEntry(Map<Version, URL> map, URL baseURL, String line) throws IOException
	{
		int index = line.indexOf('=');
		if (index < 0)
			throw new IOException(String.format("Unable to parse download description line \"%s\".", line));

		map.put(new Version(line.substring(0, index)), new URL(baseURL, line.substring(index + 1)));
	}

	static private List<Version> findNeededVersions(Set<Version> knownVersions, Version currentVersion)
	{
		List<Version> ret = new Vector<Version>();
		for (Version v : knownVersions) {
			if (currentVersion.compareTo(v) < 0)
				ret.add(v);
		}

		return ret;
	}

	static private Map<Version, URL> readVersions(URL baseURL, InputStream in) throws IOException
	{
		Map<Version, URL> map = new HashMap<Version, URL>();

		CommentSkipperReader reader = new CommentSkipperReader(new InputStreamReader(in));
		String line;

		while ((line = reader.readLine()) != null) {
			line = line.trim();
			if (line.length() == 0)
				continue;

			addEntry(map, baseURL, line);
		}
		reader.close();

		return map;
	}

	private VersionManager _versionManager;
	private ScratchSpaceManager _scratchManager;
	private ApplicationDescription _applicationDescription;

	private void doUpdate(PrintStream log, Version v, URL updateURL) throws IOException, SAXException,
		ParserConfigurationException
	{
		File downloadFile = _scratchManager.createTemporaryFile();
		URLDownloader.download(updateURL, downloadFile);
		Patcher.patch(log, _applicationDescription, downloadFile);
		_versionManager.setCurrentVersion(v);
		downloadFile.delete();
	}

	public Updater(ApplicationDescription applicationDescription)
	{
		_applicationDescription = applicationDescription;
		_scratchManager = _applicationDescription.getScratchSpaceManager();
		_versionManager = _applicationDescription.getVersionManager();
	}

	public void doUpdates(PrintStream log) throws IOException
	{
		InputStream in;
		Version currentVersion = _versionManager.getCurrentVersion();
		for (URL url : _applicationDescription.getApplicationDownloadURLs()) {
			in = null;

			try {
				in = URLDownloader.connect(url);
				Map<Version, URL> knownVersions = readVersions(url, in);
				List<Version> neededVersions = findNeededVersions(knownVersions.keySet(), currentVersion);
				Collections.sort(neededVersions);
				for (Version v : neededVersions) {
					log.format("Updating to version %s.\n", v);
					log.flush();
					doUpdate(log, v, knownVersions.get(v));
				}

				_versionManager.setLastUpdated(Calendar.getInstance());
				return;
			} catch (Throwable cause) {
				_logger.error(cause.getLocalizedMessage());
				_logger.error("Patch failed.");
			} finally {
				IOUtils.close(in);
			}
		}
	}
}