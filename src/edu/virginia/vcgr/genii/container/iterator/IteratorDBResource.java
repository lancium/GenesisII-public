package edu.virginia.vcgr.genii.container.iterator;

import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;

import org.apache.axis.message.MessageElement;
import org.apache.axis.types.UnsignedInt;
import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.ser.AnyHelper;
import edu.virginia.vcgr.genii.client.ser.DBSerializer;
import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.db.BasicDBResource;
import edu.virginia.vcgr.genii.iterator.IteratorMemberType;

public class IteratorDBResource extends BasicDBResource implements
		IteratorResource
{
	static private final String ITERATOR_ID_PROPERTY = 
		"edu.virginia.vcgr.genii.iterator.id-property";
	
	public IteratorDBResource(
			ResourceKey parentKey, 
			DatabaseConnectionPool connectionPool)
		throws SQLException
	{
		super(parentKey, connectionPool);
	}
	
	@Override
	public void setIteratorID(String id) throws ResourceException
	{
		setProperty(ITERATOR_ID_PROPERTY, id);
	}
	
	@Override
	public IteratorMemberType[] get(long startElement, int maxLength)
			throws ResourceException
	{
		Collection<IteratorMemberType> ret = 
			new LinkedList<IteratorMemberType>();
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		String id = (String)getProperty(ITERATOR_ID_PROPERTY);
		if (id == null)
			throw new ResourceException("Corrupt iterator -- id not set.");
		
		try
		{
			stmt = _connection.prepareStatement(
				"SELECT contents FROM iterators WHERE " +
					"iteratorid = ? AND elementindex >= ? AND elementindex < ?");
			stmt.setString(1, id);
			stmt.setLong(2, startElement);
			stmt.setLong(3, (startElement + maxLength));
			
			rs = stmt.executeQuery();
			while (rs.next())
			{
				Blob blob = rs.getBlob(1);
				
				ret.add(new IteratorMemberType(new MessageElement[] {
					AnyHelper.toAny(DBSerializer.fromBlob(blob)) },
					new UnsignedInt(startElement++)));
			}
			
			return ret.toArray(new IteratorMemberType[0]);
		}
		catch (SQLException sqe)
		{
			throw new ResourceException(
				"Database error while retrieving iteration elements.", sqe);
		}
		finally
		{
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
		}
	}

	@Override
	public long size() throws ResourceException
	{
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		String id = (String)getProperty(ITERATOR_ID_PROPERTY);
		if (id == null)
			throw new ResourceException("Corrupt iterator -- id not set.");
		
		try
		{
			stmt = _connection.prepareStatement(
				"SELECT COUNT(*) FROM iterators WHERE " +
					"iteratorid = ?");
			stmt.setString(1, id);
			
			rs = stmt.executeQuery();
			if (!rs.next())
				throw new ResourceException(
					"Unable to obtain element count for iterator.");

			return rs.getLong(1);
		}
		catch (SQLException sqe)
		{
			throw new ResourceException(
				"Database error while retrieving iteration elements.", sqe);
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
		String id = (String)getProperty(ITERATOR_ID_PROPERTY);
		if (id == null)
			throw new ResourceException("Corrupt iterator -- id not set.");
		
		super.destroy();
		PreparedStatement stmt = null;
		
		try
		{
			stmt = _connection.prepareStatement("DELETE FROM iterators WHERE iteratorid = ?");
			stmt.setString(1, id);
			stmt.executeUpdate();
		}
		catch (SQLException sqe)
		{
			throw new ResourceException(
				"Database error while deleting iteration elements.", sqe);
		}
		finally
		{
			StreamUtils.close(stmt);
		}
	}	
}