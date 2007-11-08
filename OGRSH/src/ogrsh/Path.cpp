#include <string>
#include <list>
#include <unistd.h>
#include <errno.h>

#include "ogrsh/EnvironmentVariables.hpp"
#include "ogrsh/Logging.hpp"
#include "ogrsh/Path.hpp"

namespace ogrsh
{
	static const size_t _DEFAULT_DIRECTORY_LENGTH = 256;
	static Path *_cwd = NULL;

	static std::list<std::string> normalizePath(const std::string &);

	Path::Path(const std::list<std::string> &pathComponents)
	{
		_references = new unsigned int;
		*_references = 1;
		_fullPathComponents = new std::vector<std::string>(
			pathComponents.begin(), pathComponents.end());

		_partialStart = 0;
	}

	Path::Path(const Path &other, unsigned int partialStart)
	{
		if (partialStart < 0)
		{
			OGRSH_FATAL("Partial start for new path must be greater than 0.");
			ogrsh::shims::real_exit(1);
		}

		if (partialStart > other._fullPathComponents->size())
		{
			OGRSH_FATAL("Partial start for new path out of range.");
			ogrsh::shims::real_exit(1);
		}
			
		_references = other._references;
		_fullPathComponents = other._fullPathComponents;

		(*_references)++;

		_partialStart = partialStart;
	}

	Path::Path(const Path &other)
	{
		_references = other._references;
		_fullPathComponents = other._fullPathComponents;
		_partialPath = other._partialPath;

		(*_references)++;

		_partialStart = other._partialStart;
	}

	Path::~Path()
	{
		(*_references)--;

		if (*_references <= 0)
		{
			delete _references;
			delete _fullPathComponents;
		}
	}

	Path& Path::operator= (const Path &other)
	{
		if (this == &other)
			return *this;

		(*_references)--;

		if (*_references <= 0)
		{
			delete _references;
			delete _fullPathComponents;
		}

		_references = other._references;
		_fullPathComponents = other._fullPathComponents;
		_partialPath = other._partialPath;

		(*_references)++;

		_partialStart = other._partialStart;

		return *this;
	}

	unsigned int Path::length() const
	{
		return _fullPathComponents->size() - _partialStart;
	}

	const std::string& Path::operator[] (unsigned int index) const
	{
		if (index < 0 || index >= length())
		{
			OGRSH_FATAL("Index out of range in path.");
			ogrsh::shims::real_exit(1);
		}

		return (*_fullPathComponents)[index + _partialStart];
	}

	Path::operator const std::string&() const
	{
		if (_partialPath.length() == 0)
		{
			unsigned int len = length();
			if (len <= 0)
			{
				_partialPath = "/";
			} else
			{
				for (unsigned int lcv = 0;
					lcv < len; lcv++)
				{
					_partialPath += "/"
						+ (*_fullPathComponents)[_partialStart + lcv];
				}
			}
		}

		return _partialPath;
	}

	const Path Path::fullPath() const
	{
		return Path(*this, 0);
	}

	const Path Path::subPath(unsigned int startComponent) const
	{
		return Path(*this, startComponent + _partialStart);
	}

	const Path Path::dirname() const
	{
		std::list<std::string> pathComps(
			_fullPathComponents->begin(), _fullPathComponents->end());
		pathComps.pop_back();
		return Path(pathComps);
	}

	const std::string Path::basename() const
	{
		return _fullPathComponents->back();
	}

	Path Path::lookup(const std::string &newPath) const
	{
		if (newPath.size() == 0)
			return Path(*this, _partialStart);

		if (newPath[0] == '/')
			return Path(normalizePath(newPath));

		return Path(normalizePath(((std::string)*this) + "/" + newPath));
	}

	const Path& Path::getCurrentWorkingDirectory()
	{
		if (_cwd == NULL)
		{
			OGRSH_FATAL("Current working directory not initialized.");
			ogrsh::shims::real_exit(1);
		}

		return *_cwd;
	}

	void Path::setCurrentWorkingDirectory(const Path &path)
	{
		if (_cwd != NULL)
			delete _cwd;
		_cwd = new Path(path);

		if (setenv(OGRSH_CWD_VAR_NAME, ((const std::string&)path).c_str(), 1))
		{
			OGRSH_FATAL("Unable to set new OGRSH_CWD variable to \""
				<< (const std::string&)path << "\".");
			ogrsh::shims::real_exit(1);
		}
	}

	std::list<std::string> normalizePath(const std::string &path)
	{
		size_t sourceLength = path.length();

		if (sourceLength == 0)
		{
			OGRSH_FATAL("Should never be asked to normalize an "
				"empty directory path.");
			ogrsh::shims::real_exit(1);
		}

		if (path[0] != '/')
		{
			OGRSH_FATAL("Should never be asked to normalize a directory "
				"path that isn't absolute.");
			ogrsh::shims::real_exit(1);
		}

		std::list<std::string> pathStack;
		size_t first = 1;
		size_t second;

		while (first < sourceLength && first != std::string::npos)
		{
			first = path.find_first_not_of('/', first);
			if (first == std::string::npos)
				break;


			second = first;
			while (true)
			{
				second = path.find('/', second);
				if (second == std::string::npos)
					break;
				if (path[second - 1] != '\\')
					break;
				second++;
			}

			std::string piece;
			if (second == std::string::npos)
			{
				piece = path.substr(first);
			} else
			{
				piece = path.substr(first, second - first);
			}

			if (piece == "..")
			{
				if (pathStack.size() > 0)
				{
					pathStack.pop_back();
				}
			} else if (piece == ".")
			{
				// Do nothing
			} else
			{
				pathStack.push_back(piece);
			}

			first = second;
		}

		return pathStack;
	}

	void Path::setCurrentWorkingDirectory(const std::string &path)
	{
		if (_cwd != NULL)
			delete _cwd;
			
		_cwd = new Path(normalizePath(path));
		if (setenv(OGRSH_CWD_VAR_NAME, ((const std::string&)(*_cwd)).c_str(), 1))
		{
			OGRSH_FATAL("Unable to set new OGRSH_CWD variable to \""
				<< (const std::string&)(*_cwd) << "\".");
			ogrsh::shims::real_exit(1);
		}
	}
}
