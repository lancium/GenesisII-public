package edu.virginia.vcgr.genii.container.bes.execution.phases;

import java.io.File;
import java.util.ArrayList;

import org.ggf.bes.factory.ActivityStateEnumeration;

import edu.virginia.vcgr.genii.client.bes.ActivityState;
import edu.virginia.vcgr.genii.client.bes.ExecutionContext;
import edu.virginia.vcgr.genii.client.cmd.tools.LogoutTool;
import edu.virginia.vcgr.genii.client.context.ClientContextResolver;
import edu.virginia.vcgr.genii.client.context.ContextFileSystem;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.history.HistoryEventCategory;
import edu.virginia.vcgr.genii.container.bes.activity.BESActivity;
import edu.virginia.vcgr.genii.container.cservices.history.HistoryContext;
import edu.virginia.vcgr.genii.container.cservices.history.HistoryContextFactory;
import edu.virginia.vcgr.genii.security.TransientCredentials;
import edu.virginia.vcgr.genii.security.credentials.NuCredential;

public class SetupContextDirectoryPhase extends AbstractExecutionPhase
{
	static final long serialVersionUID = 0L;

	private String _contextDirectoryName;

	public SetupContextDirectoryPhase(String contextDirectoryName)
	{
		super(new ActivityState(ActivityStateEnumeration.Running, "context-directory-store"));

		_contextDirectoryName = contextDirectoryName;
	}

	@Override
	public void execute(ExecutionContext context, BESActivity activity) throws Throwable
	{
		HistoryContext history = HistoryContextFactory.createContext(HistoryEventCategory.CreatingActivity);

		history.trace("Creating Grid Context");

		File dir = new File(context.getCurrentWorkingDirectory().getWorkingDirectory(), _contextDirectoryName);
		dir.mkdirs();
		// 2020-12-10 by ASG. Turns out we have been delegating queue and bes credentials to user jobs .. let's not do that anymore
		ICallingContext icontext=context.getCallingContext();
		TransientCredentials tcreds=TransientCredentials.getTransientCredentials(icontext);
		ArrayList<NuCredential> credentials = tcreds.getCredentials();
		/*
		System.out.println("Please select a credential to logout from:");
		for (int lcv = 0; lcv < credentials.size(); lcv++) {
			System.out.println("\t[" + lcv + "]:  " + credentials.get(lcv));
		}
		*/

		LogoutTool.removeByPattern(credentials, "QueuePortType");
			
		//credentials = TransientCredentials.getTransientCredentials(context.getCallingContext()).getCredentials();
		/*
		System.out.println("Please select a credential to logout from:");
		for (int lcv = 0; lcv < credentials.size(); lcv++) {
			System.out.println("\t[" + lcv + "]:  " + credentials.get(lcv));
		}
		*/
		TransientCredentials.setTransientCredentials(icontext, tcreds);
		//icontext=context.getCallingContext();
		// End of 2020-12-10 updates.
		// context.getCallingContext()
		ContextFileSystem.store(new File(dir, ClientContextResolver.USER_CONTEXT_FILENAME),
			new File(dir, ClientContextResolver.USER_TRANSIENT_FILENAME), icontext);
	}
}