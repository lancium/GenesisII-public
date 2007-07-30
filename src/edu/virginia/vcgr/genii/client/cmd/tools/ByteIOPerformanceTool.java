package edu.virginia.vcgr.genii.client.cmd.tools;

import java.rmi.RemoteException;
import java.util.Random;

import org.ggf.rbyteio.RandomByteIOPortType;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.byteio.xfer.IRByteIOTransferer;
import edu.virginia.vcgr.genii.client.byteio.xfer.dime.DimeRByteIOTransferer;
import edu.virginia.vcgr.genii.client.byteio.xfer.mtom.MtomRByteIOTransferer;
import edu.virginia.vcgr.genii.client.byteio.xfer.simple.SimpleRByteIOTransferer;
import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;

public class ByteIOPerformanceTool extends BaseGridTool
{
	static final private String _DESCRIPTION =
		"Tests the performance of byteio at various block sizes.";
	static final private String _USAGE =
		"perf <target-path> <block-size> <num-iters> <dime|mtom|simple>";
	
	public ByteIOPerformanceTool()
	{
		super(_DESCRIPTION, _USAGE, true);
	}
	
	@Override
	protected int runCommand() throws Throwable
	{
		RNSPath byteioPath = null;
		EndpointReferenceType byteioEPR = null;
		String targetStr = getArgument(0);
		long blockSize = Long.parseLong(getArgument(1));
		int numIters = Integer.parseInt(getArgument(2));
		String transferType = getArgument(3);
		
		try
		{
			RNSPath path = RNSPath.getCurrent();
			byteioPath = path.lookup(targetStr, RNSPathQueryFlags.MUST_NOT_EXIST);
			byteioPath.createFile();
			
			byteioEPR = byteioPath.getEndpoint();
			RandomByteIOPortType byteio = ClientUtils.createProxy(
				RandomByteIOPortType.class, byteioEPR);
			
			IRByteIOTransferer transferer;
			if (transferType.equalsIgnoreCase("dime"))
				transferer = new DimeRByteIOTransferer(byteio);
			else if (transferType.equalsIgnoreCase("mtom"))
				transferer = new MtomRByteIOTransferer(byteio);
			else
				transferer = new SimpleRByteIOTransferer(byteio);
			
			testWrite(transferer, blockSize, numIters);
			testRead(transferer, blockSize, numIters);
		}
		finally
		{
			if (byteioEPR != null)
			{
				try
				{
					byteioPath.delete();
				}
				catch (Throwable t)
				{
				}
			}
		}
		return 0;
	}
	
	private void testWrite(IRByteIOTransferer byteio, 
		long blockSize, int numIters)
			throws RemoteException
	{
		stdout.println("Generating data.");
		
		Random random = new Random();
		byte []data = new byte[(int)blockSize];
		random.nextBytes(data);
		
		stdout.println("Running a few iters to get JIT going.");
		for (int lcv = 20; lcv > 0; lcv--)
		{
			stdout.print('.');
			byteio.write(0, (int)blockSize, 0, data);
		}
		stdout.println();
		
		stdout.println("Doing write test.");
		long start = System.currentTimeMillis();
		for (int lcv = 0; lcv < numIters; lcv++)
		{
			byteio.write(0, (int)blockSize, 0, data);
		}
		long stop = System.currentTimeMillis();
		
		stdout.println("Finished write performance test.  Wrote " +
			(blockSize * numIters) + " bytes in " + (stop - start) + " ms at "
			+ ((blockSize * numIters) / (double)(stop - start) * 1000) / 1024 +
			" KBs");
	}

	private void testRead(IRByteIOTransferer byteio, 
		long blockSize, int numIters)
			throws RemoteException
	{
		stdout.println("Running a few iters to get JIT going.");
		for (int lcv = 20; lcv > 0; lcv--)
		{
			stdout.print('.');
			byteio.read(0, (int)blockSize, 1, 0);
		}
		stdout.println();
		
		stdout.println("Doing read test.");
		long start = System.currentTimeMillis();
		for (int lcv = 0; lcv < numIters; lcv++)
		{
			byteio.read(0, (int)blockSize, 1, 0);
		}
		long stop = System.currentTimeMillis();
		
		stdout.println("Finished read performance test.  Read " +
			(blockSize * numIters) + " bytes in " + (stop - start) + " ms at "
			+ ((blockSize * numIters) / (double)(stop - start) * 1000) / 1024 +
			" KBs");
	}

	@Override
	protected void verify() throws ToolException
	{
		if (numArguments() != 4)
			throw new InvalidToolUsageException();
	}
}