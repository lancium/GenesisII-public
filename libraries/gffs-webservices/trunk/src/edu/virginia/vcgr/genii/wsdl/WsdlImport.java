package edu.virginia.vcgr.genii.wsdl;

import java.io.IOException;

import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class WsdlImport extends AbstractXMLComponent implements IXMLComponent
{
	private String _namespace;
	private String _location;
	private WsdlDocument _importedDocument = null;

	public WsdlImport(WsdlSourcePath sourcePath, IXMLComponent parent, Node wsdlImportNode) throws WsdlException
	{
		super(sourcePath, parent, wsdlImportNode);

		_namespace = WsdlUtils.getAttribute(_representedNode.getAttributes(), WsdlConstants.NAMESPACE_ATTR, true);
		_location = WsdlUtils.getAttribute(_representedNode.getAttributes(), WsdlConstants.LOCATION_ATTR, true);
	}

	public String getNamespace()
	{
		return _namespace;
	}

	public String getLocation()
	{
		return _location;
	}

	public WsdlDocument getImportedDocument() throws WsdlException, IOException, SAXException
	{
		if (_importedDocument == null) {
			_importedDocument = new WsdlDocument(_sourcePath.createRelative(_location));
		}

		return _importedDocument;
	}
}