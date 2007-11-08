#ifndef __MOUNT_TREE_HPP__
#define __MOUNT_TREE_HPP__

#include <list>
#include <string>
#include <map>

#include "ogrsh/Mount.hpp"

namespace ogrsh
{
	/**
	  * This function is used to visit all of the Mount inside
	  * of a mount tree.  Return true if you want to continue visiting, false
	  * if you want to stop the traversal.
	  */
	typedef bool (*mountTreeVisitorFunc)(const std::string &path,
		Mount *desc);

	class MountTree
	{
		private:
			std::string _myName;
			MountTree *_parent;
			Mount *_mount;
			std::map<std::string, MountTree*> _children;

			MountTree(const MountTree&);
			MountTree& operator= (const MountTree&);

			MountTree(MountTree *parent, const std::string &nodeName,
				Mount *mount);
			MountTree(MountTree *parent, const std::string &nodeName);

		public:
			static MountTree* createRoot();

			~MountTree();

			Mount* getMount();
			MountTree* findChild(const std::string&);
			const std::string getCurrentPath() const;

			void addPath(const std::string &relativePath,
				Mount *mount);

			bool visit(mountTreeVisitorFunc visitor);

			std::list<std::string> getChildren() const;

			void print(FILE*, const std::string &spaces) const;
	};
}

#endif
