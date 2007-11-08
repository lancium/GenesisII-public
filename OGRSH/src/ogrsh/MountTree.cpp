#include <string>
#include <map>

#include "ogrsh/Logging.hpp"
#include "ogrsh/Mount.hpp"
#include "ogrsh/MountTree.hpp"

namespace ogrsh
{
	MountTree::MountTree(const MountTree&)
	{
		OGRSH_FATAL("Not allowed to copy MountTrees.");
		ogrsh::shims::real_exit(1);
	}

	MountTree& MountTree::operator= (const MountTree&)
	{
		OGRSH_FATAL("Not allowed to copy MountTrees.");
		ogrsh::shims::real_exit(1);

		return *this;
	}

	MountTree::MountTree(MountTree *parent, const std::string &nodeName,
		Mount *mount)
	{
		_myName = nodeName;
		_mount = mount;
		_parent = parent;
	}

	MountTree::MountTree(MountTree *parent, const std::string &nodeName)
	{
		_myName = nodeName;
		_mount = NULL;
		_parent = parent;
	}

	MountTree* MountTree::createRoot()
	{
		return new MountTree(NULL, "");
	}

	MountTree::~MountTree()
	{
		std::map<std::string, MountTree*>::iterator iter;
		for (iter = _children.begin(); iter != _children.end(); iter++)
		{
			MountTree *child = iter->second;
			delete child;
		}
	}

	Mount* MountTree::getMount()
	{
		return _mount;
	}

	MountTree* MountTree::findChild(const std::string &childName)
	{
		std::map<std::string, MountTree*>::iterator iter;
		iter = _children.find(childName);
		if (iter == _children.end())
			return NULL;

		return iter->second;
	}

	const std::string MountTree::getCurrentPath() const
	{
		if (_parent == NULL)
			return "";
		else
			return _parent->getCurrentPath() + "/" + _myName;
	}

	void MountTree::addPath(const std::string &relativePath,
		Mount *mount)
	{
		std::string::size_type nextSlash = relativePath.find('/', 0);
		if (nextSlash == std::string::npos)
		{
			MountTree *child = findChild(relativePath);
			if (child != NULL)
			{
				if (child->getMount() != NULL)
				{
					OGRSH_FATAL("A mount already exists at path \""
						<< child->getCurrentPath() << "\".");
					ogrsh::shims::real_exit(1);
				} else
				{
					OGRSH_FATAL(
						"A mount already exists as a child of the path \""
						<< child->getCurrentPath() << "\".");
					ogrsh::shims::real_exit(1);
				}
			}

			_children[relativePath] = new MountTree(this, relativePath,
				mount);
		} else
		{
			std::string nodeName = relativePath.substr(
				0, nextSlash);

			MountTree *child = findChild(nodeName);
			if (child == NULL)
			{
				child = new MountTree(this, nodeName);
				_children[nodeName] = child;
			}

			if (child->getMount() != NULL)
			{
				OGRSH_FATAL(
					"Cannot add  mount point \"" <<
					getCurrentPath() + "/" + relativePath
					<< "\" under existing mount point \""
					<< child->getCurrentPath() << "\".");
				ogrsh::shims::real_exit(1);
			}

			child->addPath(relativePath.substr(nextSlash+1), mount);
		}
	}

	bool MountTree::visit(mountTreeVisitorFunc visitor)
	{
		if (_mount != NULL)
		{
			return visitor(getCurrentPath(), _mount);
		} else
		{
			std::map<std::string, MountTree*>::iterator iter;
			for (iter = _children.begin(); iter != _children.end(); iter++)
			{
				if (!(iter->second->visit(visitor)))
					return false;
			}
		}

		return true;
	}

	std::list<std::string> MountTree::getChildren() const
	{
		std::list<std::string> children;
		std::map<std::string, MountTree*>::const_iterator iter;
		for (iter = _children.begin(); iter != _children.end(); iter++)
		{
			children.push_back(iter->first);
		}

		return children;
	}

	void MountTree::print(FILE *out, const std::string &spaces) const
	{
		std::map<std::string, MountTree*>::const_iterator iter;
		for (iter = _children.begin(); iter != _children.end(); iter++)
		{
			std::string name = iter->first;
			MountTree *tree = iter->second;
			Mount *mount = tree->getMount();

			if (mount != NULL)
			{
				fprintf(out, "%s%s[%s]\n", spaces.c_str(), name.c_str(),
					mount->getMountLocation().c_str());
			} else
			{
				fprintf(out, "%s%s\n", spaces.c_str(), name.c_str());
				tree->print(out, spaces + "    ");
			}
		}
	}
}
