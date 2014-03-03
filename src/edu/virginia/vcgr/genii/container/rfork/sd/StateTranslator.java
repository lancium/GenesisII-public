package edu.virginia.vcgr.genii.container.rfork.sd;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * This interface is implemented by classes that will translate state variables from some
 * input/output form. It is used for the SimpleStateResourceFork when new state is "copied" in or
 * out.
 * 
 * @author mmm2a
 */
public interface StateTranslator
{
	/**
	 * Read a new state from the target input stream.
	 * 
	 * @param <StateType>
	 *            The type of state variable.
	 * @param originatingFork
	 *            The fork which originated the translate request.
	 * @param targetType
	 *            The desired type of the resultant state.
	 * @param input
	 *            The input stream from which to read.
	 * @return The newly created state variable.
	 * 
	 * @throws IOException
	 */
	public <StateType> StateType read(SimpleStateResourceFork<StateType> originatingFork, Class<StateType> targetType,
		InputStream input) throws IOException;

	/**
	 * Write out the current state to an output stream.
	 * 
	 * @param <StateType>
	 *            The type of state variable.
	 * @param originatingFork
	 *            The fork which originated the translate request.
	 * @param state
	 *            The state variable to write out.
	 * @param output
	 *            The output stream to write the state to.
	 * 
	 * @throws IOException
	 */
	public <StateType> void write(SimpleStateResourceFork<StateType> originatingFork, StateType state, OutputStream output)
		throws IOException;
}