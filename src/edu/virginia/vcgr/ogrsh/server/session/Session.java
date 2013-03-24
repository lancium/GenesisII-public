package edu.virginia.vcgr.ogrsh.server.session;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.GUID;

import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;

public class Session
{
	static private Log _logger = LogFactory.getLog(Session.class);

	private GUID _sessionID;
	private ICallingContext _ctxt;

	private Session(ICallingContext originalCtxt)
	{
		_sessionID = new GUID();

		if (originalCtxt != null) {
			try {
				ContextManager.storeCurrentContext(originalCtxt);
				_ctxt = originalCtxt;
			} catch (IOException fnfe) {
				// can't happen.
			}
		} else {
			_ctxt = null;
		}
	}

	public Session()
	{
		this(null);
	}

	public GUID getSessionID()
	{
		return _sessionID;
	}

	public Session duplicate()
	{
		return new Session((_ctxt == null) ? null : _ctxt.deriveNewContext());
	}

	public void setCallingContext(ICallingContext callingContext)
	{
		try {
			ContextManager.storeCurrentContext(callingContext);
			_ctxt = callingContext;
		} catch (Throwable t) {
			// Basically, nothing can go wrong here.
			_logger.fatal("Unexpected exception.", t);
		}
	}
}