package edu.virginia.vcgr.genii.client.spmd.mpich.mpiexec;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import edu.virginia.vcgr.genii.client.spmd.AbstractSPMDTranslator;
import edu.virginia.vcgr.genii.client.spmd.SPMDException;
import edu.virginia.vcgr.genii.client.spmd.SPMDTranslator;

public class SPMDTranslatorImpl extends AbstractSPMDTranslator implements SPMDTranslator
{
	private List<String> _additionalArgs = new LinkedList<String>();

	public SPMDTranslatorImpl(String additionalArguments)
	{
		if (additionalArguments != null) {
			for (String arg : additionalArguments.split("\\s+"))
				_additionalArgs.add(arg);
		}
	}

	@Override
	public List<String> translateCommandLine(List<String> commandLine) throws SPMDException
	{
		List<String> ret = new ArrayList<String>(commandLine.size() + 1);
		ret.add("mpiexec");
		ret.addAll(_additionalArgs);
		ret.addAll(commandLine);
		return ret;
	}
}