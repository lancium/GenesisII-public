package edu.virginia.vcgr.genii.client.cmd.tools;

import javax.xml.namespace.QName;

import org.ggf.bes.factory.GetFactoryAttributesDocumentResponseType;
import org.ggf.bes.factory.GetFactoryAttributesDocumentType;

import edu.virginia.vcgr.genii.bes.GeniiBESPortType;
import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;
import edu.virginia.vcgr.genii.client.ser.ObjectSerializer;
import edu.virginia.vcgr.genii.client.gpath.GeniiPath;
import edu.virginia.vcgr.genii.client.gpath.GeniiPathType;

public class GetBESFactoryAttributesTool extends BaseGridTool
{
	static final private String _DESCRIPTION =
		"Retrieves and prints the attribute document for a BES target.";
	static final private String _USAGE =
		"get-bes-attributes <target>";
	
	public GetBESFactoryAttributesTool()
	{
		super(_DESCRIPTION, _USAGE, false);
	}
	
	@Override
	protected int runCommand() throws Throwable
	{
		GeniiPath gPath = new GeniiPath(getArgument(0));
		if(gPath.pathType() != GeniiPathType.Grid)
			throw new InvalidToolUsageException("<target> must be a grid path. ");
		RNSPath path = lookup(gPath, RNSPathQueryFlags.MUST_EXIST);
		
		GeniiBESPortType bes = ClientUtils.createProxy(GeniiBESPortType.class,
			path.getEndpoint());

		GetFactoryAttributesDocumentResponseType resp =
			bes.getFactoryAttributesDocument(new GetFactoryAttributesDocumentType());
		
		ObjectSerializer.serialize(stdout, resp, 
			new QName("http://tempuri.org", "bes-factory-attributes"));
		stdout.flush();
		
		return 0;
	}

	@Override
	protected void verify() throws ToolException
	{
		if (numArguments() != 1)
			throw new InvalidToolUsageException();
	}
}