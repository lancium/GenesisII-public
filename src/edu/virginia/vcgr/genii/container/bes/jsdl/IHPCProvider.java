package edu.virginia.vcgr.genii.container.bes.jsdl;

import org.ggf.jsdl.hpcp.HPCProfileApplication_Type;

import edu.virginia.vcgr.genii.container.jsdl.IJobPlanProvider;

public interface IHPCProvider extends IJobPlanProvider
{
	public HPCProfileApplicationRedux createHPCApplication(
		HPCProfileApplication_Type hpcApplication);
}
