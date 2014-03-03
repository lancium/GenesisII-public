package edu.virginia.vcgr.genii.client.logging;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.log4j.MDC;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.jdbc.JDBCAppender;
import org.apache.log4j.spi.ErrorCode;
import org.apache.log4j.spi.LoggingEvent;
import org.morgan.util.io.StreamUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class DLogJDBCAppender extends JDBCAppender
{
	// new class fields
	protected ArrayList<PatternLayout> variableList;
	protected String sql;

	protected DLogDatabase connector = null;

	protected String variables = "%m";
	protected String columns = "rpcid, message, logger, level, dated";
	protected String values = "'%X{RPCID}', ?, '%C', '%p', '%d{yyyy-MM-dd HH:mm:ss}'";

	protected String entryTable = DLogConstants.DLOG_ENTRY_TABLE_NAME;
	protected String metaTable = DLogConstants.DLOG_METADATA_TABLE_NAME;
	protected String hierTable = DLogConstants.DLOG_HIERARCHY_TABLE_NAME;

	private static boolean available = true;

	// /////////////////////////////////////////////

	public String getColumns()
	{
		return columns;
	}

	public void setColumns(String columns)
	{
		this.columns = columns;
	}

	public String getValues()
	{
		return values;
	}

	public void setValues(String values)
	{
		this.values = values;
	}

	public void setVariables(String vars)
	{
		variables = vars;
		variableList = new ArrayList<PatternLayout>();

		if (vars != null) {
			while (vars.contains(",")) {
				String next = vars.substring(0, vars.indexOf(",")).trim();
				variableList.add(new PatternLayout(next));
				vars = vars.substring(vars.indexOf(",") + 1);
			}
			if (!vars.isEmpty())
				variableList.add(new PatternLayout(vars.trim()));
		}
	}

	public String getVariables()
	{
		return variables;
	}

	public String getMetaTable()
	{
		return metaTable;
	}

	public void setMetaTable(String metaTable)
	{
		this.metaTable = metaTable;
	}

	public String getHierarchyTable()
	{
		return hierTable;
	}

	public void setHierarchyTable(String table)
	{
		this.hierTable = table;
	}

	public String getEntryTable()
	{
		return entryTable;
	}

	public void setEntryTable(String table)
	{
		entryTable = table;
	}

	// ///////////////////////////////////////

	@Override
	public void append(LoggingEvent event)
	{
		String rpcid = DLogUtils.getRPCID();
		if (rpcid != null) {
			MDC.put("RPCID", rpcid);
		} else {
			MDC.put("RPCID", "NULL RPCID");
		}

		String stackTrace = "";
		if (event.getThrowableInformation() != null) {
			for (String line : event.getThrowableStrRep()) {
				stackTrace += line + "\n";
			}
		}
		MDC.put("stackTrace", stackTrace);

		super.append(event);
	}

	protected ArrayList<String> getVariableValues(LoggingEvent event)
	{
		ArrayList<String> ret = new ArrayList<String>();
		if (variableList != null) {
			Iterator<PatternLayout> i = variableList.iterator();
			while (i.hasNext()) {
				PatternLayout p = i.next();
				ret.add(p.format(event));
			}
		}
		return ret;
	}

	@Override
	public void activateOptions()
	{
		super.activateOptions();
		connector = DLogUtils.addConnector(databaseURL, databaseUser, databasePassword, entryTable, metaTable, hierTable);

		setSql("INSERT INTO " + entryTable + " (" + columns + ") VALUES (" + values + ")");
		if (variableList == null) {
			setVariables(variables);
		}
	}

	public void execute(String eventSql, ArrayList<String> vars) throws SQLException
	{
		Connection con = null;
		PreparedStatement stmt = null;
		if (!available) {
			throw new SQLException("Logging hit a loop, bailing out now");
		}
		available = false;
		try {
			con = DLogUtils.getConnection();
			stmt = con.prepareStatement(eventSql);

			for (int i = 1; i <= vars.size(); ++i) {
				stmt.setString(i, vars.get(i - 1));
			}

			stmt.executeUpdate();

		} finally {
			available = true;
			StreamUtils.close(stmt);
			DLogUtils.closeConnection(con);
		}
	}

	@Override
	public void flushBuffer()
	{
		removes.ensureCapacity(buffer.size());

		@SuppressWarnings("unchecked")
		LoggingEvent tmp[] = ((ArrayList<LoggingEvent>) buffer).toArray(new LoggingEvent[0]);

		for (LoggingEvent logEvent : tmp) {
			try {
				String eventSql = getLogStatement(logEvent);
				ArrayList<String> vars = getVariableValues(logEvent);
				execute(eventSql, vars);
				buffer.remove(logEvent);
			} catch (SQLException e) {
				errorHandler.error("Failed to execute sql", e, ErrorCode.FLUSH_FAILURE);
			}
		}
	}
}
