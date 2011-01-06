package edu.virginia.vcgr.genii.client.cmd.tools;

import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.gpath.GeniiPath;
import edu.virginia.vcgr.genii.client.io.FileResource;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;
import edu.virginia.vcgr.genii.client.rp.ResourcePropertyManager;
import edu.virginia.vcgr.genii.cloud.CloudRP;
import edu.virginia.vcgr.genii.cloud.CloudStat;


public class CloudTool extends BaseGridTool{
	
	static private final String _DESCRIPTION =
		"Tool to control Cloud BES.";
		
	static private final String _USAGE_RESOURCE =
		"edu/virginia/vcgr/genii/client/cmd/tools/resources/cloud-tool-usage.txt";

	private int _shrink = -1;
	private int _spawn = -1;
	private int _count = 0;

	@Option({"shrink"})
	public void setStoretype(String count) {
		_shrink = Integer.parseInt(count);
		_count++;
	}

	@Option({"spawn"})
	public void setSpawn(String count) {
		_spawn = Integer.parseInt(count);
		_count++;
	}


	public CloudTool()
	{
		super(_DESCRIPTION, new FileResource(_USAGE_RESOURCE), false);
	}

	@Override
	protected int runCommand() throws Throwable
	{


		RNSPath bes = lookup(new GeniiPath(getArgument(0)), 
				RNSPathQueryFlags.MUST_EXIST);
		CloudRP rp = (CloudRP)ResourcePropertyManager.createRPInterface(bes.getEndpoint(), CloudRP.class);


		if (_spawn != -1){
			stdout.println("Attempting to spawn " + _spawn + " resources");
			rp.spawnResources(_spawn);
		}

		if(_shrink != -1){
			stdout.println("Attempting to kill " + _shrink + " resources");
			rp.shrinkResources(_shrink); 
		}

		if(_count == 0){
			CloudStat tStat = rp.getStatus();
			stdout.println(tStat.toString());
		}

		return 0;
	}

	@Override
	protected void verify() throws ToolException
	{
		if (numArguments() != 1)
			throw new InvalidToolUsageException();
		if (!(_count == 1 || _count == 0))
			throw new InvalidToolUsageException();
	}


}
