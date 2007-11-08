#include <string>

#include "ogrsh/Logging.hpp"
#include "ogrsh/Session.hpp"
#include "ogrsh/XMLUtilities.hpp"

#include "providers/localfs/LocalFSSession.hpp"
#include "providers/localfs/LocalFSMount.hpp"

#include "xercesc/dom/DOMNodeList.hpp"

using namespace xercesc_2_8;

namespace ogrsh
{
	namespace localfs
	{
		static const char *_LOCAL_FS_MOUNT_ROOT_NS
			= "http://vcgr.cs.virginia.edu/ogrsh";
		static const char *_LOCAL_FS_MOUNT_ROOT_DIR_NAME = "root-directory";

		void LocalFSSession::startSession()
		{
			OGRSH_DEBUG("LocalFS Session \"" << getSessionName()
				<< "\" started.");
		}

		void LocalFSSession::stopSession()
		{
			OGRSH_DEBUG("LocalFS Session \"" << getSessionName()
				<< "\" stopped.");
		}

		ogrsh::Mount* LocalFSSession::createMount(
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
								"Created local fs mount from directory \""
								<< sourceLocation << "\".");

							return new LocalFSMount(
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

		LocalFSSession::LocalFSSession(const std::string &sessionName)
			: ogrsh::Session(sessionName)
		{
			OGRSH_DEBUG("LocalFS Session \"" << sessionName << "\" created.");
		}
	}
}
