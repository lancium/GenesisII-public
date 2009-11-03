package edu.virginia.vcgr.genii.client.jsdl.sweep.test.person;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import edu.virginia.vcgr.genii.client.jsdl.sweep.Sweep;
import edu.virginia.vcgr.genii.client.jsdl.sweep.SweepConstants;

@XmlRootElement(namespace = PersonConstants.PERSON_NS, name = "PersonDocument")
public class PersonDocument
{
	@SuppressWarnings("unused")
	@XmlElement(namespace = PersonConstants.PERSON_NS, name = "Person",
		required = true, nillable = false)
	private Person _person;
	
	@XmlElement(namespace = SweepConstants.SWEEP_NS,
		name = SweepConstants.SWEEP_NAME, required = false, nillable = false)
	private Sweep _sweep;
	
	public PersonDocument()
	{
		_person = null;
		_sweep = null;
	}
	
	public Sweep removeSweep()
	{
		Sweep sweep = _sweep;
		_sweep = null;
		
		return sweep;
	}
}