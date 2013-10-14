package edu.virginia.vcgr.genii.client.context;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.client.ser.DBSerializer;

public class SerializedContext implements Serializable
{
	static private Log _logger = LogFactory.getLog(SerializedContext.class);

	static final long serialVersionUID = 0L;

	private byte[] _data;
	private HashMap<String, Serializable> _transientProperties;

	public SerializedContext(byte[] data, HashMap<String, Serializable> transientProperties)
	{
		_data = data;
		_transientProperties = transientProperties;

		StringBuilder builder = new StringBuilder();
		builder.append("SerializedContext created with raw data of size " + data.length
			+ " bytes and the following transients.\n");
		for (String key : transientProperties.keySet()) {
			try {
				byte[] sData = DBSerializer.serialize(transientProperties.get(key), Long.MAX_VALUE);
				if (sData != null)
					builder.append("\t" + key + ": " + sData.length + "\n");
				else
					builder.append("\t" + key + ": <null>\n");
			} catch (Throwable cause) {
				_logger.error("Error...", cause);
			}
		}

		if (_logger.isDebugEnabled())
			_logger.debug(builder);
	}

	public SerializedContext()
	{
		_data = null;
	}

	public Object readResolve() throws ObjectStreamException
	{
		ByteArrayInputStream bais = null;

		try {
			bais = new ByteArrayInputStream(_data);
			/*
			 * CallingContextImpl context = (CallingContextImpl)ContextStreamUtils.load( new
			 * InflaterInputStream(bais));
			 */
			CallingContextImpl context = (CallingContextImpl) ContextStreamUtils.load(bais);
			context.setTransientProperties(_transientProperties);
			return context;
		} catch (IOException ioe) {
			throw new NotSerializableException("SerializedContext");
		} finally {
			StreamUtils.close(bais);
		}
	}
}