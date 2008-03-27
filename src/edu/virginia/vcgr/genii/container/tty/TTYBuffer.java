package edu.virginia.vcgr.genii.container.tty;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class TTYBuffer
{
	static private Log _logger = LogFactory.getLog(TTYBuffer.class);
	
	private byte []_buffer;
	private int _position;
	
	public TTYBuffer(int bufferSize)
	{
		_buffer = new byte[bufferSize];
		_position = 0;
	}

	synchronized public void write(byte []data, int offset, int length)
	{
		_logger.debug("Asked to write " + length + " bytes to a TTY.");
		
		if (length > _buffer.length)
		{
			offset += (length - _buffer.length);
			length = _buffer.length;
			_position = 0;
		} else
		{
			int spaceAvailable = _buffer.length - _position;
			if (spaceAvailable < length)
			{
				int shift = length - spaceAvailable;
				System.arraycopy(_buffer, shift, _buffer, 0, _position - shift);
				_position -= shift;
			}
		}
		
		System.arraycopy(data, offset, _buffer, _position, length);
		_position += length;
	}
	
	synchronized public byte[] read(int length)
	{
		if (length > _position)
			length = _position;
		
		byte []ret = new byte[length];
		System.arraycopy(_buffer, 0, ret, 0, length);
		
		
		System.arraycopy(_buffer, length, _buffer, 0, _position - length);
		_position -= length;
		
		_logger.debug("Read " + ret.length + " bytes from a TTY.");
		
		return ret;
	}
}