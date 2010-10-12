package edu.virginia.vcgr.genii.container.bes.execution.phases;

import java.io.File;
import java.io.IOException;

import org.ggf.bes.factory.ActivityStateEnumeration;

import edu.virginia.vcgr.genii.client.bes.ActivityState;
import edu.virginia.vcgr.genii.client.configuration.Installation;
import edu.virginia.vcgr.genii.client.history.HistoryEventCategory;
import edu.virginia.vcgr.genii.container.bes.execution.ExecutionContext;
import edu.virginia.vcgr.genii.container.cservices.history.HistoryContext;
import edu.virginia.vcgr.genii.container.cservices.history.HistoryContextFactory;

public class SetupFUSEPhase extends AbstractFUSEPhases
{
	static final long serialVersionUID = 0L;
	
	private String _sandbox = null;
	
	public SetupFUSEPhase(String mountPoint, String sandbox)
	{
		super(mountPoint, new ActivityState(
			ActivityStateEnumeration.Running, "fuse-setup", false));
		
		_sandbox = sandbox;
	}
	
	public SetupFUSEPhase(String mountPoint)
	{
		this(mountPoint, null);
	}
	
	@Override
	public void execute(ExecutionContext context) throws Throwable
	{
		HistoryContext history = HistoryContextFactory.createContext(
			HistoryEventCategory.CreatingActivity);

		File mountPoint = getMountPoint(context);
		history.createTraceWriter("FUSE Mounting Grid").format(
				"FUSE mounting grid to %s", mountPoint).close();
		
		if (!mountPoint.exists())
		{
			if (!mountPoint.mkdirs())
			{
				history.createErrorWriter("FUSE Mount Failed").format(
					"Unable to create mount point").close();
				
				throw new IOException("Unable to create mount point:  " 
					+ mountPoint.getAbsolutePath());
			}
		} else
		{
			if (!mountPoint.isDirectory())
			{
				history.createErrorWriter("FUSE Mount Failed").format(
					"Mount point not a directory").close();
			
				throw new IOException("Mount point \""
					+ mountPoint.getAbsolutePath() + "\" is not a directory.");
			}
		}
		
		String sandbox = (_sandbox == null) ? "/" : _sandbox;
			
		ProcessBuilder builder = new ProcessBuilder(
			Installation.getGridCommand().getAbsolutePath(),
			"fuse", "--mount", String.format("--sandbox=%s", sandbox),
			getMountPoint(context).getAbsolutePath());
		builder.redirectErrorStream(true);
		
		StreamRedirectionSink sink = new FileRedirectionSink(
			new File("fuse-output.log"));
		StreamRedirectionDescription desc = new StreamRedirectionDescription(
			null, sink, null);
		
		Process proc = builder.start();
		desc.enact(context, proc.getOutputStream(), 
			proc.getInputStream(), proc.getErrorStream());
		
		try
		{
			Thread.sleep(1000L * 4);
		}
		catch (InterruptedException ie)
		{
			Thread.interrupted();
		}
	}
}