package edu.virginia.vcgr.genii.client.jsdl.sweep.test.person;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

public class Person
{
	@SuppressWarnings("unused")
	@XmlAttribute(name = "name", required = true)
	private String _name;
	
	@SuppressWarnings("unused")
	@XmlElement(namespace = PersonConstants.PERSON_NS, name = "Age",
		required = true, nillable = false)
	private int _age;
	
	@SuppressWarnings("unused")
	@XmlElement(namespace = PersonConstants.PERSON_NS, name = "Address",
		required = true, nillable = false)
	private Address _address;
	
	public Person(String name, int age, Address address)
	{
		_name = name;
		_age = age;
		_address = address;
	}
	
	public Person()
	{
		this(null, 0, null);
	}
}