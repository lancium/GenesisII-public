package edu.virginia.vcgr.genii.cloud;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;

import org.apache.axis.message.MessageElement;

public class VMStats {



	private Collection<VMStat> _resources = null;
	
	public VMStats(Collection<VMStat> resources){
		_resources = resources;
	}
	
	public VMStats(){
		_resources = new ArrayList<VMStat>();
	}
	public Collection<VMStat> getResources() {
		return _resources;
	}

	public void setResources(Collection<VMStat> resources) {
		_resources = resources;
	}
	
	private void addResource(VMStat tStat){
		_resources.add(tStat);
	}
	public MessageElement toMessageElement(QName elementName)
	{
		MessageElement ret = new MessageElement(elementName);
	
		try
		{
			for (VMStat stat : _resources){
				ret.addChild(stat.toMessageElement(CloudConstants.VM_STATUS_ATTR));
			}

		}
		catch (SOAPException se)
		{
			throw new RuntimeException(
			"Unexpected exception thrown while packageing policy.");
		}

		return ret;
	}

	static public VMStats fromMessageElement(MessageElement element)
	{


		VMStats tStats = new VMStats();
		
		Iterator<?> iter = element.getChildElements();
		while (iter.hasNext())
		{
			MessageElement child = (MessageElement)iter.next();
			QName childName = child.getQName();
		
			if (childName.equals(
					new QName(CloudConstants.GENII_CLOUDBES_NS, CloudConstants.VM_STATUS_NAME)))
				tStats.addResource(VMStat.fromMessageElement(child));
				 
		}

		return tStats;
		
	}
	
}
