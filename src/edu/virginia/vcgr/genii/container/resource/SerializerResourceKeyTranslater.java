package edu.virginia.vcgr.genii.container.resource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.xml.namespace.QName;

import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.ser.Base64;

public class SerializerResourceKeyTranslater extends
		StringResourceKeyTranslater
{
	static private final String _RESOURCE_KEY_NAME = "java-serializable";
	static private QName _RESOURCE_KEY_QNAME =
		new QName(GenesisIIConstants.GENESISII_NS, _RESOURCE_KEY_NAME);
	
	protected QName getRefParamQName()
	{
		return _RESOURCE_KEY_QNAME;
	}
	
	protected String toString(Object key) throws ResourceException
	{
		ByteArrayOutputStream baos = null;
		ObjectOutputStream oos = null;
		
		try
		{
			oos = new ObjectOutputStream( (baos = new ByteArrayOutputStream()) );
			oos.writeObject(key);
			oos.flush();
			return Base64.byteArrayToBase64(baos.toByteArray());
		}
		catch (IOException ioe)
		{
			throw new ResourceException(ioe.getLocalizedMessage(), ioe);
		}
		finally
		{
			StreamUtils.close(oos);
			StreamUtils.close(baos);
		}
	}
	
	protected Object fromString(String sKey) throws ResourceException
	{
		ObjectInputStream ois = null;
		
		try
		{
			ois = new ObjectInputStream(
				new ByteArrayInputStream(Base64.base64ToByteArray(sKey)));
			return ois.readObject();
		}
		catch (IOException ioe)
		{
			throw new ResourceException(ioe.getLocalizedMessage(), ioe);
		}
		catch (ClassNotFoundException cnfe)
		{
			throw new ResourceException(cnfe.getLocalizedMessage(), cnfe);
		}
		finally
		{
			StreamUtils.close(ois);
		}
	}
}