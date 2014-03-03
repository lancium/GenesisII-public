package edu.virginia.vcgr.genii.client.logging;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.context.ContextException;
import edu.virginia.vcgr.genii.client.db.DatabaseConnectionPool.DBPropertyNames;
import edu.virginia.vcgr.genii.common.GeniiCommon;

public class DLogUtils {
	static private Log _logger = LogFactory.getLog(DLogUtils.class);

	public static DLogDatabase getDBConnector() {
		DLogDatabase ret = DLogDatabase.getLocalConnector();
		return ret;
	}

	public static void closeConnection(Connection conn) {
		DLogDatabase connector = getDBConnector();
		if (connector != null)
			connector.closeConnection(conn);
	}

	public static Connection getConnection() throws SQLException {

		if (!DriverManager.getDrivers().hasMoreElements()) {
			setDriver("sun.jdbc.odbc.JdbcOdbcDriver");
		}

		DLogDatabase connector = getDBConnector();
		if (connector == null)
			return null;

		Connection connection = connector.getConnection();

		return connection;
	}

	private static void setDriver(String driverClass) {
		try {
			Class.forName(driverClass);
		} catch (Exception e) {
			_logger.error("Failed to load driver", e);
		}
	}

	public static String getRPCID() {
		try {
			if (LoggingContext.hasCurrentLoggingContext())
				return LoggingContext.getCurrentLoggingContext().getCurrentID();
		} catch (ContextException e) {
			e.printStackTrace();
		}

		return null;
	}

	public static DLogDatabase addConnector(String databaseURL,
			String databaseUser, String databasePassword, String entryTable,
			String metaTable, String hierTable) {
		DLogDatabase ret = new DLogDatabase(databaseURL, databaseUser,
				databasePassword, entryTable, metaTable, hierTable);
		return ret;
	}

	private static EndpointReferenceType[] getLoggerEPRs(
			EndpointReferenceType epr) throws RemoteException {
		return new EndpointReferenceType[] { epr };
	}

	public static EndpointReferenceType getLoggerEPR(EndpointReferenceType epr)
			throws RemoteException {
		if (epr == null) {
			return null;
		}
		EndpointReferenceType[] targetEprs = getLoggerEPRs(epr);
		if (targetEprs != null) {
			for (EndpointReferenceType candidate : targetEprs) {
				try {
					// try each one until the proxy works, then send that epr
					// back
					ClientUtils.createProxy(GeniiCommon.class, candidate);
					return candidate;
				} catch (RemoteException e) {

				}
			}
		}
		return null;
	}

	public static GeniiCommon getLogger(EndpointReferenceType epr)
			throws RemoteException {
		EndpointReferenceType[] targetEprs = getLoggerEPRs(epr);
		if (targetEprs != null) {
			// try each one until the proxy works, then send it back
			for (EndpointReferenceType candidate : targetEprs) {
				try {
					GeniiCommon ret = ClientUtils.createProxy(
							GeniiCommon.class, candidate);
					return ret;
				} catch (RemoteException e) {

				}
			}
		}
		return null;
	}

	private static final String[] CREATE_STMTS = { "CREATE SCHEMA user",
			"CREATE TABLE tableName (schemaDetails)", };

	public static void initializeTables(Properties connectionProperties) {
		DBPropertyNames names = new DBPropertyNames();
		String connectString = connectionProperties
				.getProperty(names._DB_CONNECT_STRING_PROPERTY);
		String user = connectionProperties.getProperty(names._DB_USER_PROPERTY);
		String password = connectionProperties
				.getProperty(names._DB_PASSWORD_PROPERTY);
		String entryTable = connectionProperties
				.getProperty(DLogConstants._DB_ENTRY_TABLE_PROPERTY);
		String metadataTable = connectionProperties
				.getProperty(DLogConstants._DB_METADATA_TABLE_PROPERTY);
		String hierarchyTable = connectionProperties
				.getProperty(DLogConstants._DB_HIERARCHY_TABLE_PROPERTY);
		try {
			Connection con = DriverManager.getConnection(connectString, user,
					password);
			PreparedStatement stmt = null;
			String str;
			try {
				str = CREATE_STMTS[0].replace("user", user);
				stmt = con.prepareStatement(str);
				stmt.execute();
			} catch (SQLException e) {
			} finally {
				if (stmt != null && !stmt.isClosed())
					stmt.close();
			}

			try {
				str = CREATE_STMTS[1].replace("tableName", entryTable);
				str = str.replace("schemaDetails",
						DLogConstants.DLOG_ENTRY_FIELD_DETAILS);
				stmt = con.prepareStatement(str);
				stmt.execute();
				stmt.close();
			} catch (SQLException e) {
			} finally {
				if (stmt != null && !stmt.isClosed())
					stmt.close();
			}

			try {
				str = CREATE_STMTS[1].replace("tableName", metadataTable);
				str = str.replace("schemaDetails",
						DLogConstants.DLOG_METADATA_FIELD_DETAILS);
				stmt = con.prepareStatement(str);
				stmt.execute();
				stmt.close();
			} catch (SQLException e) {
			} finally {
				if (stmt != null && !stmt.isClosed())
					stmt.close();
			}

			try {
				str = CREATE_STMTS[1].replace("tableName", hierarchyTable);
				str = str.replace("schemaDetails",
						DLogConstants.DLOG_HIERARCHY_FIELD_DETAILS);
				stmt = con.prepareStatement(str);
				stmt.execute();
				stmt.close();
			} catch (SQLException e) {
			} finally {
				if (stmt != null && !stmt.isClosed())
					stmt.close();
			}

		} catch (SQLException e) {
			_logger.error("caught unexpected exception", e);
		}
	}

}
