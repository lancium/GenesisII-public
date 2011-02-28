package edu.virginia.vcgr.genii.client.bes;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.client.bes.envvarexp.EnvironmentExport;
import edu.virginia.vcgr.genii.client.bes.envvarexp.EnvironmentVariableExportConstants;
import edu.virginia.vcgr.genii.client.common.ConstructionParameters;
import edu.virginia.vcgr.genii.client.nativeq.NativeQueueConfiguration;
import edu.virginia.vcgr.genii.client.utils.units.Duration;
import edu.virginia.vcgr.genii.cloud.CloudConfiguration;
import edu.virginia.vcgr.genii.cmdLineManipulator.config.CmdLineManipulatorConfiguration;
import edu.virginia.vcgr.genii.client.cmdLineManipulator.CmdLineManipulatorUtils;

@XmlAccessorType(XmlAccessType.NONE)
public class BESConstructionParameters
	extends ConstructionParameters implements Serializable
{
	static final long serialVersionUID = 0L;
	
	static private Log _logger = LogFactory.getLog(
			BESConstructionParameters.class);
	
	static final public String BES_CONS_PARMS_NS =
		"http://vcgr.cs.virginia.edu/construction-parameters/bes";
	
	private ResourceOverrides _resourceOverrides = new ResourceOverrides();
	private NativeQueueConfiguration _nativeQueueConf = null;
	private CmdLineManipulatorConfiguration _cmdLineManipulatorConf = null; 
	private Duration _preExecutionDelay = null;
	private Duration _postExecutionDelay = null;
	private CloudConfiguration _cloudBES = null;
	
	@XmlElement(namespace = EnvironmentVariableExportConstants.NAMESPACE,
		name = "environment-export", nillable = true, required = false)
	private EnvironmentExport _environmentExport = null;
	
	@SuppressWarnings("unused")
	@XmlElement(namespace = BES_CONS_PARMS_NS, name = "pre-execution-delay",
		required = false, nillable = true)
	final private String getPreExecutionDelayString() 
	{
		if (_preExecutionDelay == null)
			return null;
		
		return _preExecutionDelay.toString();
	}
	
	@SuppressWarnings("unused")
	final private void setPreExecutionDelayString(String value)
	{
		if (value == null)
			_preExecutionDelay = null;
		else
			_preExecutionDelay = new Duration(value);
	}
	
	@SuppressWarnings("unused")
	@XmlElement(namespace = BES_CONS_PARMS_NS, name = "post-execution-delay",
		required = false, nillable = true)
	final private String getPostExecutionDelayString() 
	{
		if (_postExecutionDelay == null)
			return null;
		
		return _postExecutionDelay.toString();
	}
	
	@SuppressWarnings("unused")
	final private void setPostExecutionDelayString(String value)
	{
		if (value == null)
			_postExecutionDelay = null;
		else
			_postExecutionDelay = new Duration(value);
	}
	
	public BESConstructionParameters(ResourceOverrides resourceOverrides,
		NativeQueueConfiguration queueConfiguration,
		CmdLineManipulatorConfiguration cmdLineManipulatorConf)
	{
		setResourceOverrides(resourceOverrides);
		setNativeQueueConfiguration(queueConfiguration);
		setCmdLineManipulatorConfiguration(cmdLineManipulatorConf);
	}
	
	public BESConstructionParameters()
	{
		this(null, null, null);
	}
	
	@XmlElement(namespace = BES_CONS_PARMS_NS, name = "resource-overrides",
			required = false, nillable = true)
	final public ResourceOverrides getResourceOverrides()
	{
		return _resourceOverrides;
	}
	
	final public void setResourceOverrides(ResourceOverrides resourceOverrides)
	{
		if (resourceOverrides == null)
			resourceOverrides = new ResourceOverrides();
		
		_resourceOverrides = resourceOverrides;
	}
	
	@XmlElement(namespace = BES_CONS_PARMS_NS, name = "nativeq",
			required = false, nillable = false)
	final public NativeQueueConfiguration getNativeQueueConfiguration()
	{
		return _nativeQueueConf;
	}
	
	final public void setNativeQueueConfiguration(NativeQueueConfiguration nativeQueueConf)
	{
		_nativeQueueConf = nativeQueueConf;
	}
	
	@XmlElement(namespace = BES_CONS_PARMS_NS, name = "cloudbes",
			required = false, nillable = false)
	final public CloudConfiguration getCloudConfiguration()
	{
		return _cloudBES;
	}
	
	final public void setCloudConfiguration(CloudConfiguration cloudConf)
	{
		_cloudBES = cloudConf;
	}
	
	@XmlElement(namespace = BES_CONS_PARMS_NS, name = "cmdline-manipulators",
			required = false, nillable = false)
	final public CmdLineManipulatorConfiguration getCmdLineManipulatorConfiguration()
	{
		return _cmdLineManipulatorConf;
	}
	
	final public void setCmdLineManipulatorConfiguration(
			CmdLineManipulatorConfiguration cmdLineManipulatorConf)
	{
		if (cmdLineManipulatorConf == null){
			_logger.debug("Null cmdline manipulator configuration found.");
			cmdLineManipulatorConf = new CmdLineManipulatorConfiguration();
			CmdLineManipulatorUtils.addPwrapperManipulator(cmdLineManipulatorConf);
		}
		
		_cmdLineManipulatorConf = cmdLineManipulatorConf;
	}
	
	
	final public Duration preExecutionDelay()
	{
		return _preExecutionDelay;
	}
	
	final public Duration postExecutionDelay()
	{
		return _postExecutionDelay;
	}
	
	final public EnvironmentExport environmentExport()
	{
		return _environmentExport;
	}
}