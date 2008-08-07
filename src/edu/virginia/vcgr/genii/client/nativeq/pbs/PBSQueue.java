package edu.virginia.vcgr.genii.client.nativeq.pbs;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import edu.virginia.vcgr.genii.client.nativeq.AbstractNativeQueue;
import edu.virginia.vcgr.genii.client.nativeq.BinariesDescription;
import edu.virginia.vcgr.genii.client.nativeq.NativeQueueConnection;
import edu.virginia.vcgr.genii.client.nativeq.NativeQueueException;

public class PBSQueue extends AbstractNativeQueue
{
	static final public String PROVIDER_NAME = "pbs";
	
	static final public String QUEUE_NAME_PROPERTY =
		"edu.virginia.vcgr.genii.client.nativeq.pbs.queue-name";
	
	static final public String QUEUE_BIN_DIRECTORY_PROPERTY =
		"edu.virginia.vcgr.genii.client.nativeq.pbs.bin-directory";
	
	static final public String QUEUE_QSUB_PATH_PROPERTY =
		"edu.virginia.vcgr.genii.client.nativeq.pbs.qsub-path";
	static final public String QUEUE_QDEL_PATH_PROPERTY =
		"edu.virginia.vcgr.genii.client.nativeq.pbs.qdel-path";
	static final public String QUEUE_QSTAT_PATH_PROPERTY =
		"edu.virginia.vcgr.genii.client.nativeq.pbs.qstat-path";
	
	static final public String QUEUE_SUPPORTED_SPMD_VARIATIONS_PROPERTY_BASE =
		"edu.virginia.vcgr.genii.client.nativeq.pbs.spmd.variation";
	static final public String QUEUE_SUPPORTED_SPMD_VARIATION_PROVIDER_FOOTER =
		"spmd-provider";
	
	static final public String DEFAULT_QSUB_COMMAND = "qsub";
	static final public String DEFAULT_QSTAT_COMMAND = "qstat";
	static final public String DEFAULT_QDEL_COMMAND = "qdel";
	
	static final private Map<String, String> _defaultsMap;
	
	static 
	{
		_defaultsMap = new HashMap<String, String>();
		
		_defaultsMap.put(QUEUE_QSUB_PATH_PROPERTY, DEFAULT_QSUB_COMMAND);
		_defaultsMap.put(QUEUE_QSTAT_PATH_PROPERTY, DEFAULT_QSTAT_COMMAND);
		_defaultsMap.put(QUEUE_QDEL_PATH_PROPERTY, DEFAULT_QDEL_COMMAND);
	}
	
	public PBSQueue()
	{
		super(PROVIDER_NAME);
	}
	
	@Override
	public NativeQueueConnection connect(File workingDirectory,
		Properties connectionProperties) throws NativeQueueException
	{
		if (connectionProperties == null)
			connectionProperties = new Properties();
		
		String qname = connectionProperties.getProperty(QUEUE_NAME_PROPERTY);
		
		BinariesDescription binDesc = new BinariesDescription(
			connectionProperties, QUEUE_BIN_DIRECTORY_PROPERTY,
			_defaultsMap);
		
		return new PBSQueueConnection(workingDirectory,
			connectionProperties, qname, 
			binDesc.get(QUEUE_QSUB_PATH_PROPERTY),
			binDesc.get(QUEUE_QSTAT_PATH_PROPERTY),
			binDesc.get(QUEUE_QDEL_PATH_PROPERTY));
	}
}