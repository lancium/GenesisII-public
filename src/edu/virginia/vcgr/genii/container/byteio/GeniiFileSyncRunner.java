package edu.virginia.vcgr.genii.container.byteio;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.Calendar;

import org.apache.axis.message.MessageElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.io.StreamUtils;
import org.oasis_open.docs.wsrf.rp_2.GetResourcePropertyDocument;
import org.oasis_open.docs.wsrf.rp_2.GetResourcePropertyDocumentResponse;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.byteio.ByteIOStreamFactory;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.TopicPath;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.wellknown.ByteIOTopics;
import edu.virginia.vcgr.genii.common.GeniiCommon;
import edu.virginia.vcgr.genii.container.resource.IResource;
import edu.virginia.vcgr.genii.container.sync.ReplicationThread;
import edu.virginia.vcgr.genii.container.sync.ResourceSyncRunner;

public class GeniiFileSyncRunner implements ResourceSyncRunner
{
	static private Log _logger = LogFactory.getLog(GeniiFileSyncRunner.class);
	static private final int _DEFAULT_BUFFER_SIZE = 1024 * 8;

	public void doSync(IResource vResource,
			EndpointReferenceType primaryEPR,
			EndpointReferenceType myEPR,
			ReplicationThread replicator)
		throws Throwable
	{
		IRByteIOResource resource = (IRByteIOResource) vResource;
		GeniiCommon common = ClientUtils.createProxy(GeniiCommon.class, primaryEPR);
		GetResourcePropertyDocument arg = new GetResourcePropertyDocument();
		GetResourcePropertyDocumentResponse resp = common.getResourcePropertyDocument(arg);
		MessageElement[] valueArr = resp.get_any();
		long fileSize = 0;
		boolean isRecordFile = false;
		for (MessageElement messageElement : valueArr)
		{
			try
			{
				// Attribute names: See ByteIOAttributeHandlers.get*Namespace().
				String name = messageElement.getQName().getLocalPart();
				if (name.equals("CreateTime"))
				{
					resource.setCreateTime((Calendar) messageElement.getObjectValue(Calendar.class));
				}
				else if (name.equals("ModificationTime"))
				{
					resource.setModTime((Calendar) messageElement.getObjectValue(Calendar.class));
				}
				else if (name.equals("Size"))
				{
					fileSize = (Long) messageElement.getObjectValue(Long.class);
				}
			}
			catch (Exception exception)
			{
				// Some properties may be null, unexpected types, etc.
				// Report the exception and continue processing resource properties.
				_logger.debug("GeniiFileSyncRunner: error processing property",  exception);
			}
		}
		if (isRecordFile)
		{
			setBitmapFilename(resource);
		}
		RandomAccessFile raf = null;
		InputStream inputStream = null;
		try
		{
			raf = new RandomAccessFile(resource.getCurrentFile(), "rw");
			if (isRecordFile)
			{
				raf.setLength(fileSize);
			}
			else
			{
				inputStream = ByteIOStreamFactory.createInputStream(primaryEPR);
				raf.setLength(0);
				byte[] data = new byte[_DEFAULT_BUFFER_SIZE];
				int len;
				while ((len = inputStream.read(data)) >= 0)
				{
					raf.write(data, 0, len);
				}
			}
		}
		finally
		{
			StreamUtils.close(inputStream);
			StreamUtils.close(raf);
		}
	}
	
	@Override
	public TopicPath getSyncTopic()
	{
		return ByteIOTopics.BYTEIO_CONTENTS_CHANGED_TOPIC;
	}

	/**
	 * Setup this replica as a record file (a/k/a local cache) as opposed to a document file.
	 */
	private void setBitmapFilename(IRByteIOResource resource)
		throws IOException
	{
		if (resource.getBitmapFilePath() == null)
		{
			String filename = resource.getFilePath();
			int idx = filename.lastIndexOf('.');
			if (idx > 0)
				filename = filename.substring(0, idx);
			filename = filename + ".bmp";
			FileOutputStream ostream = new FileOutputStream(filename);
			ostream.write(0);
			ostream.close();
			resource.setBitmapFilePath(filename);
		}
	}
}
