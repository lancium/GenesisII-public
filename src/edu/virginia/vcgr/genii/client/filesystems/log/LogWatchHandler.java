package edu.virginia.vcgr.genii.client.filesystems.log;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Element;

import edu.virginia.vcgr.genii.client.filesystems.Filesystem;
import edu.virginia.vcgr.genii.client.filesystems.FilesystemManager;
import edu.virginia.vcgr.genii.client.filesystems.FilesystemUsageInformation;
import edu.virginia.vcgr.genii.client.filesystems.FilesystemWatchHandler;

public class LogWatchHandler implements FilesystemWatchHandler
{
	static private Log _logger = LogFactory.getLog(LogWatchHandler.class);
	
	private LogWatchConfiguration _config;
	
	public LogWatchHandler(Element element) throws JAXBException
	{
		JAXBContext context = JAXBContext.newInstance(
			LogWatchConfiguration.class);
		Unmarshaller u = context.createUnmarshaller();
		_config = u.unmarshal(element, LogWatchConfiguration.class).getValue();	
	}
	
	@Override
	public void notifyFilesystemEvent(FilesystemManager manager,
			String filesystemName, Filesystem filesystem,
			FilesystemUsageInformation usageInformation,
			boolean matched)
	{
		_logger.warn(_config.format(filesystemName,
			usageInformation.percentUsed()));
	}
}