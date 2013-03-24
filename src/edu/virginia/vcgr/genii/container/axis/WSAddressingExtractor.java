/*
 * Copyright 2006 University of Virginia
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package edu.virginia.vcgr.genii.container.axis;

import java.util.Iterator;
import java.util.Vector;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;

import org.apache.axis.AxisFault;
import org.apache.axis.MessageContext;
import org.apache.axis.handlers.BasicHandler;
import org.apache.axis.message.MessageElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ws.addressing.AttributedURIType;
import org.ws.addressing.EndpointReferenceType;
import org.ws.addressing.MetadataType;
import org.ws.addressing.ReferenceParametersType;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.invoke.handlers.MyProxyCertificate;
import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.ser.ObjectDeserializer;
import edu.virginia.vcgr.genii.container.Container;
import edu.virginia.vcgr.genii.container.cservices.eprmapper.EPRMapperService;

public class WSAddressingExtractor extends BasicHandler
{
	static public final String AXIS_MESSAGE_CTXT_EPR_PROPERTY = "edu.virginia.vcgr.genii.container.axis.epr-property";

	static final long serialVersionUID = 0L;

	static private Log _logger = LogFactory.getLog(WSAddressingExtractor.class);

	static private String _WSA_NS = EndpointReferenceType.getTypeDesc().getXmlType().getNamespaceURI();

	static private QName _WSA_TO_QNAME = new QName(_WSA_NS, "To");

	static private QName _WSA_METADATA_QName = new QName(_WSA_NS, "Metadata");

	public void invoke(MessageContext ctxt) throws AxisFault
	{
		Vector<MessageElement> refParams = new Vector<MessageElement>();
		if (_logger.isTraceEnabled())
			_logger.trace("WSAddressingExtractor extracting EPR from header.");

		EndpointReferenceType epr = new EndpointReferenceType();

		SOAPMessage m = ctxt.getMessage();
		SOAPHeader header;
		try {
			header = m.getSOAPHeader();
		} catch (SOAPException se) {
			throw new AxisFault(se.getLocalizedMessage(), se);
		}
		Iterator<?> iter = header.examineAllHeaderElements();
		while (iter.hasNext()) {
			SOAPHeaderElement he = (SOAPHeaderElement) iter.next();
			QName heName = new QName(he.getNamespaceURI(), he.getLocalName());
			if (heName.equals(_WSA_METADATA_QName)) {
				epr.setMetadata((MetadataType) ObjectDeserializer.toObject(he, MetadataType.class));
				;
			} else if (heName.equals(_WSA_TO_QNAME)) {
				epr.setAddress(new AttributedURIType(he.getFirstChild().getNodeValue()));
				if (_logger.isDebugEnabled())
					_logger.debug("WSAddressingExtractor found target: \"" + epr.getAddress().get_value() + "\".");
			} else if (heName.equals(GenesisIIConstants.MYPROXY_QNAME)) {
				MyProxyCertificate.setPEMFormattedCertificate(he.getFirstChild().getNodeValue());
			} else {
				String isRefParam = he.getAttributeNS(_WSA_NS, "IsReferenceParameter");
				if (isRefParam != null) {
					if (isRefParam.equalsIgnoreCase("true") || isRefParam.equals("1")) {
						he.removeAttribute("actor");
						he.removeAttribute("mustUnderstand");
						refParams.add((MessageElement) he);
					}
				}
			}
		}

		if (refParams.size() > 0) {
			MessageElement[] referenceParameters = new MessageElement[refParams.size()];
			refParams.toArray(referenceParameters);
			epr.setReferenceParameters(new ReferenceParametersType(referenceParameters));
		}

		if (epr.getAddress() == null) {
			epr.setAddress(new AttributedURIType(Container.getCurrentServiceURL(ctxt)));
			if (_logger.isTraceEnabled())
				_logger.trace("WSAddressingExtractor setting target address to \"" + epr.getAddress().get_value() + "\".");
		}

		String shortParameterName = EPRUtils.getEPIShortParameter(epr);
		if (shortParameterName != null) {
			if (_logger.isTraceEnabled())
				_logger.trace(String.format("Found a shorthand epi of %s.  Looking it up.", shortParameterName));
			epr = EPRMapperService.lookup(shortParameterName);
		}

		ctxt.setProperty(AXIS_MESSAGE_CTXT_EPR_PROPERTY, epr);
	}

}
