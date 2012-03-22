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
import java.util.List;
import java.util.Map;

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
import edu.virginia.vcgr.genii.container.iterator.InMemoryIteratorEntry;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.db.BasicDBResource;
import edu.virginia.vcgr.genii.container.rns.EnhancedRNSServiceImpl;

public class WSIteratorDBResource extends BasicDBResource
	implements WSIteratorResource
{
	static Map<String,List<InMemoryIteratorEntry>> mapper = new HashMap<String,List<InMemoryIteratorEntry>>();
	static Map<String, Boolean> type = new HashMap<String, Boolean>();
	static Object _lock = new Object();
	
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
		
		boolean isIndexedIterator = consParms.isIndexedIterator();
		Iterator<MessageElement> rest;
		
		int lcv = 0;
		
		setProperty(WSIteratorResource.PREFERRED_BATCH_SIZE_PROPERTY,
			consParms.preferredBatchSize());
		
		PreparedStatement stmt = null;
		
		if(!isIndexedIterator)
		{
			rest = consParms.getContentsIterator();
			
			synchronized (_lock)
			{
				type.put(getKey(), false);
			}
			
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
				StreamUtils.close(stmt);	//null check takes place inside close
			}
		}
		
		else
		{
			List<InMemoryIteratorEntry> imieList = consParms.getIndices();
			if(imieList.size() > 0 )
			{
				synchronized(_lock)
				{
					mapper.put(getKey(), imieList);
					type.put(getKey(), true);
				}
			
			}
			else
			{
				synchronized (_lock)
				{
					type.put(getKey(), false);
				}
			}
			
		}	
		
	}

	@Override
	public Collection<Pair<Long, MessageElement>> retrieveEntries(
		int firstElement, int numElements) throws ResourceException
	{
		Collection<Pair<Long, MessageElement>> ret = new ArrayList<Pair<Long,MessageElement>>(numElements);
		PreparedStatement stmt = null;
		ResultSet rs = null;
		Boolean isIndexed = false;
		List<InMemoryIteratorEntry> imieList = null;
		
		synchronized (_lock) 
		{
			isIndexed = type.get(getKey());
			if(isIndexed==null)
			{
				throw new ResourceException("Unable to retrieve entries!");
			}
			if(isIndexed == true)
			{
				imieList = mapper.get(getKey());				
			}	
		}
		
		
		if(isIndexed == false)
		{
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
		
		else
		{
			firstElement = Math.max(firstElement, 0);
			numElements = Math.max(numElements, 0);
			int lastElement = Math.min(firstElement + numElements -1, imieList.size()-1);
			
			
			for(int lcv=firstElement; lcv<=lastElement; lcv++)
			{
				InMemoryIteratorEntry entry = imieList.get(lcv);
				if(entry!=null)
				{
					//call function in RNS. We will make this generic later when we extend it to Q
					MessageElement me = EnhancedRNSServiceImpl.getIndexedContent(getConnection(),entry);
					ret.add(new Pair<Long, MessageElement>((long)lcv, me));
				}
			}
			return ret;
			
		}
	}

	@Override
	public long iteratorSize() throws ResourceException
	{
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		Boolean isIndexed;	
		List<InMemoryIteratorEntry> imieList = null;
		
		synchronized (_lock) 
		{
			isIndexed = type.get(getKey());
			if(isIndexed == null)
				throw new ResourceException(
				"Unable to query iterator for it's size!");
			
			if(isIndexed == true)
			{
				imieList = mapper.get(getKey());
				if(imieList==null)
					throw new ResourceException(
						"Unable to query iterator for it's size!");
				else
					return imieList.size();
			}	
		}
		
		if(isIndexed == false)
		{
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
		return 0;	
	}

	@Override
	public void destroy() throws ResourceException
	{
		super.destroy();
		
		PreparedStatement stmt = null;
		Boolean isIndexed = false;		
		
		synchronized (_lock) 
		{
			isIndexed = type.get(getKey());
			
			if(isIndexed == null)	//To handle container restarts, and prevents exception on the client due to a bug in our destroy client
				return;
			
			if(isIndexed==true)
				mapper.remove(getKey());
			
			type.remove(getKey());
				
		}
		
		if(isIndexed == false)
		{
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
}