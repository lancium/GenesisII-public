package edu.virginia.vcgr.genii.client.gfs;

import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Configuration parameters for byte I/O file reading and writing in parallel. Note that all properties here are intended for global use by
 * all byte IO operations. There will be one static instance of this package of 'constants', but it may be loaded from a property file at
 * start-up.
 */
public class ByteIOConfigurationPack
{
	static private Log _logger = LogFactory.getLog(ByteIOConfigurationPack.class);

	// buffer size for standard large reads during sequential access.
	public static int BUF_SIZE_DEFAULT = 1024 * 1024 * 32;
	public int long_buffer_size = BUF_SIZE_DEFAULT;
	public static final String BUF_SIZE_PROPERTY = "gffs.byteio.long_buffer_size";

	// buffer size for smaller reads, once we've decided the client is not using a sequential access pattern.
	public static int SHORT_BUF_SIZE_DEFAULT = 1024 * 1024 * 8;
	public int short_buffer_size = SHORT_BUF_SIZE_DEFAULT;
	public static final String SHORT_BUF_SIZE_PROPERTY = "gffs.byteio.short_buffer_size";

	// max times to retry a failed read.
	public static int READ_RETRIES_DEFAULT = 5;
	public int read_retries = READ_RETRIES_DEFAULT;
	public static final String READ_RETRIES_PROPERTY = "gffs.byteio.read_retries";

	// max times to retry a failed write.
	public static int WRITE_RETRIES_DEFAULT = 5;
	public int write_retries = WRITE_RETRIES_DEFAULT;
	public static final String WRITE_RETRIES_PROPERTY = "gffs.byteio.write_retries";

	// read buffer count MUST be >= 2; this is the max read ahead distance.
	public static int READ_BUFS_DEFAULT = 4;
	public int read_buffers = READ_BUFS_DEFAULT;
	public static final String READ_BUFS_PROPERTY = "gffs.byteio.read_buffers";

	// max writers MUST be >= 1.
	public static int MAX_WRITERS_DEFAULT = 16;
	public int max_writers = MAX_WRITERS_DEFAULT;
	public static final String MAX_WRITERS_PROPERTY = "gffs.byteio.max_writers";

	// max readers MUST be >= 2, and SHOULD be > read buffers.
	public static int MAX_READERS_DEFAULT = 16;
	public int max_readers = MAX_READERS_DEFAULT;
	public static final String MAX_READERS_PROPERTY = "gffs.byteio.max_readers";

	// transferers MUST be >= read buffers.
	public static int TRANSFERERS_DEFAULT = READ_BUFS_DEFAULT + 0;
	public int transferers = TRANSFERERS_DEFAULT;
	public static final String TRANSFERERS_PROPERTY = "gffs.byteio.transferers";

	/**
	 * this constructor sets all values to their defaults.
	 */
	ByteIOConfigurationPack()
	{
	}

	/**
	 * this constructor loads from a properties set, using the property names for our values.
	 */
	ByteIOConfigurationPack(Properties loadFrom)
	{
		try {
			// for each member of this class, load from properties or use the default value.
			long_buffer_size = Integer.parseInt(loadFrom.getProperty(BUF_SIZE_PROPERTY, "" + BUF_SIZE_DEFAULT));
			short_buffer_size = Integer.parseInt(loadFrom.getProperty(SHORT_BUF_SIZE_PROPERTY, "" + SHORT_BUF_SIZE_DEFAULT));
			read_retries = Integer.parseInt(loadFrom.getProperty(READ_RETRIES_PROPERTY, "" + READ_RETRIES_DEFAULT));
			write_retries = Integer.parseInt(loadFrom.getProperty(WRITE_RETRIES_PROPERTY, "" + WRITE_RETRIES_DEFAULT));
			read_buffers = Integer.parseInt(loadFrom.getProperty(READ_BUFS_PROPERTY, "" + READ_BUFS_DEFAULT));
			max_writers = Integer.parseInt(loadFrom.getProperty(MAX_WRITERS_PROPERTY, "" + MAX_WRITERS_DEFAULT));
			max_readers = Integer.parseInt(loadFrom.getProperty(MAX_READERS_PROPERTY, "" + MAX_READERS_DEFAULT));
			transferers = Integer.parseInt(loadFrom.getProperty(TRANSFERERS_PROPERTY, "" + TRANSFERERS_DEFAULT));

			verify();

			if (_logger.isTraceEnabled())
				_logger.debug("loaded byteio configs from properties, got this: " + toString());
		} catch (Throwable t) {
			_logger.error("failed to construct a byte io config pack from properties", t);
		}
	}

	/**
	 * tests the current settings to verify that they are within bounds and make sense with respect to other settings. if they are not within
	 * range, they are forced back into range and a warning message is displayed.
	 */
	public void verify()
	{
		// hmmm: add constant definitions for the out of bounds values here in this method...

		if (long_buffer_size < 1024 * 256) {
			long_buffer_size = 1024 * 256;
			_logger.warn("long buffer size was less than 256 megs; resetting to " + long_buffer_size);
		}
		if (short_buffer_size > long_buffer_size / 2) {
			short_buffer_size = long_buffer_size / 2;
			_logger.warn("short buffer size was greater than or equal to long buffer size; resetting to " + short_buffer_size);
		}
		if (read_retries < 1) {
			read_retries = 1;
			_logger.warn("read retries was less than one; resetting to " + read_retries);
		}
		if (write_retries < 1) {
			write_retries = 1;
			_logger.warn("write retries was less than one; resetting to " + write_retries);
		}
		if (read_buffers < 2) {
			read_buffers = 2;
			_logger.warn("read buffers was less than two; resetting to " + read_buffers);
		}

		if (max_writers < 1) {
			max_writers = 1;
			_logger.warn("max writers was less than one; resetting to " + max_writers);
		}
		if ((max_readers < 2) || (max_readers <= read_buffers)) {
			max_readers = read_buffers + 2;
			_logger.warn("max readers was less than two or less than the number of read buffers; resetting to " + max_readers);
		}
		if (transferers < read_buffers) {
			transferers = read_buffers;
			_logger.warn("transferers was less than read buffers; resetting to " + transferers);
		}
	}

	@Override
	public String toString()
	{
		StringBuilder buf = new StringBuilder();
		buf.append(BUF_SIZE_PROPERTY);
		buf.append("=");
		buf.append(long_buffer_size);
		buf.append(" ");

		buf.append(SHORT_BUF_SIZE_PROPERTY);
		buf.append("=");
		buf.append(short_buffer_size);
		buf.append(" ");

		buf.append(READ_RETRIES_PROPERTY);
		buf.append("=");
		buf.append(read_retries);
		buf.append(" ");

		buf.append(WRITE_RETRIES_PROPERTY);
		buf.append("=");
		buf.append(write_retries);
		buf.append(" ");

		buf.append(READ_BUFS_PROPERTY);
		buf.append("=");
		buf.append(read_buffers);
		buf.append(" ");

		buf.append(MAX_WRITERS_PROPERTY);
		buf.append("=");
		buf.append(max_writers);
		buf.append(" ");

		buf.append(MAX_READERS_PROPERTY);
		buf.append("=");
		buf.append(max_readers);
		buf.append(" ");

		buf.append(TRANSFERERS_PROPERTY);
		buf.append("=");
		buf.append(transferers);
		buf.append(" ");

		return buf.toString();
	}

}
