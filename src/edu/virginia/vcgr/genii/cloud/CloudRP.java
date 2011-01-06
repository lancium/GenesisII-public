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
			localname = STATUS_NAME,
			translator = CloudStatusRPTranslater.class)
		public CloudStat getStatus();

}
