package edu.virginia.vcgr.genii.client.nativeq.pbs;

import javax.xml.bind.annotation.XmlRootElement;

import edu.virginia.vcgr.genii.client.nativeq.CommonScriptBasedQueueConfiguration;
import edu.virginia.vcgr.genii.client.nativeq.NativeQueueConfiguration;

@XmlRootElement(namespace = NativeQueueConfiguration.NS, name = "pbs-configuration")
public class PBSQueueConfiguration extends CommonScriptBasedQueueConfiguration
{
	static final long serialVersionUID = 0L;
}