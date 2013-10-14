package edu.virginia.vcgr.genii.client.filesystems.email;

import java.util.LinkedList;
import java.util.Collection;
import javax.xml.bind.annotation.XmlElement;

public class AddressInfoConfiguration
{
	@XmlElement(namespace = "http://vcgr.cs.virginia.edu/filesystems/address-info", name = "addressTo", required = true)
	private Collection<String> _to = new LinkedList<String>();

	@XmlElement(namespace = "http://vcgr.cs.virginia.edu/filesystems/address-info", name = "addressCC", required = false)
	private Collection<String> _cc = new LinkedList<String>();

	@XmlElement(namespace = "http://vcgr.cs.virginia.edu/filesystems/address-info", name = "addressBCC", required = false)
	private Collection<String> _bcc = new LinkedList<String>();

	@XmlElement(namespace = "http://vcgr.cs.virginia.edu/filesystems/address-info", name = "addressFrom", required = false)
	private String _from = null;

	LinkedList<String> to()
	{
		LinkedList<String> list = new LinkedList<String>();
		for (String addr : _to)
			list.add(addr);
		return list;
	}

	LinkedList<String> cc()
	{
		LinkedList<String> list = new LinkedList<String>();
		for (String addr : _cc)
			list.add(addr);
		return list;
	}

	LinkedList<String> bcc()
	{
		LinkedList<String> list = new LinkedList<String>();
		for (String addr : _bcc)
			list.add(addr);
		return list;
	}

	String from()
	{
		return _from;
	}

	@Override
	public String toString()
	{
		String temp = "";
		temp += "To: \n";
		for (String addr : _to)
			temp += addr + ", ";
		temp += "\nCC: \n";
		for (String addr : _cc)
			temp += addr + ", ";
		temp += "\nBCC: \n";
		for (String addr : _bcc)
			temp += addr + ", ";
		temp += "\nFrom: \n" + _from;
		return temp;
	}
}
