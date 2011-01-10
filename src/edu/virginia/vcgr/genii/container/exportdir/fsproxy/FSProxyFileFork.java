package edu.virginia.vcgr.genii.container.exportdir.fsproxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Calendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.io.StreamUtils;

import edu.virginia.g3.fsview.FSViewEntry;
import edu.virginia.g3.fsview.FSViewEntryType;
import edu.virginia.g3.fsview.FSViewFileEntry;
import edu.virginia.g3.fsview.FSViewRandomAccessFileEntry;
import edu.virginia.g3.fsview.FSViewSession;
import edu.virginia.g3.fsview.FSViewStreamableAccessFileEntry;
import edu.virginia.vcgr.genii.client.exportdir.FSProxyConstructionParameters;
import edu.virginia.vcgr.genii.client.security.authz.rwx.RWXCategory;
import edu.virginia.vcgr.genii.client.security.authz.rwx.RWXMapping;
import edu.virginia.vcgr.genii.container.resource.IResource;
import edu.virginia.vcgr.genii.container.resource.ResourceManager;
import edu.virginia.vcgr.genii.container.rfork.AbstractRandomByteIOResourceFork;
import edu.virginia.vcgr.genii.container.rfork.RandomByteIOResourceFork;
import edu.virginia.vcgr.genii.container.rfork.ResourceForkService;

public class FSProxyFileFork extends AbstractRandomByteIOResourceFork
	implements RandomByteIOResourceFork
{
	static private Log _logger = LogFactory.getLog(FSProxyFileFork.class);
	
	private FSViewSession session()
		throws IOException
	{
		IResource resource = 
			ResourceManager.getCurrentResource().dereference();
		FSProxyConstructionParameters consParms = 
			(FSProxyConstructionParameters)resource.constructionParameters(
				FSProxyServiceImpl.class);
		
		return consParms.connectionInformation().openSession();
	}

	public FSProxyFileFork(ResourceForkService service, String forkPath)
	{
		super(service, forkPath);
	}

	@Override
	public long size()
	{
		FSViewSession session = null;
		
		try
		{
			session = session();
			FSViewEntry entry = session.lookup(getForkPath());
			if (entry.entryType() != FSViewEntryType.File)
				throw new IOException(String.format(
					"FSViewEntry %s is not a file!", entry));
			FSViewFileEntry fileEntry = ((FSViewFileEntry)entry);
			Long size = fileEntry.size();
			if (size == null)
				size = new Long(0);
			return size;
		}
		catch (Throwable cause)
		{
			_logger.warn("Unable to get FSView size.", cause);
			return 0;
		}
		finally
		{
			StreamUtils.close(session);
		}
	}

	@Override
	public Calendar createTime()
	{
		FSViewSession session = null;
		
		try
		{
			session = session();
			FSViewEntry entry = session.lookup(getForkPath());
			if (entry.entryType() != FSViewEntryType.File)
				throw new IOException(String.format(
					"FSViewEntry %s is not a file!", entry));
			FSViewFileEntry fileEntry = ((FSViewFileEntry)entry);
			return fileEntry.createTime();
		}
		catch (Throwable cause)
		{
			_logger.warn("Unable to get FSView create time.", cause);
			return Calendar.getInstance();
		}
		finally
		{
			StreamUtils.close(session);
		}
	}

	@Override
	public Calendar modificationTime()
	{
		FSViewSession session = null;
		
		try
		{
			session = session();
			FSViewEntry entry = session.lookup(getForkPath());
			if (entry.entryType() != FSViewEntryType.File)
				throw new IOException(String.format(
					"FSViewEntry %s is not a file!", entry));
			FSViewFileEntry fileEntry = ((FSViewFileEntry)entry);
			return fileEntry.lastModified();
		}
		catch (Throwable cause)
		{
			_logger.warn("Unable to get FSView modification time.", cause);
			return Calendar.getInstance();
		}
		finally
		{
			StreamUtils.close(session);
		}
	}

	@Override
	public void modificationTime(Calendar newTime)
	{
	}

	@Override
	public Calendar accessTime()
	{
		FSViewSession session = null;
		
		try
		{
			session = session();
			FSViewEntry entry = session.lookup(getForkPath());
			if (entry.entryType() != FSViewEntryType.File)
				throw new IOException(String.format(
					"FSViewEntry %s is not a file!", entry));
			FSViewFileEntry fileEntry = ((FSViewFileEntry)entry);
			return fileEntry.lastAccessed();
		}
		catch (Throwable cause)
		{
			_logger.warn("Unable to get FSView access time.", cause);
			return Calendar.getInstance();
		}
		finally
		{
			StreamUtils.close(session);
		}
	}

	@Override
	public void accessTime(Calendar newTime)
	{
	}

	@Override
	public boolean readable()
	{
		FSViewSession session = null;
		
		try
		{
			session = session();
			FSViewEntry entry = session.lookup(getForkPath());
			if (entry.entryType() != FSViewEntryType.File)
				throw new IOException(String.format(
					"FSViewEntry %s is not a file!", entry));
			FSViewFileEntry fileEntry = ((FSViewFileEntry)entry);
			return fileEntry.canRead();
		}
		catch (Throwable cause)
		{
			_logger.warn("Unable to get FSView can read.", cause);
			return false;
		}
		finally
		{
			StreamUtils.close(session);
		}
	}

	@Override
	public boolean writable()
	{
		FSViewSession session = null;
		
		try
		{
			session = session();
			FSViewEntry entry = session.lookup(getForkPath());
			if (entry.entryType() != FSViewEntryType.File)
				throw new IOException(String.format(
					"FSViewEntry %s is not a file!", entry));
			FSViewFileEntry fileEntry = ((FSViewFileEntry)entry);
			return fileEntry.canWrite();
		}
		catch (Throwable cause)
		{
			_logger.warn("Unable to get FSView can write.", cause);
			return false;
		}
		finally
		{
			StreamUtils.close(session);
		}
	}

	@Override
	@RWXMapping(RWXCategory.READ)
	public void read(long offset, ByteBuffer dest) throws IOException
	{
		FSViewSession session = null;
		
		try
		{
			session = session();
			FSViewEntry entry = session.lookup(getForkPath());
			if (entry.entryType() != FSViewEntryType.File)
				throw new IOException(String.format(
					"FSViewEntry %s is not a file!", entry));
			FSViewFileEntry fileEntry = ((FSViewFileEntry)entry);
			
			switch (fileEntry.fileType())
			{
				case RandomAccessFile :
					((FSViewRandomAccessFileEntry)fileEntry).read(offset, dest);
					break;
					
				case StreamableAccessFile :
				{
					InputStream in = null;
					try
					{
						in = ((FSViewStreamableAccessFileEntry)fileEntry).openInputStream();
						in.skip(offset);
						byte []data = new byte[dest.remaining()];
						int read = in.read(data);
						if (read > 0)
							dest.put(data, 0, read);
					}
					finally
					{
						StreamUtils.close(in);
					}
				}
			}
		}
		finally
		{
			StreamUtils.close(session);
		}
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	public void write(long offset, ByteBuffer source) throws IOException
	{
		FSViewSession session = null;
		
		try
		{
			session = session();
			FSViewEntry entry = session.lookup(getForkPath());
			if (entry.entryType() != FSViewEntryType.File)
				throw new IOException(String.format(
					"FSViewEntry %s is not a file!", entry));
			FSViewFileEntry fileEntry = ((FSViewFileEntry)entry);
			
			switch (fileEntry.fileType())
			{
				case RandomAccessFile :
					((FSViewRandomAccessFileEntry)fileEntry).write(
						offset, source);
					break;
					
				case StreamableAccessFile :
					throw new IOException("Cannot write to a FSView of this type.");
			}
		}finally
		{
			StreamUtils.close(session);
		}
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	public void truncAppend(long offset, ByteBuffer source) throws IOException
	{
		FSViewSession session = null;
		
		try
		{
			session = session();
			FSViewEntry entry = session.lookup(getForkPath());
			if (entry.entryType() != FSViewEntryType.File)
				throw new IOException(String.format(
					"FSViewEntry %s is not a file!", entry));
			FSViewFileEntry fileEntry = ((FSViewFileEntry)entry);
			
			switch (fileEntry.fileType())
			{
				case RandomAccessFile :
					((FSViewRandomAccessFileEntry)fileEntry).truncate(offset);
					((FSViewRandomAccessFileEntry)fileEntry).append(source);
					break;
					
				case StreamableAccessFile :
					if (offset == 0L)
					{
						OutputStream out = null;
						
						try
						{
							out = ((FSViewStreamableAccessFileEntry)fileEntry).openOutputStream();
							byte []data = new byte[source.remaining()];
							source.get(data);
							out.write(data);
						}
						finally
						{
							StreamUtils.close(out);
						}
					} else
						throw new IOException("Cannot write to a FSView of this type.");
			}
		}
		finally
		{
			StreamUtils.close(session);
		}
	}
}
