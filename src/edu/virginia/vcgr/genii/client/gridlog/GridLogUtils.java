package edu.virginia.vcgr.genii.client.gridlog;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Vector;

import org.apache.log4j.spi.LoggingEvent;

import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;

public class GridLogUtils
{
	static public byte[] convert(LoggingEvent event) throws IOException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(event);
		oos.close();
		baos.close();
		return baos.toByteArray();
	}
	
	static public LoggingEvent convert(byte []data) throws IOException
	{
		try
		{
			ByteArrayInputStream bais = new ByteArrayInputStream(data);
			ObjectInputStream ois = new ObjectInputStream(bais);
			return (LoggingEvent)ois.readObject();
		}
		catch (ClassNotFoundException cnfe)
		{
			throw new IOException("Unable to read logging event data.", cnfe);
		}
	}
	
	static public void addTarget(ICallingContext context, GridLogTarget target)
	{
		Collection<Serializable> parameter = context.getProperty(
			GridLogConstants.CONTEXT_PARAMTER_NAME);
		if (parameter == null)
		{
			parameter = new Vector<Serializable>();
			parameter.add(target);
			context.setProperty(GridLogConstants.CONTEXT_PARAMTER_NAME,
				parameter);
		} else
			parameter.add(target);
	}
	
	static public void addTarget(GridLogTarget target) throws FileNotFoundException, IOException
	{
		ICallingContext context = ContextManager.getCurrentContext();
		addTarget(context, target);
	}
	
	static public Collection<GridLogTarget> getTargets(ICallingContext context)
	{
		Collection<Serializable> parameter = context.getProperty(
			GridLogConstants.CONTEXT_PARAMTER_NAME);
		if (parameter != null)
		{
			Collection<GridLogTarget> ret =
				new Vector<GridLogTarget>(parameter.size());
			
			for (Serializable ser : parameter)
				ret.add((GridLogTarget)ser);
			
			return ret;
		}
		
		return new Vector<GridLogTarget>();
	}
	
	static public Collection<GridLogTarget> getTargets() throws FileNotFoundException, IOException
	{
		ICallingContext context = ContextManager.getCurrentContext();
		return getTargets(context);
	}
	
	static public void removeTarget(ICallingContext context, GridLogTarget target)
	{
		Collection<Serializable> parameter = context.getProperty(
			GridLogConstants.CONTEXT_PARAMTER_NAME);
		if (parameter != null)
			parameter.remove(target);
		if (parameter.size() == 0)
			context.setProperty(GridLogConstants.CONTEXT_PARAMTER_NAME, null);
	}
	
	static public void removeTarget(GridLogTarget target) throws FileNotFoundException, IOException
	{
		ICallingContext context = ContextManager.getCurrentContext();
		removeTarget(context, target);
	}
}