#ifndef __IOEXCEPTION_HPP__
#define __IOEXCEPTION_HPP__

#include <exception>
#include <string>

namespace jcomm
{
	class IOException : public std::exception
	{
		private:
			std::string _what;
			
		public:
			IOException(const std::string &message);
			virtual ~IOException() throw ();

			virtual const char* what() const throw();
	};
}

#endif
