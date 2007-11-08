#include <string>
#include <errno.h>

#include "ogrsh/FileDescriptor.hpp"
#include "ogrsh/FileFunctions.hpp"
#include "ogrsh/Logging.hpp"
#include "ogrsh/MountTree.hpp"

#include "ogrsh/VirtualFileFunctions.hpp"
#include "ogrsh/VirtualProviderUtilities.hpp"

namespace ogrsh
{
	VirtualFileFunctions::VirtualFileFunctions(MountTree *mountTree)
	{
		_mountTree = mountTree;
	}

	FileDescriptor* VirtualFileFunctions::open64(const Path &relativePath,
		int flags, mode_t mode)
	{
		OGRSH_DEBUG("VirtualFileFunctions::open64(\""
			<< (const std::string&)relativePath << "\", " << flags << ").");

		std::string name = relativePath.basename();
		Path subRelativePath = relativePath.dirname();

		MountTree *tree = findMount(_mountTree, subRelativePath);
		if (tree == NULL)
		{
			// Couldn't find the path
			errno = ENOENT;
			return NULL;
		} else
		{
			Mount *mount = tree->getMount();
			if (mount != NULL)
			{
				// We have another provider, call it.
				subRelativePath = subRelativePath.lookup(name);
				FileDescriptor *desc = mount->getFileFunctions()->open64(
					subRelativePath, flags, mode);
				if (desc != NULL)
					desc->setFullVirtualPath(relativePath);
				return desc;
			} else
			{
				// It's an internal entry, and we don't support files
				errno = EROFS;
				return NULL;
			}
		}

		// Shouldn't get here.
		return NULL;
	}

	FileDescriptor* VirtualFileFunctions::creat(const Path &relativePath,
		mode_t mode)
	{
		OGRSH_TRACE("VirtualFileFunctions::creat(\""
			<< (const std::string&)relativePath << "\", " << mode << ").");

		std::string name = relativePath.basename();
		Path subRelativePath = relativePath.dirname();

		MountTree *tree = findMount(_mountTree, subRelativePath);
		if (tree == NULL)
		{
			// Couldn't find the path
			errno = ENOENT;
			return NULL;
		} else
		{
			Mount *mount = tree->getMount();
			if (mount != NULL)
			{
				// We have another provider, call it.
				subRelativePath = subRelativePath.lookup(name);
				return mount->getFileFunctions()->creat(
					subRelativePath, mode);
			} else
			{
				// It's an internal entry, and we don't support files
				errno = EROFS;
				return NULL;
			}
		}

		// Shouldn't get here.
		return NULL;
	}

	int VirtualFileFunctions::unlink(const Path &relativePath)
	{
		OGRSH_TRACE("VirtualFileFunctions::unlink(\""
			<< (const std::string&)relativePath << "\").");

		Path subRelativePath = relativePath;

		MountTree *tree = findMount(_mountTree, subRelativePath);
		if (tree == NULL)
		{
			// Couldn't find the path
			errno = ENOENT;
			return -1;
		} else
		{
			Mount *mount = tree->getMount();
			if (mount != NULL)
			{
				// We have another provider, call it.
				return mount->getFileFunctions()->unlink(
					subRelativePath);
			} else
			{
				// It's an internal entry, and we don't support files
				errno = EROFS;
				return -1;
			}
		}

		// Shouldn't get here.
		return -1;
	}
}
