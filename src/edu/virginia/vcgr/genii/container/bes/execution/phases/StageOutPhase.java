package edu.virginia.vcgr.genii.container.bes.execution.phases;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.URI;

import org.ggf.bes.factory.ActivityStateEnumeration;
import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.client.bes.ActivityState;
import edu.virginia.vcgr.genii.client.io.URIManager;
import edu.virginia.vcgr.genii.container.bes.execution.ContinuableExecutionException;
import edu.virginia.vcgr.genii.container.bes.execution.ExecutionContext;

public class StageOutPhase extends AbstractExecutionPhase
	implements Serializable
{
	static final long serialVersionUID = 0L;
	
	static private final String STAGING_OUT_STATE = "staging-out";
	
	private URI _target;
	private String _sourceName;
	
	public StageOutPhase(String sourceName, URI target)
	{
		super(new ActivityState(ActivityStateEnumeration.Running,
			STAGING_OUT_STATE, false));
		
		if (sourceName == null)
			throw new IllegalArgumentException("Parameter \"sourceName\" cannot be null.");
		
		if (target == null)
			throw new IllegalArgumentException("Parameter \"target\" cannot be null.");
		
		_sourceName = sourceName;
		_target = target;
	}
	
	@Override
	public void execute(ExecutionContext context) throws Throwable
	{
		InputStream in = null;
		OutputStream out = null;
		
		try
		{
			in = new FileInputStream(new File(
				context.getCurrentWorkingDirectory(), _sourceName));
			out = URIManager.openOutputStream(_target);
			StreamUtils.copyStream(in, out);
		}
		catch (Throwable cause)
		{
			throw new ContinuableExecutionException(
				"A continuable exception has occurred while " +
					"running a BES activity.", cause);
		}
		finally
		{
			StreamUtils.close(out);
			StreamUtils.close(in);
		}
	}
}