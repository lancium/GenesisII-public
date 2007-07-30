package edu.virginia.vcgr.genii.client.comm.attachments;

import java.io.IOException;
import java.io.InputStream;

import javax.activation.DataHandler;
import javax.xml.soap.SOAPException;

import org.apache.axis.attachments.AttachmentPart;
import org.morgan.util.io.StreamUtils;

public class GeniiAttachment
{
	private String _name;
	private byte[] _data;
	
	public GeniiAttachment(String name, byte []data)
	{
		_name = name;
		_data = data;
	}
	
	public GeniiAttachment(byte []data)
	{
		this(null, data);
	}
	
	public byte[] getData()
	{
		return _data;
	}
	
	public String getName()
	{
		return _name;
	}
	
	static public byte[] extractData(AttachmentPart part)
		throws SOAPException, IOException
	{
		InputStream in = null;
		
		try
		{
			DataHandler handler = part.getDataHandler();
			in = handler.getInputStream();
			byte []data = new byte[in.available()];
			in.read(data);
			return data;
		}
		finally
		{
			StreamUtils.close(in);
		}
	}
}