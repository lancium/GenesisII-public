package edu.virginia.vcgr.genii.container.bes.execution.phases;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.ggf.bes.factory.ActivityStateEnumeration;
import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.client.bes.ActivityState;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.container.bes.execution.ExecutionContext;
import edu.virginia.vcgr.genii.container.bes.execution.ExecutionPhase;

public class StoreContextPhase extends AbstractExecutionPhase implements ExecutionPhase, Serializable
{
	static final long serialVersionUID = 0L;

	static public final String STORING_CONTEXT_PHASE = "storing-context";

	private String _filename;

	public StoreContextPhase(String filename)
	{
		super(new ActivityState(ActivityStateEnumeration.Running, STORING_CONTEXT_PHASE, false));

		_filename = filename;
	}

	@Override
	public void execute(ExecutionContext context) throws Throwable
	{
		File callingContextFile = new File(context.getCurrentWorkingDirectory().getWorkingDirectory(), _filename);
		FileOutputStream fos = null;

		try {
			ICallingContext cc = context.getCallingContext();
			fos = new FileOutputStream(callingContextFile);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(cc);
			oos.flush();
			oos.close();
		} finally {
			StreamUtils.close(fos);
		}
	}
}