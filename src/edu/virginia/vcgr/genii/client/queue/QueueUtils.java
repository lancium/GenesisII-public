package edu.virginia.vcgr.genii.client.queue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;

import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.client.security.gamlauthz.identity.Identity;

public class QueueUtils
{
	static private final byte[] serializeIdentity(Identity id)
		throws IOException
	{
		ByteArrayOutputStream baos = null;
		ObjectOutputStream oos = null;
		
		try
		{
			baos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(baos);
			oos.writeObject(id);
			oos.flush();
			return baos.toByteArray();
		}
		finally
		{
			StreamUtils.close(oos);
		}
	}
	
	static public final byte[][] serializeIdentities(
		Collection<Identity> identities) throws IOException
	{
		byte [][]ret = new byte[identities.size()][];
		int lcv = 0;
		for (Identity identity : identities)
		{
			ret[lcv++] = serializeIdentity(identity);
		}
		
		return ret;
	}
	
	static private final Identity deserializeIdentity(byte []data)
		throws IOException, ClassNotFoundException
	{
		ByteArrayInputStream bais = null;
		ObjectInputStream ois = null;
		
		try
		{
			bais = new ByteArrayInputStream(data);
			ois = new ObjectInputStream(bais);
			return (Identity)ois.readObject();
		}
		finally
		{
			StreamUtils.close(ois);
		}
	}
	
	static public final Collection<Identity> deserializeIdentities(byte [][]data)
		throws IOException, ClassNotFoundException
	{
		ArrayList<Identity> ret = new ArrayList<Identity>();
		
		for (byte []id : data)
		{
			ret.add(deserializeIdentity(id));
		}
		
		return ret;
	}
}