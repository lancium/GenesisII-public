package edu.virginia.vcgr.genii.container.rfork.sd;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import edu.virginia.vcgr.genii.container.rfork.ResourceForkService;

/**
 * This is an example of a SimpleStateResourceFork.  It is not intended to
 * be used.
 * 
 * @author mmm2a
 */
@StateDescription( {XMLStateTranslator.class, TextStateTranslator.class} )
@XMLStateDescription(namespace = "http://tempuri.org", localName = "Mark")
public class ExampleSimpleStateResourceFork
	extends SimpleStateResourceFork<Integer>
{
	/**
	 * This just serves as our state variable.  We don't even really have
	 * to have a state variable, just the get/set functions.
	 */
	private int _initialValue;
	
	/**
	 * Construct a new example with an initial value and the other
	 * required parameters.
	 * 
	 * @param initValue
	 * @param service
	 * @param forkPath
	 */
	public ExampleSimpleStateResourceFork(int initValue,
		ResourceForkService service, String forkPath)
	{
		super(service, forkPath);
		
		_initialValue = initValue;
	}

	/** {@inheritDoc} */
	@Override
	protected Integer get()
	{
		System.err.println("Getting value:  " + _initialValue);
		return _initialValue;
	}

	/** {@inheritDoc */
	@Override
	protected void set(Integer state)
	{
		System.err.println("Setting value:  " + state);
		_initialValue = state;
	}
	
	static public void main(String []args) throws Throwable
	{
		ExampleSimpleStateResourceFork e = 
			new ExampleSimpleStateResourceFork(7,
				null, "/test");
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		e.snapshotState(baos);
		e.snapshotState(System.out);
		ByteArrayInputStream bais = new ByteArrayInputStream(
			baos.toByteArray());
		e.modifyState(bais);
	}
}