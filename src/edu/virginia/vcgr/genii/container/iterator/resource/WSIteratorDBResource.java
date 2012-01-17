package edu.virginia.vcgr.genii.container.iterator.resource;

import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.morgan.util.Pair;
import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.client.common.ConstructionParameters;
import edu.virginia.vcgr.genii.client.iterator.WSIteratorConstructionParameters;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.ser.DBSerializer;
import edu.virginia.vcgr.genii.client.ser.ObjectDeserializer;
import edu.virginia.vcgr.genii.client.ser.ObjectSerializer;
import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.db.BasicDBResource;

public class WSIteratorDBResource extends BasicDBResource
	implements WSIteratorResource
{
	WSIteratorDBResource(ResourceKey parentKey,
		DatabaseConnectionPool connectionPool) throws SQLException
	{
		super(parentKey, connectionPool);
	}

	WSIteratorDBResource(String parentKey, Connection connection)
	{
		super(parentKey, connection);
	}
	
	@Override
	public void initialize(HashMap<QName, Object> constructionParams)
		throws ResourceException
	{
		super.initialize(constructionParams);
		if (isServiceResource())
			return;
		
		WSIteratorConstructionParameters consParms =
			(WSIteratorConstructionParameters)constructionParams.get(
				ConstructionParameters.CONSTRUCTION_PARAMTERS_QNAME);
		if (consParms == null)
			throw new ResourceException(
				"Can't create a WS-iterator without construction parameters!");
		
	
		Iterator<MessageElement> rest = consParms.remainingContents();
		int lcv = 0;
		
		PreparedStatement stmt = null;
		
		setProperty(WSIteratorResource.PREFERRED_BATCH_SIZE_PROPERTY,
			consParms.preferredBatchSize());
		
		try
		{ 
			stmt = getConnection().prepareStatement(
				"INSERT INTO iterators(" +
					"iteratorid, elementindex, contents) " +
					"VALUES (?, ?, ?)");
			
			if (rest != null)
			{
				while (rest.hasNext())
				{
					MessageElement next = rest.next();
					stmt.setString(1, getKey());
					stmt.setLong(2, (long)lcv);
					stmt.setBlob(3, 
						DBSerializer.toBlob(
							ObjectSerializer.anyToBytes(
									new MessageElement[] { next }), 
								"iterators", "contents"));
					
					stmt.addBatch();
					lcv++;
				}
			}
			
			stmt.executeBatch();
		}
		catch (SQLException e)
		{
			throw new ResourceException("Unable to create iterator!", e);
		}
		finally
		{
			StreamUtils.close(stmt);
		}
	}

	@Override
	public Collection<Pair<Long, MessageElement>> retrieveEntries(
		int firstElement, int numElements) throws ResourceException
	{
		Collection<Pair<Long, MessageElement>> ret;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		ret = new ArrayList<Pair<Long,MessageElement>>(numElements);
		
		try
		{
			stmt = getConnection().prepareStatement(
				"SELECT elementindex, contents FROM iterators WHERE " +
				"iteratorid = ? AND elementindex >= ? AND elementindex < ?");
			stmt.setString(1, getKey());
			stmt.setLong(2, firstElement);
			stmt.setLong(3, firstElement + numElements);
			
			rs = stmt.executeQuery();
			while (rs.next())
			{
				long index = rs.getLong(1);
				Blob blob = rs.getBlob(2);
				
				MessageElement me = ObjectDeserializer.anyFromBytes(
					(byte[])DBSerializer.fromBlob(blob))[0];
				ret.add(new Pair<Long, MessageElement>(index, me));
			}
			
			return ret;
		}
		catch (SQLException e)
		{
			throw new ResourceException("Unable to retrieve entries!", e);
		}
		finally
		{
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
		}
	}

	@Override
	public long iteratorSize() throws ResourceException
	{
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try
		{
			stmt = getConnection().prepareStatement(
				"SELECT COUNT(*) FROM iterators WHERE iteratorid = ?");
			stmt.setString(1, getKey());
			rs = stmt.executeQuery();
			
			if (!rs.next())
				throw new ResourceException(
					"Unable to query iterator for it's size!");
			return rs.getLong(1);
		}
		catch (SQLException e)
		{
			throw new ResourceException(
			"Unable to query iterator for it's size!", e);
		}
		finally
		{
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
		}
	}

	@Override
	public void destroy() throws ResourceException
	{
		super.destroy();
		
		PreparedStatement stmt = null;
		
		try
		{
			stmt = getConnection().prepareStatement(
				"DELETE FROM iterators WHERE iteratorid = ?");
			stmt.setString(1, _resourceKey);
			stmt.executeUpdate();
		}
		catch (SQLException e)
		{
			throw new ResourceException(
				"Error cleaning up an iterator!", e);
		}
		finally
		{
			StreamUtils.close(stmt);
		}
	}
}