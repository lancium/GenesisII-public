package edu.virginia.vcgr.genii.client.filesystems;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FilesystemManager
{
	static private Log _logger = LogFactory.getLog(FilesystemManager.class);
	
	static private class FilesystemWatcherComparator
		implements Comparator<FilesystemWatcher>
	{
		@Override
		final public int compare(FilesystemWatcher o1, FilesystemWatcher o2)
		{
			return o1.nextCheck().compareTo(o2.nextCheck());
		}
	}
	
	static private FilesystemsConfiguration readConfiguration(
		File configurationSource) throws IOException
	{
		try
		{
			JAXBContext context = JAXBContext.newInstance(
				FilesystemsConfiguration.class);
			Unmarshaller u = context.createUnmarshaller();
			return (FilesystemsConfiguration)u.unmarshal(configurationSource);
		}
		catch (JAXBException e)
		{
			throw new IOException(
				"Unable to parse Filesystems configuration file.", e);
		}
	}
	
	static private FilesystemsConfiguration readConfiguration(
		InputStream configurationSource) throws IOException
	{
		try
		{
			JAXBContext context = JAXBContext.newInstance(
				FilesystemsConfiguration.class);
			Unmarshaller u = context.createUnmarshaller();
			return (FilesystemsConfiguration)u.unmarshal(configurationSource);
		}
		catch (JAXBException e)
		{
			throw new IOException(
				"Unable to parse Filesystems configuration file.", e);
		}
	}
	
	private class FilesystemWatchRegistrationImpl 
		implements FilesystemWatchRegistration
	{
		private FilesystemWatchCallback _callback;
		private FilesystemWatcher _watcher;
		
		private FilesystemWatchRegistrationImpl(
			FilesystemWatchCallback callback,
			FilesystemWatcher watcher)
		{
			_callback = callback;
			_watcher = watcher;
		}
		
		@Override
		public void cancel()
		{
			synchronized(_watchers)
			{
				_watchers.remove(_watcher);
				_watcher.cancel();
			}
		}

		@Override
		final public void resetCallCount()
		{
			_callback.resetCallCount();
		}
	}
	
	private Map<String, Filesystem> _filesystems =
		new HashMap<String, Filesystem>(4);
	private PriorityQueue<FilesystemWatcher> _watchers =
		new PriorityQueue<FilesystemWatcher>(8,
			new FilesystemWatcherComparator());
	
	private FilesystemManager(FilesystemsConfiguration conf) 
		throws FileNotFoundException
	{
		for (FilesystemConfiguration fsConf : conf.filesystems())
		{
			Filesystem fs = new FilesystemImpl(this, fsConf.name(),
				fsConf);
			_filesystems.put(fsConf.name(), fs);
		}
		
		for (FilesystemWatcherConfiguration watcherConfig : conf.watchers())
		{
			String filesystemName = watcherConfig.filesystemName();
			Filesystem filesystem = lookup(filesystemName);
			Collection<WatchCallbackConfiguration> callbackConfigs =
				watcherConfig.watchCallback();
			
			if (callbackConfigs == null || callbackConfigs.size() == 0)
				continue;
			
			Collection<FilesystemWatchCallback> callbacks = 
				new ArrayList<FilesystemWatchCallback>(callbackConfigs.size());
			
			try
			{
				for (WatchCallbackConfiguration config : callbackConfigs)
				{
					callbacks.add(new FilesystemWatchCallback(
						config.callLimit(), config.registerAntiCallback(),
						config.handlerClass(),
						config.configurationContent()));
				}
				
				FilesystemWatcher watcher = new FilesystemWatcher(
					watcherConfig.checkPeriod().getMilliseconds(),
					filesystemName, filesystem,
					watcherConfig.filter(), callbacks);
				_watchers.add(watcher);
			}
			catch (Throwable cause)
			{
				_logger.error(String.format(
					"Unable to load filesystem watcher for filesystem %s.",
					watcherConfig.filesystemName()), cause);
				continue;
			}
		}
	}
	
	FilesystemWatchRegistration addWatch(String filesystemName,
		Filesystem filesystem,
		Integer callLimit, 
		long checkPeriod, TimeUnit checkPeriodUnits, 
		FilesystemWatchFilter filter, FilesystemWatchHandler handler)
	{
		Collection<FilesystemWatchCallback> callbacks = 
			new ArrayList<FilesystemWatchCallback>(1);
		FilesystemWatchCallback callback = new FilesystemWatchCallback(
			callLimit, handler);
		callbacks.add(callback);
		
		FilesystemWatcher watcher = new FilesystemWatcher(
			checkPeriodUnits.toMillis(checkPeriod),
			filesystemName, filesystem,
			filter, callbacks);
		synchronized(_watchers)
		{
			_watchers.add(watcher);
			_watchers.notifyAll();
		}
		return new FilesystemWatchRegistrationImpl(callback, watcher);
	}
	
	public FilesystemManager(File configurationSource)
		throws IOException
	{
		this(readConfiguration(configurationSource));
	}
	
	public FilesystemManager(InputStream configurationSource)
		throws IOException
	{
		this(readConfiguration(configurationSource));
	}
	
	public FilesystemManager() throws FileNotFoundException
	{
		this(new FilesystemsConfiguration());
	}
	
	final public Filesystem lookup(String filesystemName)
		throws FileNotFoundException
	{
		Filesystem fs = _filesystems.get(filesystemName);
		if (fs == null)
			throw new FileNotFoundException(String.format(
				"Filesystem %s not found!", filesystemName));
		
		return fs;
	}
	
	final public void enterPollingLoop() throws InterruptedException
	{
		Collection<FilesystemWatcher> toHandle = 
			new LinkedList<FilesystemWatcher>();
		long toSleep;
		Calendar now;
		
		while (true)
		{
			toHandle.clear();
			toSleep = 1000L * 60 * 5;	// By default, sleep for 5 minutes.
			now = Calendar.getInstance();
			
			synchronized(_watchers)
			{
				while (!_watchers.isEmpty())
				{
					FilesystemWatcher watcher = _watchers.peek();
					if (now.before(watcher.nextCheck()))
						break;
					
					toHandle.add(_watchers.poll());
				}
			}
			
			for (FilesystemWatcher watcher : toHandle)
				watcher.performCheck(this);
			
			synchronized(_watchers)
			{
				for (FilesystemWatcher watcher : toHandle)
				{
					if (!watcher.cancelled())
						_watchers.add(watcher);
				}
				
				if (!_watchers.isEmpty())
				{
					toSleep = _watchers.peek().nextCheck().getTimeInMillis() -
						now.getTimeInMillis();
				}
				
				if (toSleep <= 0)
					toSleep = 1;
				
				_watchers.wait(toSleep);
			}
		}
	}
	
	static public void main(String []args) throws Throwable
	{
		InputStream in = FilesystemManager.class.getResourceAsStream("myFilesystems.xml");
		FilesystemManager mgr = new FilesystemManager(in);
		in.close();
		
		mgr.enterPollingLoop();
	}
}