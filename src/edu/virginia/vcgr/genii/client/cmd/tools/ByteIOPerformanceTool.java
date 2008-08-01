package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.Random;

import org.apache.axis.types.URI;
import org.ggf.rbyteio.RandomByteIOPortType;
import org.morgan.util.ini.INIFile;
import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.client.byteio.ByteIOConstants;
import edu.virginia.vcgr.genii.client.byteio.transfer.RandomByteIOTransferer;
import edu.virginia.vcgr.genii.client.byteio.transfer.RandomByteIOTransfererFactory;
import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.io.FileResource;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;
import edu.virginia.vcgr.genii.client.utils.units.Duration;

public class ByteIOPerformanceTool extends BaseGridTool
{
	static final private String _DESCRIPTION =
		"Runs a test that determines the write and read bandwidth for a given ByteIO transfer mechanism and block size.";
	static final private FileResource _USAGE =
		new FileResource("edu/virginia/vcgr/genii/client/cmd/tools/resources/byteioperf-usage.txt");
	
	static private Random _random = new Random();
	
	public ByteIOPerformanceTool()
	{
		super(_DESCRIPTION, _USAGE, true);
	}
	
	static private void addSection(
		HashMap<String, HashMap<Integer, PerfRun>> results, 
		INIFile description, String sectionName)
	{
		Properties section = description.section(sectionName);
		if (section != null)
		{
			int startSize = Integer.parseInt(section.getProperty("start.size"));
			int stopSize = Integer.parseInt(section.getProperty("stop.size"));
			int numIntervals = Integer.parseInt(section.getProperty("num.intervals"));
			int delta = ((stopSize - startSize) / (numIntervals - 1));
			HashMap<Integer, PerfRun> blocks = new HashMap<Integer, PerfRun>();
			results.put(sectionName, blocks);
			for (int lcv = startSize; lcv <= stopSize; lcv += delta)
				blocks.put(new Integer(lcv), new PerfRun());
		}
	}
	
	private void doPlannedRun(String iniFile, String target, 
		String resultsFile) throws Throwable
	{
		INIFile description = new INIFile(iniFile);
		PrintStream out = null;
		HashMap<String, HashMap<Integer, PerfRun>> readResults =
			new HashMap<String, HashMap<Integer,PerfRun>>();
		HashMap<String, HashMap<Integer, PerfRun>> writeResults =
			new HashMap<String, HashMap<Integer,PerfRun>>();
		
		Properties global = description.section("Global");
		if (global == null)
			throw new IOException(
				"INI File does not contain a global section.");
		
		int numRuns = Integer.parseInt(global.getProperty("num.runs"));
		int trimSize = Integer.parseInt(global.getProperty("trim.size"));
		String minimumDurationString = global.getProperty("minimum.duration");
		Duration minimumDuration = Duration.parse(minimumDurationString);
		
		addSection(readResults, description, "SIMPLE");
		addSection(readResults, description, "DIME");
		addSection(readResults, description, "MTOM");
		
		RNSPath t = RNSPath.getCurrent().lookup(target, 
			RNSPathQueryFlags.MUST_EXIST);
		RandomByteIOPortType targetPT = ClientUtils.createProxy(
			RandomByteIOPortType.class, t.getEndpoint());
		URI xferMech = null;
		
		for (String section : readResults.keySet())
		{
			HashMap<Integer, PerfRun> readBlocks = readResults.get(section);
			HashMap<Integer, PerfRun> writeBlocks = 
				new HashMap<Integer, PerfRun>();
			writeResults.put(section, writeBlocks);
			
			if (section.equalsIgnoreCase("SIMPLE"))
				xferMech = ByteIOConstants.TRANSFER_TYPE_SIMPLE_URI;
			else if (section.equalsIgnoreCase("DIME"))
				xferMech = ByteIOConstants.TRANSFER_TYPE_DIME_URI;
			else if (section.equalsIgnoreCase("MTOM"))
				xferMech = ByteIOConstants.TRANSFER_TYPE_MTOM_URI;
			else
			{
				System.err.println("Unknown transfer type \"" + section 
					+ "\".");
				continue;
			}
			
			RandomByteIOTransferer transferer = 
				RandomByteIOTransfererFactory.createRandomByteIOTransferer(
					targetPT, xferMech);
			
			System.out.println(section);
			for (Integer blockSize : readBlocks.keySet())
			{
				int bSize = blockSize.intValue();
				System.out.format("Read[%d]\n", bSize);
				System.out.flush();
				for (int run = 0; run < numRuns; run++)
				{
					double value = doRead(transferer, 
						minimumDuration.getMilliseconds(), bSize); 
					readBlocks.get(blockSize).addValue(value);
				}
			}
			
			for (Integer blockSize : writeBlocks.keySet())
			{
				int bSize = blockSize.intValue();
				System.out.format("Write[%d]\n", bSize);
				System.out.flush();
				for (int run = 0; run < numRuns; run++)
				{
					double value = doWrite(transferer, 
						minimumDuration.getMilliseconds(), bSize); 
					writeBlocks.get(blockSize).addValue(value);
				}
			}
		}
		
		try
		{
			out = new PrintStream(resultsFile);
			for (String section : readResults.keySet())
			{
				out.format("\"%s\"\n", section);
				out.println("\"Block Size\",\"Read Bandwidth (MBs)\",\"Write Bandwidth (MBs)\"");
				HashMap<Integer, PerfRun> readBlocks =
					readResults.get(section);
				HashMap<Integer, PerfRun> writeBlocks =
					writeResults.get(section);
				for (Integer blockSize : readBlocks.keySet())
				{
					out.format("%d,%.3f,%.3f\n", blockSize.intValue(),
						readBlocks.get(blockSize).average(trimSize),
						writeBlocks.get(blockSize).average(trimSize));
				}
				
				out.println();
			}
		}
		finally
		{
			StreamUtils.close(out);
		}
	}
	
	@Override
	protected int runCommand() throws Throwable
	{
		if (numArguments() == 3)
			doPlannedRun(getArgument(1), getArgument(2), getArgument(3));
		else
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
	
	static private double doWrite(RandomByteIOTransferer transferer, 
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
		
		double result = ((bytesTransferred / 1024.0 / 1024.0) / 
			((stop - start) / 1000.0));
		System.out.format("%d bytes in %d seconds:  %.3f MBs\n", 
			bytesTransferred, (stop - start) / 1000L, result);
		System.out.flush();
		return result;
	}
	
	static private double doRead(RandomByteIOTransferer transferer,
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
		
		double result = ((bytesTransferred / 1024.0 / 1024.0) / 
			((stop - start) / 1000.0));
		System.out.format("%d bytes in %d seconds:  %.3f MBs\n", 
			bytesTransferred, (stop - start) / 1000L, result);
		System.out.flush();
		return result;
	}

	@Override
	protected void verify() throws ToolException
	{
		if ((numArguments() != 3) && (numArguments() != 5))
			throw new InvalidToolUsageException("Invalid tool usage.");
	}
	
	static private class PerfRun
	{
		private ArrayList<Double> _values = new ArrayList<Double>();
		
		public void addValue(double value)
		{
			_values.add(new Double(value));
		}
		
		public double average(int trimSize)
		{
			if ((_values.size() - (trimSize * 2)) <= 1)
				throw new RuntimeException(
					"Trim size is too large for data set.");
			
			double total = 0.0;
			int count = 0;
			for (int lcv = trimSize; lcv < (_values.size() - trimSize); lcv++)
			{
				total += _values.get(lcv).doubleValue();
				count++;
			}
			
			return total / count;
		}
	}
}