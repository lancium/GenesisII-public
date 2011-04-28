package edu.virginia.vcgr.genii.client.naming.eprbuild;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.apache.axis.message.MessageElement;
import org.morgan.util.GUID;
import org.w3c.dom.Element;

import edu.virginia.vcgr.genii.client.container.ContainerConstants;
import edu.virginia.vcgr.genii.client.ogsa.OGSAWSRFBPConstants;
import edu.virginia.vcgr.genii.client.resource.PortType;

public class GenesisIIEPRBuilder extends OGSAEPRBuilder
{
	private Set<PortType> _portTypes = new HashSet<PortType>();
	private GUID _containerID = null;
	
	public GenesisIIEPRBuilder(URI address)
	{
		super(address);
	}
	
	final public GUID containerID()
	{
		return _containerID;
	}
	
	final public void containerID(GUID containerID)
	{
		_containerID = containerID;
	}
	
	final public void addPortTypes(PortType...pt)
	{
		for (PortType p : pt)
			_portTypes.add(p);
	}
	
	final public Set<PortType> portTypes()
	{
		return Collections.unmodifiableSet(_portTypes);
	}

	@Override
	public Collection<Element> metadata()
	{
		Collection<Element> ret = super.metadata();
		
		if (ret != null)
			ret = new LinkedList<Element>(ret);
		else
			ret = new LinkedList<Element>();
		
		/* Add port types */
		ret.add(new MessageElement(
			OGSAWSRFBPConstants.WS_RESOURCE_INTERFACES_ATTR_QNAME,
			PortType.translate(_portTypes)));
		
		/* Add container ID */
		if (_containerID != null)
			ret.add(new MessageElement(
				ContainerConstants.CONTAINER_ID_METADATA_ELEMENT,
				_containerID.toString()));
		
		return Collections.unmodifiableCollection(ret);
	}
}