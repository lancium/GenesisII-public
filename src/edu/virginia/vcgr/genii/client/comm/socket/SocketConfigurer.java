package edu.virginia.vcgr.genii.client.comm.socket;

import java.net.Socket;
import java.net.SocketException;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SocketConfigurer
{
	static private Log _logger = LogFactory.getLog(SocketConfigurer.class);
	
	static final private String PROPERTY_BASE =
		"edu.virginia.vcgr.genii.client.socket.";
	
	static final public String KEEP_ALIVE_PROPERTY =
		PROPERTY_BASE + "keep-alive";
	static final public String PERFORMANCE_PREFERENCES_PROPERTY =
		PROPERTY_BASE + "performance-preferences";
	
	static final public String SEND_BUFFER_SIZE_PROPERTY =
		PROPERTY_BASE + "send-buffer-size";
	static final public String RECEIVE_BUFFER_SIZE_PROPERTY =
		PROPERTY_BASE + "receive-buffer-size";

	static final public String REUSE_ADDRESS_PROPERTY =
		PROPERTY_BASE + "reuse-address";

	static final public String LINGER_PROPERTY =
		PROPERTY_BASE + "so-linger";
	static final public String LINGER_TIME_PROPERTY =
		PROPERTY_BASE + "so-linger.time";

	static final public String TIMEOUT_PROPERTY = 
		PROPERTY_BASE + "timeout";

	static final public String TCP_NO_DELAY_PROPERTY =
		PROPERTY_BASE + "tcp-no-delay";

	static final public String TRAFFIC_CLASS_PROPERTY =
		PROPERTY_BASE + "traffic-class";
	
	private Boolean _keepAlive;
	private IntegerTuple _performancePreferences;
	private Integer _sendBufferSize;
	private Integer _receiveBufferSize;
	private Boolean _reuseAddress;
	private Boolean _linger;
	private Integer _lingerTime;
	private Integer _timeout;
	private Boolean _noDelay;
	private TrafficClass _trafficClass;
	
	static final private Pattern SIZE_PATTERN = Pattern.compile(
		"^\\s*(\\d+)\\s*([kbKB]?)\\s*$");
	static private int parseSize(String value)
	{
		Matcher matcher = SIZE_PATTERN.matcher(value);
		if (!matcher.matches())
			throw new NumberFormatException(String.format(
				"Unable to parse \"%s\" into a size.", value));
		
		int number = Integer.parseInt(matcher.group(1));
		String multiplier = matcher.group(2);
		if (multiplier.equalsIgnoreCase("k"))
			number *= 1024;

		return number;
	}
	
	static private Boolean readBoolean(Properties properties, String property)
	{
		String value = properties.getProperty(property);
		if (value != null)
			return Boolean.valueOf(value);
		
		return null;
	}
	
	static private Integer readInteger(Properties properties, String property)
	{
		String value = properties.getProperty(property);
		if (value != null)
			return Integer.valueOf(value);
		
		return null;
	}
	
	static private IntegerTuple readIntegerTuple(Properties properties,
		String property)
	{
		String value = properties.getProperty(property);
		if (value != null)
			return IntegerTuple.parseIntegerTuple(value);
		
		return null;
	}
	
	static private Integer readSize(Properties properties, String property)
	{
		String value = properties.getProperty(property);
		if (value != null)
			return parseSize(value);
		
		return null;
	}
	
	static private TrafficClass readTrafficClass(Properties properties,
		String property)
	{
		String value = properties.getProperty(property);
		if (value != null)
			return TrafficClass.valueOf(value.trim().toUpperCase());
		
		return null;
	}
	
	public SocketConfigurer(Properties socketProperties)
	{
		_keepAlive = readBoolean(socketProperties, KEEP_ALIVE_PROPERTY);
		_performancePreferences = readIntegerTuple(socketProperties, 
			PERFORMANCE_PREFERENCES_PROPERTY);
		_sendBufferSize = readSize(socketProperties, 
			SEND_BUFFER_SIZE_PROPERTY);
		_receiveBufferSize = readSize(socketProperties, 
			RECEIVE_BUFFER_SIZE_PROPERTY);
		_reuseAddress = readBoolean(socketProperties, REUSE_ADDRESS_PROPERTY);
		_linger = readBoolean(socketProperties, LINGER_PROPERTY);
		_lingerTime = readInteger(socketProperties, LINGER_TIME_PROPERTY);
		_timeout = readInteger(socketProperties, TIMEOUT_PROPERTY);
		_noDelay = readBoolean(socketProperties, TCP_NO_DELAY_PROPERTY);
		_trafficClass = readTrafficClass(socketProperties, 
			TRAFFIC_CLASS_PROPERTY);
	}
	
	final public void configureSocket(Socket socket)
	{
		_logger.debug("Configuring socket.");
		
		try
		{
			if (_keepAlive != null)
			{
				_logger.debug(String.format("Setting SO_KEEPALIVE to %s.", 
					_keepAlive));
				socket.setKeepAlive(_keepAlive);
			}
			
			if (_performancePreferences != null)
			{
				_logger.debug(String.format(
					"Setting performance preferences to (connection=%d, latency=%d, bandwidth=%d)",
					_performancePreferences.first(), _performancePreferences.second(),
					_performancePreferences.third()));
				socket.setPerformancePreferences(
					_performancePreferences.first(), 
					_performancePreferences.second(), 
					_performancePreferences.third());
			}
			
			if (_sendBufferSize != null)
			{
				socket.setSendBufferSize(_sendBufferSize);
				_logger.debug(String.format("Set send buffer size to %d (now it's %d).",
					_sendBufferSize, socket.getSendBufferSize()));
			} else
			{
				_logger.debug(String.format("Left send buffer size at %d.",
					socket.getSendBufferSize()));
			}
			
			if (_receiveBufferSize != null)
			{
				socket.setReceiveBufferSize(_receiveBufferSize);
				_logger.debug(String.format("Set receive buffer size to %d (now it's %d).",
					_receiveBufferSize, socket.getReceiveBufferSize()));
			} else
			{
				_logger.debug(String.format("Left receive buffer size at %d.",
					socket.getReceiveBufferSize()));
			}
			
			if (_reuseAddress != null)
			{
				_logger.debug(String.format(
					"Setting SO_REUSEADDR to %s.", _reuseAddress));
				socket.setReuseAddress(_reuseAddress);
			}
			
			if (_linger != null && _lingerTime != null)
			{
				_logger.debug(String.format(
					"Setting SO_LINGER to %s(%d).", _linger, _lingerTime));
				socket.setSoLinger(_linger, _lingerTime);
			}
			
			if (_timeout != null)
			{
				_logger.debug(String.format(
					"Setting SO_TIMEOUT to %d.", _timeout));
				socket.setSoTimeout(_timeout);
			}
			
			if (_noDelay != null)
			{
				_logger.debug(String.format(
				"Setting TCP_NO_DELAY to %s.", _noDelay));
				socket.setTcpNoDelay(_noDelay);
			}
			
			if (_trafficClass != null)
			{
				_logger.debug(String.format(
					"Setting Traffic Class to %s.", _trafficClass));
				socket.setTrafficClass(_trafficClass.trafficClassBitVector());
			}
		}
		catch (SocketException se)
		{
			_logger.warn("Unable to configure socket properties.", se);
		}
	}
	
	static public void main(String []args)
	{
		System.err.println(TrafficClass.THROUGHPUT);
		System.err.println(TrafficClass.valueOf("throughput".toUpperCase()));
	}
}