/*
 * Copyright 2006 University of Virginia
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package org.morgan.util.updater;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;

import org.morgan.util.Version;
import org.morgan.util.io.GuaranteedDirectory;
import org.morgan.util.io.StreamUtils;

/**
 * @author Mark Morgan (mark@mark-morgan.org)
 */
public class UpdateManager extends UpdateListenerHandler
{
	static private final String _VERSION_FILE = "version-information.ver";
	private File _installationPath;
	private String _downloadBase;
	
	private VersionList _currentVersion = null;
	private VersionList _latestVersion = null;
	
	public UpdateManager(File installationPath, String downloadSite,
		String projectName, String instanceName) throws MalformedURLException
	{
		_installationPath = installationPath;
		_downloadBase = downloadSite + "/" + projectName + "/" + instanceName;
	}
	
	public VersionList getLatestVersionList() throws MalformedURLException, IOException
	{
		URLConnection connection = null;
		InputStream in = null;
		
		if (_latestVersion == null)
		{
			URL url = new URL(_downloadBase + "/" + _VERSION_FILE);

			try
			{
				connection = url.openConnection();
				in = connection.getInputStream();
				_latestVersion = new VersionList(in);
			}
			finally
			{
				StreamUtils.close(in);
			}
		}
		
		return _latestVersion;
	}
	
	public VersionList getCurrentVersionList()
	{
		FileReader freader = null;
		
		if (_currentVersion == null)
		{
			try
			{
				freader = new FileReader(new File(_installationPath, _VERSION_FILE));
				_currentVersion = new VersionList(freader);
			}
			catch (IOException ioe)
			{
				return new VersionList();
			}
			finally
			{
				StreamUtils.close(freader);
			}
		}
		
		return _currentVersion;
	}
	
	public boolean update() throws IOException
	{
		try
		{
			UpdateWorker worker = new UpdateWorker();
			worker.start();
			worker.join();
			if (worker.getException() != null)
				throw worker.getException();
			return worker.getChangesMade();
		}
		catch (InterruptedException ie)
		{
			return true;
		}
	}
	
	private class UpdateWorker extends Thread
	{
		private boolean _changesMade = false;
		static final private int _DEFAULT_BUFFER_SIZE = 1024 * 8;
		private File _tmpDir;
		private IOException _ioe = null;
		
		public UpdateWorker() throws IOException
		{
			_tmpDir = new GuaranteedDirectory(_installationPath, "updates");
		}
		
		public IOException getException()
		{
			return _ioe;
		}
		
		public boolean getChangesMade()
		{
			return _changesMade;
		}
		
		private void downloadFile(String relativeName, File target)
			throws MalformedURLException, IOException
		{
			int bytesRead;
			byte []data = new byte[_DEFAULT_BUFFER_SIZE];
			URL downloadLocation = new URL(_downloadBase + "/" + relativeName);
			InputStream in = null;
			OutputStream out = null;
			
			try
			{
				in = downloadLocation.openConnection().getInputStream();
				target.getParentFile().mkdirs();
				out = new FileOutputStream(target);
				
				while ( (bytesRead = in.read(data)) >= 0)
				{
					out.write(data, 0, bytesRead);
				}
			}
			finally
			{
				StreamUtils.close(in);
				StreamUtils.close(out);
			}
		}
		
		private OutOfDateRecord[] findOutOfDates(VersionList current, VersionList latest)
			throws IOException
		{
			int ci = 0;
			int li = 0;
			
			ArrayList<OutOfDateRecord> records = new ArrayList<OutOfDateRecord>();
			
			String []currentNames = current.getRelativeFiles();
			String []latestNames = latest.getRelativeFiles();
			
			Arrays.sort(currentNames);
			Arrays.sort(latestNames);
			
			while ( (ci < currentNames.length) || (li < latestNames.length) )
			{
				if (ci >= currentNames.length)
				{
					records.add(new OutOfDateRecord(latestNames[li], _tmpDir,
						null, latest.getVersion(latestNames[li])));
					li++;
					continue;
				}
				
				if (li >= latestNames.length)
				{
					File realFile = new File(_installationPath, currentNames[ci]);
					realFile.delete();
					ci++;
					continue;
				}
				
				int cmp = currentNames[ci].compareTo(latestNames[li]);
				if (cmp < 0)
				{
					// current exists, latest doesn't
					File realFile = new File(_installationPath, currentNames[ci]);
					realFile.delete();
					ci++;
					continue;
				}
				
				if (cmp > 0)
				{
					// latest exists, current doesn't
					records.add(new OutOfDateRecord(latestNames[li], _tmpDir,
						null, latest.getVersion(latestNames[li])));
					li++;
					continue;
				}
				
				Version latestV = latest.getVersion(latestNames[li]);
				Version currentV = current.getVersion(currentNames[ci]);
				
				cmp = latestV.compareTo(currentV);
				if (cmp < 0)
					throw new IOException("Latest version is older then newest version.");
				if (cmp > 0)
					records.add(new OutOfDateRecord(latestNames[li], _tmpDir,
						currentV, latestV));
				
				ci++;
				li++;
			}
			
			OutOfDateRecord []ret = new OutOfDateRecord[records.size()];
			records.toArray(ret);
			return ret;
		}
		
		private void commitFile(File source, File target) throws IOException
		{
			if (!source.renameTo(target))
			{
				byte []data = new byte[_DEFAULT_BUFFER_SIZE];
				int bytesRead;
				
				InputStream in = null;
				OutputStream out = null;
				
				target.getParentFile().mkdirs();
				try
				{
					in = new FileInputStream(source);
					out = new FileOutputStream(target);
					
					while ( (bytesRead = in.read(data)) >= 0)
					{
						out.write(data, 0, bytesRead);
					}
				}
				finally
				{
					StreamUtils.close(in);
					StreamUtils.close(out);
				}
			}
		}
		
		public void run()
		{
			try
			{
				VersionList current = getCurrentVersionList();
				VersionList latest = getLatestVersionList();
				OutOfDateRecord []outOfDates = findOutOfDates(current, latest);
				
				if (outOfDates.length == 0)
				{
					fireFinishedUpdate();
					return;
				}
				
				_changesMade = true;
				
				for (OutOfDateRecord record : outOfDates)
				{
					fireStartingFileUpdate(record.getRelativeName(), record.getOldVersion(),
						record.getNewVersion());
					downloadFile(record.getRelativeName(), record.getTmpFile());
					fireFinishedFileUpdate(record.getRelativeName());
				}
				
				fireStartingCommit();
				for (OutOfDateRecord record : outOfDates)
				{
					File realFile = new File(_installationPath, record.getRelativeName());
					commitFile(record.getTmpFile(), realFile);
				}
				OutOfDateRecord record = new OutOfDateRecord(
					_VERSION_FILE, _tmpDir, null, null);
				latest.store(record.getTmpFile());
				File realFile = new File(_installationPath, _VERSION_FILE);
				commitFile(record.getTmpFile(), realFile);
				fireFinishedCommit();
				fireFinishedUpdate();
			}
			catch (IOException ioe)
			{
				_ioe = ioe;
				fireExceptionOccurred(ioe.toString(), ioe);
			}
		}
	}
}
