package edu.virginia.vcgr.genii.container.container.forks;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import edu.virginia.vcgr.genii.container.rfork.AbstractStreamableByteIOFactoryResourceFork;
import edu.virginia.vcgr.genii.container.rfork.ResourceForkService;
import edu.virginia.vcgr.genii.security.RWXCategory;
import edu.virginia.vcgr.genii.security.rwx.RWXMapping;

public class StorageContainerPropertiesFork extends AbstractStreamableByteIOFactoryResourceFork
{
	public StorageContainerPropertiesFork(ResourceForkService service, String forkPath)
	{
		super(service, forkPath);
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	public void modifyState(InputStream source) throws IOException
	{
		throw new IOException("Not allowed to modify the the filesystem summary.");
	}

	@Override
	@RWXMapping(RWXCategory.READ)
	public void snapshotState(OutputStream sink) throws IOException
	{
		PrintStream ps = new PrintStream(sink);

		ps.print("# Storage Container Properties Summary\n\n");
		ps.print("# Format is <key> <value>\n");
		ps.print("# These are mostly constants right now\n");
		ps.print("ReservableStorage 1000000\n");
		ps.print("StorageTotal 2000000000\n");
		ps.print("StorageCommitted 1000000\n");
		ps.print("StorageUsed 1000000\n");
		ps.print("PathToSwitch /home/xsede.org/grimshaw/NetMap/UVA/CS/5thFloor\n");
		ps.print("CoresAvailable 2\n");
		ps.print("ContainerAvailability 99\n");
		ps.print("StorageRBW 200\n");
		ps.print("StorageWBW 100\n");
		ps.print("StorageRLatency 10000\n");
		ps.print("StorageWLatency 10000\n");
		ps.print("StorageRAIDLevel 0\n");
		ps.print("CostPerGBMonth 4\n");
		ps.print("DataIntegrity 10\n");

		ps.close();
	}
}