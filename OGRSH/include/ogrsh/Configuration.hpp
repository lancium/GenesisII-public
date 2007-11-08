#ifndef __CONFIGURATION_HPP__
#define __CONFIGURATION_HPP__

#include <map>
#include <string>

#include "ogrsh/ConfigurationFileParseHandler.hpp"
#include "ogrsh/MountTree.hpp"
#include "ogrsh/Mount.hpp"

namespace ogrsh
{
	class Configuration : public ConfigurationFileParseHandler
	{
		private:
			std::map<std::string, Provider*> _providers;
			MountTree *_mountTree;

			Mount *_rootMount;
			std::string _homeDirectory;

			Configuration(const Configuration&);
			Configuration& operator= (const Configuration&);

			void parseConfigurationFile(const std::string &configFile);

			Configuration();

		public:
			virtual ~Configuration();

			virtual void setHomeDirectory(const std::string &homeDirectory);

			virtual void addProvider(Provider *provider);
			Provider* findProvider(const std::string &providerName) const;

			virtual void addMount(const std::string &providerName,
				const std::string &sessionName,
				const std::string &mountLocation,
				const xercesc_2_8::DOMElement &configNode);

			Mount* getRootMount();

			static void createConfiguration();
			static void destroyConfiguration();

			static Configuration& getConfiguration();
	};
}

#endif
