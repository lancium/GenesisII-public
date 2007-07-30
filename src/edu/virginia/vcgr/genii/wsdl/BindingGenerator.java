package edu.virginia.vcgr.genii.wsdl;

import java.io.File;

public class BindingGenerator
{
	static public void main(String []args) throws Throwable
	{
		if (args.length != 5)
		{
			System.err.println("USAGE:  BindingGenerator <input.wsdl> <output.wsdl> <port-type> <binding-name> <service-name>");
			System.exit(1);
		}
		
		WsdlDocument doc = new WsdlDocument(args[0]);
		doc.normalize();
		doc.generateBindingAndService(
			args[4],	
			args[3], WsdlUtils.getQNameFromString(args[2]), new File(args[1]));
	}
}
