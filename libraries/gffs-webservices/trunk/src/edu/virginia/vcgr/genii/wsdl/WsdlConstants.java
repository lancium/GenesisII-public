package edu.virginia.vcgr.genii.wsdl;

import javax.xml.namespace.QName;

public class WsdlConstants
{
	/* Namespaces */
	static public final String WSDL_NS = "http://schemas.xmlsoap.org/wsdl/";
	static public final String GENII_EXT_NS = "http://vcgr.cs.virginia.edu/genii/2006/12/wsdl-extensions";
	static public final String WS_ADDRESSING_NS = "http://www.w3.org/2005/08/addressing";
	static public final String SOAP_NS = "http://schemas.xmlsoap.org/wsdl/soap/";

	/* Element Names */
	static public final String DEFINITIONS = "definitions";
	static public final String IMPORT = "import";
	static public final String PORT_TYPE = "portType";
	static public final String OPERATION = "operation";
	static public final String INPUT = "input";
	static public final String OUTPUT = "output";
	static public final String FAULT = "fault";
	static public final String EXTEND = "extend";
	static public final String ACTION = "Action";

	/* Element QNames */
	static public QName DEFINITIONS_QNAME = new QName(WSDL_NS, DEFINITIONS);
	static public QName IMPORT_QNAME = new QName(WSDL_NS, IMPORT);
	static public QName PORT_TYPE_QNAME = new QName(WSDL_NS, PORT_TYPE);
	static public QName OPERATION_QNAME = new QName(WSDL_NS, OPERATION);
	static public QName INPUT_QNAME = new QName(WSDL_NS, INPUT);
	static public QName OUTPUT_QNAME = new QName(WSDL_NS, OUTPUT);
	static public QName FAULT_QNAME = new QName(WSDL_NS, FAULT);
	static public QName EXTEND_QNAME = new QName(GENII_EXT_NS, EXTEND);
	static public QName ACTION_QNAME = new QName(WS_ADDRESSING_NS, ACTION);

	/* Attribute Names */
	static public final String NAME_ATTR = "name";
	static public final String TARGET_NAMESPACE_ATTR = "targetNamespace";
	static public final String NAMESPACE_ATTR = "namespace";
	static public final String LOCATION_ATTR = "location";
	static public final String MESSAGE_ATTR = "message";
	static public final String PORT_TYPE_ATTR = "portType";
}