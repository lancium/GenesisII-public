package edu.virginia.vcgr.genii.container.bes.execution.phases;

import java.io.File;
import java.io.Serializable;
import java.net.URI;

import org.ggf.bes.factory.ActivityStateEnumeration;
import org.ggf.jsdl.CreationFlagEnumeration;

import edu.virginia.vcgr.genii.client.bes.ActivityState;
import edu.virginia.vcgr.genii.client.io.URIManager;
import edu.virginia.vcgr.genii.client.security.gamlauthz.identity.UsernamePasswordIdentity;
import edu.virginia.vcgr.genii.container.bes.execution.ExecutionContext;

public class StageInPhase extends AbstractExecutionPhase
	implements Serializable
{
	static final long serialVersionUID = 0L;
	
	static private final String STAGING_IN_STATE = "staging-in";
	
	private URI _source;
	private String _targetName;
	private CreationFlagEnumeration _creationFlag;
	private UsernamePasswordIdentity _credential;
	
	public StageInPhase(URI source, String targetName, 
		CreationFlagEnumeration creationFlag, 
		UsernamePasswordIdentity credential)
	{
		super(new ActivityState(ActivityStateEnumeration.Running,
			STAGING_IN_STATE, false));
		
		_credential = credential;
		
		if (source == null)
			throw new IllegalArgumentException("Parameter \"source\" cannot be null.");
		
		if (targetName == null)
			throw new IllegalArgumentException("Parameter \"targetName\" cannot be null.");
		
		_source = source;
		_targetName = targetName;
		_creationFlag = 
			(creationFlag == null) ? CreationFlagEnumeration.overwrite : creationFlag;
	}
	
	@Override
	public void execute(ExecutionContext context) throws Throwable
	{
		File target = new File(
			context.getCurrentWorkingDirectory(), _targetName);
		if (_creationFlag.equals(CreationFlagEnumeration.dontOverwrite)
			&& target.exists())
			return;
		
		URIManager.get(_source, target, _credential);
	}
}