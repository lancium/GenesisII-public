package edu.virginia.vcgr.genii.ui.progress;

import java.io.Closeable;

import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.ui.UIContext;

public abstract class AbstractContextSwitchingTask<ResultType> extends AbstractTask<ResultType>
{
	private UIContext _context;

	protected abstract ResultType executeInsideContext(TaskProgressListener progressListener) throws Exception;

	final protected UIContext context()
	{
		return _context;
	}

	protected AbstractContextSwitchingTask(UIContext context)
	{
		_context = context;
	}

	@Override
	final public ResultType execute(TaskProgressListener progressListener) throws Exception
	{
		Closeable token = null;

		try {
			token = ContextManager.temporarilyAssumeContext(_context.callingContext());
			return executeInsideContext(progressListener);
		} finally {
			StreamUtils.close(token);
		}
	}
}