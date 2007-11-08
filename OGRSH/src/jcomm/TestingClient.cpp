#include "jcomm/Invocation.hpp"
#include "jcomm/Socket.hpp"
#include "jcomm/TestingClient.hpp"
#include "jcomm/DefaultOGRSHWriteBuffer.hpp"
#include "jcomm/DefaultOGRSHReadBuffer.hpp"

namespace jcomm
{
	TestingClient::TestingClient(Socket socket)
		: _socket(socket)
	{
	}

	int TestingClient::OGRSHAdder(int a, int b)
		throw (OGRSHException, IOException)
	{
		Invocation inv(_socket, "OGRSHAdder");
		inv.addInt(a);
		inv.addInt(b);

		int result;
		IOGRSHReadBuffer *buffer = inv.invoke();
		buffer->readInt(result);
		delete buffer;
		return result;
	}
	
	float TestingClient::OGRSHDivide(int a, int b)
		throw (OGRSHException, IOException)
	{
		Invocation inv(_socket, "OGRSHDivide");
		inv.addInt(a);
		inv.addInt(b);
		
		float result;
		IOGRSHReadBuffer *buffer = inv.invoke();
		buffer->readFloat(result);
		delete buffer;
		return result;
	}
}
