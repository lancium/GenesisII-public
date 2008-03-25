#ifndef __SESSION_CLIENT_HPP__
#define __SESSION_CLIENT_HPP__

#include "jcomm/Socket.hpp"
#include "jcomm/IOException.hpp"
#include "jcomm/OGRSHException.hpp"

namespace jcomm
{
	class SessionClient
	{
		private:
			Socket _socket;

		public:
			SessionClient(Socket socket);

			std::string connectSession(const char *sessionID = NULL)
				throw (OGRSHException, IOException);
			int connectNet(const std::string &url, int isStoredContext)
				throw (OGRSHException, IOException);
			int loginSession(const char *credentialFile,
				const std::string &password,
				const std::string &credentialPattern)
				throw (OGRSHException, IOException);
	};
}

#endif
