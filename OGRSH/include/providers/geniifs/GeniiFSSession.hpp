#ifndef __GENIIFS_SESSION_HPP__
#define __GENIIFS_SESSION_HPP__

#include <string>

#include "ogrsh/Session.hpp"

#include "jcomm/Socket.hpp"

namespace ogrsh
{
	namespace geniifs
	{
		class GeniiFSSession : public ogrsh::Session
		{
			private:
				std::string _rootRNSUrl;
				jcomm::Socket *_socket;
				std::string _credFile;
				std::string _credPassword;
				std::string _credPattern;

			protected:
				virtual void startSession();
				virtual void stopSession();

				virtual Mount* createMount(
					const std::string &mountLocation,
					const xercesc_2_8::DOMElement&);

			public:
				GeniiFSSession(const std::string &sessionName,
					const std::string &rootRNSUrl,
					const std::string &credFile,
					const std::string &credPassword,
					const std::string &credPattern);
				virtual ~GeniiFSSession();

				jcomm::Socket *getSocket();
		};

		// INLINES
		inline jcomm::Socket* GeniiFSSession::getSocket()
		{
			return _socket;
		}
	}
}

#endif
