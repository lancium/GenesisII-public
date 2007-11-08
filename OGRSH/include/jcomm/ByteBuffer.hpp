#ifndef __BYTE_BUFFER_HPP__
#define __BYTE_BUFFER_HPP__

namespace jcomm
{
	class ByteBuffer
	{
		friend class Socket;

		private:
			char* _buffer;
			int *_referenceCount;
			int _limit;
			int _position;
			int _capacity;

			int flowException(bool isUnderflow) const;
			void readVerify(int spaceNeeded) const;
			void writeVerify(int spaceNeeded) const;

		public:
			ByteBuffer(int capacity);
			ByteBuffer(const ByteBuffer &);
			ByteBuffer(const char *data, int capacity);

			~ByteBuffer();

			ByteBuffer& operator= (const ByteBuffer&);

			ByteBuffer& compact();
			ByteBuffer& flip();
			ByteBuffer& rewind();

			int position() const;
			int limit() const;
			ByteBuffer duplicate() const;
			int remaining() const;
			const char* asArray() const;

			char get();
			ByteBuffer& get(char *dest, int offset, int length);
			short int getShort();
			int getInt();
			long long getLongLong();
			float getFloat();
			double getDouble();

			void put(char c);
			void put(const char *src, int offset, int length);
			void put(ByteBuffer&);
			void putShort(short s);
			void putInt(int i);
			void putLongLong(long long l);
			void putFloat(float f);
			void putDouble(double d);
	};


	inline void ByteBuffer::readVerify(int needed) const
	{
		(remaining() < needed) ? flowException(true) : 0;
	}

	inline void ByteBuffer::writeVerify(int needed) const
	{
		(remaining() < needed) ? flowException(false) : 0;
	}

	inline ByteBuffer& ByteBuffer::compact()
	{
		memmove(_buffer, _buffer + _position, _limit - _position);
		_position = _limit - _position;

		return *this;
	}

	inline ByteBuffer& ByteBuffer::flip()
	{
		_limit = _position;
		_position = 0;

		return *this;
	}

	inline ByteBuffer& ByteBuffer::rewind()
	{
		_position = 0;

		return *this;
	}

	inline int ByteBuffer::position() const
	{
		return _position;
	}

	inline int ByteBuffer::limit() const
	{
		return _limit;
	}

	inline int ByteBuffer::remaining() const
	{
		return _limit - _position;
	}

	inline ByteBuffer ByteBuffer::duplicate() const
	{
		return ByteBuffer(*this);
	}

	inline const char* ByteBuffer::asArray() const
	{
		return _buffer + _position;
	}
}

#endif
