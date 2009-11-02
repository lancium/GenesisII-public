package edu.virginia.vcgr.genii.client.jsdl.sweep.test;

import java.io.IOException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.xpath.XPathExpressionException;

import edu.virginia.vcgr.genii.client.jsdl.sweep.Sweep;

public class Driver
{
	static private Sweep unmarshall(JAXBContext context, String resourceName) throws JAXBException
	{
		Unmarshaller u = context.createUnmarshaller();
		Sweep sweep = (Sweep)u.unmarshal(Driver.class.getResourceAsStream(resourceName));
		Marshaller m = context.createMarshaller();
		m.marshal(sweep, System.out);
		System.out.println();

		return sweep;
	}
	
	static public void main(String []args) throws JAXBException, IOException, XPathExpressionException
	{
		JAXBContext context = JAXBContext.newInstance(Sweep.class);
		
		unmarshall(context, "example-sweep-1.xml");
		unmarshall(context, "example-sweep-2.xml");
		unmarshall(context, "example-sweep-3.xml");
	}
}