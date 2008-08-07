package edu.virginia.vcgr.genii.client.spmd.mpich.mpiexec;

import java.util.ArrayList;
import java.util.List;

import edu.virginia.vcgr.genii.client.spmd.AbstractSPMDTranslator;
import edu.virginia.vcgr.genii.client.spmd.SPMDException;
import edu.virginia.vcgr.genii.client.spmd.SPMDTranslator;

public class SPMDTranslatorImpl extends AbstractSPMDTranslator
	implements SPMDTranslator
{
	static final public String PROVIDER_NAME = "mpich.mpiexec";
	
	public SPMDTranslatorImpl()
	{
		super(PROVIDER_NAME);
	}
	
	@Override
	public List<String> translateCommandLine(List<String> commandLine)
			throws SPMDException
	{
		List<String> ret = new ArrayList<String>(commandLine.size() + 1);
		ret.add("mpiexec");
		ret.addAll(commandLine);
		return ret;
	}
}