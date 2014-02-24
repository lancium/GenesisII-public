package edu.virginia.vcgr.genii.client.comm.axis;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Vector;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;

import org.apache.axis.AxisFault;
import org.apache.axis.MessageContext;
import org.apache.axis.message.MessageElement;
import org.apache.axis.message.SOAPHeaderElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.axis.security.WSDoAllReceiver;
import org.morgan.util.GUID;
import org.ws.addressing.AttributedURIType;
import org.ws.addressing.EndpointReferenceType;
import org.ws.addressing.MetadataType;
import org.ws.addressing.ReferenceParametersType;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.invoke.handlers.MyProxyCertificate;
import edu.virginia.vcgr.genii.client.logging.DLogDatabase;
import edu.virginia.vcgr.genii.client.logging.DLogUtils;
import edu.virginia.vcgr.genii.client.ser.ObjectDeserializer;

public class DLogHierarchySender extends WSDoAllReceiver
{
	static final long serialVersionUID = 0L;

	static private Log _logger = LogFactory.getLog(DLogHierarchySender.class);
	static private final String _FLOW_SIDE_KEY = "flow-side";
	static private final String _FLOW_SIDE_REQUEST_VALUE = "request";
	static private final String _FLOW_SIDE_RESPONSE_VALUE = "response";

	private DLogDatabase database = null;

	private Boolean _isRequest = null;

	private static final String TEMP_SESSION_RPC_ID = "TEMP_SESSION_RPC_ID";

	private boolean isRequest() throws AxisFault
	{
		synchronized (this) {
			if (_isRequest == null) {
				AxisFault fault = null;

				String value = (String) getOption(_FLOW_SIDE_KEY);
				if (value != null) {
					if (value.equals(_FLOW_SIDE_REQUEST_VALUE))
						_isRequest = Boolean.TRUE;
					else if (value.equals(_FLOW_SIDE_RESPONSE_VALUE))
						_isRequest = Boolean.FALSE;
					else
						fault =
							new AxisFault(_FLOW_SIDE_KEY + " property not recognized.  Expected " + _FLOW_SIDE_REQUEST_VALUE
								+ " or " + _FLOW_SIDE_RESPONSE_VALUE);
				} else {
					fault = new AxisFault("Couldn't find " + _FLOW_SIDE_KEY + " parameter.");
				}

				if (fault != null)
					throw fault;
			}
		}

		return _isRequest.booleanValue();
	}

	public DLogHierarchySender()
	{

	}

	static private String _WSA_NS = EndpointReferenceType.getTypeDesc().getXmlType().getNamespaceURI();
	static private QName _WSA_TO_QNAME = new QName(_WSA_NS, "To");
	static private QName _WSA_METADATA_QName = new QName(_WSA_NS, "Metadata");

	private EndpointReferenceType extractEPR(MessageContext ctxt) throws AxisFault
	{
		Vector<MessageElement> refParams = new Vector<MessageElement>();
		_logger.trace("DLog extracting EPR from header.");

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
			} else if (heName.equals(_WSA_TO_QNAME)) {
				epr.setAddress(new AttributedURIType(he.getFirstChild().getNodeValue()));
				_logger.trace("DLog found target: \"" + epr.getAddress().get_value() + "\".");
			} else if (heName.equals(GenesisIIConstants.MYPROXY_QNAME)) {
				MyProxyCertificate.setPEMFormattedCertificate(he.getFirstChild().getNodeValue());
			} else {
				String isRefParam = he.getAttributeNS(_WSA_NS, "IsReferenceParameter");
				if (isRefParam != null) {
					if (isRefParam.equalsIgnoreCase("true") || isRefParam.equals("1")) {
						he.removeAttribute("actor");
						he.removeAttribute("mustUnderstand");
						refParams.add(he);
					}
				}
			}
		}

		if (refParams.size() > 0) {
			epr.setReferenceParameters(new ReferenceParametersType((MessageElement[])refParams.toArray()));
		}

		return epr;
	}

	public void invoke(MessageContext msgContext) throws AxisFault
	{
		if (isRequest()) {
			_logger.trace("Sending a request...");
			if (database == null) {
				database = DLogUtils.getDBConnector();
				if (database == null)
					return;
			}
			try {
				String tempID = new GUID().toString();
				SOAPMessage msg = msgContext.getMessage();
				msg.setProperty(TEMP_SESSION_RPC_ID, tempID);
				byte[] bytes = msgContext.getRequestMessage().getSOAPPartAsBytes();
				EndpointReferenceType epr = extractEPR(msgContext);
				String op = msgContext.getOperation().getName();
				database.recordMeta1(tempID, bytes, epr, op);
			} catch (SOAPException e) {
				_logger.error("Problem retrieving SOAP body from request message", e);
			} catch (SQLException e) {
				_logger.error("Problem putting request metadata into database", e);
			} catch (IOException e) {
				_logger.error("Couldn't serialize request message for database", e);
			}
		} else {
			try {
				SOAPMessage msg = msgContext.getMessage();
				@SuppressWarnings("unchecked")
				Iterator<SOAPHeaderElement> rpcids = msg.getSOAPHeader().getChildElements(GenesisIIConstants.RPC_ID_QNAME);

				if (rpcids.hasNext()) {
					String rpcid = rpcids.next().getValue();
					_logger.trace("Got a response. RPCID is " + rpcid);
					if (database == null) {
						database = DLogUtils.getDBConnector();
						if (database == null)
							return;
					}
					database.recordRPCID(rpcid);

					String tempID = (String) msgContext.getRequestMessage().getProperty(TEMP_SESSION_RPC_ID);
					byte[] bytes = msgContext.getResponseMessage().getSOAPPartAsBytes();
					database.recordMeta2(tempID, bytes, rpcid);
				} else {
					_logger.trace("Got a response, no RPCID included");
				}

			} catch (SOAPException e) {
				_logger.error("Problem retrieving SOAP body from response message", e);
			} catch (SQLException e) {
				_logger.error("Problem putting response metadata into database", e);
			} catch (IOException e) {
				_logger.error("Couldn't serialize response message for database", e);
			}
		}
	}
}
