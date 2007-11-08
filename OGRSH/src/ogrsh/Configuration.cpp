#include <stdlib.h>
#include <dlfcn.h>

#include "ogrsh/Configuration.hpp"
#include "ogrsh/ConfigurationFileParser.hpp"
#include "ogrsh/EnvironmentVariables.hpp"
#include "ogrsh/Logging.hpp"
#include "ogrsh/Mount.hpp"
#include "ogrsh/Path.hpp"
#include "ogrsh/VirtualMount.hpp"

using namespace xercesc_2_8;

namespace ogrsh
{
	static const char *_OGRSH_CONFIG_VAR_NAME = "OGRSH_CONFIG";

	static Configuration *_configurationInstance = NULL;

	static bool mountTreeCleaner(const std::string &path, Mount *mount);

	Configuration::Configuration(const Configuration&)
	{
		OGRSH_FATAL("Not allowed to copy configurations.");
		ogrsh::shims::real_exit(1);
	}

	Configuration& Configuration::operator= (const Configuration&)
	{
		OGRSH_FATAL("Not allowed to copy configurations.");
		ogrsh::shims::real_exit(1);

		return *this;
	}

	void Configuration::parseConfigurationFile(const std::string &configFile)
	{
		ogrsh::parseConfigurationFile(configFile, this);
	}

	Configuration::Configuration()
	{
		const char *configFile = getenv(_OGRSH_CONFIG_VAR_NAME);
		if (configFile == NULL)
		{
			OGRSH_FATAL(_OGRSH_CONFIG_VAR_NAME << " variable not defined.");
			ogrsh::shims::real_exit(1);
		}

		_mountTree = MountTree::createRoot();
		OGRSH_DEBUG("Config file is " << configFile);

		parseConfigurationFile(configFile);

		_rootMount = new VirtualMount(_mountTree);
	}

	Configuration::~Configuration()
	{
		// Clean up root mount
		delete _rootMount;

		// Clean up mounts
		_mountTree->visit(mountTreeCleaner);
		delete _mountTree;

		// Clean up providers
		std::map<std::string, Provider*>::iterator provIter;
		for (provIter = _providers.begin(); provIter != _providers.end();
			provIter++)
		{
			Provider *prov = provIter->second;
			delete prov;
		}
	}

	void Configuration::setHomeDirectory(const std::string &homeDirectory)
	{
		OGRSH_DEBUG("Setting home directory to \"" << homeDirectory << "\".");

		if (homeDirectory.length() == 0)
		{
			OGRSH_FATAL("Invalid home directory set.");
			ogrsh::shims::real_exit(1);
		}

		if (homeDirectory[0] != '/')
		{
			OGRSH_FATAL("Invalid home directory set (" << homeDirectory
				<< ") -- must be absolute.");
			ogrsh::shims::real_exit(1);
		}

		char *cwd = getenv(OGRSH_CWD_VAR_NAME);
		if (cwd == NULL)
			Path::setCurrentWorkingDirectory(homeDirectory);
		else
			Path::setCurrentWorkingDirectory(cwd);

		_homeDirectory = homeDirectory;
		setenv("HOME", _homeDirectory.c_str(), 1);
	}

	Mount* Configuration::getRootMount()
	{
		return _rootMount;
	}

	void Configuration::addProvider(Provider *provider)
	{
		const std::string &providerName = provider->getProviderName();
		if (findProvider(providerName) != NULL)
		{
			OGRSH_FATAL("Provider \"" << providerName
				<< "\" has already been defined.");
			ogrsh::shims::real_exit(1);
		}

		_providers[providerName] = provider;
	}

	Provider* Configuration::findProvider(
		const std::string &providerName) const
	{
		std::map<std::string, Provider*>::const_iterator iter;
		iter = _providers.find(providerName);
		if (iter == _providers.end())
			return NULL;

		return iter->second;
	}

	void Configuration::addMount(const std::string &providerName,
		const std::string &sessionName,
		const std::string &mountLocation,
		const DOMElement &configNode)
	{
		OGRSH_DEBUG("Adding mount \"" << mountLocation << "\" for session \""
			<< sessionName << "\" hosted by provider \""
			<< providerName << "\".");

		if (mountLocation.length() == 0)
		{
			OGRSH_ERROR("Mount location is empty.");
			ogrsh::shims::real_exit(1);
		}

		if (mountLocation[0] != '/')
		{
			OGRSH_ERROR("Mount location \"" << mountLocation
				<< "\" is not absolute.");
			ogrsh::shims::real_exit(1);
		}

		Path locationPath =
			Path::getCurrentWorkingDirectory().lookup(mountLocation);
		const std::string &nLocation = (const std::string&)locationPath;

		if (nLocation == "/")
		{
			OGRSH_ERROR("Mount location \"" << mountLocation
				<< "\" is not valid -- can't be /.");
			ogrsh::shims::real_exit(1);
		}

		Provider *provider = findProvider(providerName);
		if (provider == NULL)
		{
			OGRSH_FATAL("Provider \"" << providerName
				<< "\" is unknown.");
			ogrsh::shims::real_exit(1);
		}

		Session *session = provider->getSession(sessionName);
		if (session == NULL)
		{
			OGRSH_FATAL("Session \"" << sessionName
				<< "\" is not known to provider \"" << providerName
				<< "\".");
			ogrsh::shims::real_exit(1);
		}

		Mount *mount = session->mountLocation(mountLocation, configNode);
		_mountTree->addPath(nLocation.substr(1), mount);
	}

	void Configuration::createConfiguration()
	{
		if (_configurationInstance != NULL)
		{
			OGRSH_FATAL("Configuration has already been created.");
			ogrsh::shims::real_exit(1);
		}

		_configurationInstance = new Configuration();
	}

	void Configuration::destroyConfiguration()
	{
		if (_configurationInstance == NULL)
		{
			OGRSH_FATAL("Configuration does not exist.");
			ogrsh::shims::real_exit(1);
		}

		delete _configurationInstance;
		_configurationInstance = NULL;
	}

	Configuration& Configuration::getConfiguration()
	{
		return *_configurationInstance;
	}

	bool mountTreeCleaner(const std::string &path,
		Mount *mount)
	{
		OGRSH_DEBUG("Cleaning up mount \"" << path << "\".");

		delete mount;

		return true;
	}
}
