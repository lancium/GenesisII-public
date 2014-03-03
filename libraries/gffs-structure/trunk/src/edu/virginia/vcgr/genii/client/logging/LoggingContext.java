package edu.virginia.vcgr.genii.client.logging;

import java.io.Serializable;
import java.util.Stack;

import org.morgan.util.GUID;

import edu.virginia.vcgr.genii.client.context.ContextException;

public class LoggingContext implements Serializable, Cloneable {
	private static final long serialVersionUID = 1L;

	// All of the existing context's for the local thread
	static private ThreadLocal<Stack<LoggingContext>> _loggingContext = new ThreadLocal<Stack<LoggingContext>>();

	// The log id for this context
	private String _rpcid;

	/***
	 * Determine whether a LoggingContext chain exists for the current thread
	 * 
	 * @return True if context(s) exist for this thread, false otherwise
	 */
	static public boolean hasCurrentLoggingContext() {
		return (_loggingContext.get() != null)
				&& (!_loggingContext.get().isEmpty() && (_loggingContext.get()
						.peek() != null));
	}

	/***
	 * Retrieves the current LoggingContext on the top of the stack
	 * 
	 * @return The current context
	 * @throws ContextException
	 *             If there is no existing LoggingContext for this thread
	 */
	static public LoggingContext getCurrentLoggingContext()
			throws ContextException {
		Stack<LoggingContext> stack = _loggingContext.get();
		if (stack == null || stack.isEmpty())
			throw new ContextException("Logging context is null.");

		LoggingContext ret = stack.peek();
		return ret;
	}

	/***
	 * Starts a new context stack from scratch
	 */
	static public void assumeNewLoggingContext() {
		assumeLoggingContext(new LoggingContext());
	}

	/***
	 * Starts a new context stack from scratch, starting with the context
	 * provided
	 * 
	 * @param context
	 */
	static public void assumeLoggingContext(LoggingContext context) {
		Stack<LoggingContext> stack = _loggingContext.get();
		if (stack == null)
			_loggingContext.set(stack = new Stack<LoggingContext>());

		stack.clear();

		stack.push(context);
	}

	/***
	 * Pop's the currently used logging context from the stack
	 * 
	 * @return The context just popped, may be null
	 */
	static public LoggingContext releaseCurrentLoggingContext() {
		Stack<LoggingContext> stack = _loggingContext.get();
		LoggingContext ret = null;
		if (stack != null) {
			ret = stack.pop();
		}
		return ret;
	}

	/***
	 * Pushes a new context onto the stack, leaving the current stack to be
	 * resumed later
	 * 
	 * @return
	 */
	static public LoggingContext adoptNewContext() {
		try {
			return adoptExistingContext(new LoggingContext());
		} catch (ContextException e) {
			// Should never happen, since it only throws on null input, and we
			// just made a new one
		}
		return null;
	}

	/***
	 * Pushes a copy of the provided context to the stack
	 * 
	 * @param context
	 *            The context to adopt
	 * @return The cloned copy
	 * @throws ContextException
	 *             if the context provided is null
	 */
	static public LoggingContext adoptExistingContext(LoggingContext context)
			throws ContextException {
		if (context == null) {
			throw new ContextException("Cannot adopt a null context");
		}

		Stack<LoggingContext> stack = _loggingContext.get();
		if (stack == null)
			_loggingContext.set(stack = new Stack<LoggingContext>());

		LoggingContext ret = (LoggingContext) context.clone();
		stack.push(ret);
		return ret;
	}

	public LoggingContext() {
		_rpcid = new GUID().toString();
	}

	public LoggingContext(String rpcid) {
		_rpcid = rpcid;
	}

	public String getCurrentID() {
		return _rpcid;
	}

	@Override
	public Object clone() {
		return new LoggingContext(_rpcid);
	}
}
