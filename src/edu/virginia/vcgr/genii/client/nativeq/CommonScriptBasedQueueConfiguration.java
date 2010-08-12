package edu.virginia.vcgr.genii.client.nativeq;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.NONE)
public class CommonScriptBasedQueueConfiguration 
	extends ScriptBasedQueueConfiguration
{
	static final long serialVersionUID = 0L;
	
	static final private String DEFAULT_QSUB_BINARY = "qsub";
	static final private String DEFAULT_QSTAT_BINARY = "qstat";
	static final private String DEFAULT_QDEL_BINARY = "qdel";
	
	@XmlAttribute(name = "queue-name", required = false)
	private String _queueName = null;
	
	@XmlAttribute(name = "bin-directory", required = false)
	private String _binDirectory = null;
	
	@XmlElement(namespace = NS, name = "qsub",
		required = false, nillable = true)
	private ExecutableApplicationConfiguration _qsub = 
		new ExecutableApplicationConfiguration();
	
	@XmlElement(namespace = NS, name = "qstat",
		required = false, nillable = true)
	private ExecutableApplicationConfiguration _qstat = 
		new ExecutableApplicationConfiguration();
	
	@XmlElement(namespace = NS, name = "qdel",
		required = false, nillable = true)
	private ExecutableApplicationConfiguration _qdel = 
		new ExecutableApplicationConfiguration();
	
	private File binDirectory()
	{
		return (_binDirectory == null) ? null : new File(_binDirectory);
	}
	
	final public void binDirectory(String directory)
	{
		_binDirectory = directory;
	}
	
	final public String queueName()
	{
		return _queueName;
	}
	
	final public void queueName(String queueName)
	{
		_queueName = queueName;
	}
	
	final public List<String> startQSub() throws FileNotFoundException
	{
		return _qsub.startCommandLine(binDirectory(), DEFAULT_QSUB_BINARY);
	}
	
	final public void qsub(ExecutableApplicationConfiguration conf)
	{
		_qsub = conf;
	}
	
	final public List<String> startQStat() throws FileNotFoundException
	{
		return _qstat.startCommandLine(binDirectory(), DEFAULT_QSTAT_BINARY);
	}
	
	final public void qstat(ExecutableApplicationConfiguration conf)
	{
		_qstat = conf;
	}
	
	final public List<String> startQDel() throws FileNotFoundException
	{
		return _qdel.startCommandLine(binDirectory(), DEFAULT_QDEL_BINARY);
	}
	
	final public void qdel(ExecutableApplicationConfiguration conf)
	{
		_qdel = conf;
	}
}