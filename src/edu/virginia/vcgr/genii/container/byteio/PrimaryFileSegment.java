package edu.virginia.vcgr.genii.container.byteio;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.rmi.RemoteException;
import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.client.byteio.transfer.RandomByteIOTransferer;

public class PrimaryFileSegment
{
	static private Log _logger = LogFactory.getLog(PrimaryFileSegment.class);

	public long _firstBlock;
	public int _blockCount;
	public byte[] _data;

	public PrimaryFileSegment(long firstBlock, int blockCount)
	{
		if (_logger.isDebugEnabled())
			_logger.debug("segment firstBlock=" + firstBlock + " blockCount=" + blockCount);
		_firstBlock = firstBlock;
		_blockCount = blockCount;
	}

	public void download(RandomByteIOTransferer transferer, int blockSize) throws RemoteException
	{
		if (_logger.isDebugEnabled())
			_logger.debug("download firstBlock=" + _firstBlock + " blockCount=" + _blockCount);
		_data = null;
		long firstByte = _firstBlock * blockSize;
		int byteCount = _blockCount * blockSize;
		while (byteCount > 0) {
			byte[] tdata = transferer.read(firstByte, byteCount, 1, 0);
			if ((tdata == null) || (tdata.length == 0))
				break;
			if (_data == null)
				_data = tdata;
			else {
				int length = _data.length;
				_data = Arrays.copyOf(_data, length + tdata.length);
				System.arraycopy(tdata, 0, _data, length, tdata.length);
				if (_logger.isDebugEnabled())
					_logger.debug("download merged length=" + _data.length);
			}
			byteCount -= tdata.length;
			firstByte += tdata.length;
		}
	}

	public void write(BitmapFile bitmapFile, RandomAccessFile raf, int blockSize) throws IOException
	{
		if (_logger.isDebugEnabled())
			_logger.debug("write firstBlock=" + _firstBlock + " blockCount=" + _blockCount);
		bitmapFile.seekBit(_firstBlock);
		int startOfWrite = 0;
		while (startOfWrite < _blockCount) {
			int isValid = bitmapFile.readBit();
			if (isValid == 1) {
				if (_logger.isDebugEnabled())
					_logger.debug("write skip " + (_firstBlock + startOfWrite));
				startOfWrite++;
				continue;
			}
			// Find the next valid block
			int blocksToWrite = 1;
			while (startOfWrite + blocksToWrite < _blockCount) {
				isValid = bitmapFile.readBit();
				if (isValid == 1)
					break;
				blocksToWrite++;
			}
			long seekBlock = _firstBlock + startOfWrite;
			raf.seek(seekBlock * blockSize);
			int startByte = startOfWrite * blockSize;
			int bytesToWrite = blocksToWrite * blockSize;
			int bytesAvailable = _data.length - startByte;
			if (bytesToWrite > bytesAvailable)
				bytesToWrite = bytesAvailable;
			if (bytesToWrite > 0)
				raf.write(_data, startByte, bytesToWrite);
			bitmapFile.seekBit(seekBlock);
			for (int bnum = 0; bnum < blocksToWrite; bnum++)
				bitmapFile.writeBit(1);
			startOfWrite += blocksToWrite + 1;
		}
	}
}
