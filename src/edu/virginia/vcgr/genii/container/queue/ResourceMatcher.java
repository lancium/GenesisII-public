package edu.virginia.vcgr.genii.container.queue;

import java.io.Closeable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.io.StreamUtils;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.resource.ResourceException;

public class ResourceMatcher implements Closeable
{	
	static private Log _logger = LogFactory.getLog(ResourceMatcher.class);

/* Uber query
	static final private String _FIND_AVAILABLE_SLOTS =
		"SELECT res.resourceid, res.endpoint, res.slotsavail " +
		"FROM " +
			"(SELECT resourceid " +
					"FROM queuedynamicresourceinfo " +
					"WHERE available) AS avail " +
				"INNER JOIN " +
					"(SELECT resourceid, endpoint, slotsavail " +
						"FROM " +
								"(SELECT res.resourceid, res.endpoint, (res.totalslots - active.used) AS slotsavail " + 
										"FROM queueresourceinfo AS res " +
									"INNER JOIN " +
										"(SELECT resourceid, COUNT(*) AS used " + 
											"FROM queueactivejobs GROUP BY resourceid) AS active " + 
									"ON res.resourceid = active.resourceid " +
							"UNION " +
								"SELECT res.resourceid, res.endpoint, res.totalslots AS slotsavail " + 
										"FROM queueresourceinfo AS res " +
									"LEFT JOIN " +
										"queueactivejobs as active " + 
									"ON res.resourceid = active.resourceid " + 
								"WHERE active.resourceid IS NULL) " +
						"WHERE res.slotsavail > 0) AS res " +
				"ON " +
					"avail.resourceid = res.resourceid " + 
				"INNER JOIN " +
					"(SELECT resourceid " + 
						"FROM queueresources " + 
						"WHERE queueid = ?) AS x " + 
				"ON " +
					"res.resourceid = x.resourceid ";
*/
	static final private String _FIND_AVAILABLE_SLOTS =
		"SELECT res.resourceid, res.endpoint, res.slotsavail FROM " +
			"(SELECT resourceid FROM " +
				"queuedynamicresourceinfo WHERE available != 0) AS avail " +
		"INNER JOIN " +
			"(SELECT resourceid, endpoint, slotsavail FROM " +
				"queuebigquerypiece1 WHERE slotsavail > 0) AS res " +
		"ON avail.resourceid = res.resourceid " +
		"INNER JOIN " + 
			"(SELECT resourceid FROM queueresources WHERE queueid = ?) AS x " +
		"ON res.resourceid = x.resourceid";

	private IJobResourceMatcher _matcher;
	private String _queueID;
	private PreparedStatement _stmt = null;
	private ResultSet _results = null;
	private ResourceSlot _current = null;
	private int _slotsAvail = 0;
	
	public ResourceMatcher(Connection conn, String queueID)
		throws SQLException
	{
		_matcher = new DefaultJobResourceMatcher();
		_queueID = queueID;
		_stmt = conn.prepareStatement(_FIND_AVAILABLE_SLOTS);
	}
	
	protected void finalize() throws Throwable
	{
		try
		{
			close();
		}
		finally
		{
			super.finalize();
		}
	}
	
	synchronized public void close()
	{
		StreamUtils.close(_results);
		StreamUtils.close(_stmt);
		
		_results = null;
		_stmt = null;
	}
	
	public ResourceSlot match(JobRequest request)
		throws SQLException
	{
		if (_current != null && _slotsAvail > 0)
		{
			if (_matcher.matches(request, _current))
			{
				_slotsAvail--;
				return _current;
			}
		}
		
		_current = null;
		
		if (_results != null)
		{
			while (_results.next())
			{
				try
				{
					int resourceID = _results.getInt(1);
					EndpointReferenceType endpoint =
						EPRUtils.fromBytes(_results.getBytes(2));
					int slotsAvail = _results.getInt(1);
					
					_current = new ResourceSlot(resourceID, endpoint);
					_slotsAvail = slotsAvail;
					if (_matcher.matches(request, _current))
					{
						_slotsAvail--;
						return _current;
					}
				}
				catch (ResourceException re)
				{
					_logger.error("Unable to deserialize EPR from database.");
				}
			}
			
			StreamUtils.close(_results);
			_results = null;
		}
		
		_stmt.setString(1, _queueID);
		_results = _stmt.executeQuery();
		while (_results.next())
		{
			try
			{
				int resourceID = _results.getInt(1);
				EndpointReferenceType endpoint =
					EPRUtils.fromBytes(_results.getBytes(2));
				int slotsAvail = _results.getInt(1);
				
				_current = new ResourceSlot(resourceID, endpoint);
				_slotsAvail = slotsAvail;
				if (_matcher.matches(request, _current))
				{
					_slotsAvail--;
					return _current;
				}
			}
			catch (ResourceException re)
			{
				_logger.error("Unable to deserialize EPR from database.");
			}
		}
		
		_current = null;
		StreamUtils.close(_results);
		_results = null;
		
		return null;
	}
	
	static private class DefaultJobResourceMatcher implements IJobResourceMatcher
	{
		public boolean matches(JobRequest request, ResourceSlot slot)
		{
			return true;
		}
	}
}
