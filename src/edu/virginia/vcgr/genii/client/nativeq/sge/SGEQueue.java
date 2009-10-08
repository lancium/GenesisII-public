package edu.virginia.vcgr.genii.client.nativeq.sge;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import edu.virginia.vcgr.genii.client.nativeq.AbstractNativeQueue;
import edu.virginia.vcgr.genii.client.nativeq.AdditionalArguments;
import edu.virginia.vcgr.genii.client.nativeq.BinariesDescription;
import edu.virginia.vcgr.genii.client.nativeq.JobStateCache;
import edu.virginia.vcgr.genii.client.nativeq.NativeQueueConnection;
import edu.virginia.vcgr.genii.client.nativeq.NativeQueueException;

public class SGEQueue extends AbstractNativeQueue
{
	static final public String PROVIDER_NAME = "sge";
	
	static final public String QUEUE_NAME_PROPERTY =
		"edu.virginia.vcgr.genii.client.nativeq.sge.queue-name";
	
	static final public String QUEUE_BIN_DIRECTORY_PROPERTY =
		"edu.virginia.vcgr.genii.client.nativeq.sge.bin-directory";
	
	static final public String QUEUE_QSUB_PATH_PROPERTY =
		"edu.virginia.vcgr.genii.client.nativeq.sge.qsub-path";
	static final public String QUEUE_QDEL_PATH_PROPERTY =
		"edu.virginia.vcgr.genii.client.nativeq.sge.qdel-path";
	static final public String QUEUE_QSTAT_PATH_PROPERTY =
		"edu.virginia.vcgr.genii.client.nativeq.sge.qstat-path";
	
	static final public String QUEUE_QSUB_ADDITIONAL_ARG_PROPERTY_PATTERN =
		"edu.virginia.vcgr.genii.client.nativeq.sge.qsub.additional-argument.%d";
	static final public String QUEUE_QSTAT_ADDITIONAL_ARG_PROPERTY_PATTERN =
		"edu.virginia.vcgr.genii.client.nativeq.sge.qstat.additional-argument.%d";
	static final public String QUEUE_QDEL_ADDITIONAL_ARG_PROPERTY_PATTERN =
		"edu.virginia.vcgr.genii.client.nativeq.sge.qdel.additional-argument.%d";
	
	static final public String QUEUE_SUPPORTED_SPMD_VARIATIONS_PROPERTY_BASE =
		"edu.virginia.vcgr.genii.client.nativeq.sge.spmd.variation";
	static final public String QUEUE_SUPPORTED_SPMD_VARIATION_PROVIDER_FOOTER =
		"spmd-provider";
	static final public String QUEUE_SUPPORTED_SPMD_ADDITIONAL_CMDLINE_ARGS =
		"additional-commandline-args";
	
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
	
	private JobStateCache _statusCache = new JobStateCache();
	
	public SGEQueue()
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
		
		return new SGEQueueConnection(workingDirectory,
			connectionProperties, qname, 
			binDesc.get(QUEUE_QSUB_PATH_PROPERTY),
			binDesc.get(QUEUE_QSTAT_PATH_PROPERTY),
			binDesc.get(QUEUE_QDEL_PATH_PROPERTY),
			AdditionalArguments.parseAdditionalArguments(
				connectionProperties,
				QUEUE_QSUB_ADDITIONAL_ARG_PROPERTY_PATTERN, 
				QUEUE_QSTAT_ADDITIONAL_ARG_PROPERTY_PATTERN, 
				QUEUE_QDEL_ADDITIONAL_ARG_PROPERTY_PATTERN),
			_statusCache);
	}
}