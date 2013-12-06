package edu.virginia.vcgr.genii.client.cmd.tools;

import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;
import edu.virginia.vcgr.genii.client.io.LoadFileResource;
import edu.virginia.vcgr.genii.client.logging.DLogDatabase;
import edu.virginia.vcgr.genii.client.logging.DLogUtils;
import edu.virginia.vcgr.genii.common.GeniiCommon;
import edu.virginia.vcgr.genii.common.LogEntryType;
import edu.virginia.vcgr.genii.common.LogHierarchyEntryType;
import edu.virginia.vcgr.genii.common.LogRetrieveResponseType;
import edu.virginia.vcgr.genii.common.RPCCallerType;

public class LogReaderTool extends BaseGridTool
{
	private final Log _logger = LogFactory.getLog(LogReaderTool.class);
	static final private String _DESCRIPTION = "config/tooldocs/description/dreadlog";
	static final private String _USAGE = "config/tooldocs/usage/ureadlog";
	static final private String _MANPAGE = "config/tooldocs/man/readlog";

	private String _logServicePath = null;
	private String _resourcePath = null;
	private boolean _rpcs = true;
	private boolean _entries = true;
	private boolean _recursive = false;

	public LogReaderTool()
	{
		super(new LoadFileResource(_DESCRIPTION), new LoadFileResource(_USAGE), false, ToolCategory.DATA);
		addManPage(new LoadFileResource(_MANPAGE));
	}

	@Option({ "resource" })
	public void setResource(String path)
	{
		_resourcePath = path;
	}

	@Option({ "rpcs-only" })
	public void setRpcs()
	{
		_rpcs = true;
		_entries = false;
	}

	@Option({ "entries-only" })
	public void setLogEntries()
	{
		_entries = true;
		_rpcs = false;
	}

	@Option({ "recursive", "r" })
	public void setrecursive()
	{
		_recursive = true;
	}

	private Collection<EndpointReferenceType> getEPRFromDatabase(String[] whichRPCs) throws ToolException
	{
		Collection<EndpointReferenceType> ret = null;
		if (whichRPCs != null && whichRPCs.length > 0) {
			DLogDatabase db = DLogUtils.getDBConnector();
			ret = new ArrayList<EndpointReferenceType>();
			if (db != null) {
				try {
					for (String rpc : whichRPCs) {
						EndpointReferenceType epr = db.getEndpoint(rpc);
						if (epr != null)
							ret.add(epr);
					}
				} catch (SQLException e) {
					throw new ToolException("Problem retrieving EPRs for specified RPCID(s)", e);
				}
			}
		}
		return ret;
	}

	@Override
	protected int runCommand() throws Throwable
	{
		List<String> args = getArguments();
		List<GeniiCommon> loggers = new LinkedList<GeniiCommon>();

		String[] whichRPCs;
		if (args.size() == 0) {
			whichRPCs = null;
		} else {
			whichRPCs = args.toArray(new String[args.size()]);
		}

		if (_logServicePath == null && _resourcePath == null) {
			Collection<EndpointReferenceType> resourceEprs = getEPRFromDatabase(whichRPCs);

			// if the epr list is empty, these are probably local ids
			if (!resourceEprs.isEmpty()) {
				GeniiCommon target = null;

				for (EndpointReferenceType epr : resourceEprs) {
					target = DLogUtils.getLogger(epr);

					if (target != null) {
						loggers.add(target);
					}
				}
			}
		} else if (_resourcePath != null) {
			RNSPath resourcePath = RNSPath.getCurrent().lookup(_resourcePath, RNSPathQueryFlags.MUST_EXIST);

			GeniiCommon logger = DLogUtils.getLogger(resourcePath.getEndpoint());
			if (logger == null) {
				stdout.println("No logger services available for this resource");
				return 0;
			} else {
				loggers.add(logger);
			}
		} else {
			RNSPath loggerPath = RNSPath.getCurrent().lookup(_logServicePath, RNSPathQueryFlags.MUST_EXIST);
			loggers.add(ClientUtils.createProxy(GeniiCommon.class, loggerPath.getEndpoint()));
		}

		// create a port
		if (loggers.size() == 0) {
			DLogDatabase db = DLogUtils.getDBConnector();
			if (db == null) {
				stdout.println("\nDLogs not supported");
			} else if (args.size() == 0) {
				stdout.println("\nAll rpcs:\n");
				printEntries(null, db);
			} else {
				for (String rpc : whichRPCs) {
					stdout.println("\n" + rpc + ":");
					printEntries(rpc, db);
				}
			}
		} else {
			for (GeniiCommon logger : loggers) {
				if (args.size() == 0) {
					stdout.println("\nAll rpcs:\n");
					printEntries(null, logger);
				} else {
					for (String rpc : whichRPCs) {
						stdout.println("\n" + rpc + ":");
						printEntries(rpc, logger);
					}
				}
			}
		}
		return 0;
	}

	private void printEntries(String rpcID, DLogDatabase db) throws SQLException, RemoteException
	{
		// for the local kind
		printEntries(rpcID, db, 1);
	}

	private void printEntries(String rpcID, DLogDatabase db, int level) throws SQLException, RemoteException
	{
		String indent = "";
		for (int i = 0; i < level; ++i) {
			indent += "  ";
		}

		if (_rpcs) {
			Map<String, Collection<RPCCallerType>> childIDs = db.selectChildren(rpcID);

			if (childIDs != null && !childIDs.isEmpty()) {
				for (String parent : childIDs.keySet()) {
					Collection<RPCCallerType> children = childIDs.get(parent);
					if (rpcID == null) {
						stdout.println(indent + parent + ":");
					}

					for (RPCCallerType child : children) {
						stdout.println(indent + "+" + child.getRpcid());
						try {
							if (_recursive) {
								EndpointReferenceType targetEPR = child.getMetadata().getTargetEPR();

								if (targetEPR == null) {
									printEntries(child.getRpcid(), db, level + 1);
								} else {
									GeniiCommon logger = DLogUtils.getLogger(targetEPR);
									if (logger != null)
										printEntries(child.getRpcid(), logger, level + 1);
									else
										stdout.println(indent + "No logger for " + child.getRpcid());
								}
							}
						} catch (RemoteException e) {
							stderr.println(indent + "Couldn't find endpoint for entry " + child.getRpcid());
							_logger.error("Couldn't find endpoint for entry " + child.getRpcid(), e);
						}
					}
				}
			} else {
				if (!_entries) {
					stdout.println(indent + "no results");
				}
			}
		}
		if (_entries) {
			Collection<LogEntryType> logs = db.selectLogs(rpcID);
			if (logs != null) {
				for (LogEntryType entry : logs) {
					stdout.println(indent + "-" + entry.getMessage());
					if (entry.getStackTrace() != null && !entry.getStackTrace().isEmpty()) {
						stdout.println(indent + "--- Stack trace follows: ---");
						stdout.println(entry.getStackTrace());
						stdout.println(indent + "--- End Stack Trace ---");
					}
				}
			} else {
				stdout.println(indent + "no entries");
			}
		}
	}

	protected void printEntries(String rpcID, GeniiCommon logger) throws RemoteException
	{
		printEntries(rpcID, logger, 1);
	}

	protected void printEntries(String rpcID, GeniiCommon logger, int level) throws RemoteException
	{
		String indent = "";
		for (int i = 0; i < level; ++i) {
			indent += "  ";
		}

		String[] array = new String[] { rpcID };
		LogRetrieveResponseType response = logger.getAllLogs(array);
		if (_rpcs) {
			LogHierarchyEntryType childIDs[] = response.getChildRPCs();
			if (childIDs != null) {
				for (LogHierarchyEntryType link : childIDs) {
					if (rpcID == null) {
						stdout.println(indent + link.getParent() + ":");
					}

					for (RPCCallerType child : link.getChildren()) {
						stdout.println(indent + "+" + child.getRpcid());

						try {
							if (_recursive) {
								EndpointReferenceType targetEPR = child.getMetadata().getTargetEPR();
								GeniiCommon newlogger = DLogUtils.getLogger(targetEPR);
								printEntries(child.getRpcid(), newlogger, level + 1);
							}
						} catch (RemoteException e) {
							stderr.println(indent + "Couldn't find endpoint for entry " + child.getRpcid());
							_logger.error("Couldn't find endpoint for entry " + child.getRpcid(), e);
						}
					}
				}
			} else {
				if (!_entries) {
					stdout.println(indent + "no results");
				}
			}
		}
		if (_entries) {
			LogEntryType logs[] = response.getLogEntries();
			if (logs != null) {
				for (LogEntryType entry : logs) {
					stdout.println(indent + "-" + entry.getMessage());
					if (entry.getStackTrace() != null && !entry.getStackTrace().isEmpty()) {
						stdout.println(indent + "--- Stack trace follows: ---");
						stdout.println(entry.getStackTrace());
						stdout.println(indent + "--- End Stack Trace ---");
					}
				}
			} else {
				stdout.println(indent + "no entries");
			}
		}
	}

	@Override
	protected void verify() throws ToolException
	{
		if (_logServicePath == null && _resourcePath == null && getArguments().size() == 0) {
			throw new InvalidToolUsageException(usage());
		}
	}
}
