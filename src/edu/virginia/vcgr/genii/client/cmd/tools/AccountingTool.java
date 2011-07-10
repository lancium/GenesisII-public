package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Collection;
import java.util.Properties;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.axis.types.URI;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.io.StreamUtils;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.cmd.tools.gamllogin.AbstractGamlLoginHandler;
import edu.virginia.vcgr.genii.client.cmd.tools.gamllogin.GuiGamlLoginHandler;
import edu.virginia.vcgr.genii.client.cmd.tools.gamllogin.TextGamlLoginHandler;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.configuration.UserPreferences;
import edu.virginia.vcgr.genii.client.gpath.GeniiPath;
import edu.virginia.vcgr.genii.client.gui.GuiUtils;
import edu.virginia.vcgr.genii.client.io.FileResource;
import edu.virginia.vcgr.genii.client.iterator.WSIterable;
import edu.virginia.vcgr.genii.client.naming.WSName;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.filters.RNSFilter;
import edu.virginia.vcgr.genii.client.rns.recursived.RNSRecursiveDescent;
import edu.virginia.vcgr.genii.client.rns.recursived.RNSRecursiveDescentCallback;
import edu.virginia.vcgr.genii.client.rns.recursived.RNSRecursiveDescentCallbackResult;
import edu.virginia.vcgr.genii.client.security.VerbosityLevel;
import edu.virginia.vcgr.genii.client.security.credentials.identity.Identity;
import edu.virginia.vcgr.genii.client.ser.DBSerializer;
import edu.virginia.vcgr.genii.container.AccountingRecordType;
import edu.virginia.vcgr.genii.container.CommitAccountingRecordsRequestType;
import edu.virginia.vcgr.genii.container.VCGRContainerPortType;

public class AccountingTool extends BaseGridTool
{
	static private Log _logger = LogFactory.getLog(AccountingTool.class);
	
	static final private String ACCOUNTING_TOOL_DESCRIPTION =
		"edu/virginia/vcgr/genii/client/cmd/tools/description/daccounting";
	
	static private final String USAGE_RESOURCE =
		"edu/virginia/vcgr/genii/client/cmd/tools/usage/uaccounting";
		
	static private class StatementBundle implements Closeable
	{
		private PreparedStatement _lookupCID = null;
		private PreparedStatement _insertCredential = null;
		private PreparedStatement _lookupBESID = null;
		private PreparedStatement _insertBES = null;
		private PreparedStatement _lookupAccountingRecord = null;
		private PreparedStatement _insertAccountingRecord = null;
		private PreparedStatement _insertMapping = null;
		private PreparedStatement _insertCommandLineElement = null;
		
		
		
		private StatementBundle(Connection connection) throws SQLException
		{
			_lookupCID = connection.prepareStatement(
				"SELECT cid, credential FROM xcgcredentials " +
				"WHERE credentialhash = ?");
			_insertCredential = connection.prepareStatement(
				"INSERT INTO xcgcredentials " +
					"(credential, credentialdesc, credentialhash) " +
				"VALUES (?, ?, ?)",
				Statement.RETURN_GENERATED_KEYS);
			_lookupBESID = connection.prepareStatement(
				"SELECT besid, besmachinename, arch, os " +
					"FROM xcgbescontainers WHERE besepi = ?");
			_insertBES = connection.prepareStatement(
				"INSERT INTO xcgbescontainers " +
					"(besepi, besmachinename, arch, os) VALUES (?, ?, ?, ?)",
				Statement.RETURN_GENERATED_KEYS);
			_lookupAccountingRecord = connection.prepareStatement(
				"SELECT arid FROM xcgaccountingrecords " +
				"WHERE besaccountingrecordid = ? AND besid = ?");
			_insertAccountingRecord = connection.prepareStatement(
				"INSERT INTO xcgaccountingrecords " +
					"(besaccountingrecordid, besid, exitcode, " +
						"usertimemicrosecs, kerneltimemicrosecs, " +
						"wallclocktimemicrosecs, maxrssbytes, recordtimestamp) " +
				"VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
				Statement.RETURN_GENERATED_KEYS);
			_insertMapping = connection.prepareStatement(
				"INSERT INTO xcgareccredmap (cid, arid) VALUES (?, ?)");
			
			_insertCommandLineElement = connection.prepareStatement(
				"INSERT INTO xcgcommandlines (arid, elementindex, element) VALUES (?, ?, ?)");
		}
		
		@Override
		public void close() throws IOException
		{
			StreamUtils.close(_lookupCID);
			StreamUtils.close(_insertCredential);
			StreamUtils.close(_lookupBESID);
			StreamUtils.close(_lookupAccountingRecord);
			StreamUtils.close(_insertAccountingRecord);
			StreamUtils.close(_insertMapping);
			StreamUtils.close(_insertCommandLineElement);
		}
	}
	
	private void setPassword()
	{
		AbstractGamlLoginHandler handler = null;
		if (!useGui() || !GuiUtils.supportsGraphics() 
			|| !UserPreferences.preferences().preferGUI()) 
		{
			handler = new TextGamlLoginHandler(stdout, stderr, stdin);
		} else {
			handler = new GuiGamlLoginHandler(stdout, stderr, stdin);
		}
		char []pword = handler.getPassword("Accounting Database Password",
			"Password for accounting database:  ");
		String password = (pword == null) ? "" : new String(pword);
		_connectProperties = new Properties();
		_connectProperties.setProperty("password", password);
		
	}
	
	private Connection openTargetConnection(EndpointReferenceType epr)
		throws SQLException
	{
		URI uri = epr.getAddress().get_value();
		
		Connection cleanupConn = null;
		try
		{
			synchronized(_connect)
			{
				cleanupConn = DriverManager.getConnection(uri.toString(),
					_connectProperties);
			}
			cleanupConn.setAutoCommit(false);
			Connection ret = cleanupConn;
			cleanupConn = null;
			return ret;
		}
		finally
		{
			StreamUtils.close(cleanupConn);
		}
	}
	
	private long getCID(StatementBundle sBundle, Identity credential) 
		throws SQLException
	{
		ResultSet rs = null;
		int hash = credential.hashCode();
		Identity query = null;
		
		try
		{
			sBundle._lookupCID.setInt(1, hash);
			rs = sBundle._lookupCID.executeQuery();
			while (rs.next())
			{
				query = (Identity)DBSerializer.fromBlob(rs.getBlob(2));
				if (query.equals(credential))
					return rs.getLong(1);
			}
			rs.close();
			rs = null;
			
			sBundle._insertCredential.setBlob(1, DBSerializer.toBlob(
				credential, null, null));
			sBundle._insertCredential.setString(2, 
				credential.describe(VerbosityLevel.HIGH));
			sBundle._insertCredential.setInt(3, hash);
			sBundle._insertCredential.executeUpdate();
			rs = sBundle._insertCredential.getGeneratedKeys();
			
			if (!rs.next())
				throw new SQLException(
					"Unable to get auto-generated keys!");
			
			return rs.getLong(1);
		}
		finally
		{
			StreamUtils.close(rs);
		}
	}
	
	private long getBESId(StatementBundle sBundle, String besEPI,
		String besMachineName, String arch, String os) throws SQLException
	{
		ResultSet rs = null;
		
		try
		{
			sBundle._lookupBESID.setString(1, besEPI);
			rs = sBundle._lookupBESID.executeQuery();
			if (rs.next())
			{
				return rs.getLong("besid");
			}
			
			rs.close();
			rs = null;
			
			sBundle._insertBES.setString(1, besEPI);
			if (besMachineName == null)
				sBundle._insertBES.setNull(2, Types.VARCHAR);
			else
				sBundle._insertBES.setString(2, besMachineName);
			if (arch == null)
				sBundle._insertBES.setNull(3, Types.VARCHAR);
			else
				sBundle._insertBES.setString(3, arch);
			if (os == null)
				sBundle._insertBES.setNull(4, Types.VARCHAR);
			else
				sBundle._insertBES.setString(4, os);
			sBundle._insertBES.executeUpdate();
			
			rs = sBundle._insertBES.getGeneratedKeys();
			if (!rs.next())
				throw new SQLException(
					"Unable to get BESID auto-generated key.");
			
			return rs.getLong(1);
		}
		finally
		{
			StreamUtils.close(rs);
		}
	}
	
	private Long lookupAccountingRecordID(StatementBundle sBundle,
		long besaccountingrecordid, long besid) throws SQLException
	{
		ResultSet rs = null;
		
		try
		{
			sBundle._lookupAccountingRecord.setLong(1, besaccountingrecordid);
			sBundle._lookupAccountingRecord.setLong(2, besid);
			
			rs = sBundle._lookupAccountingRecord.executeQuery();
			if (rs.next())
				return new Long(rs.getLong(1));
			
			return null;
		}
		finally
		{
			StreamUtils.close(rs);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void addRecordToTargetDatabase(AccountingRecordType art,
		StatementBundle sBundle) throws IOException, ClassNotFoundException, SQLException
	{
		ResultSet rs = null;
		
		long besid = getBESId(sBundle, art.getBesEpi(),
				art.getBesMachineName(), art.getArch(), art.getOs());
		
		if (lookupAccountingRecordID(
			sBundle, art.getRecordId(), besid) != null)
		{
			stdout.format(
				"Accounting record %d from bes %s already exists.  " +
				"Skipping.\n", art.getRecordId(), art.getBesEpi());
			return;
		}
		
		Collection<Identity> credentials =
			(Collection<Identity>)DBSerializer.deserialize(art.getCredentials());
	
		Collection<Long> cids = new Vector<Long>(credentials.size());
		
		for (Identity id : credentials)
			cids.add(new Long(getCID(sBundle, id)));
		
		try
		{
			sBundle._insertAccountingRecord.setLong(1, art.getRecordId());
			sBundle._insertAccountingRecord.setLong(2, besid);
			sBundle._insertAccountingRecord.setInt(3, art.getExitCode());
			sBundle._insertAccountingRecord.setLong(4, art.getUserTime());
			sBundle._insertAccountingRecord.setLong(5, art.getKernelTime());
			sBundle._insertAccountingRecord.setLong(6, art.getWallclockTime());
			sBundle._insertAccountingRecord.setLong(7, art.getMaximumRss());
			Timestamp ts = new Timestamp(art.getRecordaddtime().getTimeInMillis());
			sBundle._insertAccountingRecord.setTimestamp(8, ts);
			sBundle._insertAccountingRecord.executeUpdate();
			rs = sBundle._insertAccountingRecord.getGeneratedKeys();
			if (!rs.next())
				throw new SQLException("Unable to retrieve auto-generated record key.");
			
			long arid = rs.getLong(1);
			
			for (Long cid : cids)
			{
				sBundle._insertMapping.setLong(1, cid.longValue());
				sBundle._insertMapping.setLong(2, arid);
				sBundle._insertMapping.addBatch();
			}
			
			sBundle._insertMapping.executeBatch();
			
			int lcv = 0;
			for (String value : art.getCommandLineValue())
			{
				sBundle._insertCommandLineElement.setLong(1, arid);
				sBundle._insertCommandLineElement.setInt(2, lcv++);
				sBundle._insertCommandLineElement.setString(3, value);
				
				sBundle._insertCommandLineElement.addBatch();
			}
			
			sBundle._insertCommandLineElement.executeBatch();
		}
		finally
		{
			StreamUtils.close(rs);
		}
	}
	
	private boolean _isCollect = false;
	private boolean _isRecursive = false;
	private boolean _isNoCommit = false;
	private int _maxThreads = 1;
	
	private Properties _connectProperties;
	private Lock _count;
	private Object _connect;
	
	@Override
	protected int runCommand() throws Throwable
	{
		RNSPath current = RNSPath.getCurrent();
		RNSPath source = lookup(current, new GeniiPath(getArgument(0)));
		RNSPath target = lookup(current, new GeniiPath(getArgument(1)));
		
		setPassword();
		_count = new Lock();
		_connect = new Object();
		
		collect(source.getEndpoint(), target.getEndpoint(), !_isNoCommit,
			_isRecursive);
		
		_count.join();
		
		return 0;
	}

	@Override
	protected void verify() throws ToolException
	{
		if (!_isCollect)
			throw new InvalidToolUsageException(
				"The accounting tool requires the --collect flag.");
		
		if (numArguments() != 2)
			throw new InvalidToolUsageException(
				"Missing required arguments.");
	}
	
	public AccountingTool()
	{
		super(new FileResource(ACCOUNTING_TOOL_DESCRIPTION), 
			new FileResource(USAGE_RESOURCE), true,
			ToolCategory.INTERNAL);
	}
	
	@Option({"collect"})
	public void setCollect()
	{
		_isCollect = true;
	}
	
	@Option({"recursive","r"})
	public void setRecursive()
	{
		_isRecursive = true;
	}
	
	@Option({"no-commit"})
	public void setNo_commit()
	{
		_isNoCommit = true;
	}
	
	@Option({"max-threads"})
	public void setMax_threads(String max)
	{
		_maxThreads = Integer.parseInt(max);
	}
	
	public void collect(EndpointReferenceType source,
		EndpointReferenceType target, boolean doCommit, boolean isRecursive)
			throws Throwable
	{
		Connection targetConnection = null;
		
		try
		{
			TypeInformation typeInfo = new TypeInformation(source);
			if (typeInfo.isContainer())
			{
				WSName containerName = new WSName(source);
				if (!containerName.isValidWSName())
					throw new IllegalArgumentException(
						"Container EPR is not a valid WS-Name.");
				
				VCGRContainerPortType container = ClientUtils.createProxy(
					VCGRContainerPortType.class, source);
				
				targetConnection = openTargetConnection(target);
				collect(container, 
					containerName.getEndpointIdentifier().toString(),
					targetConnection, doCommit);
			} else if (typeInfo.isRNS())
			{
				RNSPath sourceRoot = new RNSPath(source);
				collect(sourceRoot, target, doCommit, isRecursive);
			} else
				throw new InvalidToolUsageException(
					"Source is neither a directory nor a GenesisII container.");
		}
		finally
		{
			StreamUtils.close(targetConnection);
		}
	}
	
	public void collect(VCGRContainerPortType container,
		String containerEPI, Connection targetConnection,
		boolean doCommit) 
			throws SQLException, IOException, ClassNotFoundException
	{
		StatementBundle sBundle = null;
		int read = 0;
		WSIterable<AccountingRecordType> iterable = null;
		long maxRecordId = Long.MIN_VALUE;
		
		try
		{
			ClientUtils.setTimeout(container, 1000 * 60 * 20);
			iterable = WSIterable.axisIterable(
				AccountingRecordType.class, 
				container.iterateAccountingRecords(null).getResult(), 
				100);
			
			sBundle = new StatementBundle(targetConnection);
			
			for (AccountingRecordType art : iterable)
			{
				read++;
				maxRecordId = Math.max(maxRecordId, art.getRecordId());
				stdout.format(
					"Retrieved record %d from %s.\n", art.getRecordId(),
					containerEPI);
				
				addRecordToTargetDatabase(art, sBundle);
			}
			
			if (doCommit && read > 0)
			{
				targetConnection.commit();
				
				stdout.format(
					"Committing up to record %d on %s.",
					maxRecordId, containerEPI);
				
				container.commitAccountingRecords(
					new CommitAccountingRecordsRequestType(maxRecordId));
			} else if (read > 0)
			{
				targetConnection.rollback();
			}
		}
		finally
		{
			StreamUtils.close(iterable);
			StreamUtils.close(sBundle);
		}
	}
	
	public void collect(RNSPath sourceDirectory,
		EndpointReferenceType target, boolean doCommit, boolean isRecursive)
			throws Throwable
	{
		RNSRecursiveDescent descent = RNSRecursiveDescent.createDescent();
		descent.setAvoidCycles(true);
		descent.setRNSFilter(new IsContainerFilter());
		
		if (!isRecursive)
			descent.setMaximumDepth(1);
		
		descent.descend(sourceDirectory,
			new RNSRecursiveDescentCallbackHandler(
				target, doCommit));	
	}
	
	private class RNSRecursiveDescentCallbackHandler
		implements RNSRecursiveDescentCallback
	{
		private boolean _doCommit;
		private ExecutorService _exec;
		private EndpointReferenceType _target;
		
		private RNSRecursiveDescentCallbackHandler(
			EndpointReferenceType target, boolean doCommit)
		{
			_target = target;
			_doCommit = doCommit;
			_exec = Executors.newFixedThreadPool(_maxThreads);
		}
		
		@Override
		public void finish() throws Throwable
		{
			// Nothing to do here
		}

		@Override
		public RNSRecursiveDescentCallbackResult handleRNSPath(
			RNSPath path) throws Throwable
		{
			_count.increment();
			_exec.submit(new ThreadHandler(path));
			
			return RNSRecursiveDescentCallbackResult.ContinueLeaf;
		}
		
		private class ThreadHandler implements Runnable
		{
			RNSPath _path;
			
			public ThreadHandler(RNSPath path)
			{
				_path = path;
			}
			
			public void run()
			{
				stdout.format("Handling \"%s\".\n", _path);
				stdout.flush();
				
				Connection targetConnection = null;
				
				try
				{
					EndpointReferenceType epr = _path.getEndpoint();
					WSName name = new WSName(epr);
					if (!name.isValidWSName())
						throw new IllegalArgumentException(
							"Container EPR is not a valid WS-Name.");
					VCGRContainerPortType container = ClientUtils.createProxy(
						VCGRContainerPortType.class, epr);
					targetConnection = openTargetConnection(_target);
					collect(container, name.getEndpointIdentifier().toString(),
						targetConnection, _doCommit);
				}
				catch (Throwable cause)
				{
					stderr.format(
						"Unable to collect accounting information from container %s:  %s.\n",
						_path, cause);
					cause.printStackTrace(stderr);
				}
				finally
				{
					StreamUtils.close(targetConnection);
				}
				
				_count.decrement();
			}	
		}
	}
	
	private class Lock
	{
		private int _count;
		
		public Lock()
		{
			_count = 0;
		}
		
		public synchronized void increment()
		{
			_count++;
		}
		
		public synchronized void decrement()
		{
			_count--;
			if (_count < 0)
				throw new RuntimeException(
					"Count Underflow exception.");
			
			if (_count <= 0)
				notifyAll();
		}

		public synchronized void join() throws InterruptedException
		{
			while (_count > 0)
				wait();
		}
		
		public String toString()
		{
			return Integer.toString(_count);
		}
	}
	
	static private class IsContainerFilter implements RNSFilter
	{
		@Override
		public boolean matches(RNSPath testEntry)
		{
			try
			{
				TypeInformation typeInfo =
					new TypeInformation(testEntry.getEndpoint());
				return typeInfo.isContainer();
			}
			catch (Throwable cause)
			{
				_logger.warn("Filter encountered a problem getting the EPR.",
					cause);
				return false;
			}
		}
	}
}