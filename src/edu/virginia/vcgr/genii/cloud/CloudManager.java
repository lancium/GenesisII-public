package edu.virginia.vcgr.genii.cloud;

import java.io.OutputStream;
import java.sql.SQLException;
import java.util.Collection;

//Warning: It is expected that any implementor of this interface is thread safe

public interface CloudManager {

	//Returns true if can add number of resources indicated by count
	public boolean spawnResources(int count) throws Exception;
	
	//Returns new size of resource pool, will only kill idle resources,
	public boolean setResources(int count) throws Exception;
	
	//Kills all idle resources
	public boolean shrink() throws Exception;
	
	//Sets max resources, limits the spawn and set calls
	//Does not shrink resource pool if already over the limit
	public void setMaxResources(int count);
	public int getMaxResources();
	
	//Returns true if can kill number of resources indicated by count
	//Will not kill resources that are not idle
	//if fails will not kill any resources
	public boolean killResources(int count) throws Exception;
	
	//Forces kill of a resource (even if running job)
	//Does not fail activity
	public boolean killResource(String id) throws Exception;
	
	
	//Returns number of resources being held by cloud controller
	//Includes available, idle, busy, pending
	public int count();
	
	//Returns number of resources that are idle
	public int idle();
	
	//Number of resources that can accept work
	public int available();
	
	//Number of resources that cannot yet be used for
	//useful work (i.e is shuttingdown/starting up/suspended..)
	public int pending();
	
	//Number of resources that have work
	public int busy();
	
	//Sets how much work "Jobs" a single resource can run
	public void setWorkPerResource(int count);
	
	public CloudStat getStatus() throws Exception;
	
	//Frees resource for future use
	public boolean releaseResource(String activityID) throws SQLException;
	
	//Gets resource if one available, returns automatically if one
	//is already allocated, blocks until one is ready
	public String aquireResource(String activityID) throws InterruptedException;
	
	public void setController(CloudController controller);
	
	public boolean sendFileTo(
			String resourceID, String localPath,
			String remotePath) throws Exception;
	
	public boolean recieveFileFrom(
			String resourceID, String localPath,
			String remotePath) throws Exception;
	
	public int sendCommand(
			String resourceID, String command,
			OutputStream out, OutputStream err) throws Exception;

	boolean checkFile(String resourceID, String path) throws Exception;
	
	public boolean freeResources() throws Exception;
	
	public Collection<VMStat> getResourceStatus() throws Exception;
	

}
