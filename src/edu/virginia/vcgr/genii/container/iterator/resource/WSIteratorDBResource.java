package edu.virginia.vcgr.genii.container.iterator.resource;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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

import org.apache.axis.message.MessageElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.Pair;
import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.client.comm.axis.Elementals;
import edu.virginia.vcgr.genii.client.common.ConstructionParameters;
import edu.virginia.vcgr.genii.client.common.GenesisHashMap;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.ser.DBSerializer;
import edu.virginia.vcgr.genii.client.ser.ObjectDeserializer;
import edu.virginia.vcgr.genii.client.ser.ObjectSerializer;
import edu.virginia.vcgr.genii.container.db.ServerDatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.exportdir.lightweight.LightWeightExportDirFork;
import edu.virginia.vcgr.genii.container.iterator.InMemoryIteratorEntry;
import edu.virginia.vcgr.genii.container.iterator.InMemoryIteratorWrapper;
import edu.virginia.vcgr.genii.container.iterator.WSIteratorConstructionParameters;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.db.BasicDBResource;

public class WSIteratorDBResource extends BasicDBResource implements WSIteratorResource
{
	static private Log _logger = LogFactory.getLog(WSIteratorDBResource.class);

	static Map<String, InMemoryIteratorWrapper> mapper = new HashMap<String, InMemoryIteratorWrapper>();
	static Map<String, Boolean> type = new HashMap<String, Boolean>();
	static Object _lock = new Object();

	WSIteratorDBResource(ResourceKey parentKey, ServerDatabaseConnectionPool connectionPool) throws SQLException
	{
		super(parentKey, connectionPool);
	}

	WSIteratorDBResource(String parentKey, Connection connection)
	{
		super(parentKey, connection);
	}

	@Override
	public void initialize(GenesisHashMap constructionParams) throws ResourceException
	{
		super.initialize(constructionParams);
		if (isServiceResource())
			return;

		WSIteratorConstructionParameters consParms =
			(WSIteratorConstructionParameters) constructionParams.get(ConstructionParameters.CONSTRUCTION_PARAMETERS_QNAME);
		if (consParms == null)
			throw new ResourceException("Can't create a WS-iterator without construction parameters!");

		boolean isIndexedIterator = consParms.isIndexedIterator();
		Iterator<MessageElement> rest;

		int lcv = 0;

		setProperty(WSIteratorResource.PREFERRED_BATCH_SIZE_PROPERTY, consParms.preferredBatchSize());

		PreparedStatement stmt = null;

		if (!isIndexedIterator) {
			rest = consParms.getContentsIterator();

			synchronized (_lock) {
				type.put(getKey(), false);
			}

			try {

				stmt =
					getConnection().prepareStatement(
						"INSERT INTO iterators(" + "iteratorid, elementindex, contents) " + "VALUES (?, ?, ?)");

				if (rest != null) {
					while (rest.hasNext()) {
						MessageElement next = rest.next();
						stmt.setString(1, getKey());
						stmt.setLong(2, (long) lcv);
						stmt.setBlob(3, DBSerializer.toBlob(ObjectSerializer.anyToBytes(Elementals.unitaryArray(next)),
							"iterators", "contents"));
						stmt.addBatch();
						lcv++;
					}
				}
				stmt.executeBatch();
			} catch (SQLException e) {
				throw new ResourceException("Unable to create iterator!", e);
			} finally {
				StreamUtils.close(stmt); // null check takes place inside close
			}
		}

		else {
			InMemoryIteratorWrapper imiw = consParms.getWrapper();

			if (imiw == null || imiw.getIndices() == null || imiw.getIndices().size() == 0) {
				synchronized (_lock) {
					type.put(getKey(), false);
				}

			} else {
				synchronized (_lock) {
					mapper.put(getKey(), imiw);
					type.put(getKey(), true);
				}
			}

		}

	}

	@Override
	public Collection<Pair<Long, MessageElement>> retrieveEntries(int firstElement, int numElements) throws ResourceException
	{
		Collection<Pair<Long, MessageElement>> ret = new ArrayList<Pair<Long, MessageElement>>(numElements);
		PreparedStatement stmt = null;
		ResultSet rs = null;
		Boolean isIndexed = false;
		InMemoryIteratorWrapper imiw = null;

		synchronized (_lock) {
			isIndexed = type.get(getKey());
			if (isIndexed == null) {
				throw new ResourceException("Unable to retrieve entries!");
			}
			if (isIndexed == true) {
				imiw = mapper.get(getKey());
			}
		}

		if (isIndexed == false) {
			try {

				stmt =
					getConnection().prepareStatement(
						"SELECT elementindex, contents FROM iterators WHERE "
							+ "iteratorid = ? AND elementindex >= ? AND elementindex < ?");

				stmt.setString(1, getKey());
				stmt.setLong(2, firstElement);
				stmt.setLong(3, firstElement + numElements);

				rs = stmt.executeQuery();
				while (rs.next()) {
					long index = rs.getLong(1);
					Blob blob = rs.getBlob(2);

					MessageElement me = ObjectDeserializer.anyFromBytes((byte[]) DBSerializer.fromBlob(blob))[0];
					ret.add(new Pair<Long, MessageElement>(index, me));
				}

				return ret;

			} catch (SQLException e) {
				throw new ResourceException("Unable to retrieve entries!", e);
			} finally {
				StreamUtils.close(rs);
				StreamUtils.close(stmt);
			}
		}

		else {
			// ---------------------------------------------------------------------------------------------------
			// Checking if short form is requested by the client. Properly handling this attributes
			// is Prof.'s
			// responsibility. ASG
			// ---------------------------------------------------------------------------------------------------
			boolean ShortForm = false;
			try {
				ICallingContext context = ContextManager.getCurrentContext();
				Object form = context.getSingleValueProperty("RNSShortForm");
				if (form != null && Boolean.TRUE.equals(form)) {
					ShortForm = true;
					_logger.info("Short form attribute found to be set to true in the calling context");
				}
			} catch (Exception e) {
				_logger.trace("could not get information about the short form");
			}
			// ---------------------------------------------------------------------------------------------------

			firstElement = Math.max(firstElement, 0);
			numElements = Math.max(numElements, 0);
			List<InMemoryIteratorEntry> imieList = imiw.getIndices();
			Object[] commonObjs = imiw.getCommonMembers();// loop-invariant
			String invokee = imiw.getClassName();

			Class<?> clazz;
			Method meth;

			try {
				clazz = Class.forName(invokee);
				meth =
					clazz.getMethod("getIndexedContent", new Class[] { Connection.class, InMemoryIteratorEntry.class,
						Object[].class, boolean.class });
			} catch (ClassNotFoundException e) {
				throw new ResourceException("Unable to retrieve entries!", e);
			} catch (NoSuchMethodException e) {
				throw new ResourceException("Unable to retrieve entries!", e);
			}

			int lastElement = Math.min(firstElement + numElements - 1, imieList.size() - 1);

			if (invokee.indexOf("LightWeight") != -1) {
				try {
					return LightWeightExportDirFork.getEntries(imieList, firstElement, numElements, commonObjs, ShortForm);
				} catch (IOException e) {
					throw new ResourceException("Unable to retrieve entries!", e);
				}
			}

			for (int lcv = firstElement; lcv <= lastElement; lcv++) {
				InMemoryIteratorEntry entry = imieList.get(lcv);
				if (entry != null) {

					try {
						Object obj = meth.invoke(null, getConnection(), entry, commonObjs, ShortForm);
						if (obj instanceof MessageElement) {
							ret.add(new Pair<Long, MessageElement>((long) lcv, (MessageElement) obj));
						} else {
							_logger.error("unknown type in list: " + obj.getClass().getCanonicalName());
						}
					}

					catch (IllegalArgumentException e) {
						throw new ResourceException("Unable to retrieve entries!", e);
					} catch (IllegalAccessException e) {
						throw new ResourceException("Unable to retrieve entries!", e);
					} catch (InvocationTargetException e) {
						throw new ResourceException("Unable to retrieve entries!", e);
					}

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

		InMemoryIteratorWrapper imiw = null;

		synchronized (_lock) {
			isIndexed = type.get(getKey());
			if (isIndexed == null)
				throw new ResourceException("Unable to query iterator for it's size!");

			if (isIndexed == true) {
				imiw = mapper.get(getKey());

				if (imiw == null)
					throw new ResourceException("Unable to query iterator for it's size!");
				else
					return imiw.getIndices().size();
			}
		}

		if (isIndexed == false) {
			try {
				stmt = getConnection().prepareStatement("SELECT COUNT(*) FROM iterators WHERE iteratorid = ?");
				stmt.setString(1, getKey());
				rs = stmt.executeQuery();

				if (!rs.next())
					throw new ResourceException("Unable to query iterator for it's size!");
				return rs.getLong(1);
			} catch (SQLException e) {
				throw new ResourceException("Unable to query iterator for it's size!", e);
			} finally {
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

		synchronized (_lock) {
			isIndexed = type.get(getKey());

			if (isIndexed == null) // To handle container restarts, and prevents exception on the
									// client due to a bug in our destroy client
				return;

			if (isIndexed == true)
				mapper.remove(getKey());

			type.remove(getKey());

		}

		if (isIndexed == false) {
			try {
				stmt = getConnection().prepareStatement("DELETE FROM iterators WHERE iteratorid = ?");
				stmt.setString(1, _resourceKey);
				stmt.executeUpdate();
			} catch (SQLException e) {
				throw new ResourceException("Error cleaning up an iterator!", e);
			} finally {
				StreamUtils.close(stmt);
			}
		}
	}
}