package edu.virginia.vcgr.genii.client.cmd.tools.dair;


import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.cmd.tools.BaseGridTool;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;
import edu.virginia.vcgr.genii.genesis_dair.CreateDataResourceRequest;
import edu.virginia.vcgr.genii.genesis_dair.SQLAccessCombinedPortType;


public class TestQuery extends BaseGridTool{
	
	

	static private final String _DESCRIPTION =
		"You can use this tool to query tables through the GenesisII DAIR services";
	static private final String _USAGE =
		"dair-query <Database driver path> <DAIR service> <query> <username> " +
		"<password> <resourceName>[<other properties>]";
	
	public TestQuery() {
		super(_DESCRIPTION, _USAGE, false);
	}

	/**
	 * @throws Throwable
	 */
	@Override
	protected int runCommand() throws Throwable {
		connect(new String(getArgument(0)), 
				new String(getArgument(1)), 
				new String(getArgument(2)),
				new String(getArgument(3)),
				new String(getArgument(4)),
				new String(getArgument(5)));
		return 0;
	}

	/**
	 * @throws ToolException
	 */
	@Override
	protected void verify() throws ToolException {
		if (numArguments() != 6)
			throw new InvalidToolUsageException();
		
	}

	/**
	 * @param databaseDriverPath
	 * @param servicePath
	 * @param query - the query to be executed
	 * @param username 
	 * @param password
	 * @param resourceName - the name of the new DAIR resource
	 * @throws Throwable
	 */
	private void connect (String databaseDriverPath, String servicePath, String query, String username,
			String password, String resourceName) 
		throws Throwable
	{
		/*
		 * creating the new directory for the results
		 */
		
		RNSPath path = RNSPath.getCurrent().lookup(servicePath, RNSPathQueryFlags.MUST_EXIST);
		@SuppressWarnings("unused")
		RNSPath resultsDirRNS = createResultDir(path);
		
		/*
		 * preparing the parameters to be passed on
		 */
		String hardcodedDatabaseDriverPath = "org.apache.derby.jdbc.EmbeddedDriver";
		SQLAccessCombinedPortType SQLAccess = ClientUtils.createProxy(
				SQLAccessCombinedPortType.class, path.getEndpoint());
		CreateDataResourceRequest myRequest = new CreateDataResourceRequest(); 
		myRequest.setDriverPath(hardcodedDatabaseDriverPath);
		myRequest.setQuery(query);
		myRequest.setUsername("sa");
		myRequest.setPassword("");
		myRequest.setResourceName(resourceName);
		String result = (String) SQLAccess.createDataResource(myRequest);
		System.out.println(result);
		
		/*File dataResource = new File (resultsDirRNS.toString(), resourceName+".txt");
		dataResource.canWrite();
		dataResource.canRead();
		dataResource.createNewFile(); */
		
		
		/*
		File dataResource = new File (resultsDirRNS + File.separator + resourceName +".txt");
	//	String filepath = dataResource.getCanonicalPath();
		BufferedWriter output = new BufferedWriter(new FileWriter(dataResource));
		output.write(result);
		output.close();
		*/
	}
	/**
	 * @throws RNSException
	 * 
	 * creates the directory where the results will be stored
	 */
	static public RNSPath createResultDir(RNSPath path) throws RNSException
	{
		String resultsDirRNSPath = path.pwd() + "-results";
		RNSPath resultsDirRNS = RNSPath.getCurrent().lookup(resultsDirRNSPath, RNSPathQueryFlags.DONT_CARE);
		if (!resultsDirRNS.exists())
		{
			resultsDirRNS.mkdir();
		}
		return resultsDirRNS;
	}
}
