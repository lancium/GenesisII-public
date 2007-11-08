#include <string>
#include <errno.h>

#include "ogrsh/ExecuteFunctions.hpp"
#include "ogrsh/Logging.hpp"
#include "ogrsh/MountTree.hpp"

#include "ogrsh/VirtualExecuteFunctions.hpp"
#include "ogrsh/VirtualProviderUtilities.hpp"

namespace ogrsh
{
	VirtualExecuteFunctions::VirtualExecuteFunctions(MountTree *mountTree)
	{
		_mountTree = mountTree;
	}

	int VirtualExecuteFunctions::execve(ExecuteState *eState,
		const Path &relativePath,
		char *const argv[], char *const envp[])
	{
		OGRSH_TRACE("VirtualExecuteFunctions::execve(\""
			<< (const std::string&)relativePath << "\", ...).");

		std::string name = relativePath.basename();
		Path subRelativePath = relativePath.dirname();

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
				subRelativePath = subRelativePath.lookup(name);
				return mount->getExecuteFunctions()->execve(eState,
					subRelativePath, argv, envp);
			} else
			{
				// It's an internal entry, and we don't support files
				errno = EISDIR;
				return -1;
			}
		}

		// Shouldn't get here.
		return -1;
	}
}
