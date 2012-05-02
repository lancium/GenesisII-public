package edu.virginia.vcgr.genii.container.byteio;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Map;
import java.util.TreeMap;

public class BitmapFile implements Closeable
{
	private RandomAccessFile _raf;
	private long _bitNum;
	private Map<Long, Integer> _byteMap;
	
	public BitmapFile(File file, boolean write)
		throws IOException
	{
		String mode = (write ? "rw" : "r");
		_raf = new RandomAccessFile(file, mode);
		_bitNum = 0;
		_byteMap = new TreeMap<Long, Integer>();
	}
	
	public void seekBit(long bitNum)
	{
		_bitNum = bitNum;
	}
	
	public int readBit()
		throws IOException
	{
		long byteNum = _bitNum >> 3;
		int byteValue = 0;
		Integer mapValue = _byteMap.get(byteNum);
		if (mapValue == null)
		{
			// Read from the file the byte that contains the requested bit.
			_raf.seek(byteNum);
			byteValue = _raf.read();
			if (byteValue < 0)
				byteValue = 0;
			_byteMap.put(byteNum, byteValue);
		}
		else
		{
			byteValue = mapValue.intValue();
			if (byteValue < 0)
				byteValue = -byteValue;
		}
		// Get the specific bit from the byte.
		int localBit = (int)(_bitNum & 7);
		int bitValue = byteValue & (1 << localBit);
		// The next call to readBit() or writeBit() accesses the next bit.
		_bitNum++;
		return(bitValue == 0 ? 0 : 1);
	}
	
	public void writeBit(int value)
		throws IOException
	{
		long byteNum = _bitNum >> 3;
		int byteValue = 0;
		Integer mapValue = _byteMap.get(byteNum);
		if (mapValue == null)
		{
			// Read from the file the byte that contains the requested bit.
			_raf.seek(byteNum);
			byteValue = _raf.read();
			if (byteValue < 0)
				byteValue = 0;
		}
		else
		{
			byteValue = mapValue.intValue();
			if (byteValue < 0)
				byteValue = -byteValue;
		}
		// Update the specific bit in the byte.
		int localBit = (int)(_bitNum & 7);
		if (value == 0)
			byteValue = byteValue & (255 - (1 << localBit));
		else
			byteValue = byteValue | (1 << localBit);
		// Set the value in the map to negative, which indicates that it's dirty.
		_byteMap.put(byteNum, -byteValue);
		// The next call to readBit() or writeBit() accesses the next bit.
		_bitNum++;
	}
	
	public void close()
		throws IOException
	{
		// Write dirty values to the file.
		long curSeek = -2;
		for (Map.Entry<Long, Integer> entry : _byteMap.entrySet())
		{
			long byteNum = entry.getKey();
			int byteValue = entry.getValue();
			if (byteValue < 0)
			{
				if (byteNum != curSeek)
				{
					_raf.seek(byteNum);
					curSeek = byteNum;
				}
				_raf.write((byte)(-byteValue));
				curSeek++;
			}
		}
		_raf.close();
	}
}
