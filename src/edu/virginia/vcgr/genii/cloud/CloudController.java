package edu.virginia.vcgr.genii.cloud;

import java.util.Collection;

public interface CloudController {

	public Collection<VMStat> spawnResources(int count) throws Exception;
	public boolean killResources(Collection<VMStat> vms) throws Exception;
	public boolean updateState(VMStat vm) throws Exception;
	public boolean updateState(Collection<VMStat> vms) throws Exception;
	
}
