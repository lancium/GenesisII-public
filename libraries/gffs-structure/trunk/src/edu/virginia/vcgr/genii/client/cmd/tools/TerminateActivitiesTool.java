package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.ggf.bes.factory.TerminateActivitiesResponseType;
import org.ggf.bes.factory.TerminateActivitiesType;
import org.ggf.bes.factory.TerminateActivityResponseType;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.bes.GeniiBESPortType;
import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ReloadShellException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.dialog.UserCancelException;
import edu.virginia.vcgr.genii.client.gpath.GeniiPath;
import edu.virginia.vcgr.genii.client.gpath.GeniiPathType;
import edu.virginia.vcgr.genii.client.io.LoadFileResource;
import edu.virginia.vcgr.genii.client.rcreate.CreationException;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;
import edu.virginia.vcgr.genii.client.rp.ResourcePropertyException;
import edu.virginia.vcgr.genii.client.security.axis.AuthZSecurityException;

public class TerminateActivitiesTool extends BaseGridTool
{

	static private final String _DESCRIPTION = "config/tooldocs/description/dterminateActivities";
	static private final String _USAGE_RESOURCE = "config/tooldocs/usage/uterminateActivities";

	private String besFactory = null;

	private String activities = null;

	private String activityFolders = null;

	private int batchSize = 250;

	public TerminateActivitiesTool()
	{
		super(new LoadFileResource(_DESCRIPTION), new LoadFileResource(_USAGE_RESOURCE), false, ToolCategory.EXECUTION);

	}

	@Option({ "bes" })
	public void setBESFactory(String besFactory)
	{
		this.besFactory = besFactory;
	}

	@Option({ "activities" })
	public void setActivities(String activities)
	{
		this.activities = activities;
	}

	@Option({ "activityFolders" })
	public void setActivityFolders(String activityFolders)
	{
		this.activityFolders = activityFolders;
	}

	@Option({ "batchSize" })
	public void setBatchSize(int batchSize)
	{
		this.batchSize = batchSize;
	}

	@Override
	protected int runCommand() throws ReloadShellException, ToolException, UserCancelException, RNSException,
		AuthZSecurityException, IOException, ResourcePropertyException, CreationException
	{

		GeniiPath factoryPath = new GeniiPath(besFactory);
		if (factoryPath.pathType() != GeniiPathType.Grid)
			throw new InvalidToolUsageException("<bes> must be a grid path. ");

		RNSPath path = lookup(factoryPath, RNSPathQueryFlags.MUST_EXIST);

		GeniiBESPortType bes = ClientUtils.createProxy(GeniiBESPortType.class, path.getEndpoint());

		List<EndpointReferenceType> activityEprs = new ArrayList<EndpointReferenceType>();
		List<String> activityPaths = new ArrayList<String>();
		if (activities != null) {
			String[] split = activities.split(",");
			if (split.length == 0)
				throw new InvalidToolUsageException("<activities> must not be empty. ");
			for (String s : split) {
				GeniiPath actPath = new GeniiPath(s);
				path = lookup(actPath, RNSPathQueryFlags.DONT_CARE);
				activityPaths.add(s);
				activityEprs.add(path.getEndpoint());
			}
		}
		if (activityFolders != null) {
			String[] activityFolderPaths = activityFolders.split(",");
			if (activityFolderPaths.length == 0)
				throw new InvalidToolUsageException("<activity-folders> must not be empty. ");
			for (String s : activityFolderPaths) {
				GeniiPath actFolderPath = new GeniiPath(s);
				path = lookup(actFolderPath, RNSPathQueryFlags.MUST_EXIST);
				Collection<RNSPath> children = path.listContents();
				for (RNSPath child : children) {
					// TypeInformation type = new TypeInformation(path.getEndpoint());
					// if(type.isBESActivity())
					{
						activityPaths.add(child.pwd());
						activityEprs.add(child.getEndpoint());
					}
				}
			}
		}

		int exitCode = 0;
		if (activityPaths.size() == 0) {
			stderr.println("No activities to terminate...");
			return 1;
		}
		// terminate activities in batches
		for (int h = 0; h < Math.ceil(activityPaths.size() / ((double) batchSize)); h++) {
			int start = h;
			int end = Math.min(activityEprs.size(), batchSize * (1 + h));

			List<EndpointReferenceType> currActivityEprs = activityEprs.subList(start, end);

			TerminateActivitiesResponseType resp =
				bes.terminateActivities(new TerminateActivitiesType(currActivityEprs
					.toArray(new EndpointReferenceType[currActivityEprs.size()]), null));

			if (resp != null) {
				TerminateActivityResponseType[] resps = resp.getResponse();
				if (resps != null) {
					int i = 0;
					for (TerminateActivityResponseType r : resps) {
						if (!r.isTerminated()) {
							stderr.println("Failed to terminate the activity " + activityPaths.get(start + i) + ": "
								+ r.getFault().getFaultstring());
							exitCode = 1;
						}
						i++;
					}
				} else
					exitCode = 1;
			}
		}

		return exitCode;

	}

	@Override
	protected void verify() throws ToolException
	{

		if (besFactory == null || besFactory.trim().length() == 0) {
			throw new InvalidToolUsageException("No BES container specified.");
		}
		if ((activities == null || activities.trim().length() == 0)
			&& (activityFolders == null || activityFolders.trim().length() == 0)) {
			throw new InvalidToolUsageException("No activities specified.");
		}
	}
}
