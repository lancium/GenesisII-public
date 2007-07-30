package edu.virginia.vcgr.genii.wsdl.ant;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;

import org.apache.tools.ant.BuildException;

import edu.virginia.vcgr.genii.wsdl.WsdlDocument;
import edu.virginia.vcgr.genii.wsdl.WsdlException;
import edu.virginia.vcgr.genii.wsdl.WsdlUtils;

public class GenesisIIServiceGenerator extends GenesisIIGWsdlNormalizer
{
	static private final String _PORT_TYPE_ATTR = "portType";
	
	@SuppressWarnings("unused")
	static private final String _BINDING_NAME_ATTR = "bindingName";
	@SuppressWarnings("unused")
	static private final String _SERVICE_NAME_ATTR = "serviceName";
	
	protected QName _portType = null;
	protected String _bindingName = null;
	protected String _serviceName = null;
	
	public void setPortType(String str) throws BuildException
	{
		try
		{
			_portType = WsdlUtils.getQNameFromString(str);
		}
		catch (WsdlException we)
		{
			throw new BuildException(we.getLocalizedMessage(), we);
		}
	}
	
	public void setBindingName(String bindingName)
	{
		_bindingName = bindingName;
	}
	
	public void setServiceName(String serviceName)
	{
		_serviceName = serviceName;
	}
	
	static private Pattern _PORT_TYPE_PATTERN = Pattern.compile(
		"^(.+)PortType$", Pattern.CASE_INSENSITIVE);
	
	public void validate() throws BuildException
	{
		super.validate();
		
		if (_portType == null)
			throw new BuildException("\"" + _PORT_TYPE_ATTR + "\" attribute not set.");

		Matcher matcher;
		String portTypeLocal = _portType.getLocalPart();
		matcher = _PORT_TYPE_PATTERN.matcher(portTypeLocal);
		if (matcher.matches())
			portTypeLocal = matcher.group(1);
		
		if (_bindingName == null)
			_bindingName = portTypeLocal + "SOAPBinding";
		if (_serviceName == null)
			_serviceName = portTypeLocal + "Service";
	}
	
	protected void internalExecute() throws BuildException
	{
		try
		{
			WsdlDocument doc = new WsdlDocument(_source);
			doc.normalize();
			doc.generateBindingAndService(
				_serviceName, _bindingName, _portType, _target);
		}
		catch (Exception e)
		{
			throw new BuildException(e.getLocalizedMessage(), e);
		}
	}
}