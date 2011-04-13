package edu.virginia.vcgr.genii.container.pipe;

import java.nio.ByteBuffer;

final class PipeBuffer
{
	private Object _lockObject = new Object();
	
	private ByteBuffer _buffer;
	
	PipeBuffer(int pipeSize)
	{
		_buffer = ByteBuffer.allocate(pipeSize);
	}
	
	void read(ByteBuffer sink, long timeoutMS) throws InterruptedException
	{
		long startTime = System.currentTimeMillis();
		
		synchronized(_lockObject)
		{
			while (_buffer.position() == 0)
			{
				long sleepTime = 
					timeoutMS - (System.currentTimeMillis() - startTime);
				if (sleepTime <= 0)
					throw new InterruptedException();
				
				_lockObject.wait(sleepTime);
			}
			
			_buffer.flip();
			ByteBuffer tmpSource = _buffer.slice();
			tmpSource.limit(Math.min(tmpSource.limit(), sink.remaining()));
			_buffer.position(tmpSource.limit());
			sink.put(tmpSource);
			_buffer.compact();
			
			_lockObject.notifyAll();
		}
	}
	
	void write(ByteBuffer source, long timeoutMS) throws InterruptedException
	{
		long startTime = System.currentTimeMillis();
		
		synchronized(_lockObject)
		{
			while (!_buffer.hasRemaining())
			{
				long sleepTime =
					timeoutMS - (System.currentTimeMillis() - startTime);
				if (sleepTime <= 0)
					throw new InterruptedException();
				
				_lockObject.wait(sleepTime);
			}
			
			int toCopy = Math.min(source.remaining(), _buffer.remaining());
			ByteBuffer tmpSource = source.slice();
			tmpSource.limit(toCopy);
			source.position(source.position() + toCopy);
			_buffer.put(tmpSource);
			
			_lockObject.notifyAll();
		}
	}
}