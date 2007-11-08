#ifndef __LOCALFS_SESSION_HPP__
#define __LOCALFS_SESSION_HPP__

#include <string>

#include "ogrsh/Session.hpp"

namespace ogrsh
{
	namespace localfs
	{
		class LocalFSSession : public ogrsh::Session
		{
			protected:
				virtual void startSession();
				virtual void stopSession();

				virtual Mount* createMount(
					const std::string &mountLocation,
					const xercesc_2_8::DOMElement&);

			public:
				LocalFSSession(const std::string &sessionName);
		};
	}
}

#endif
