package edu.virginia.vcgr.genii.client.db;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.client.stats.ContainerStatistics;
import edu.virginia.vcgr.genii.client.stats.DBConnectionDataPoint;
import edu.virginia.vcgr.genii.client.stats.DatabaseHistogramStatistics;

public class DatabaseConnectionInterceptor implements InvocationHandler {
	static private Log _logger = LogFactory
			.getLog(DatabaseConnectionInterceptor.class);

	private Method _CLOSE_METHOD;
	private Method _COMMIT_METHOD;
	private Method _ROLLBACK_METHOD;

	private DBConnectionDataPoint _stat = null;
	private Object _instance;
	private DatabaseHistogramStatistics _histo = null;

	public DatabaseConnectionInterceptor(Object instance) {
		try {
			_CLOSE_METHOD = Connection.class.getDeclaredMethod("close",
					new Class[0]);
			_COMMIT_METHOD = Connection.class.getDeclaredMethod("commit",
					new Class[0]);
			_ROLLBACK_METHOD = Connection.class.getDeclaredMethod("rollback",
					new Class[0]);
		} catch (Throwable t) {
			_logger.error("Couldn't load the close/commit/rollback methods.", t);
		}
		_instance = instance;
	}

	public Connection getConnection() {
		return (Connection) _instance;
	}

	public void setAcquired() {
		_stat = ContainerStatistics.instance().getDatabaseStatistics()
				.openConnection();
		_histo = ContainerStatistics.instance()
				.getDatabaseHistogramStatistics();
		_histo.addActiveConnection();
	}

	public void setReleased() {
		if (_stat != null) {
			_stat.markClosed();
			_histo.removeActiveConncetion();
		}

		_stat = null;
		_histo = null;
	}

	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		if (method.equals(_CLOSE_METHOD)) {
			_logger.error("Someone tried to close a pooled connection.");
			throw new RuntimeException(
					"Someone tried to close a pooled connection.");
		} else if (method.equals(_COMMIT_METHOD)) {
			if (!((Connection) _instance).getAutoCommit())
				return method.invoke(_instance, args);
			else
				return null;
		} else if (method.equals(_ROLLBACK_METHOD)) {
			if (!((Connection) _instance).getAutoCommit())
				return method.invoke(_instance, args);
			else
				return null;
		}

		boolean interrupted = Thread.interrupted();
		try {
			return method.invoke(_instance, args);
		} catch (Throwable cause) {
			_logger.error("Error calling method on connection.", cause);
			if (cause instanceof InvocationTargetException) {
				throw cause.getCause();
			}

			throw cause;
		} finally {
			if (interrupted)
				Thread.currentThread().interrupt();
		}
	}
}
