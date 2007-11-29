#include <errno.h>

#include <string>

#include "ogrsh/DirectoryStream.hpp"
#include "ogrsh/DirectoryFunctions.hpp"
#include "ogrsh/Logging.hpp"
#include "ogrsh/MountTree.hpp"
#include "ogrsh/VirtualDirectoryFunctions.hpp"
#include "ogrsh/VirtualDirectoryStream.hpp"
#include "ogrsh/VirtualProviderUtilities.hpp"
#include "ogrsh/Path.hpp"

namespace ogrsh
{
	VirtualDirectoryFunctions::VirtualDirectoryFunctions(
		MountTree *mountTree)
	{
		_mountTree = mountTree;
	}

	int VirtualDirectoryFunctions::utime(const Path &relativePath,
		const struct utimbuf *buf)
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
				return mount->getDirectoryFunctions()->utime(
					subRelativePath, buf);
			} else
			{
				// It's an internal entry
				errno = EROFS;
				return -1;
			}
		}

		// Shouldn't get here.
		return -1;
	}

	int VirtualDirectoryFunctions::utimes(const Path &relativePath,
		const struct timeval *times)
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
				return mount->getDirectoryFunctions()->utimes(
					subRelativePath, times);
			} else
			{
				// It's an internal entry
				errno = EROFS;
				return -1;
			}
		}

		// Shouldn't get here.
		return -1;
	}

	int VirtualDirectoryFunctions::chdir(
		const Path &relativePath)
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
				return mount->getDirectoryFunctions()->chdir(subRelativePath);
			} else
			{
				// It's an internal entry
				return 0;
			}
		}

		// Shouldn't get here.
		return -1;
	}

	int VirtualDirectoryFunctions::mkdir(
		const Path &relativePath, mode_t mode)
	{
		OGRSH_TRACE("VirtualDirectoryFunctions::mkdir(\""
			<< (const std::string&)relativePath << "\", " << mode << ").");

		std::string name = relativePath.basename();
		Path subRelativePath = relativePath.dirname();

		OGRSH_DEBUG("VirtualDirectoryFunctions::mkdir -- dirname is \""
			<< (const std::string&)subRelativePath << "\" and basename is \""
			<< name << "\".");

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
				return mount->getDirectoryFunctions()->mkdir(
					subRelativePath, mode);
			} else
			{
				// It's an internal entry
				errno = EROFS;
				return -1;
			}
		}

		// Shouldn't get here.
		return -1;
	}

	int VirtualDirectoryFunctions::chmod(
		const Path &relativePath, mode_t mode)
	{
		OGRSH_TRACE("VirtualDirectoryFunctions::chmod(\""
			<< (const std::string&)relativePath << "\", " << mode << ").");

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
				return mount->getDirectoryFunctions()->chmod(
					subRelativePath, mode);
			} else
			{
				// It's an internal entry
				errno = EROFS;
				return -1;
			}
		}

		// Shouldn't get here.
		return -1;
	}

	int VirtualDirectoryFunctions::rmdir(const Path &relativePath)
	{
		OGRSH_TRACE("VirtualDirectoryFunctions::rmdir(\""
			<< (const std::string&)relativePath << "\").");

		Path subRelativePath = relativePath;

		MountTree *tree = findMount(_mountTree, subRelativePath);
		if ((const std::string&)subRelativePath == "/")
		{
			errno = EROFS;
			return -1;
		}

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
				return mount->getDirectoryFunctions()->rmdir(
					subRelativePath);
			} else
			{
				// It's an internal entry
				errno = EROFS;
				return -1;
			}
		}

		// Shouldn't get here.
		return -1;
	}

	int VirtualDirectoryFunctions::link(
		const Path &fullOldPath, const Path &fullNewPath)
	{
		std::string oldName = fullOldPath.basename();
		Path subRelativeOldPath = fullOldPath.dirname();

		std::string newName = fullNewPath.basename();
		Path subRelativeNewPath = fullNewPath.dirname();

		MountTree *oldTree = findMount(_mountTree, subRelativeOldPath);
		MountTree *newTree = findMount(_mountTree, subRelativeNewPath);

		if (oldTree == NULL || newTree == NULL)
		{
			// Couldn't find old path.
			errno = ENOENT;
			return -1;
		}

		Mount *oldMount = oldTree->getMount();
		Mount *newMount = newTree->getMount();

		if (oldMount != newMount)
		{
			// They are on different file systems.
			errno = EXDEV;
			return -1;
		}

		if (oldMount == NULL)
		{
			// It's an internal node
			errno = EROFS;
			return -1;
		} else
		{
			// We have another provider.
			subRelativeOldPath = subRelativeOldPath.lookup(oldName);
			subRelativeNewPath = subRelativeNewPath.lookup(newName);
			return oldMount->getDirectoryFunctions()->link(subRelativeOldPath,
				subRelativeNewPath);
		}
	}

	int VirtualDirectoryFunctions::rename(
		const Path &fullOldPath, const Path &fullNewPath)
	{
		std::string oldName = fullOldPath.basename();
		Path subRelativeOldPath = fullOldPath.dirname();

		std::string newName = fullNewPath.basename();
		Path subRelativeNewPath = fullNewPath.dirname();

		MountTree *oldTree = findMount(_mountTree, subRelativeOldPath);
		MountTree *newTree = findMount(_mountTree, subRelativeNewPath);

		if (oldTree == NULL || newTree == NULL)
		{
			// Couldn't find old path.
			errno = ENOENT;
			return -1;
		}

		Mount *oldMount = oldTree->getMount();
		Mount *newMount = newTree->getMount();

		if (oldMount != newMount)
		{
			// They are on different file systems.
			errno = EXDEV;
			return -1;
		}

		if (oldMount == NULL)
		{
			// It's an internal node
			errno = EROFS;
			return -1;
		} else
		{
			// We have another provider.
			subRelativeOldPath = subRelativeOldPath.lookup(oldName);
			subRelativeNewPath = subRelativeNewPath.lookup(newName);
			return oldMount->getDirectoryFunctions()->rename(
				subRelativeOldPath, subRelativeNewPath);
		}
	}

	DirectoryStream* VirtualDirectoryFunctions::opendir(
		const Path &relativePath)
	{
		Path subRelativePath = relativePath;
		MountTree *tree = findMount(_mountTree, subRelativePath);

		if (tree == NULL)
		{
			// Couldn't find the path
			errno = ENOENT;
			return NULL;
		} else
		{
			Mount *mount = tree->getMount();
			DirectoryStream *stream;
			if (mount != NULL)
			{
				// We have another provider, call it.
				stream =
					mount->getDirectoryFunctions()->opendir(subRelativePath);
			} else
			{
				// It's an internal entry
				stream = new VirtualDirectoryStream(
					tree->getChildren());
			}

			if (stream != NULL)
			{
				FileDescriptor *desc = stream->getFileDescriptor();
				if (desc != NULL)
					desc->setFullVirtualPath(relativePath);
			}

			return stream;
		}

		// Shouldn't get here.
		return NULL;
	}

	int VirtualDirectoryFunctions::__xstat(int version,
		const Path &relativePath, struct stat *statbuf)
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
				return mount->getDirectoryFunctions()->__xstat(version,
					subRelativePath, statbuf);
			} else
			{
				// It's an internal entry
				statbuf->st_dev = 0;
				statbuf->st_ino = 0;
				statbuf->st_mode = S_IFDIR | S_IRUSR | S_IXUSR |
					S_IRGRP | S_IXGRP | S_IROTH | S_IXOTH;
				statbuf->st_nlink = 0;
				statbuf->st_uid = 0;
				statbuf->st_gid = 0;
				statbuf->st_rdev = 0;
				statbuf->st_size = 0;
				statbuf->st_blksize = (1024 * 4);
				statbuf->st_blocks = 1;
				statbuf->st_atime = time(NULL);
				statbuf->st_mtime = time(NULL);
				statbuf->st_ctime = time(NULL);

				return 0;
			}
		}

		// Shouldn't get here.
		return -1;
	}

	int VirtualDirectoryFunctions::__xstat64(int version,
		const Path &relativePath, struct stat64 *statbuf)
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
				return mount->getDirectoryFunctions()->__xstat64(version,
					subRelativePath, statbuf);
			} else
			{
				// It's an internal entry
				statbuf->st_dev = 0;		// TODO -- we need a better system
				statbuf->st_ino = 0;		// TODO -- we need a better system
				statbuf->st_mode = S_IFDIR | S_IRUSR | S_IXUSR |
					S_IRGRP | S_IXGRP | S_IROTH | S_IXOTH;
				statbuf->st_nlink = 0;
				statbuf->st_uid = 0;
				statbuf->st_gid = 0;
				statbuf->st_rdev = 0;
				statbuf->st_size = 0;
				statbuf->st_blksize = (1024 * 4);
				statbuf->st_blocks = 1;
				statbuf->st_atime = time(NULL);
				statbuf->st_mtime = time(NULL);
				statbuf->st_ctime = time(NULL);

				return 0;
			}
		}

		// Shouldn't get here.
		return -1;
	}

	int VirtualDirectoryFunctions::__lxstat(int version,
		const Path &relativePath, struct stat *statbuf)
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
				return mount->getDirectoryFunctions()->__lxstat(version,
					subRelativePath, statbuf);
			} else
			{
				// It's an internal entry
				statbuf->st_dev = 0;		// TODO -- we need a better system
				statbuf->st_ino = 0;		// TODO -- we need a better system
				statbuf->st_mode = S_IFDIR | S_IRUSR | S_IXUSR |
					S_IRGRP | S_IXGRP | S_IROTH | S_IXOTH;
				statbuf->st_nlink = 0;
				statbuf->st_uid = 0;
				statbuf->st_gid = 0;
				statbuf->st_rdev = 0;
				statbuf->st_size = 0;
				statbuf->st_blksize = (1024 * 4);
				statbuf->st_blocks = 1;
				statbuf->st_atime = time(NULL);
				statbuf->st_mtime = time(NULL);
				statbuf->st_ctime = time(NULL);

				return 0;
			}
		}

		// Shouldn't get here.
		return -1;
	}

	int VirtualDirectoryFunctions::__lxstat64(int version,
		const Path &relativePath, struct stat64 *statbuf)
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
				return mount->getDirectoryFunctions()->__lxstat64(version,
					subRelativePath, statbuf);
			} else
			{
				// It's an internal entry
				statbuf->st_dev = 0;
				statbuf->st_ino = 0;
				statbuf->st_mode = S_IFDIR | S_IRUSR | S_IXUSR |
					S_IRGRP | S_IXGRP | S_IROTH | S_IXOTH;
				statbuf->st_nlink = 0;
				statbuf->st_uid = 0;
				statbuf->st_gid = 0;
				statbuf->st_rdev = 0;
				statbuf->st_size = 0;
				statbuf->st_blksize = (1024 * 4);
				statbuf->st_blocks = 1;
				statbuf->st_atime = time(NULL);
				statbuf->st_mtime = time(NULL);
				statbuf->st_ctime = time(NULL);

				return 0;
			}
		}

		// Shouldn't get here.
		return -1;
	}

	int VirtualDirectoryFunctions::readlink(
		const Path &relativePath, char *buf, size_t bufsize)
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
				return mount->getDirectoryFunctions()->readlink(
					subRelativePath, buf, bufsize);
			} else
			{
				// It's an internal entry
				errno = EINVAL;
				return -1;
			}
		}

		// Shouldn't get here.
		return -1;
	}
}
