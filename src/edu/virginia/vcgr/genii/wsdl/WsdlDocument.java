package edu.virginia.vcgr.genii.wsdl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.morgan.util.io.StreamUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class WsdlDocument extends AbstractXMLComponent implements IXMLComponent
{
	private Document _rootDocument;
	private String _name;
	private HashMap<String, WsdlImport> _imports =
		new HashMap<String, WsdlImport>();
	private HashMap<QName, WsdlPortType> _portTypes = 
		new HashMap<QName, WsdlPortType>();
	
	public WsdlDocument(WsdlSourcePath sourcePath) 
		throws WsdlException, IOException, SAXException
	{
		super(sourcePath, null, null);
		
		parse();
	}
	
	public WsdlDocument(File wsdlFile) throws WsdlException, IOException, SAXException
	{
		super(new WsdlSourcePath(wsdlFile), null, null);
		
		parse();
	}
	
	public WsdlDocument(URL wsdlURL) throws WsdlException, IOException, SAXException
	{
		super(new WsdlSourcePath(wsdlURL), null, null);
		
		parse();
	}
	
	public WsdlDocument(InputStream input) 
		throws WsdlException, IOException, SAXException
	{
		super(null, null, null);
		
		parse(input);
	}
	
	public WsdlDocument(String path) throws WsdlException, IOException, SAXException
	{
		super(new WsdlSourcePath(path), null, null);
		
		parse();
	}
	
	protected void parse() throws WsdlException, SAXException, IOException
	{
		InputStream in = null;
		try
		{
			in = _sourcePath.open();
			parse(in);
		}
		finally
		{
			StreamUtils.close(in);
		}	
	}
	
	protected void parse(InputStream in) throws WsdlException, SAXException, IOException
	{
		DocumentBuilderFactory factory;
		DocumentBuilder builder;
		
		try
		{
			factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			builder = factory.newDocumentBuilder();
			_rootDocument = builder.parse(in);
			_representedNode = _rootDocument.getDocumentElement();
			understandNode();
		}
		catch (ParserConfigurationException pce)
		{
			throw new WsdlException(pce.getLocalizedMessage(), pce);
		}
	}
	
	protected void understandNode() throws WsdlException
	{
		QName nodeName = WsdlUtils.getQName(_representedNode);
		if (!nodeName.equals(WsdlConstants.DEFINITIONS_QNAME))
			throw new WsdlException("Expected " + WsdlConstants.DEFINITIONS_QNAME
				+ " element.");
		
		_name = WsdlUtils.getAttribute(_representedNode.getAttributes(),
			WsdlConstants.NAME_ATTR, true);
		
		NodeList children = _representedNode.getChildNodes();
		for (int lcv = 0; lcv < children.getLength(); lcv++)
		{
			Node child = children.item(lcv);
			if (child.getNodeType() == Node.ELEMENT_NODE)
			{
				QName childName = WsdlUtils.getQName(child);
				
				if (childName.equals(WsdlConstants.IMPORT_QNAME))
				{
					WsdlImport imp = new WsdlImport(_sourcePath, this, child);
					_imports.put(imp.getNamespace(), imp);
				} else if (childName.equals(WsdlConstants.PORT_TYPE_QNAME))
				{
					WsdlPortType portType = new WsdlPortType(_sourcePath, this, 
						child);
					_portTypes.put(portType.getName(), portType);
				}
			}
		}
	}
	
	public String getName()
	{
		return _name;
	}
	
	public WsdlPortType findPortType(QName portTypeName) throws WsdlException
	{
		WsdlPortType portType = _portTypes.get(portTypeName);
		if (portType == null)
		{
			WsdlImport imp = _imports.get(portTypeName.getNamespaceURI());
			if (imp != null)
			{
				try
				{
					WsdlDocument importedDoc = imp.getImportedDocument();
					portType = importedDoc.findPortType(portTypeName);
				}
				catch (SAXException se)
				{
					throw new WsdlException(se.getLocalizedMessage(), se);
				}
				catch (IOException ioe)
				{
					throw new WsdlException(ioe.getLocalizedMessage(), ioe);
				}
			}
		}
		
		if (portType == null)
			throw new WsdlException("Unable to find port type " + portTypeName);
		
		return portType;
	}
	
	public void write(File outputFile) throws WsdlException, IOException
	{
		writeDocument(_rootDocument, outputFile);
	}
	
	public void normalize() throws WsdlException
	{
		for (WsdlPortType portType : _portTypes.values())
		{
			portType.normalize();
		}
	}
	
	public void generateBindingAndService(
		String serviceName, String bindingName, QName portType, File targetFile)
			throws IOException, WsdlException
	{
		Document newDoc = (Document)_rootDocument.cloneNode(true);
		stripDocument(newDoc);
		
		newDoc.getDocumentElement().appendChild(createLocalImportElement(newDoc,
			_sourcePath));
		newDoc.getDocumentElement().appendChild(createBindingElement(newDoc,
			bindingName, portType));
		newDoc.getDocumentElement().appendChild(createServiceElement(newDoc,
			serviceName, portType.getLocalPart(), bindingName));
		
		writeDocument(newDoc, targetFile);
	}
	
	static private void stripDocument(Document doc)
	{
		Element docElement = doc.getDocumentElement();
		NodeList children = docElement.getChildNodes();
		for (int lcv = 0; lcv < children.getLength(); lcv++)
		{
			Node child = children.item(lcv);
			if (child.getNodeType() == Node.ELEMENT_NODE)
			{
				QName childName = WsdlUtils.getQName(child);
				if (childName.equals(WsdlConstants.IMPORT_QNAME))
					continue;
				
				docElement.removeChild(child);
			} else if (child.getNodeType() == Node.COMMENT_NODE)
				docElement.removeChild(child);
		}	
	}
	
	static private void writeDocument(Document doc, File targetFile)
		throws IOException, WsdlException
	{
		FileOutputStream out = null;
		synchronized(WsdlDocument.class)
		{
			String value = System.getProperty("javax.xml.transform.TransformerFactory");
			try
			{
				System.setProperty("javax.xml.transform.TransformerFactory",
					"com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl");
				TransformerFactory tFactory = TransformerFactory.newInstance();
				Transformer transformer = tFactory.newTransformer();
				
				out = new FileOutputStream(targetFile);
				transformer.transform(new DOMSource(doc), new StreamResult(out));
			} 
			catch (TransformerException te)
			{
				throw new WsdlException(te.getLocalizedMessage(), te);
			}
			finally
			{
				if (value != null)
					System.setProperty("javax.xml.transform.TransformerFactory", value);
				StreamUtils.close(out);
			}
		}
	}
	
	private Element createBindingElement(Document ownerDoc,
		String bindingName, QName portType) throws WsdlException
	{
		Element bindingElement = ownerDoc.createElementNS(
			WsdlConstants.WSDL_NS, 
			ownerDoc.lookupPrefix(WsdlConstants.WSDL_NS) + ":" + "binding");
		bindingElement.setAttribute("name", bindingName);
		bindingElement.setAttribute("type",
			ownerDoc.lookupPrefix(portType.getNamespaceURI()) + ":" +
			portType.getLocalPart());
		
		bindingElement.appendChild(createSoapBindingElement(ownerDoc));
		
		WsdlPortType wPortType = _portTypes.get(portType);
		if (wPortType == null)
			throw new WsdlException("Couldn't find port type " + portType);
		
		for (WsdlOperation oper : wPortType.getOperations())
		{
			bindingElement.appendChild(createOperationElement(ownerDoc,
				oper));
		}
		
		return bindingElement;
	}
	
	static private Element createSoapBindingElement(Document ownerDoc)
	{
		Element soapBindingElement = ownerDoc.createElementNS(
			WsdlConstants.SOAP_NS,
			"soap:binding");
		soapBindingElement.setAttribute("style", "document");
		soapBindingElement.setAttribute("transport", 
			"http://schemas.xmlsoap.org/soap/http");
		
		return soapBindingElement;
	}
	
	private Element createServiceElement(Document ownerDoc,
			String serviceName, String portName, String bindingName)
	{
		Element serviceElement = ownerDoc.createElementNS(
			WsdlConstants.WSDL_NS, "wsdl:service");
		serviceElement.setAttribute("name", serviceName);
		
		Element portElement = ownerDoc.createElementNS(
			WsdlConstants.WSDL_NS, "wsdl:port");
		portElement.setAttribute("name", portName);
		portElement.setAttribute("binding",
			ownerDoc.lookupPrefix(findTargetNamespace()) + ":" +
			bindingName);
		
		Element addressElement = ownerDoc.createElementNS(
			WsdlConstants.SOAP_NS, "soap:address");
		addressElement.setAttribute("location", "http://localhost:8080/wsrf/services");
		portElement.appendChild(addressElement);
		
		serviceElement.appendChild(portElement);
		
		return serviceElement;
	}
	
	static private Element createOperationElement(Document ownerDoc,
		WsdlOperation oper)
	{
		Element operationElement = ownerDoc.createElementNS(
			WsdlConstants.WSDL_NS, "wsdl:" + WsdlConstants.OPERATION);
		operationElement.setAttribute("name", oper.getOperationName());
		
		WsdlInputOutput io = oper.getInput();
		if (io != null)
		{
			String action = io.getSoapAction();
			if (action != null)
			{
				Element soapAction = ownerDoc.createElementNS(
					WsdlConstants.SOAP_NS, "soap:operation");
				soapAction.setAttribute("soapAction", action);
				operationElement.appendChild(soapAction);
			}
			operationElement.appendChild(createInputOutputElement(
				ownerDoc, io, "input"));
		}
		io = oper.getOutput();
		if (io != null)
			operationElement.appendChild(createInputOutputElement(
				ownerDoc, io, "output"));
		
		for (WsdlFault fault : oper.getFaults())
		{
			operationElement.appendChild(createFaultElement(ownerDoc, fault));
		}
		
		return operationElement;
	}
	
	static private Element createInputOutputElement(
		Document ownerDoc, WsdlInputOutput io, String elementName)
	{
		Element ioElement = ownerDoc.createElementNS(
			WsdlConstants.WSDL_NS, "wsdl:" + elementName);
		Element body = ownerDoc.createElementNS(
			WsdlConstants.SOAP_NS, "soap:body");
		body.setAttribute("use", "literal");
		ioElement.appendChild(body);
		
		return ioElement;
	}
	
	static private Element createFaultElement(Document ownerDoc, WsdlFault fault)
	{
		Element faultElement = ownerDoc.createElementNS(
			WsdlConstants.WSDL_NS, "wsdl:fault");
		faultElement.setAttribute("name", fault.getFaultName());
		Element body = ownerDoc.createElementNS(
			WsdlConstants.SOAP_NS, "soap:fault");
		body.setAttribute("use", "literal");
		faultElement.appendChild(body);
		
		return faultElement;
	}
	
	private Element createLocalImportElement(
		Document ownerDoc, WsdlSourcePath sourcePath)
	{
		Element importElement = ownerDoc.createElementNS(
			WsdlConstants.WSDL_NS, "wsdl:import");
		importElement.setAttribute("namespace", findTargetNamespace());
		importElement.setAttribute("location", sourcePath.getPath());
		
		return importElement;
	}
}