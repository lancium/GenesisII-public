package edu.virginia.vcgr.genii.client.cmd.tools.dair;

import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.cmd.tools.BaseGridTool;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;
import edu.virginia.vcgr.genii.genesis_dair.SQLAccessCombinedPortType;
import edu.virginia.vcgr.genii.genesis_dair.SQLExecuteRequest;
import edu.virginia.vcgr.genii.genesis_dair.SQLExecuteResponse;
import edu.virginia.vcgr.genii.genesis_dair.SQLExpressionType;

public class GetDataTool extends BaseGridTool {

	static private final String _DESCRIPTION =
		"You can use this tool to query the resultsets of previous queries. " +
		"The EPR of the resultset is used to specify which resultset the SQL query reffers to.";
	static private final String _USAGE =
		"dair-data <DAIR service> <ResultsetEPR> <SQL query>";
	
	protected GetDataTool() {
		super(_DESCRIPTION, _USAGE, false);
	}

	/**
	 * @throws Throwable
	 */
	@Override
	protected int runCommand() throws Throwable {
		getData(new String(getArgument(0)),
				new String(getArgument(1)),
				new String(getArgument(2))
				);
		return 0;
	}

	/**
	 * @throws ToolException, InvalidToolUsageException
	 */
	@Override
	protected void verify() throws ToolException {
		if (numArguments() != 3)
			throw new InvalidToolUsageException();
	}
	
	/**
	 * @param servicePath
	 * @param resultsetEPR
	 * @param SQLquery
	 * @throws Throwable
	 */
	private void getData(String servicePath, String resultsetEPR, String SQLquery) throws Throwable{
		
		RNSPath path = RNSPath.getCurrent().lookup(servicePath, RNSPathQueryFlags.MUST_EXIST);
		SQLAccessCombinedPortType SQLAccess = ClientUtils.createProxy(
				SQLAccessCombinedPortType.class, path.getEndpoint());
		
		SQLExpressionType sql = new SQLExpressionType();
		sql.setExpression(SQLquery);
		SQLExecuteRequest request = new SQLExecuteRequest();
		request.setSQLExpression(sql);
		
		SQLExecuteResponse response = SQLAccess.SQLExecute(request);
		System.out.println(response.getSQLDataset().getDatasetData().toString());
		
	}

}
