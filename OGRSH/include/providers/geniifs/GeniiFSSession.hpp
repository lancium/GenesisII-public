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
				int _isStoredContext;
				std::string _rootRNSUrl;
				jcomm::Socket *_socket;

			protected:
				virtual void startSession();
				virtual void stopSession();

				virtual Mount* createMount(
					const std::string &mountLocation,
					const xercesc_2_8::DOMElement&);

			public:
				GeniiFSSession(const std::string &sessionName,
					const std::string &rootRNSUrl,
					const int isStoredContext = 0);
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
