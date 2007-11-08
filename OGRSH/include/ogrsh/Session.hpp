#ifndef __SESSION_HPP__
#define __SESSION_HPP__

#include <string>

#include "ogrsh/Mount.hpp"

#include "xercesc/dom/DOMElement.hpp"

namespace ogrsh
{
	class Session
	{
		private:
			bool _sessionStarted;
			std::string _sessionName;

			Session(const Session&);
			Session& operator= (const Session&);

		protected:
			Session(const std::string &sessionName);

			virtual void startSession() = 0;
			virtual void stopSession() = 0;

			virtual Mount* createMount(
				const std::string &mountLocation,
				const xercesc_2_8::DOMElement&) = 0;
		public:
			virtual ~Session();

			const std::string& getSessionName() const;

			void beginSession();
			void endSession();
			bool isStarted() const;

			Mount* mountLocation(
				const std::string &mountLocation,
				const xercesc_2_8::DOMElement&);
	};
}

#endif
