package edu.virginia.vcgr.genii.client.ser;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.sql.Blob;
import java.sql.SQLException;

import javax.sql.rowset.serial.SerialBlob;
import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.io.StreamUtils;
import org.xml.sax.InputSource;

public class DBSerializer
{
	static private Log _logger = LogFactory.getLog(DBSerializer.class);

	static public Blob toBlob(Object obj, String tableName, String columnName) throws SQLException
	{
		long maxLength = BlobLimits.limits().getLimit(tableName, columnName);
		SerialBlob blob;

		if (obj == null)
			return null;

		try {
			blob = new SerialBlob(serialize(obj, maxLength));
			if (_logger.isDebugEnabled())
				_logger.debug(String.format("Created a blob of length %d bytes for %s.%s which has a "
					+ "max length of %d bytes.", blob.length(), tableName, columnName, maxLength));
			if (blob.length() > maxLength) {
				_logger.error(String.format("Error:  Blob was created with %d bytes for %s.%s, "
					+ "but the maximum length for that column is %d bytes.", blob.length(), tableName, columnName, maxLength));
			}

			return blob;
		} catch (IOException ioe) {
			System.out.println(ioe.toString());
			throw new SQLException("Unable to serialize to blob.", ioe);
		}
	}

	static public Object fromBlob(Blob b) throws SQLException
	{
		InputStream in = null;
		ObjectInputStream oin = null;

		if (b == null)
			return null;

		try {
			oin = new ObjectInputStream(in = b.getBinaryStream());
			return oin.readObject();
		} catch (IOException e) {
			String msg = "unable to deserialize from blob (io exception): " + e.getMessage();
			_logger.error(msg);
			throw new SQLException(msg, e);
		} catch (ClassNotFoundException e) {
			String msg = "unable to deserialize from blob (class not found): " + e.getMessage();
			_logger.error(msg);
			throw new SQLException(msg, e);
		} finally {
			StreamUtils.close(in);
		}
	}

	static public byte[] serialize(Object obj, long maxLength) throws IOException
	{
		ObjectOutputStream oos = null;
		ByteArrayOutputStream baos = null;

		if (obj == null)
			return null;

		try {
			baos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(baos);
			oos.writeObject(obj);
		} finally {
			StreamUtils.close(oos);
		}

		byte[] data = baos.toByteArray();
		if ((maxLength > 0) && (data.length > maxLength)) {
			if (_logger.isDebugEnabled())
				_logger.debug(String.format("The blob was too large (%d), we no longer attempt to compress it.", data.length));
		}

		return data;
	}

	static public Object deserialize(byte[] data) throws IOException, ClassNotFoundException
	{
		ObjectInputStream ois = null;

		if (data == null)
			return null;

		try {
			ois = new ObjectInputStream(new ByteArrayInputStream(data));
			return ois.readObject();
		} finally {
			StreamUtils.close(ois);
		}
	}

	static private QName _SERIALIZE_NAME = new QName("http://tempuri.org", "serialized-entity");

	static public byte[] xmlSerialize(Object obj) throws IOException
	{
		ByteArrayOutputStream baos = null;
		OutputStreamWriter writer = null;

		if (obj == null)
			return null;

		try {
			baos = new ByteArrayOutputStream();
			writer = new OutputStreamWriter(baos);
			ObjectSerializer.serialize(writer, obj, _SERIALIZE_NAME);
			writer.flush();
			return baos.toByteArray();
		} finally {
			StreamUtils.close(writer);
		}
	}

	@SuppressWarnings("unchecked")
	static public <Type> Type xmlDeserialize(Class<Type> type, byte[] data) throws IOException
	{
		ByteArrayInputStream bais = null;

		if (data == null)
			return null;

		try {
			bais = new ByteArrayInputStream(data);
			return (Type) ObjectDeserializer.deserialize(new InputSource(bais), type);
		} finally {
			StreamUtils.close(bais);
		}
	}

	static public Blob xmlToBlob(Object obj, String tableName, String columnName) throws SQLException
	{
		long maxLength = BlobLimits.limits().getLimit(tableName, columnName);

		if (obj == null)
			return null;

		try {
			byte[] data = xmlSerialize(obj);

			if (_logger.isDebugEnabled())
				_logger.debug(String.format("Created a blob of length %d bytes for %s.%s which has a "
					+ "max length of %d bytes.", data.length, tableName, columnName, maxLength));
			if (data.length > maxLength) {
				_logger.error(String.format("Error:  Blob was created with %d bytes for %s.%s, "
					+ "but the maximum length for that column is %d bytes.", data.length, tableName, columnName, maxLength));
			}

			return new SerialBlob(data);
		} catch (IOException ioe) {
			throw new SQLException("Unable to xml serialize object.", ioe);
		}
	}

	@SuppressWarnings("unchecked")
	static public <Type> Type xmlFromBlob(Class<Type> type, Blob blob) throws IOException, ClassNotFoundException
	{
		InputStream in = null;

		if (blob == null)
			return null;

		try {
			in = blob.getBinaryStream();
			return (Type) ObjectDeserializer.deserialize(new InputSource(in), type);
		} catch (SQLException sqe) {
			throw new IOException("Unable to deserialize object.", sqe);
		} finally {
			StreamUtils.close(in);
		}
	}
}