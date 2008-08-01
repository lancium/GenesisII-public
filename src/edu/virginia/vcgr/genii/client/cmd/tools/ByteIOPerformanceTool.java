package edu.virginia.vcgr.genii.client.cmd.tools;

import java.util.Random;

import org.apache.axis.types.URI;
import org.ggf.rbyteio.RandomByteIOPortType;

import edu.virginia.vcgr.genii.client.byteio.ByteIOConstants;
import edu.virginia.vcgr.genii.client.byteio.transfer.RandomByteIOTransferer;
import edu.virginia.vcgr.genii.client.byteio.transfer.RandomByteIOTransfererFactory;
import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;
import edu.virginia.vcgr.genii.client.utils.units.Duration;

public class ByteIOPerformanceTool extends BaseGridTool
{
	static final private String _DESCRIPTION =
		"Runs a test that determines the write and read bandwidth for a given ByteIO transfer mechanism and block size.";
	static final private String _USAGE =
		"ByteIOPerformance <SIMPLE|DIME|MTOM> <block-size> <minimum-duration> <num-runs> <target>";
	
	static private Random _random = new Random();
	
	public ByteIOPerformanceTool()
	{
		super(_DESCRIPTION, _USAGE, true);
	}
	
	@Override
	protected int runCommand() throws Throwable
	{
		int numRuns = Integer.parseInt(getArgument(3));
		Duration d = Duration.parse(getArgument(2));
		int blockSize = Integer.parseInt(getArgument(1));
		String xferMechString = getArgument(0);
		URI xferMech;
		
		if (xferMechString.equalsIgnoreCase("SIMPLE"))
			xferMech = ByteIOConstants.TRANSFER_TYPE_SIMPLE_URI;
		else if (xferMechString.equalsIgnoreCase("DIME"))
			xferMech = ByteIOConstants.TRANSFER_TYPE_DIME_URI;
		else if (xferMechString.equalsIgnoreCase("MTOM"))
			xferMech = ByteIOConstants.TRANSFER_TYPE_MTOM_URI;
		else
			throw new InvalidToolUsageException(
				"Unrecognized Transfer Mechanism.");
		
		RNSPath target = RNSPath.getCurrent().lookup(getArgument(4), 
			RNSPathQueryFlags.MUST_EXIST);
		RandomByteIOPortType targetPT = ClientUtils.createProxy(
			RandomByteIOPortType.class, target.getEndpoint());
		RandomByteIOTransferer transferer = 
			RandomByteIOTransfererFactory.createRandomByteIOTransferer(
				targetPT, xferMech);
		
		for (int lcv = 0; lcv < numRuns; lcv++)
		{
			System.out.format("Run %d\n", lcv + 1);
			System.out.flush();
			doRun(transferer, d.getMilliseconds(), blockSize);
		}
		
		return 0;
	}
	
	static private void doRun(RandomByteIOTransferer transferer, 
		long minimumDuration, int blockSize) throws Throwable
	{
		System.out.print("\tWrite -- ");
		System.out.flush();
		doWrite(transferer, minimumDuration, blockSize);
		
		System.out.print("\tRead -- ");
		System.out.flush();
		doRead(transferer, minimumDuration, blockSize);
	}
	
	static private void doWrite(RandomByteIOTransferer transferer, 
		long minimumDuration, int blockSize) throws Throwable
	{
		long start;
		long stop;
		long bytesTransferred = 0L;

		byte []data = new byte[blockSize];
		_random.nextBytes(data);
		
		start = System.currentTimeMillis();
		stop = 0;
		while ((stop - start) < minimumDuration)
		{
			transferer.write(0, blockSize, 0, data);
			bytesTransferred += blockSize;
			stop = System.currentTimeMillis();
		}
		
		System.out.format("%d bytes in %d seconds:  %.3f MBs\n", 
			bytesTransferred, (stop - start) / 1000L,
			((bytesTransferred / 1024.0 / 1024.0) / ((stop - start) / 1000.0)));
		System.out.flush();
	}
	
	static private void doRead(RandomByteIOTransferer transferer,
		long minimumDuration, int blockSize) throws Throwable
	{
		long start;
		long stop;
		long bytesTransferred = 0L;

		start = System.currentTimeMillis();
		stop = 0;
		while ((stop - start) < minimumDuration)
		{
			transferer.read(0, blockSize, 1, 0);
			bytesTransferred += blockSize;
			stop = System.currentTimeMillis();
		}
		
		System.out.format("%d bytes in %d seconds:  %.3f MBs\n", 
			bytesTransferred, (stop - start) / 1000L,
			((bytesTransferred / 1024.0 / 1024.0) / ((stop - start) / 1000.0)));
		System.out.flush();
	}

	@Override
	protected void verify() throws ToolException
	{
		if (numArguments() != 5)
			throw new InvalidToolUsageException("Invalid tool usage.");
	}
}