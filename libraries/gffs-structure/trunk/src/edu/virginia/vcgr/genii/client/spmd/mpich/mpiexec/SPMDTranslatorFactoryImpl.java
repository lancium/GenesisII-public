package edu.virginia.vcgr.genii.client.spmd.mpich.mpiexec;

import java.util.Properties;

import edu.virginia.vcgr.genii.client.spmd.AbstractSPMDTranslatorFactory;
import edu.virginia.vcgr.genii.client.spmd.SPMDException;
import edu.virginia.vcgr.genii.client.spmd.SPMDTranslator;

public class SPMDTranslatorFactoryImpl extends AbstractSPMDTranslatorFactory
{
	static final public String PROVIDER_NAME = "mpich.mpiexec";

	public SPMDTranslatorFactoryImpl()
	{
		super(PROVIDER_NAME);
	}

	@Override
	public SPMDTranslator newTranslator(Properties constructionProperties) throws SPMDException
	{
		return new SPMDTranslatorImpl(constructionProperties.getProperty(ADDITIONAL_CMDLINE_ARGS_PROPERTY));
	}
}