package edu.virginia.vcgr.smb.server;

import java.io.UnsupportedEncodingException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

/**
 * An SMBBuffer attempts to simplify working with the SMB protocol by keeping track of some important information. For instance, this buffer
 * keeps track of alignment, so that unicode strings are properly aligned.
 */
public class SMBBuffer
{
	private ByteBuffer netBIOSBuffer;
	private ByteBuffer buffer;
	// Offset from start of header
	private int offset;

	public static SMBBuffer EMPTY = new SMBBuffer(0, false);

	private SMBBuffer(ByteBuffer init, int offset)
	{
		this.buffer = init;
		this.offset = offset;

		// future: should this be here
		this.buffer.order(ByteOrder.LITTLE_ENDIAN);
	}

	private SMBBuffer(int size, boolean isPacket)
	{
		if (isPacket) {
			this.netBIOSBuffer = ByteBuffer.allocate(4 + size);

			this.netBIOSBuffer.position(4);
			this.buffer = this.netBIOSBuffer.slice();

			this.netBIOSBuffer.position(0);
			this.buffer.position(SMBHeader.size());
		} else {
			this.buffer = ByteBuffer.allocate(size);
		}

		this.offset = 0;
		this.buffer.order(ByteOrder.LITTLE_ENDIAN);
	}

	public static SMBBuffer allocatePacket(int maxBufferSize)
	{
		return new SMBBuffer(maxBufferSize, true);
	}

	public static SMBBuffer allocateBuffer(int size)
	{
		return new SMBBuffer(size, false);
	}

	public static SMBBuffer wrap(byte[] packet)
	{
		return new SMBBuffer(ByteBuffer.wrap(packet), 0);
	}

	public static SMBBuffer wrap(ByteBuffer packet)
	{
		return new SMBBuffer(packet, 0);
	}

	public final int capacity()
	{
		return buffer.capacity();
	}

	public final int position()
	{
		return buffer.position();
	}

	public final SMBBuffer position(int newPosition)
	{
		buffer.position(newPosition);
		return this;
	}

	public final int limit()
	{
		return buffer.limit();
	}

	public final SMBBuffer limit(int newLimit)
	{
		buffer.limit(newLimit);
		return this;
	}

	public final Buffer clear()
	{
		return buffer.clear();
	}

	public final SMBBuffer flip()
	{
		buffer.flip();
		return this;
	}

	public final SMBBuffer rewind()
	{
		buffer.rewind();
		return this;
	}

	public final int remaining()
	{
		return buffer.remaining();
	}

	public SMBBuffer slice()
	{
		return new SMBBuffer(buffer.slice(), offset + buffer.position());
	}

	public SMBBuffer duplicate()
	{
		return new SMBBuffer(buffer.duplicate(), offset);
	}

	public SMBBuffer put(byte b)
	{
		buffer.put(b);
		return this;
	}

	public SMBBuffer put(int loc, byte val)
	{
		this.buffer.put(loc, val);
		return this;
	}

	public SMBBuffer get(byte[] dst, int offset, int length)
	{
		buffer.get(dst, offset, length);
		return this;
	}

	public SMBBuffer get(byte[] dst)
	{
		buffer.get(dst);
		return this;
	}

	public SMBBuffer put(ByteBuffer src)
	{
		buffer.put(src);
		return this;
	}

	public SMBBuffer put(byte[] src, int offset, int length)
	{
		buffer.put(src, offset, length);
		return this;
	}

	public SMBBuffer put(byte[] src)
	{
		buffer.put(src);
		return this;
	}

	public SMBBuffer putChar(char value)
	{
		buffer.putChar(value);
		return this;
	}

	public SMBBuffer putShort(short value)
	{
		buffer.putShort(value);
		return this;
	}

	public SMBBuffer putShort(int index, short value)
	{
		this.buffer.putShort(index, value);
		return this;
	}

	public SMBBuffer putInt(int value)
	{
		buffer.putInt(value);
		return this;
	}

	public SMBBuffer putInt(int index, int value)
	{
		this.buffer.putInt(index, value);
		return this;
	}

	public SMBBuffer putLong(long value)
	{
		buffer.putLong(value);
		return this;
	}

	public char getChar()
	{
		return buffer.getChar();
	}

	public byte get()
	{
		return buffer.get();
	}

	public short getU()
	{
		return (short) (buffer.get() & 0xff);
	}

	public short getShort()
	{
		return buffer.getShort();
	}

	public int getUShort()
	{
		return buffer.getShort() & 0xffff;
	}

	public int getInt()
	{
		return buffer.getInt();
	}

	public long getUInt()
	{
		return buffer.getInt() & 0xffffffff;
	}

	public long getLong()
	{
		return buffer.getLong();
	}

	// New functions below

	public int getOffset()
	{
		return offset + this.buffer.position();
	}

	public int skip(int bytes)
	{
		int pos = this.buffer.position();
		this.buffer.position(pos + bytes);
		return pos;
	}

	public void align(int multiple)
	{
		int overrun = (getOffset() + multiple - 1) % multiple;
		skip(multiple - 1 - overrun);
	}

	public SMBBuffer slice(int index)
	{
		int cur = buffer.position();
		buffer.position(index);
		ByteBuffer ret = buffer.slice();
		buffer.position(cur);
		return new SMBBuffer(ret, offset + index);
	}

	/**
	 * Reads the buffer of the given length, and then increments the position.
	 * 
	 * @param len
	 * @return
	 */
	public SMBBuffer getBuffer(int len)
	{
		ByteBuffer ret = buffer.slice();
		ret.limit(len);

		int pos = buffer.position();
		buffer.position(pos + len);

		return new SMBBuffer(ret, offset + pos);
	}

	/**
	 * Reads the buffer of the given length at the given position.
	 * 
	 * @param off
	 * @param len
	 * @return
	 */
	public SMBBuffer getBuffer(int index, int len)
	{
		return slice(index).limit(len);
	}

	public void putString(String s, boolean unicode) throws SMBException
	{
		if (unicode) {
			align(2);
			
			try {
			
			//hmmm: is this really the right translation process here?
			//original version:
			for (int i = 0; i < s.length(); i++) {
				this.buffer.putChar(s.charAt(i));
			}
			
			//hmmm: this seems to produce chinese characters!?
				// newer version:
//				byte[] utf16 = s.getBytes("UTF-16");
//				buffer.put(utf16);

			// always add a zero char at end.
				this.buffer.putChar('\0');
				
			} catch (Throwable e) {
				throw new SMBException(NTStatus.NOT_IMPLEMENTED);
			}

		} else {
			try {
				// hmmm: we are trying something different here; always respond with unicode, since windows should be happy with that.
				// ... no, it hates it.				
//				align(2);				
//				byte[] utf16 = s.getBytes("UTF-16");
//				buffer.put(utf16);				
//				// it still really wants a zero at the end?
//				this.buffer.putChar('\0');


				// old approach with simple ascii.
				byte[] ascii = s.getBytes("US-ASCII");
				buffer.put(ascii);
				buffer.put((byte) 0);// NUL terminator


				
			} catch (UnsupportedEncodingException e) {
				throw new SMBException(NTStatus.NOT_IMPLEMENTED);
			}
		}
	}

	public int strlen(String s, boolean unicode) throws SMBException
	{
		if (unicode) {
			// Don't include null terminator; may be incorrect
			return s.length() * 2;
		} else {
			try {
				byte[] ascii = s.getBytes("US-ASCII");
				return ascii.length;
			} catch (UnsupportedEncodingException e) {
				throw new SMBException(NTStatus.NOT_IMPLEMENTED);
			}
		}
	}

	public void putDataBuffer(ByteBuffer block)
	{
		buffer.put((byte) 1);
		buffer.putShort((short) block.remaining());
		buffer.put(block);
	}

	public void putOEMString(String s) throws SMBException
	{
		buffer.put((byte) 2);
		putString(s, false);
	}

	public void putPathname(String s, boolean unicode) throws SMBException
	{
		buffer.put((byte) 3);
		putString(s, unicode);
	}

	public void putSMBString(String s, boolean unicode) throws SMBException
	{
		buffer.put((byte) 4);
		putString(s, unicode);
	}

	public void putVarBlock(ByteBuffer block)
	{
		buffer.put((byte) 5);
		buffer.putShort((short) block.remaining());
		buffer.put(block);
	}

	public void putResponse(SMBBuffer params, SMBBuffer data)
	{
		buffer.put((byte) (params.remaining() >> 1));
		buffer.put(params.buffer);
		buffer.putShort((short) data.remaining());
		buffer.put(data.buffer);
	}

	private SMBBuffer getBlock()
	{
		int length = buffer.getShort() & 0xffff;
		return getBuffer(length);
	}

	public String getString(boolean unicode) throws SMBException
	{
		if (unicode) {
			align(2);

			int length = 0;
			buffer.mark();
			while (true) {
				if (buffer.getChar() == '\0')
					break;

				length++;
			}

			buffer.reset();
			char[] data = new char[length];
			for (int i = 0; i < length; i++)
				data[i] = buffer.getChar();

			buffer.getChar();

			return new String(data);
		} else {
			int length = 0;
			buffer.mark();
			while (true) {
				if (buffer.get() == 0)
					break;

				length++;
			}

			buffer.reset();
			byte[] data = new byte[length];
			buffer.get(data);
			buffer.get();

			try {
				return new String(data, "US-ASCII");
			} catch (UnsupportedEncodingException e) {
				throw new SMBException(NTStatus.NOT_IMPLEMENTED);
			}
		}
	}

	public SMBBuffer getDataBuffer() throws SMBException
	{
		byte type = buffer.get();
		if (type != 1)
			throw new SMBException(NTStatus.INVALID_SMB);
		return getBlock();
	}

	public String getOEMString() throws SMBException
	{
		byte type = buffer.get();
		if (type != 2)
			throw new SMBException(NTStatus.INVALID_SMB);
		return getString(false);
	}

	public String getPathname() throws SMBException
	{
		byte type = buffer.get();
		if (type != 3)
			throw new SMBException(NTStatus.INVALID_SMB);
		return getString(false);
	}

	public String getSMBString(boolean unicode) throws SMBException
	{
		byte type = buffer.get();
		if (type != 4)
			throw new SMBException(NTStatus.INVALID_SMB);
		return getString(unicode);
	}

	public SMBBuffer getVarBlock() throws SMBException
	{
		byte type = buffer.get();
		if (type != 5)
			throw new SMBException(NTStatus.INVALID_SMB);
		return getBlock();
	}

	public String getSMBGEA() throws SMBException
	{
		int length = getU();

		byte[] data = new byte[length + 1];
		this.buffer.get(data);

		try {
			return new String(data, 0, length, "US-ASCII");
		} catch (UnsupportedEncodingException e) {
			throw new SMBException(NTStatus.NOT_IMPLEMENTED);
		}
	}

	public List<String> getSMBGEAList() throws SMBException
	{
		int size = buffer.getInt();
		SMBBuffer parse = getBuffer(size - 4);

		List<String> xattrs = new ArrayList<String>();
		while (parse.remaining() > 0) {
			xattrs.add(parse.getSMBGEA());
		}

		return xattrs;
	}

	public SMBBuffer put(SMBBuffer input)
	{
		this.buffer.put(input.buffer);
		return this;
	}

	public SMBBuffer put(int off, SMBBuffer input)
	{
		ByteBuffer tmp = buffer.duplicate();
		tmp.position(off);
		tmp.put(input.buffer);
		return this;
	}

	public void putHeader(SMBHeader h)
	{
		h.encode(slice(0));
	}

	// future: move this stuff out of here

	int paramBlockStart = -1;

	public void startParameterBlock() throws SMBException
	{
		if (paramBlockStart != -1)
			throw new SMBException(NTStatus.INTERNAL_ERROR);

		this.paramBlockStart = this.buffer.position();
		this.buffer.position(this.paramBlockStart + 1);
	}

	public void finishParameterBlock() throws SMBException
	{
		if (paramBlockStart == -1)
			throw new SMBException(NTStatus.INTERNAL_ERROR);

		int diff = this.buffer.position() - (this.paramBlockStart + 1);
		this.buffer.put(this.paramBlockStart, (byte) (diff >> 1));

		this.paramBlockStart = -1;
	}

	int dataBlockStart = -1;

	public void startDataBlock() throws SMBException
	{
		if (dataBlockStart != -1)
			throw new SMBException(NTStatus.INTERNAL_ERROR);

		this.dataBlockStart = this.buffer.position();
		this.buffer.position(this.dataBlockStart + 2);
	}

	public void finishDataBlock() throws SMBException
	{
		if (dataBlockStart == -1)
			throw new SMBException(NTStatus.INTERNAL_ERROR);

		int diff = this.buffer.position() - (this.dataBlockStart + 2);
		this.buffer.putShort(this.dataBlockStart, (short) diff);

		this.dataBlockStart = -1;
	}

	public void emptyParamBlock()
	{
		this.buffer.put((byte) 0);
	}

	public void emptyDataBlock()
	{
		this.buffer.putShort((short) 0);
	}

	public void resetPacket()
	{
		this.buffer.limit(this.buffer.capacity());
		this.buffer.position(SMBHeader.size());
	}

	public int putMax(SMBBuffer other)
	{
		int space = this.buffer.remaining();
		int place = other.remaining();

		if (space < place) {
			put(other.getBuffer(space));
			return space;
		} else {
			put(other);
			return place;
		}
	}

	/**
	 * Prepares the response by adding the NetBIOS stuff.
	 * 
	 * @return A buffer that can be returned to the SMB client.
	 */
	public ByteBuffer preparePacket()
	{
		int len = this.buffer.remaining();
		this.netBIOSBuffer.putInt(0, len);
		this.netBIOSBuffer.limit(4 + len);

		return this.netBIOSBuffer.slice();
	}

	public ByteBuffer prepareBuffer()
	{
		return this.buffer.slice();
	}

	public int startDataBuffer()
	{
		buffer.put((byte) 1);
		skip(2);
		return buffer.position();
	}

	public void finishDataBuffer(int startBuffer)
	{
		int size = buffer.position() - startBuffer;
		buffer.putShort(startBuffer - 2, (short) size);
	}
}
