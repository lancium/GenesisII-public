package edu.virginia.vcgr.genii.container.rfork.sd;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;

import org.apache.commons.logging.Log;
import org.mortbay.log.LogFactory;

import edu.virginia.vcgr.genii.client.security.authz.RWXMappingResolver;
import edu.virginia.vcgr.genii.container.rfork.AbstractStreamableByteIOFactoryResourceFork;
import edu.virginia.vcgr.genii.container.rfork.ResourceForkService;

/**
 * The SimpleStateResourceFork is an implementation of the 
 * StreamableByteIOFactory fork which can automatically get
 * and or set a piece of state information based off of
 * having data cat'ted into it or from it.
 * 
 * This class has a further restriction in that when a user
 * inherits from this class, he or she MUST supply a
 * StateDescription annotation describing this piece of
 * state.
 * 
 * @author mmm2a
 *
 * @param <StateType> This is any state type that you want.  The only
 * restriction is that you must have a translater that can translate
 * to and from this state type.
 */
public abstract class SimpleStateResourceFork<StateType>
	extends AbstractStreamableByteIOFactoryResourceFork
{
	static private Log _logger = LogFactory.getLog(
		SimpleStateResourceFork.class);
	
	/**
	 * The user overrides this method to implement the get
	 * version of the state variable.  If the user describes this
	 * state fork as non-readable, then this method is guaranteed
	 * never to be called.
	 * 
	 * @return The state variable as it currently exists.
	 * @throws Throwable We allow the user to throw anything he
	 * or she wishes and we translate for them later.
	 */
	protected abstract StateType get() throws Throwable;
	protected abstract void set(StateType state) throws Throwable;
	
	private Class<StateType> _stateTypeClass;
	
	private StateDescription getStateDescription()
		throws IOException
	{
		StateDescription description = getClass().getAnnotation(
			StateDescription.class);
		if (description == null)
			throw new IOException(
				"Missing required annotation \"StateDescription\" " +
				"on class \"" + getClass().getName() + "\".");
		
		return description;
	}
	
	@SuppressWarnings("unchecked")
	protected SimpleStateResourceFork(ResourceForkService service, 
		String forkPath)
	{
		super(service, forkPath);
		
		try
		{
			_stateTypeClass = (Class<StateType>)getClass().getDeclaredMethod(
				"get").getReturnType();
		}
		catch (Throwable cause)
		{
			// Shouldn't happen.
			_logger.fatal("Got an exception trying to acquire type class.", 
				cause);
		}
	}
	
	static private StateTranslator instantiateTranslater(
		Class<? extends StateTranslator> translaterClass)
	{
		try
		{
			Constructor<? extends StateTranslator> cons =
				translaterClass.getConstructor();
			return cons.newInstance();
		}
		catch (Throwable cause)
		{
			_logger.warn("Unable to instantiate state translater.", cause);
			return null;
		}
	}
	
	/** {@inheritDoc} */
	@Override
	@RWXMappingResolver(SimpleStateRWXMappingResolver.class)
	final public void modifyState(InputStream source) throws IOException
	{
		StateDescription description = getStateDescription();
		
		if (!description.writable())
			throw new IOException(
				"This state fork is not writable!");
		
		if (!source.markSupported())
			throw new IOException(
				"The modify state function equires a " +
				"stream that supports marking.");
		
		for (Class<? extends StateTranslator> translaterClass : 
			description.value())
		{
			source.mark(Integer.MAX_VALUE);
			
			StateTranslator translater = instantiateTranslater(translaterClass);
			if (translater != null)
			{
				try
				{
					StateType ret = translater.read(this, 
						_stateTypeClass, source);
					set(ret);
					return;
				}
				catch (Throwable cause)
				{
					_logger.debug("Unable to translate state variable.", 
						cause);
				}
			}
			
			source.reset();
		}
		
		throw new IOException(
			"Unable to translate state from input stream.");
	}

	/** {@inheritDoc} */
	@Override
	@RWXMappingResolver(SimpleStateRWXMappingResolver.class)
	final public void snapshotState(OutputStream sink) throws IOException
	{
		StateDescription description = getStateDescription();
		
		if (!description.readable())
			return;
		
		StateType state;
		try
		{
			state = get();
		}
		catch (IOException ioe)
		{
			throw ioe;
		}
		catch (Throwable cause)
		{
			throw new IOException("Unable to acquire state.", cause);
		}
		
		for (Class<? extends StateTranslator> translaterClass :
			description.value())
		{
			StateTranslator translater = instantiateTranslater(translaterClass);
			if (translater != null)
			{
				translater.write(this, state, sink);
				return;
			}
		}
	}
	
	@Override
	final public boolean readable()
	{
		try
		{
			return getStateDescription().readable();
		}
		catch (IOException ioe)
		{
			throw new RuntimeException(
				"Unable to get readable attribute from stream.", ioe);
		}
	}
	
	@Override
	final public boolean writable()
	{
		try
		{
			return getStateDescription().writable();
		}
		catch (IOException ioe)
		{
			throw new RuntimeException(
				"Unable to get writable attribute from stream.", ioe);
		}
	}
}