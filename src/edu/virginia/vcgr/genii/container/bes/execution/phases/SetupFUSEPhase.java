package edu.virginia.vcgr.genii.container.bes.execution.phases;

import java.io.File;
import java.io.IOException;

import org.ggf.bes.factory.ActivityStateEnumeration;

import edu.virginia.vcgr.genii.client.bes.ActivityState;
import edu.virginia.vcgr.genii.client.configuration.Installation;
import edu.virginia.vcgr.genii.container.bes.execution.ExecutionContext;

public class SetupFUSEPhase extends AbstractFUSEPhases
{
	static final long serialVersionUID = 0L;
	
	public SetupFUSEPhase(String mountPoint)
	{
		super(mountPoint, new ActivityState(
			ActivityStateEnumeration.Running, "fuse-setup", false));
	}
	
	@Override
	public void execute(ExecutionContext context) throws Throwable
	{
		File mountPoint = getMountPoint(context);
		if (!mountPoint.exists())
		{
			if (!mountPoint.mkdirs())
				throw new IOException("Unable to create mount point:  " 
					+ mountPoint.getAbsolutePath());
		} else
		{
			if (!mountPoint.isDirectory())
				throw new IOException("Mount point \""
					+ mountPoint.getAbsolutePath() + "\" is not a directory.");
		}
		
		ProcessBuilder builder = new ProcessBuilder(
			Installation.getGridCommand().getAbsolutePath(),
			"fuse", "--mount", getMountPoint(context).getAbsolutePath());
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