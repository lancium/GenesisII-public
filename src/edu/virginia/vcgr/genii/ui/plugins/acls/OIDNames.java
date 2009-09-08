package edu.virginia.vcgr.genii.ui.plugins.acls;

public enum OIDNames
{
	EMAILADDRESS("Email Address"),
	CN("Common Name"),
	L("Location"),
	ST("State"),
	O("Organization"),
	OU("Organizational Unit"),
	C("Country"),
	STREET("Street"),
	DC(null),
	UID("User ID"),
	T(null),
	DNQ(null),
	DNQUALIFIER(null),
	SURNAME("Surname"),
	GIVENNAME("Given Name"),
	INITIALS("Initials"),
	GENERATION(null),
	SERIALNUMBER("Serial Number");
	
	private String _friendlyName;
	
	private OIDNames(String friendlyName)
	{
		_friendlyName = friendlyName;
	}
	
	@Override
	public String toString()
	{
		return _friendlyName;
	}
}