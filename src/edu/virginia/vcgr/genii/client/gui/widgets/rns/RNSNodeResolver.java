package edu.virginia.vcgr.genii.client.gui.widgets.rns;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathDoesNotExistException;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;

public class RNSNodeResolver implements Runnable
{
	static private Log _logger = LogFactory.getLog(RNSNodeResolver.class);
	
	private RNSTreeModel _model;
	private RNSTreeNode _parent;
	
	public RNSNodeResolver(RNSTreeModel model, RNSTreeNode parent)
	{
		_model = model;
		_parent = parent;
	}
	
	public void run()
	{
		try
		{
			RNSPath target = _parent.getRNSPath();
			RNSPath []entries = target.list(".*", RNSPathQueryFlags.MUST_EXIST);
			_parent.resolveChildren(_model, entries);
		}
		catch (RNSPathDoesNotExistException rpde)
		{
			_parent.resolveChildren(_model, new RNSPath[0]);
		}
		catch (Throwable t)
		{
			_logger.warn("Error trying to resolve an RNS path.", t);
			_parent.resolveChildrenError(_model, "Lookup Error:  " + t.getLocalizedMessage());
		}
	}
}
