package edu.virginia.vcgr.genii.container.queue;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.ggf.jsdl.JobDefinition_Type;

import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.ser.DBSerializer;

public class JobRequest
{
	private int _jobID;
	private ICallingContext _callingContext = null;
	private byte[] _jsdlBytes;
	private JobDefinition_Type _jsdl = null;
	private int _failedAttempts;
	
	public JobRequest(ResultSet rs) throws SQLException
	{
		try
		{
			_jobID = rs.getInt(1);
			_callingContext = (ICallingContext)DBSerializer.fromBlob(
				rs.getBlob(2));
			_jsdlBytes = rs.getBytes(3);
			_failedAttempts = rs.getInt(4);
		}
		catch (ClassNotFoundException cnfe)
		{
			throw new SQLException(
				"Unable to deserialize calling context -- class not found.");
		}
		catch (IOException ioe)
		{
			throw new SQLException(
				"Unable to deserialize calling context -- IOException.");
		}
	}
	
	public int getJobID()
	{
		return _jobID;
	}
	
	public ICallingContext getCallingContext() 
		throws IOException, ClassNotFoundException
	{
		return _callingContext;
	}
	
	public JobDefinition_Type getJSDL()
		throws IOException
	{
		synchronized(this)
		{
			if (_jsdl == null && _jsdlBytes != null)
				_jsdl = DBSerializer.xmlDeserialize(
					JobDefinition_Type.class, _jsdlBytes);
		}
		
		return _jsdl;
	}
	
	public int getFailedAtempts()
	{
		return _failedAttempts;
	}
}
