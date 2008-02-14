package edu.virginia.vcgr.genii.client.cmd.tools;

import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;
import edu.virginia.vcgr.genii.informationService.InformationServicePortType;
import edu.virginia.vcgr.genii.informationService.QueryRequestType;
import edu.virginia.vcgr.genii.informationService.QueryResponseType;

public class InfoSQueryTool extends BaseGridTool {

	
	static private final String _DESCRIPTION =
		"allows you to query the XML database";
	static private final String _USAGE =
		"xmldb-query <Berkeley DB query> <informationService-service-path>";

	public InfoSQueryTool() 
	{
		super(_DESCRIPTION, _USAGE, false);
	}

	
	@Override
	protected int runCommand() throws Throwable {
		query(new String(getArgument(0)),
				getArgument(1));
		return 0;
	}
	@Override
	protected void verify() throws ToolException {
		if (numArguments() != 2)
			throw new InvalidToolUsageException();

	}

	private void query(String query, String servicePath) throws Throwable
	{
		/*
		 * create a proxy to the target
		 */
		RNSPath path = RNSPath.getCurrent().lookup(servicePath, RNSPathQueryFlags.MUST_EXIST);
		InformationServicePortType informationService = ClientUtils.createProxy(
				InformationServicePortType.class, path.getEndpoint());
		QueryResponseType rs =informationService.queryForProperties(new QueryRequestType(query));
		
		System.err.println("Your query: " + query + "\n" + "The results are:" + "\n" + rs.getResult());
	}

}
