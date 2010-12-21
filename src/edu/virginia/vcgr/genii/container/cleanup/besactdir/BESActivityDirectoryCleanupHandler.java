package edu.virginia.vcgr.genii.container.cleanup.besactdir;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.client.bes.BESConstructionParameters;
import edu.virginia.vcgr.genii.client.common.ConstructionParameters;
import edu.virginia.vcgr.genii.client.io.FileSystemUtils;
import edu.virginia.vcgr.genii.client.ser.DBSerializer;
import edu.virginia.vcgr.genii.container.bes.activity.BESActivityServiceImpl;
import edu.virginia.vcgr.genii.container.cleanup.CleanupHandler;

final public class BESActivityDirectoryCleanupHandler implements CleanupHandler
{
	static private Log _logger = LogFactory.getLog(
		BESActivityDirectoryCleanupHandler.class);
	
	static final private long KEEP_WINDOW = 31l;
	static final private TimeUnit KEEP_WINDOW_UNITS = TimeUnit.DAYS;
	
	static final private Pattern GUID_PATTERN = Pattern.compile(
		"^\\p{XDigit}{8}-\\p{XDigit}{4}-\\p{XDigit}{4}-\\p{XDigit}{4}-\\p{XDigit}{12}$");
	
	static private Set<File> findBESDirectories(Connection connection)
		throws Throwable
	{
		_logger.info("Finding BES Parent Directories for cleanup.");
		Set<File> ret = new HashSet<File>();
		
		ResultSet rs = null;
		PreparedStatement stmt = null;
		
		try
		{
			stmt = connection.prepareStatement(
				"SELECT b.propvalue FROM bespolicytable AS a, properties AS b WHERE a.besid = b.resourceid AND b.propname = ?");
			stmt.setString(1, ConstructionParameters.CONSTRUCTION_PARAMTERS_QNAME.toString());
			rs = stmt.executeQuery();
			
			while (rs.next())
			{
				BESConstructionParameters consParms =
					(BESConstructionParameters)DBSerializer.fromBlob(
						rs.getBlob(1));
				ret.add(BESActivityServiceImpl.getCommonDirectory(consParms).getAbsoluteFile());
			}
		}
		finally
		{
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
		}
		
		return ret;
	}

	static private Map<File, Set<String>> createPotentialSet(Set<File> parentDirectories)
	{
		_logger.info("Iterating through directories for activity working dirs.");
		
		Map<File, Set<String>> dir2dirNameMap = new HashMap<File, Set<String>>();
		for (File parentDir : parentDirectories)
		{
			Set<String> children = new HashSet<String>();
			dir2dirNameMap.put(parentDir, children);
			
			File []childrenFiles = parentDir.listFiles();
			if (childrenFiles != null)
			{
				for (File child : childrenFiles)
				{
					Matcher matcher = GUID_PATTERN.matcher(child.getName());
					if (matcher.matches())
						children.add(child.getName());
				}
			}
		}
		
		return dir2dirNameMap;
	}
	
	static private void removeRecentDirectoryEntries( 
		Map<File, Set<String>> dir2dirNameMap) throws SQLException
	{
		long now = System.currentTimeMillis();
		Set<String> childrenToRemove = new HashSet<String>();
		
		for (Map.Entry<File, Set<String>> entry : dir2dirNameMap.entrySet())
		{
			File parentDir = entry.getKey();
			Set<String> children = entry.getValue();
			for (String child : children)
			{
				File childDir = new File(parentDir, child);
				if ( (now - childDir.lastModified()) <= 
					KEEP_WINDOW_UNITS.toMillis(KEEP_WINDOW))
					childrenToRemove.add(child);
			}
			
			children.removeAll(childrenToRemove);
		}
	}
	
	@Override
	final public void doCleanup(Connection connection, boolean enactCleanup)
	{
		try
		{
			Set<File> besDirectories = findBESDirectories(connection);
			Map<File, Set<String>> dir2dirNameMap = createPotentialSet(besDirectories);
			removeRecentDirectoryEntries(dir2dirNameMap);
			
			for (Map.Entry<File, Set<String>> entry : dir2dirNameMap.entrySet())
			{
				File parent = entry.getKey();
				Set<String> children = entry.getValue();
				
				for (String child : children)
				{
					File childFile = new File(parent, child);
					if (enactCleanup)
					{
						_logger.info(String.format("Cleaning up old activity directory %s [%tc]\n",
							childFile, childFile.lastModified()));
						FileSystemUtils.recursiveDelete(childFile, false);
					} else
					{
						_logger.info(String.format("Not cleaning up old activity directory %s [%tc]\n",
							childFile, childFile.lastModified()));
					}
				}
			}
		}
		catch (Throwable cause)
		{
			_logger.error("Couldn't clean up activity directories.", cause);
		}
	}
}