#include <errno.h>

#include "jcomm/SessionClient.hpp"
#include "jcomm/OGRSHException.hpp"

#include "ogrsh/Logging.hpp"
#include "ogrsh/XMLUtilities.hpp"

#include "ogrsh/shims/File.hpp"

#include "providers/geniifs/GeniiFSSession.hpp"
#include "providers/geniifs/GeniiFSMount.hpp"

#include "xercesc/dom/DOMElement.hpp"
#include "xercesc/dom/DOMNode.hpp"
#include "xercesc/dom/DOMNodeList.hpp"

using namespace xercesc_2_8;

namespace ogrsh
{
	namespace geniifs
	{
		static std::string prompt(const std::string &prmpt,
			const char *defaultValue) throw (jcomm::OGRSHException);

		static std::string getCredentialFile(const std::string &sessionName)
			throw (jcomm::OGRSHException);
		static std::string getUsername();
		static std::string getPassword();
		static std::string getPattern();

		static const char *_OGRSH_JSERVER_ADDRESS_VAR = "OGRSH_JSERVER_ADDRESS";
		static const char *_OGRSH_JSERVER_PORT_VAR = "OGRSH_JSERVER_PORT";
		static const char *_OGRSH_JSERVER_SECRET_VAR = "OGRSH_JSERVER_SECRET";
		static const char *_OGRSH_JSERVER_SESSION_VAR = "OGRSH_JSERVER_SESSION";

		static const char *_LOCAL_FS_MOUNT_ROOT_NS
			= "http://vcgr.cs.virginia.edu/ogrsh";
		static const char *_LOCAL_FS_MOUNT_ROOT_DIR_NAME = "root-directory";

		void GeniiFSSession::startSession()
		{
			const char *jserverIP = getenv(_OGRSH_JSERVER_ADDRESS_VAR);
			const char *jserverPort = getenv(_OGRSH_JSERVER_PORT_VAR);
			const char *jserverSecret = getenv(_OGRSH_JSERVER_SECRET_VAR);
			const char *jserverSession = getenv(_OGRSH_JSERVER_SESSION_VAR);

			if (jserverPort == NULL)
			{
				OGRSH_FATAL("Missing required env. var. " <<
					_OGRSH_JSERVER_PORT_VAR);
				ogrsh::shims::real_exit(1);
			}

			if (jserverSecret == NULL)
			{
				OGRSH_FATAL("Missing required env. var. " <<
					_OGRSH_JSERVER_SECRET_VAR);
				ogrsh::shims::real_exit(1);
			}

			if (jserverIP == NULL)
			{
				OGRSH_FATAL("Missing required env. var. " <<
					_OGRSH_JSERVER_ADDRESS_VAR);
				ogrsh::shims::real_exit(1);
			}

			_socket = new jcomm::Socket(jserverIP, atoi(jserverPort),
				jserverSecret);

			jcomm::SessionClient sessionClient(*_socket);
			std::string newSessionID;
			newSessionID = sessionClient.connectSession(jserverSession);

			OGRSH_DEBUG("Created a new session with ID \""
				<< newSessionID << "\".");
			
			setenv(_OGRSH_JSERVER_SESSION_VAR, newSessionID.c_str(), 1);

			if (jserverSession == NULL)
			{
				// We are a brand new session, so we have to make sure that
				// we initialize the net (root RNS Url)

				OGRSH_DEBUG("Trying to connect to RNS configuration url \""
					<< _rootRNSUrl << "\".");
				sessionClient.connectNet(_rootRNSUrl);

				std::string credentialFile;
				std::string password;
				std::string patternOrUsername;

				while (1)
				{
					try
					{
						credentialFile = getCredentialFile(
							getSessionName());
						std::string::size_type index =
							credentialFile.find("rns:");
						if (index == 0)
						{
							// It's an RNS path.
							patternOrUsername = getUsername();
							password = getPassword();
						} else
						{
							password = getPassword();
							patternOrUsername = getPattern();
						}
					}
					catch (jcomm::OGRSHException oe)
					{
						// The log in was cancelled.
						ogrsh::shims::uber_real_fprintf(stderr,
							"Log in cancelled.  Exiting.\n");
						ogrsh::shims::real_exit(0);
					}

					// We also need to log in to it.
					OGRSH_DEBUG("Attempting to log in to the Genesis II net.");

					try
					{
						sessionClient.loginSession(
							credentialFile.c_str(), password,
							patternOrUsername);
						password = "";
						break;
					}
					catch (jcomm::OGRSHException oe)
					{
						ogrsh::shims::uber_real_fprintf(stderr,
							"%s\n\n", oe.what());
					}
				}
			}
		}

		void GeniiFSSession::stopSession()
		{
			if (_socket != NULL)
				delete _socket;
			_socket = NULL;
		}

		Mount* GeniiFSSession::createMount(
			const std::string &mountLocation,
			const xercesc_2_8::DOMElement &configNode)
		{
			DOMNodeList *children = configNode.getChildNodes();

			XMLSize_t length = children->getLength();
			for (XMLSize_t lcv = 0; lcv < length; lcv++)
			{
				DOMNode *node = children->item(lcv);
				if (node->getNodeType() == DOMNode::ELEMENT_NODE)
				{
					DOMElement *el = (DOMElement*)node;
					std::string namespaceURI = convert(el->getNamespaceURI());
					std::string localName = convert(el->getLocalName());

					if (namespaceURI == _LOCAL_FS_MOUNT_ROOT_NS)
					{
						if (localName == _LOCAL_FS_MOUNT_ROOT_DIR_NAME)
						{
							std::string sourceLocation =
								ogrsh::convert(el->getTextContent());
							OGRSH_DEBUG(
								"Created genii fs mount from directory \""
								<< sourceLocation << "\".");

							return new GeniiFSMount(
								this,
								mountLocation, sourceLocation);
						}
					}

					OGRSH_ERROR("Unknown configuration element {" <<
						namespaceURI << "}" << localName << " for mount at \""
						<< mountLocation << "\".");
					ogrsh::shims::real_exit(1);
				}
			}

			OGRSH_ERROR("Missing required configuration element {"
				<< _LOCAL_FS_MOUNT_ROOT_NS << "}"
				<< _LOCAL_FS_MOUNT_ROOT_DIR_NAME << " in mount \""
				<< mountLocation << "\".");
			ogrsh::shims::real_exit(1);
			return NULL;
		}

		GeniiFSSession::GeniiFSSession(const std::string  &sessionName,
			const std::string &rootRNSUrl)
			: ogrsh::Session(sessionName), _rootRNSUrl(rootRNSUrl)
		{
			_socket = NULL;
		}

		GeniiFSSession::~GeniiFSSession()
		{
			if (_socket != NULL)
				delete _socket;
		}

		std::string getCredentialFile(const std::string &sessionName)
			throw (jcomm::OGRSHException)
		{
			ogrsh::shims::uber_real_fprintf(stdout, "Session \"%s\"\n",
				sessionName.c_str());
			ogrsh::shims::uber_real_fprintf(stdout,
				"Please enter the URI for your credential information."
				"This can either be\na local file system path, or an rns "
				"path with rns: pre-pended to it.\n");

			return prompt("Grid Credential URI:  ", NULL);
		}

		std::string getPassword()
		{
			try
			{
				return getpass("Keystore password?  ");
			}
			catch (jcomm::OGRSHException oe)
			{
				// can't happen
				return "";
			}
		}

		std::string getUsername()
		{
			try
			{
				return prompt("Username?  ", "");
			}
			catch (jcomm::OGRSHException oe)
			{
				// Can't happen.
				return "";
			}
		}

		std::string getPattern()
		{
			try
			{
				return prompt("Certificate Pattern?  ", "");
			}
			catch (jcomm::OGRSHException oe)
			{
				// can't happen
				return "";
			}
		}

		std::string prompt(const std::string &prmpt, const char *defaultValue)
			throw (jcomm::OGRSHException)
		{
			char *buffer = new char[512];
			ogrsh::shims::uber_real_fprintf(stdout, "%s", prmpt.c_str());
			ogrsh::shims::real_fgets(buffer, 512, stdin);

			int len = strlen(buffer);
			while (1)
			{
				len--;
				if (len < 0)
					break;
				if (isspace(buffer[len]))
					buffer[len] = '\0';
				else
					break;
			}
	
			std::string str(buffer);
			delete []buffer;

			if (str.empty())
			{
				if (defaultValue != NULL)
					return defaultValue;
				else
					throw jcomm::OGRSHException("No answer selected.");
			} else
			{
				return str;
			}
		}
	}
}
