package edu.virginia.vcgr.appmgr.patch;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Properties;
import java.util.jar.JarFile;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import edu.virginia.vcgr.appmgr.launcher.ApplicationDescription;
import edu.virginia.vcgr.appmgr.os.OperatingSystemType;
import edu.virginia.vcgr.appmgr.os.ProcessorArchitecture;
import edu.virginia.vcgr.appmgr.util.ElementIterable;
import edu.virginia.vcgr.appmgr.util.XMLUtilities;

public class PatchDescriptionParser
{
	static public final String PATCH_NS = "http://edu.virginia.vcgr/patch";
	static public final String JSDL_NS = "http://schemas.ggf.org/jsdl/2005/11/jsdl";

	static public final String PATCHES_ELEMENT_NAME = "patches";
	static public final String PATCH_ELEMENT_NAME = "patch";
	static public final String RESTRICTIONS_ELEMENT_NAME = "restrictions";
	static public final String HOST_ELEMENT_NAME = "host";
	static public final String PATTERN_ELEMENT_NAME = "pattern";
	static public final String HOSTNAME_ELEMENT_NAME = "hostname";
	static public final String WRITE_ELEMENT_NAME = "write";
	static public final String DELETE_ELEMENT_NAME = "delete";
	static public final String RUN_ELEMENT_NAME = "run";
	static public final String PROPERTY_ELEMENT_NAME = "property";

	static public final String OPERATING_SYSTEM_ELEMENT_NAME = "OperatingSystem";
	static public final String OPERATING_SYSTEM_TYPE_ELEMENT_NAME = "OperatingSystemType";
	static public final String OPERATING_SYSTEM_NAME_ELEMENT_NAME = "OperatingSystemName";
	static public final String OPERATING_SYSTEM_VERSION_ELEMENT_NAME = "OperatingSystemVersion";
	static public final String CPU_ARCHITECTURE_ELEMENT_NAME = "CPUArchitecture";
	static public final String CPU_ARCHITECTURE_NAME_ELEMENT_NAME = "CPUArchitectureName";

	static public final QName PATCHES_ELEMENT = new QName(PATCH_NS, PATCHES_ELEMENT_NAME);
	static public final QName PATCH_ELEMENT = new QName(PATCH_NS, PATCH_ELEMENT_NAME);
	static public final QName RESTRICTIONS_ELEMENT = new QName(PATCH_NS, RESTRICTIONS_ELEMENT_NAME);
	static public final QName HOST_ELEMENT = new QName(PATCH_NS, HOST_ELEMENT_NAME);
	static public final QName PATTERN_ELEMENT = new QName(PATCH_NS, PATTERN_ELEMENT_NAME);
	static public final QName HOSTNAME_ELEMENT = new QName(PATCH_NS, HOSTNAME_ELEMENT_NAME);
	static public final QName WRITE_ELEMENT = new QName(PATCH_NS, WRITE_ELEMENT_NAME);
	static public final QName DELETE_ELEMENT = new QName(PATCH_NS, DELETE_ELEMENT_NAME);
	static public final QName RUN_ELEMENT = new QName(PATCH_NS, RUN_ELEMENT_NAME);
	static public final QName PROPERTY_ELEMENT = new QName(PATCH_NS, PROPERTY_ELEMENT_NAME);

	static public final QName OPERATING_SYSTEM_ELEMENT = new QName(JSDL_NS, OPERATING_SYSTEM_ELEMENT_NAME);
	static public final QName OPERATING_SYSTEM_TYPE_ELEMENT = new QName(JSDL_NS, OPERATING_SYSTEM_TYPE_ELEMENT_NAME);
	static public final QName OPERATING_SYSTEM_NAME_ELEMENT = new QName(JSDL_NS, OPERATING_SYSTEM_NAME_ELEMENT_NAME);
	static public final QName OPERATING_SYSTEM_VERSION_ELEMENT = new QName(JSDL_NS, OPERATING_SYSTEM_VERSION_ELEMENT_NAME);
	static public final QName CPU_ARCHITECTURE_ELEMENT = new QName(JSDL_NS, CPU_ARCHITECTURE_ELEMENT_NAME);
	static public final QName CPU_ARCHITECTURE_NAME_ELEMENT = new QName(JSDL_NS, CPU_ARCHITECTURE_NAME_ELEMENT_NAME);

	static public final String USE_IP_ATTRIBUTE = "use-ip";
	static public final String PERMISSIONS_ATTRIBUTE = "permissions";
	static public final String PAR_FILE_ATTRIBUTE = "par-file";
	static public final String CLASS_ATTRIBUTE = "class";
	static public final String NAME_ATTRIBUTE = "name";
	static public final String VALUE_ATTRIBUTE = "value";

	static private OperatingSystemType parseOperatingSystemType(Element osTypeElement) throws SAXException
	{
		String osName = null;

		for (Element child : new ElementIterable(osTypeElement.getChildNodes())) {
			QName childName = XMLUtilities.getQName(child);
			if (childName.equals(OPERATING_SYSTEM_NAME_ELEMENT)) {
				if (osName != null)
					throw new SAXException(String.format("Only one %s is permitted as a child of a %s element.",
						OPERATING_SYSTEM_NAME_ELEMENT, OPERATING_SYSTEM_TYPE_ELEMENT));

				osName = XMLUtilities.getTextContent(child);
			} else
				throw new SAXException(String.format("Unrecognized element %s in patch.", childName));
		}

		if (osName == null)
			throw new SAXException(String.format("Missing required %s element inside of %s element.",
				OPERATING_SYSTEM_NAME_ELEMENT, OPERATING_SYSTEM_TYPE_ELEMENT));

		return OperatingSystemType.valueOf(osName);
	}

	static private ProcessorArchitecture parseCPUArchitecture(Element cpuArchElement) throws SAXException
	{
		String archName = null;

		for (Element child : new ElementIterable(cpuArchElement.getChildNodes())) {
			QName childName = XMLUtilities.getQName(child);
			if (childName.equals(CPU_ARCHITECTURE_NAME_ELEMENT)) {
				if (archName != null)
					throw new SAXException(String.format("Only one %s is permitted as a child of a %s element.",
						CPU_ARCHITECTURE_NAME_ELEMENT, CPU_ARCHITECTURE_ELEMENT));

				archName = XMLUtilities.getTextContent(child);
			} else
				throw new SAXException(String.format("Unrecognized element %s in patch.", childName));
		}

		if (archName == null)
			throw new SAXException(String.format("Missing required %s element inside of %s element.",
				CPU_ARCHITECTURE_NAME_ELEMENT, CPU_ARCHITECTURE_ELEMENT));

		return ProcessorArchitecture.valueOf(archName);
	}

	static private void parseOperatingSystem(Element osElement, PatchRestrictions restrictions) throws SAXException
	{
		for (Element child : new ElementIterable(osElement.getChildNodes())) {
			QName childName = XMLUtilities.getQName(child);
			if (childName.equals(OPERATING_SYSTEM_TYPE_ELEMENT)) {
				restrictions.setOperatingSystemTypeRestriction(parseOperatingSystemType(child));
			} else if (childName.equals(OPERATING_SYSTEM_VERSION_ELEMENT)) {
				restrictions.setOperatingSystemVersionRestriction(XMLUtilities.getTextContent(child));
			} else
				throw new SAXException(String.format("Unrecognized element %s in patch.", childName));
		}
	}

	static private HostRestriction parseHostRestriction(Element hostElement) throws SAXException
	{
		String useIPString = XMLUtilities.getAttribute(hostElement, USE_IP_ATTRIBUTE, "false");
		boolean useIP = Boolean.parseBoolean(useIPString);

		int count = 0;
		String pattern = null;
		String hostname = null;

		for (Element child : new ElementIterable(hostElement.getChildNodes())) {
			QName childName = XMLUtilities.getQName(child);
			if (childName.equals(HOSTNAME_ELEMENT)) {
				if (hostname != null)
					throw new SAXException(String.format("Only one %s element is allowed as a child of a %s element.",
						HOSTNAME_ELEMENT, HOST_ELEMENT));

				count++;
				hostname = XMLUtilities.getTextContent(child);
			} else if (childName.equals(PATTERN_ELEMENT)) {
				if (pattern != null)
					throw new SAXException(String.format("Only one %s element is allowed as a child of a %s element.",
						PATTERN_ELEMENT, HOST_ELEMENT));

				count++;
				pattern = XMLUtilities.getTextContent(child);
			} else
				throw new SAXException(String.format("Unrecognized element %s in patch.", childName));
		}

		if (count < 1)
			throw new SAXException(String.format("%s element requires a child element.", HOST_ELEMENT));
		if (count > 1)
			throw new SAXException(String.format("%s must have only one child element.", HOST_ELEMENT));

		if (hostname != null)
			return useIP ? HostRestriction.restrictToIPAddress(hostname) : HostRestriction.restrictToHostname(hostname);
		else
			return useIP ? HostRestriction.restrictToIPAddressPattern(pattern) : HostRestriction
				.restrictToHostnamePattern(pattern);
	}

	static private void parseRestrictions(Element restrictionsElement, PatchRestrictions restrictions) throws SAXException
	{
		for (Element child : new ElementIterable(restrictionsElement.getChildNodes())) {
			QName childName = XMLUtilities.getQName(child);
			if (childName.equals(OPERATING_SYSTEM_ELEMENT)) {
				parseOperatingSystem(child, restrictions);
			} else if (childName.equals(CPU_ARCHITECTURE_ELEMENT)) {
				restrictions.setProcessorArchitectureRestriction(parseCPUArchitecture(child));
			} else if (childName.equals(HOST_ELEMENT)) {
				restrictions.setHostRestriction(parseHostRestriction(child));
			} else {
				throw new SAXException(String.format("Unrecognized element %s in patch.", childName));
			}
		}
	}

	static private PatchOperation parseWriteElement(OperationFactory opFactory, PatchRestrictions restrictions,
		Element writeElement) throws SAXException
	{
		String relativePath = XMLUtilities.getTextContent(writeElement);
		String permissions = XMLUtilities.getAttribute(writeElement, PERMISSIONS_ATTRIBUTE, null);

		return opFactory.createWriteOperation(restrictions, permissions, relativePath);
	}

	static private PatchOperation parseDeleteElement(OperationFactory opFactory, PatchRestrictions restrictions,
		Element deleteElement) throws SAXException
	{
		String relativePath = XMLUtilities.getTextContent(deleteElement);

		return opFactory.createDeleteOperation(restrictions, relativePath);
	}

	static private Properties parseProperties(Element parent) throws SAXException
	{
		Properties ret = new Properties();

		for (Element child : new ElementIterable(parent.getChildNodes())) {
			QName childName = XMLUtilities.getQName(child);
			if (!childName.equals(PROPERTY_ELEMENT))
				throw new SAXException(String.format("Only %s elements are permitted as children of a %s element.",
					PROPERTY_ELEMENT, XMLUtilities.getQName(parent)));

			ret.setProperty(XMLUtilities.getRequiredAttribute(child, NAME_ATTRIBUTE),
				XMLUtilities.getRequiredAttribute(child, VALUE_ATTRIBUTE));
		}

		return ret;
	}

	static private PatchOperation
		parseRunElement(OperationFactory opFactory, PatchRestrictions restrictions, Element runElement) throws SAXException
	{
		String parFile = XMLUtilities.getRequiredAttribute(runElement, PAR_FILE_ATTRIBUTE);
		String className = XMLUtilities.getRequiredAttribute(runElement, CLASS_ATTRIBUTE);

		return opFactory.createRunOperation(restrictions, parFile, className, parseProperties(runElement));
	}

	static private void parsePatch(OperationFactory opFactory, Collection<PatchOperation> operations, Element patchElement)
		throws SAXException
	{
		PatchRestrictions restrictions = new PatchRestrictions();

		for (Element child : new ElementIterable(patchElement.getChildNodes())) {
			QName childName = XMLUtilities.getQName(child);
			if (childName.equals(RESTRICTIONS_ELEMENT)) {
				parseRestrictions(child, restrictions);
			} else if (childName.equals(WRITE_ELEMENT)) {
				operations.add(parseWriteElement(opFactory, restrictions, child));
			} else if (childName.equals(DELETE_ELEMENT)) {
				operations.add(parseDeleteElement(opFactory, restrictions, child));
			} else if (childName.equals(RUN_ELEMENT)) {
				operations.add(parseRunElement(opFactory, restrictions, child));
			} else
				throw new SAXException(String.format("Unrecognized element %s in patch.", childName));
		}
	}

	static public Collection<PatchOperation> parse(JarFile patchFile, ApplicationDescription applicationDescription,
		InputStream in) throws ParserConfigurationException, SAXException, IOException
	{
		OperationFactory opFactory = new OperationFactory(applicationDescription, patchFile);
		Collection<PatchOperation> operations = new LinkedList<PatchOperation>();

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(in);
		Element docE = doc.getDocumentElement();
		QName docElementName = XMLUtilities.getQName(docE);
		if (!docElementName.equals(PATCHES_ELEMENT))
			throw new SAXException(String.format("Document element must be %s.", PATCHES_ELEMENT));

		for (Element child : new ElementIterable(docE.getChildNodes())) {
			QName childName = XMLUtilities.getQName(child);
			if (!childName.equals(PATCH_ELEMENT))
				throw new SAXException(String.format("Found element %s while looking for %s.", childName, PATCH_ELEMENT));

			parsePatch(opFactory, operations, child);
		}

		return operations;
	}

	static public void main(String[] args) throws Throwable
	{
		InputStream in = PatchDescriptionParser.class.getResourceAsStream("example-patch-description.xml");
		parse(null, null, in);
		in.close();
	}
}