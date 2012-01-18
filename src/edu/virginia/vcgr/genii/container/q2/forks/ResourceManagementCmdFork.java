package edu.virginia.vcgr.genii.container.q2.forks;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.sql.SQLException;

import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;

import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.security.GenesisIISecurityException;
import edu.virginia.vcgr.genii.client.security.authz.rwx.RWXMapping;
import edu.virginia.vcgr.genii.container.q2.BESData;
import edu.virginia.vcgr.genii.container.q2.BESManager;
import edu.virginia.vcgr.genii.container.q2.BESUpdateInformation;
import edu.virginia.vcgr.genii.container.q2.QueueManager;
import edu.virginia.vcgr.genii.container.q2.besinfo.BESInformation;
import edu.virginia.vcgr.genii.container.q2.matching.MatchingParameters;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.rfork.AbstractStreamableByteIOFactoryResourceFork;
import edu.virginia.vcgr.genii.container.rfork.ResourceForkService;
import edu.virginia.vcgr.genii.container.rfork.cmd.CommandChannelManager;
import edu.virginia.vcgr.genii.container.rfork.cmd.CommandHandler;
import edu.virginia.vcgr.genii.container.rfork.cmd.CommandParameter;
import edu.virginia.vcgr.genii.security.RWXCategory;

public class ResourceManagementCmdFork 
	extends AbstractStreamableByteIOFactoryResourceFork
{
	public ResourceManagementCmdFork(ResourceForkService service,
		String forkPath)
	{
		super(service, forkPath);
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	public void modifyState(InputStream source) throws IOException
	{
		CommandChannelManager.handleCommand(this, source);
	}

	@Override
	@RWXMapping(RWXCategory.READ)
	public void snapshotState(OutputStream sink) throws IOException
	{
		PrintStream ps = new PrintStream(sink);
		
		ResourceKey rKey = getService().getResourceKey();
		
		QueueManager mgr;
		
		try
		{
			mgr = QueueManager.getManager(rKey.getResourceKey());
		}
		catch (SQLException e)
		{
			throw new IOException("Unable to get BES information.", e);
		}
		
		BESManager besMgr = mgr.getBESManager();
		if (besMgr == null)
			ps.format("Unable to find BES manager in queue.\n");
		else
		{
			BESData data = besMgr.getBESData(getForkName());
			if (data == null)
				ps.format("Unable to find BES data for \"%s\".\n", 
					getForkName());
			else
			{
				BESInformation besInfo = besMgr.getBESInformation(
					data.getID());
				if (besInfo == null)
					ps.format(
						"Unable to find BES Information for \"%s(%d)\".\n", 
						getForkName(), data.getID());
				else
				{
					ps.format("%s\n", getForkName());
					ps.format("\tSlots:  %d\n", mgr.getBESConfiguration(getForkName()));
					ps.format("\tOS:  %s, %s\n",
						besInfo.getOperatingSystemType(),
						besInfo.getOperatingSystemVersion());
					ps.format("\tArch:  %s\n",
						besInfo.getProcessorArchitecture());
					ps.format("\tMemory:  %d\n",
						besInfo.getPhysicalMemory().longValue());
					MatchingParameters matching = 
						besInfo.getMatchingParameters();
					ps.format("\tMatching Parameters:\n");
					
					ps.println(matching);
					ps.println();
					ps.format("Supported Filesystems:  %s\n", besInfo.supportedFilesystems());
					
					BESUpdateInformation updateInfo = besMgr.getUpdateInformation(
						data.getID());
					ps.format("BES Update Information:\n");
					if (updateInfo == null)
						ps.format("None Available\n");
					else
					{
						ps.format("\t%s\n", updateInfo.toString());
					}
				}
			}
		}
		
		ps.println();
		ps.format("Commands:\n");
		for (String description : CommandChannelManager.describeCommands(this))
			ps.format("\t%s\n", description);
		
		ps.flush();
	}
	
	@CommandHandler("SLOTS")
	public void setSlots(@CommandParameter("slotCount") int slots) 
		throws SQLException, IOException
	{
		ResourceKey rKey = getService().getResourceKey();
		
		if (slots < 0)
			throw new IOException("Can't configure BES to have negative slots.");
		
		QueueManager mgr = QueueManager.getManager(rKey.getResourceKey());
		mgr.configureBES(getForkName(), slots);
	}
	
	@CommandHandler("UPDATE")
	public void update() throws ResourceUnknownFaultType, 
		ResourceException, SQLException, GenesisIISecurityException
	{
		ResourceKey rKey = getService().getResourceKey();
		
		QueueManager mgr = QueueManager.getManager(rKey.getResourceKey());
		mgr.forceBESUpdate(getForkName());
	}
}