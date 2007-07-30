package edu.virginia.vcgr.genii.wsdl;

import java.io.File;

public class GWsdlTranslater
{
	static public void main(String []args) throws Throwable
	{
		if (args.length != 2)
		{
			System.err.println("USAGE:  GWsdlTranslater <input.gwsdl> <output.wsdl>");
			System.exit(1);
		}
		
		WsdlDocument doc = new WsdlDocument(args[0]);
		doc.normalize();
		doc.write(new File(args[1]));
	}
}