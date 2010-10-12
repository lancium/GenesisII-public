package edu.virginia.vcgr.genii.container.bes.execution.phases;

import java.io.File;
import java.io.Serializable;
import java.net.URI;

import org.ggf.bes.factory.ActivityStateEnumeration;

import edu.virginia.vcgr.genii.client.bes.ActivityState;
import edu.virginia.vcgr.genii.client.history.HistoryEventCategory;
import edu.virginia.vcgr.genii.client.io.URIManager;
import edu.virginia.vcgr.genii.client.security.credentials.identity.UsernamePasswordIdentity;
import edu.virginia.vcgr.genii.container.bes.execution.ContinuableExecutionException;
import edu.virginia.vcgr.genii.container.bes.execution.ExecutionContext;
import edu.virginia.vcgr.genii.container.cservices.history.HistoryContext;
import edu.virginia.vcgr.genii.container.cservices.history.HistoryContextFactory;

public class StageOutPhase extends AbstractExecutionPhase
	implements Serializable
{
	static final long serialVersionUID = 0L;
	
	static private final String STAGING_OUT_STATE = "staging-out";
	
	private URI _target;
	private File _source;
	private UsernamePasswordIdentity _credential;
	
	public StageOutPhase(File source, URI target, 
		UsernamePasswordIdentity credential)
	{
		super(new ActivityState(ActivityStateEnumeration.Running,
			STAGING_OUT_STATE, false));
		
		_credential = credential;
		
		if (source == null)
			throw new IllegalArgumentException("Parameter \"sourceName\" cannot be null.");
		
		if (target == null)
			throw new IllegalArgumentException("Parameter \"target\" cannot be null.");
		
		_source = source;
		_target = target;
	}
	
	@Override
	public void execute(ExecutionContext context) throws Throwable
	{
		HistoryContext history = HistoryContextFactory.createContext(
			HistoryEventCategory.StageOut);
		
		history.createInfoWriter("Staging %s out.", _source.getName()).format(
			"Staging %s out to %s.", _source, _target).close();
		
		if (!_source.exists())
		{
			history.createErrorWriter(
				"Can't stage %s out.", _source.getName()).format(
				"Source file (%s) does not seem to exist.", _source).close();
			
			throw new ContinuableExecutionException(
				"Unable to locate source file \"" +
				_source.getName() + "\" for staging-out -- skipping it.");
		}
		
		try
		{
			URIManager.put(_source,
				_target, _credential);
		}
		catch (Throwable cause)
		{
			history.error(cause, "Can't stage %s out.", 
				_source.getName());
			
			throw new ContinuableExecutionException(
				"A continuable exception has occurred while " +
					"running a BES activity.", cause);
		}
	}
}