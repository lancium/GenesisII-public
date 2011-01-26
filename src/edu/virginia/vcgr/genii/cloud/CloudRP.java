package edu.virginia.vcgr.genii.cloud;

import edu.virginia.vcgr.genii.client.rp.ResourceProperty;


public interface CloudRP extends CloudConstants{


	@ResourceProperty(namespace = GENII_CLOUDBES_NS,
			localname = SPAWN_RESOURCES_NAME)
			public void spawnResources(int count);
	
	@ResourceProperty(namespace = GENII_CLOUDBES_NS,
			localname = SHRINK_RESOURCES_NAME)
			public void shrinkResources(int count);
	
	@ResourceProperty(namespace = GENII_CLOUDBES_NS,
			localname = RESOURCE_KILL_NAME)
			public void killResource(String id);
	
	@ResourceProperty(namespace = GENII_CLOUDBES_NS, 
			localname = STATUS_NAME,
			translator = CloudStatusRPTranslater.class)
		public CloudStat getStatus();
	
	@ResourceProperty(namespace = GENII_CLOUDBES_NS, 
			localname = VM_INFO_NAME,
			translator = VMStatusRPTranslater.class)
		public VMStats getVMStatus();

}
