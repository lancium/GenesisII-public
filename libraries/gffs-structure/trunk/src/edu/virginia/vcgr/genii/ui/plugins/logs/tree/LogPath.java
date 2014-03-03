package edu.virginia.vcgr.genii.ui.plugins.logs.tree;

import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.logging.DLogDatabase;
import edu.virginia.vcgr.genii.client.logging.DLogUtils;
import edu.virginia.vcgr.genii.common.GeniiCommon;
import edu.virginia.vcgr.genii.common.LogHierarchyEntryType;
import edu.virginia.vcgr.genii.common.LogRetrieveResponseType;
import edu.virginia.vcgr.genii.common.RPCCallerType;
import edu.virginia.vcgr.genii.common.RPCMetadataType;

public class LogPath
{
	static private Log _logger = LogFactory.getLog(LogPath.class);

	private DLogDatabase _db;
	private String _myID;
	private String _myName;
	private LogPath _parent;
	private EndpointReferenceType _loggerEPR;
	private EndpointReferenceType _targetEPR;
	private DisplayByType _displayType;

	public static LogPath getCurrent()
	{
		return new LogPath(DLogUtils.getDBConnector(), DisplayByType.DISPLAY_BY_RPC_ID);
	}

	private LogPath(DLogDatabase db, DisplayByType type)
	{
		_db = db;
		_myID = null;
		_myName = "/";
		_parent = null;
		_loggerEPR = null;
		_targetEPR = null;
		_displayType = type;
	}

	private LogPath(String id, String name, EndpointReferenceType targetEPR, DisplayByType displayType, LogPath parent)
	{
		_db = null;
		_myID = id;
		_myName = name;
		_parent = parent;
		_loggerEPR = null;
		_targetEPR = targetEPR;
		_displayType = displayType;
	}

	private LogPath createChildPath(String id, String name, EndpointReferenceType targetEPR, DisplayByType displayType)
		throws RemoteException
	{
		return new LogPath(id, name, targetEPR, displayType, this);
	}

	public EndpointReferenceType getEndpoint() throws LogPathDoesNotExistException, RemoteException
	{
		if (_loggerEPR != null) {
			return _loggerEPR;
		} else {
			if (_targetEPR != null) {
				return DLogUtils.getLoggerEPR(_targetEPR);
			}
			if (_parent == null) {
				return null;
			}
			return _parent.getEndpointFromParent(_myID);
		}
	}

	private EndpointReferenceType getEndpointFromParent(String id) throws LogPathDoesNotExistException, RemoteException
	{
		// I am the parent...
		EndpointReferenceType parentEPR = getEndpoint();
		if (parentEPR == null) {
			if (_db == null)
				return null;
			else {
				try {
					return DLogUtils.getLoggerEPR(_db.getEndpoint(id));
				} catch (Exception e) {
					_logger.error("caught unexpected exception", e);
				}
			}
		}

		try {
			GeniiCommon dpt = DLogUtils.getLogger(getEndpoint());
			LogRetrieveResponseType child = dpt.getChildLogIDs(new String[] { id });
			return child.getChildRPCs(0).getParent().getMetadata().getTargetEPR();
		} catch (RemoteException e) {
			_logger.error("caught unexpected exception", e);
		}

		return null;
	}

	public Vector<LogPath> listContents() throws RemoteException, SQLException
	{
		EndpointReferenceType epr = null;
		RPCCallerType[] children = null;

		try {
			epr = getEndpoint();
		} catch (Exception e) {
			// means the endpoint is no longer valid
		}

		if (epr != null) {
			GeniiCommon logger = DLogUtils.getLogger(epr);
			LogRetrieveResponseType res = logger.getChildLogIDs(new String[] { _myID });

			if (res != null && res.getChildRPCs() != null)
				for (LogHierarchyEntryType result : res.getChildRPCs()) {
					if (result.getParent().getRpcid().equals(_myID)) {
						children = result.getChildren();
						break;
					}
				}
		} else {
			DLogDatabase logger = DLogUtils.getDBConnector();
			Map<String, Collection<RPCCallerType>> res;
			if (logger != null) {
				if (_myID != null) {
					res = logger.selectChildren(_myID);
				} else {
					Collection<String> parents = logger.selectParentIDs();
					res = new HashMap<String, Collection<RPCCallerType>>();
					ArrayList<RPCCallerType> values = new ArrayList<RPCCallerType>();
					for (String parent : parents) {
						RPCMetadataType meta = new RPCMetadataType();
						meta.setMethodName(logger.getCommand(parent));
						meta.setTargetEPR(logger.getEndpoint(parent));
						values.add(new RPCCallerType(parent, meta));
					}
					res.put(_myID, values);
				}
				if (res != null && res.containsKey(_myID)) {
					children = res.get(_myID).toArray(new RPCCallerType[0]);
				}
			}
		}

		Vector<LogPath> ret = new Vector<LogPath>();
		if (children != null) {
			for (RPCCallerType child : children) {
				ret.add(createChildPath(child.getRpcid(), child.getMetadata().getMethodName(), child.getMetadata()
					.getTargetEPR(), _displayType));
			}
		}
		return ret;
	}

	public String getName()
	{
		if (_displayType.equals(DisplayByType.DISPLAY_BY_RPC_ID))
			return getID();
		if (_displayType.equals(DisplayByType.DISPLAY_BY_COMMAND))
			return getMyName();
		return "problem";
	}

	public String getMyName()
	{
		if (_myName == null) {
			return " ";
		}
		return _myName;
	}

	public String getID()
	{
		return _myID;
	}

	public String pwd()
	{
		if (_parent == null)
			return "/";

		String parent = _parent.pwd();
		if (parent == "/")
			return parent + _myID;

		return parent + "/" + _myID;
	}

	public EndpointReferenceType getTargetEndpoint()
	{
		return _targetEPR;
	}

	public void setDisplayType(DisplayByType type)
	{
		_displayType = type;
	}
}
