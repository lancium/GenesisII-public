#ifndef __FILE_FUNCTIONS_HPP__
#define __FILE_FUNCTIONS_HPP__

#include <sys/types.h>
#include <sys/stat.h>
#include <unistd.h>

#include <string>

#include "ogrsh/FileDescriptor.hpp"
#include "ogrsh/Path.hpp"

namespace ogrsh
{
	class FileFunctions
	{
		private:
			FileFunctions(const FileFunctions&);
			FileFunctions& operator= (const FileFunctions&);

		protected:
			FileFunctions();

		public:
			virtual ~FileFunctions();

			virtual FileDescriptor* open64(const Path &relativePath,
				int flags, mode_t mode) = 0;
			virtual FileDescriptor* creat(const Path &relativePath,
				mode_t mode) = 0;
			virtual int unlink(const Path &relativePath) = 0;
	};
}

#endif
