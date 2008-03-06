/**
 * @author Krasi
 */

package edu.virginia.vcgr.genii.client.cmd.tools;

import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.gui.GuiUtils;
import edu.virginia.vcgr.genii.client.informationService.GUIInternalStruct;
import edu.virginia.vcgr.genii.client.informationService.QueryBuilder;
import edu.virginia.vcgr.genii.client.informationService.QueryGUI;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;
import edu.virginia.vcgr.genii.informationService.InformationServicePortType;
import edu.virginia.vcgr.genii.informationService.QueryRequestType;
import edu.virginia.vcgr.genii.informationService.QueryResponseType;

public class InfoSQueryTool extends BaseGridTool {

	
	static private final String _DESCRIPTION =
		"allows you to query the XML database";
	static private final String _USAGE =
		"xmldb-query <informationService-service-path>";

	public InfoSQueryTool() 
	{
		super(_DESCRIPTION, _USAGE, false);
	}

	
	@Override
	protected int runCommand() throws Throwable {
		/*
		 * starting up the GUI for the information service
		 */
		QueryGUI currentQueryGUI = new QueryGUI();
		GuiUtils.centerComponent(currentQueryGUI);
		/*
		 * get the paramaters entered by the user
		 */
		GUIInternalStruct queryParameters = currentQueryGUI.runGUI();
		QueryBuilder myQueryBuillder = new QueryBuilder();

		/*
		 * build a XQuery query corresponding to the GUI parameters entered by
		 * the user
		 */
		query(myQueryBuillder.BuildQueryFromGUI(queryParameters),
				getArgument(0));
		return 0;
	}
	@Override
	protected void verify() throws ToolException {
		if (numArguments() != 1)
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
		
		System.err.println("The results of your query are:" + "\n" + rs.getResult());
	}

}
