package edu.virginia.vcgr.genii.ui.plugins.queue.resources;

import javax.swing.JComponent;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.ggf.rns.RNSEntryResponseType;
import org.ggf.rns.RNSMetadataType;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.queue.CurrentResourceInformation;
import edu.virginia.vcgr.genii.client.queue.QueueConstants;
import edu.virginia.vcgr.genii.ui.plugins.UIPluginContext;

class QueueResourceInformation
{
	static private Unmarshaller _unmarshaller;
	
	static
	{
		try
		{
			JAXBContext context = JAXBContext.newInstance(
				CurrentResourceInformation.class);
			_unmarshaller = context.createUnmarshaller();
		}
		catch (JAXBException e)
		{
			throw new RuntimeException(
				"Unable to create Queue resource info unmarshaller.", e);
		}
	}
	
	private JComponent _parent = null;
	private UIPluginContext _uiContext;
	private String _resourceName;
	private EndpointReferenceType _resourceEPR;
	private CurrentResourceInformation _resourceInfo = null;
	
	QueueResourceInformation(UIPluginContext uiContext,
		RNSEntryResponseType entry) throws JAXBException
	{
		_uiContext = uiContext;
		_resourceName = entry.getEntryName();
		_resourceEPR = entry.getEndpoint();
		
		RNSMetadataType mdt = entry.getMetadata();
		MessageElement []any = (mdt == null) ? null : mdt.get_any();
		if (any != null)
		{
			for (MessageElement e : any)
			{
				QName name = e.getQName();
				if (name.equals(
					QueueConstants.CURRENT_RESOURCE_INFORMATION_QNAME))
				{
					_resourceInfo = _unmarshaller.unmarshal(
						e, CurrentResourceInformation.class).getValue();
					break;
				}
			}
		}
		
		if (_resourceInfo == null)
			throw new IllegalArgumentException(String.format(
				"Resource %s has no resource information.", _resourceName));
	}
	
	final JComponent parent()
	{
		return _parent;
	}
	
	final void parent(JComponent parent)
	{
		_parent = parent;
	}
	
	final UIPluginContext uiContext()
	{
		return _uiContext;
	}
	
	final EndpointReferenceType endpoint()
	{
		return _resourceEPR;
	}
	
	final String name()
	{
		return _resourceName;
	}
	
	final CurrentResourceInformation resourceInformation()
	{
		return _resourceInfo;
	}
}