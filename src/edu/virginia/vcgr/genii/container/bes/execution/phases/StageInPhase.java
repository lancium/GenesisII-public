package edu.virginia.vcgr.genii.container.bes.execution.phases;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.URI;

import org.ggf.bes.factory.ActivityStateEnumeration;
import org.ggf.jsdl.CreationFlagEnumeration;
import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.client.bes.ActivityState;
import edu.virginia.vcgr.genii.client.io.URIManager;
import edu.virginia.vcgr.genii.container.bes.execution.ExecutionContext;

public class StageInPhase extends AbstractExecutionPhase
	implements Serializable
{
	static final long serialVersionUID = 0L;
	
	static private final String STAGING_IN_STATE = "staging-in";
	
	private URI _source;
	private String _targetName;
	private CreationFlagEnumeration _creationFlag;
	
	public StageInPhase(URI source, String targetName, 
		CreationFlagEnumeration creationFlag)
	{
		super(new ActivityState(ActivityStateEnumeration.Running,
			STAGING_IN_STATE, false));
		
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
		InputStream in = null;
		OutputStream out = null;
		
		File target = new File(
			context.getCurrentWorkingDirectory(), _targetName);
		if (_creationFlag.equals(CreationFlagEnumeration.dontOverwrite)
			&& target.exists())
			return;
		
		boolean append = _creationFlag.equals(CreationFlagEnumeration.append);
		
		try
		{
			in = URIManager.openInputStream(_source);
			out = new FileOutputStream(target, append);
			StreamUtils.copyStream(in, out);
		}
		finally
		{
			StreamUtils.close(out);
			StreamUtils.close(in);
		}
	}
}