package edu.virginia.vcgr.genii.client.nativeq.slurm;

import javax.xml.bind.annotation.XmlRootElement;

import edu.virginia.vcgr.genii.client.nativeq.CommonScriptBasedQueueConfiguration;
import edu.virginia.vcgr.genii.client.nativeq.NativeQueueConfiguration;

@XmlRootElement(namespace = NativeQueueConfiguration.NS, name = "slurm-configuration")
public class SLURMQueueConfiguration extends CommonScriptBasedQueueConfiguration
{
	static final long serialVersionUID = 0L;
}
