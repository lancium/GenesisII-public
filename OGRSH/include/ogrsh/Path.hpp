#ifndef __PATH_HPP__
#define __PATH_HPP__

#include <string>
#include <vector>
#include <list>

namespace ogrsh
{
	class Path
	{
		private:
			unsigned int *_references;
			std::vector<std::string> *_fullPathComponents;

			unsigned int _partialStart;
			mutable std::string _partialPath;

			Path(const std::list<std::string> &pathComponents);
			Path(const Path &other, unsigned int partialStart);

		public:
			Path(const Path&);
			~Path();

			Path& operator= (const Path&);

			unsigned int length() const;

			const std::string& operator[] (unsigned int index) const;
			operator const std::string&() const;

			const Path fullPath() const;
			const Path subPath(unsigned int startComponent) const;
			const Path dirname() const;
			const std::string basename() const;

			Path lookup(const std::string &newPath) const;
			static const Path& getCurrentWorkingDirectory();
			static void setCurrentWorkingDirectory(const Path&);
			static void setCurrentWorkingDirectory(const std::string&);
	};
}

#endif
