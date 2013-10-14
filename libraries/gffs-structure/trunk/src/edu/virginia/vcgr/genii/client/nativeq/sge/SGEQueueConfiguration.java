package edu.virginia.vcgr.genii.client.nativeq.sge;

import javax.xml.bind.annotation.XmlRootElement;

import edu.virginia.vcgr.genii.client.nativeq.CommonScriptBasedQueueConfiguration;
import edu.virginia.vcgr.genii.client.nativeq.NativeQueueConfiguration;

@XmlRootElement(namespace = NativeQueueConfiguration.NS, name = "sge-configuration")
public class SGEQueueConfiguration extends CommonScriptBasedQueueConfiguration
{
	static final long serialVersionUID = 0L;
}