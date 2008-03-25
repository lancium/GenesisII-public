#include "jcomm/Invocation.hpp"
#include "jcomm/Socket.hpp"
#include "jcomm/SessionClient.hpp"
#include "jcomm/DefaultOGRSHWriteBuffer.hpp"
#include "jcomm/DefaultOGRSHReadBuffer.hpp"

namespace jcomm
{
	SessionClient::SessionClient(Socket socket)
		: _socket(socket)
	{
	}

	std::string SessionClient::connectSession(const char *sessionID)
		throw (OGRSHException, IOException)
	{
		Invocation inv(_socket, "setupConnection");

		if (sessionID == NULL)
			inv.addPackable(NULL);
		else
			inv.addString(sessionID);

		std::string result;
		IOGRSHReadBuffer *buffer = inv.invoke();
		buffer->readString(result);
		delete buffer;
		return result;
	}

	int SessionClient::connectNet(const std::string &url, int isStoredContext)
		throw (OGRSHException, IOException)
	{
		Invocation inv(_socket, "connectNet");
		inv.addString(url);
		inv.addInt(isStoredContext);

		int result;
		IOGRSHReadBuffer *buffer = inv.invoke();
		buffer->readInt(result);
		delete buffer;
		return result;
	}

	int SessionClient::loginSession(const char *credentialFile,
		const std::string &password, const std::string &credentialPattern)
			 throw (OGRSHException, IOException)
	{
		Invocation inv(_socket, "loginSession");
		inv.addString(credentialFile);
		inv.addString(password);
		inv.addString(credentialPattern);

		int result;
		IOGRSHReadBuffer *buffer = inv.invoke();
		buffer->readInt(result);
		delete buffer;
		return result;
	}
}
