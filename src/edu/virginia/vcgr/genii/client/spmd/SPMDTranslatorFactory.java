package edu.virginia.vcgr.genii.client.spmd;

import java.util.Properties;

public interface SPMDTranslatorFactory
{
	static final public String ADDITIONAL_CMDLINE_ARGS_PROPERTY = "edu.virginia.vcgr.genii.client.spmd.additional-commandline-args";

	public String getProviderName();

	public SPMDTranslator newTranslator(Properties constructionProperties) throws SPMDException;
}