#include <string>

#include "ogrsh/Mount.hpp"
#include "ogrsh/MountTree.hpp"
#include "ogrsh/VirtualProviderUtilities.hpp"

namespace ogrsh
{
	/**
	  * This function takes a full path and the system's mount tree and
	  * finds the provider (if any) that should handle that path.  e.g.
	  * Suppose that you had a file system with exactly one mount (being that
	  * of provider type Alpha) at location /one/two/three (provider alpha is
	  * mounted as /one/two/three).  Here are some examples and what this
	  * function should return:
	  *		If queried with fullPath for /, we should get back the root
	  *		mountTree, and the fullPath of "" (meaning nothing left).
	  *		If queried with fullPath for /one/two, we should get back the
	  *		mountTree at /one/two, and the fullPath of ""
	  *		(meaning nothing left).
	  *		If queried with fullPath for /one/two/three, we should get back the
	  *		mountTree at /one/two/three, fullPath of "" (meaning nothing left).
	  *		If queried with fullPath for /one/two/three/four, we should get
	  *		back the mountTree at /one/two/three, fullPath of "four".
	  */
	MountTree* findMount(MountTree *mountTree, Path &fullPath)
	{
		for (unsigned int lcv = 0; lcv < fullPath.length(); lcv++)
		{
			mountTree = mountTree->findChild(fullPath[lcv]);
			if (mountTree == NULL)
				return NULL;

			if (mountTree->getMount() != NULL)
			{
				fullPath = fullPath.subPath(lcv + 1);
				return mountTree;
			}
		}

		fullPath = fullPath.subPath(fullPath.length());
		return mountTree;
	}
}
