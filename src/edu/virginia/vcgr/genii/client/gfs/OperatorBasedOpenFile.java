package edu.virginia.vcgr.genii.client.gfs;

import java.io.IOException;
import java.nio.ByteBuffer;

import edu.virginia.vcgr.fsii.exceptions.FSException;
import edu.virginia.vcgr.genii.client.byteio.buffer.BasicFileOperator;

class OperatorBasedOpenFile extends GeniiOpenFile
{
	private BasicFileOperator _operator;

	protected OperatorBasedOpenFile(String[] path, BasicFileOperator operator, boolean canRead, boolean canWrite,
		boolean isAppend)
	{
		super(path, canRead, canWrite, isAppend);

		_operator = operator;
	}

	@Override
	protected void appendImpl(ByteBuffer source) throws FSException
	{
		try {
			synchronized (_operator) {
				_operator.append(source);
			}
		} catch (Throwable cause) {
			throw FSExceptions.translate("Unable to append to file.", cause);
		}
	}

	@Override
	protected void flush() throws FSException
	{
		try {
			synchronized (_operator) {
				_operator.flush();
			}
		} catch (Throwable cause) {
			throw FSExceptions.translate("Unable to flush file.", cause);
		}
	}

	@Override
	protected void closeImpl() throws IOException
	{
		synchronized (_operator) {
			_operator.flush();
			_operator.close();
		}
	}

	@Override
	protected void readImpl(long offset, ByteBuffer target) throws FSException
	{
		try {
			while (target.hasRemaining()) {
				int start = target.position();
				synchronized (_operator) {
					_operator.read(offset, target);
				}
				int read = target.position() - start;
				if (read <= 0)
					return;
				offset += read;
			}
		} catch (Throwable cause) {
			throw FSExceptions.translate("Unable to read from file.", cause);
		}
	}

	@Override
	protected void writeImpl(long offset, ByteBuffer source) throws FSException
	{
		try {
			synchronized (_operator) {
				_operator.write(offset, source);
			}
		} catch (Throwable cause) {
			throw FSExceptions.translate("Unable to write to file.", cause);
		}
	}
}