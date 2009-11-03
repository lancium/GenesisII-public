package edu.virginia.vcgr.genii.client.jsdl.sweep.test.person;

import javax.xml.bind.annotation.XmlElement;

public class Address
{
	@SuppressWarnings("unused")
	@XmlElement(namespace = PersonConstants.PERSON_NS, name = "Street",
		required = true, nillable = false)
	private String _street;
	
	@SuppressWarnings("unused")
	@XmlElement(namespace = PersonConstants.PERSON_NS, name = "City",
		required = true, nillable = false)
	private String _city;
	
	@SuppressWarnings("unused")
	@XmlElement(namespace = PersonConstants.PERSON_NS, name = "State",
		required = true, nillable = false)
	private String _state;
	
	@SuppressWarnings("unused")
	@XmlElement(namespace = PersonConstants.PERSON_NS, name = "ZipCode",
		required = true, nillable = false)
	private String _zipcode;
	
	@SuppressWarnings("unused")
	private Address()
	{
		this(null, null, null, null);
	}
	
	public Address(String street, String city, String state, String zipcode)
	{
		_street = street;
		_city = city;
		_state = state;
		_zipcode = zipcode;
	}
}