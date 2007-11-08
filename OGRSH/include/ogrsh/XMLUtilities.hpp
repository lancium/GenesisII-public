#ifndef __XMLUTILITIES_HPP__
#define __XMLUTILITIES_HPP__

#include <string>

#include "xercesc/dom/DOMElement.hpp"
#include "xercesc/util/XMLString.hpp"

namespace ogrsh
{
	std::string convert(const XMLCh *xch);
	std::string getAttribute(xercesc_2_8::DOMElement *element,
		const std::string attributeName);
}

#endif
