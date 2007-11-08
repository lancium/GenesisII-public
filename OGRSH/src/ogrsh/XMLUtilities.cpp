#include "ogrsh/XMLUtilities.hpp"

using namespace xercesc_2_8;

namespace ogrsh
{
	std::string convert(const XMLCh *xch)
	{
		char *tmp = XMLString::transcode(xch);
		std::string ret(tmp);
		XMLString::release(&tmp);

		return ret;
	}

	std::string getAttribute(DOMElement *element,
		const std::string attributeName)
	{
		XMLCh *attrName = XMLString::transcode(attributeName.c_str());
		const XMLCh *attrValue = element->getAttribute(attrName);
		XMLString::release(&attrName);

		return (attrValue == NULL) ?
			std::string() : convert(attrValue);
	}
}
