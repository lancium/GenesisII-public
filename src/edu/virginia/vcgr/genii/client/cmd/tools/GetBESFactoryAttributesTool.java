package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.OutputStreamWriter;

import javax.xml.namespace.QName;

import org.ggf.bes.BESPortType;
import org.ggf.bes.factory.GetFactoryAttributesDocumentResponseType;
import org.ggf.bes.factory.GetFactoryAttributesDocumentType;

import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;
import edu.virginia.vcgr.genii.client.ser.ObjectSerializer;

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
		RNSPath path = RNSPath.getCurrent();
		path = path.lookup(getArgument(0), RNSPathQueryFlags.MUST_EXIST);
		
		BESPortType bes = ClientUtils.createProxy(BESPortType.class,
			path.getEndpoint());

		GetFactoryAttributesDocumentResponseType resp =
			bes.getFactoryAttributesDocument(new GetFactoryAttributesDocumentType());
		
		OutputStreamWriter writer = new OutputStreamWriter(stdout);
		ObjectSerializer.serialize(writer, resp, 
			new QName("http://tempuri.org", "bes-factory-attributes"));
		writer.flush();
		
		return 0;
	}

	@Override
	protected void verify() throws ToolException
	{
		if (numArguments() != 1)
			throw new InvalidToolUsageException();
	}
}