#include <errno.h>

#include <string>

#include "ogrsh/Logging.hpp"
#include "ogrsh/ACLFunctions.hpp"
#include "ogrsh/MountTree.hpp"
#include "ogrsh/VirtualACLFunctions.hpp"
#include "ogrsh/VirtualProviderUtilities.hpp"
#include "ogrsh/Path.hpp"

namespace ogrsh
{
	VirtualACLFunctions::VirtualACLFunctions(
		MountTree *mountTree)
	{
		_mountTree = mountTree;
	}

	acl_t VirtualACLFunctions::acl_get_file(const Path &path, acl_type_t type)
	{
		Path subRelativePath = path;
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
				return mount->getACLFunctions()->acl_get_file(subRelativePath,
					type);
			} else
			{
				// It's an internal entry
				errno = ENOTSUP;

				// We don't support acls in the virtual provider
				return NULL;
			}
		}

		// Shouldn't get here.
		return NULL;
	}

	int VirtualACLFunctions::acl_extended_file(const Path &relativePath)
	{
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
				return mount->getACLFunctions()->acl_extended_file(
					subRelativePath);
			} else
			{
				// It's an internal entry

				// We don't support acls in the virtual provider
				return 0;
			}
		}

		// Shouldn't get here.
		return -1;
	}

	int VirtualACLFunctions::access(const Path &relativePath, int mode)
	{
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
				return mount->getACLFunctions()->access(subRelativePath, mode);
			} else
			{
				// It's an internal entry

				if (mode & W_OK > 0)
				{
					errno = EACCES;
					return -1;
				} else
				{
					return 0;
				}
			}
		}

		// Shouldn't get here.
		return -1;
	}

	int VirtualACLFunctions::eaccess(const Path &relativePath, int mode)
	{
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
				return mount->getACLFunctions()->eaccess(subRelativePath, mode);
			} else
			{
				// It's an internal entry

				if (mode & W_OK > 0)
				{
					errno = EACCES;
					return -1;
				} else
				{
					return 0;
				}
			}
		}

		// Shouldn't get here.
		return -1;
	}

	int VirtualACLFunctions::euidaccess(const Path &relativePath, int mode)
	{
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
				return mount->getACLFunctions()->euidaccess(
					subRelativePath, mode);
			} else
			{
				// It's an internal entry

				if (mode & W_OK > 0)
				{
					errno = EACCES;
					return -1;
				} else
				{
					return 0;
				}
			}
		}

		// Shouldn't get here.
		return -1;
	}
}
