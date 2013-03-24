package edu.virginia.vcgr.genii.container.cservices.wsn;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "WSNNotificationConfiguration")
@XmlAccessorType(XmlAccessType.NONE)
public class WSNotificationConfiguration
{
	@XmlAttribute(name = "num-threads", required = true)
	private int _numThreads;

	final int numThreads()
	{
		return _numThreads;
	}
}