package edu.virginia.vcgr.genii.container.bes.execution.phases;

import java.io.File;
import java.io.Serializable;
import java.net.URI;

import org.ggf.bes.factory.ActivityStateEnumeration;
import org.ggf.jsdl.CreationFlagEnumeration;

import edu.virginia.vcgr.genii.client.bes.ActivityState;
import edu.virginia.vcgr.genii.client.io.URIManager;
import edu.virginia.vcgr.genii.client.security.credentials.identity.UsernamePasswordIdentity;
import edu.virginia.vcgr.genii.container.bes.execution.ExecutionContext;
import edu.virginia.vcgr.genii.container.cservices.ContainerServices;
import edu.virginia.vcgr.genii.container.cservices.downloadmgr.DownloadManagerContainerService;

public class StageInPhase extends AbstractExecutionPhase
	implements Serializable
{
	static final long serialVersionUID = 0L;
	
	static private final String STAGING_IN_STATE = "staging-in";
	
	private URI _source;
	private File _target;
	private CreationFlagEnumeration _creationFlag;
	private UsernamePasswordIdentity _credential;
	
	public StageInPhase(URI source, File target, 
		CreationFlagEnumeration creationFlag, 
		UsernamePasswordIdentity credential)
	{
		super(new ActivityState(ActivityStateEnumeration.Running,
			STAGING_IN_STATE, false));
		
		_credential = credential;
		
		if (source == null)
			throw new IllegalArgumentException("Parameter \"source\" cannot be null.");
		
		if (target == null)
			throw new IllegalArgumentException("Parameter \"targetName\" cannot be null.");
		
		_source = source;
		_target = target;
		_creationFlag = 
			(creationFlag == null) ? CreationFlagEnumeration.overwrite : creationFlag;
	}
	
	@Override
	public void execute(ExecutionContext context) throws Throwable
	{
		File target = _target;
		if (_creationFlag.equals(CreationFlagEnumeration.dontOverwrite))
		{
			DownloadManagerContainerService service =
				(DownloadManagerContainerService)ContainerServices.findService(
					DownloadManagerContainerService.SERVICE_NAME);
			service.download(_source, target, _credential);
		} else
			URIManager.get(_source, target, _credential);
	}
}