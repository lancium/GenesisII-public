package edu.virginia.vcgr.genii.container.rfork.sd;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Translates state variables using the state variable's toString method 
 * and a constructor which takes as its one-and-only parameter a string.
 * 
 * @author mmm2a
 */
public class TextStateTranslator implements StateTranslator
{
	/** {@inheritDoc} */
	@Override
	public <StateType> StateType read(
		SimpleStateResourceFork<StateType> originatingFork,
		Class<StateType> targetType,
		InputStream input) throws IOException
	{
		BufferedReader reader = new BufferedReader(
			new InputStreamReader(input));
		String line = reader.readLine();
		if (line == null)
			throw new IOException("Premature end of stream.");
		
		if (targetType.equals(String.class))
			return targetType.cast(line);
		
		try
		{
			Constructor<StateType> stringCons = targetType.getConstructor(
				String.class);
			return stringCons.newInstance(line);
		}
		catch (InvocationTargetException ite)
		{
			Throwable cause = ite.getCause();
			if (cause == null)
				cause = ite;
			
			if (cause instanceof IOException)
				throw (IOException)cause;
			
			throw new IOException(
				"Unable to instantiate type using string constructor.", cause);
		}
		catch (Throwable cause)
		{
			throw new IOException(
				"Unable to instantiate type using string constructor.", cause);
		}
	}

	/** {@inheritDoc} */
	@Override
	public <StateType> void write(
		SimpleStateResourceFork<StateType> originatingFork,
		StateType state, OutputStream output)
			throws IOException
	{
		PrintStream ps = new PrintStream(output);
		ps.println(state);
		ps.flush();
	}
}