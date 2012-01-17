package edu.virginia.vcgr.genii.container.cservices.accounting;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.pwrapper.ElapsedTime;
import edu.virginia.vcgr.genii.client.security.SecurityUtils;
import edu.virginia.vcgr.genii.container.AccountingRecordType;
import edu.virginia.vcgr.genii.container.bes.BESAttributesHandler;
import edu.virginia.vcgr.genii.container.cservices.AbstractContainerService;
import edu.virginia.vcgr.genii.security.credentials.identity.Identity;
import edu.virginia.vcgr.jsdl.OperatingSystemNames;
import edu.virginia.vcgr.jsdl.ProcessorArchitecture;

public class AccountingService extends AbstractContainerService
{
	static private Log _logger = LogFactory.getLog(AccountingService.class);
	
	static final public String SERVICE_NAME = "Accounting Service";
	
	@Override
	protected void loadService() throws Throwable
	{
		_logger.info(String.format("Loading %s.", SERVICE_NAME));
		Connection connection = null;
		
		try
		{
			connection = getConnectionPool().acquire(true);
			AccountingDatabase.createTables(connection);
		}
		finally
		{
			getConnectionPool().release(connection);
		}
	}

	@Override
	protected void startService() throws Throwable
	{
		// This isn't an active service, so nothing needs to be done.
	}
	
	public AccountingService()
	{
		super(SERVICE_NAME);
	}
	
	public void addAccountingRecord(
		ICallingContext callingContext,
		String besepi,
		ProcessorArchitecture arch,
		OperatingSystemNames os,
		String machineName,
		Collection<String> commandLine,
		int exitCode,
		ElapsedTime user, ElapsedTime kernel, ElapsedTime wallclock,
		long maximumRSS) throws SQLException,
			IOException, GeneralSecurityException
	{
		Connection conn = null;
		
		if (callingContext == null)
			callingContext = ContextManager.getCurrentContext();
		if (arch == null)
			arch = ProcessorArchitecture.getCurrentArchitecture();
		if (arch == null)
			arch = ProcessorArchitecture.x86_64;
		
		if (os == null)
			os = OperatingSystemNames.getCurrentOperatingSystem();
		if (machineName == null)
			machineName = BESAttributesHandler.getName();
		
		Collection<Identity> identities = 
			SecurityUtils.getCallerIdentities(callingContext);
		
		try
		{
			conn = getConnectionPool().acquire(false);
			AccountingDatabase.addRecord(conn, besepi,
				arch, os, machineName, commandLine, exitCode, 
				user, kernel, wallclock, maximumRSS, identities);
			conn.commit();
		}
		finally
		{
			getConnectionPool().release(conn);
		}
	}
	
	public Collection<AccountingRecordType> getAccountingRecords()
		throws SQLException, IOException
	{
		Connection conn = null;

		try
		{
			conn = getConnectionPool().acquire(true);
			return AccountingDatabase.getAccountingRecords(conn);
		}
		finally
		{
			getConnectionPool().release(conn);
		}
	}
	
	public void deleteAccountingRecords(long lastRecordToDelete)
		throws SQLException
	{
		Connection conn = null;

		try
		{
			conn = getConnectionPool().acquire(false);
			AccountingDatabase.deleteAccountingRecords(
				conn, lastRecordToDelete);
			conn.commit();
		}
		finally
		{
			getConnectionPool().release(conn);
		}
	}
}