package edu.virginia.vcgr.genii.cloud.ec2;

import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.cloud.CloudConfiguration;
import edu.virginia.vcgr.genii.cloud.CloudController;
import edu.virginia.vcgr.genii.cloud.CloudManager;
import edu.virginia.vcgr.genii.cloud.CloudMonitor;
import edu.virginia.vcgr.genii.cloud.CloudStat;
import edu.virginia.vcgr.genii.cloud.ResourceController;
import edu.virginia.vcgr.genii.cloud.VMStat;
import edu.virginia.vcgr.genii.cloud.VMState;
import edu.virginia.vcgr.genii.cloud.ssh.SSHSession;


//This class must be thread safe!!
public class EC2Manager implements CloudManager{


	private HashMap<String, VMStat> _vms;
	private final Lock vmLock = new ReentrantLock();
	
	
	private String _user;
	private String _pass;
	private String _authFile;
	private CloudController _controller = null;
	private int _maxResources = 10;
	private int _workPerVM = 1;
	private StatusPoller _poller = null;
	private String _besid = null;
	private String _script = null;
	private String _archive = null;
	private String _remoteSetupDir = null;
	private int _pollInterval = 30;
	private String _type = "";
	private String _desc = "";
	private int _prepThreads = 0;
	private int _maxPrepThreads = 10;
	
	static private Log _logger = LogFactory.getLog(EC2Manager.class);
	
	public void addVMS(List<VMStat> vms) throws Exception{
		for (VMStat tStat : vms){
			_vms.put(tStat.getID(), tStat);
		}
		//Update their the state of vms
		if (_controller != null){
			vmLock.lock();
			_controller.updateState(_vms.values());
			vmLock.unlock();
		}
		
	}


	public EC2Manager(
			CloudConfiguration config, int pollInterval, String besid){
		
		_vms = new HashMap<String, VMStat>();
		_user = config.getUsername();
		_pass = config.getPassword();
		_authFile = config.getAuthFile();
		_besid = besid;
		_script = config.getSetupScript();
		_archive = config.getSetupArchive();
		_remoteSetupDir = config.getRemoteSetupDir();
		_maxResources = config.getMaxResources();
		_workPerVM = config.getWorkPerVM();
		_pollInterval = pollInterval;
		_type = config.getType();
		_desc = config.getDescription();
		
		_poller = new StatusPoller(pollInterval, this);
		Thread thread = new Thread(_poller, "Cloud Poller Thread");
		thread.setDaemon(true);
		thread.start();
	}


	private ResourceController getVMController(VMStat tStat){
		if (tStat != null){
			SSHSession tSession =
				new SSHSession(_user, tStat.getPort(), tStat.getHost(), _pass);
	
			
			if (_authFile != null || !(_authFile.equals(""))){
				tSession.setPrivateKeyAuth(_authFile);
			}
			return tSession;
		}

		return null;
	}

	@Override
	public boolean spawnResources(int count) throws Exception {


		vmLock.lock();

		if ((this.count() + count) > _maxResources){
			vmLock.unlock();
			return false;
		}

		if (_controller != null){
			_logger.info("(" + _desc + ") Spawning " + count +
					" VMS at " + System.currentTimeMillis());
			Collection<VMStat> vms = _controller.spawnResources(count);
			if (vms != null){
				for (VMStat tStat : vms){
					//Set besid in each vm
					tStat.setBESID(_besid);
					_vms.put(tStat.getID(), tStat);
					CloudMonitor.createResource(_besid, tStat.getID(),
							tStat.getHost(), tStat.getPort(), 0, 0);	
					
				}
				vmLock.unlock();
				return true;
			}
		}
		vmLock.unlock();
		return false;
	}

	@Override
	public boolean setResources(int count) throws Exception {
		boolean result = false;
		vmLock.lock();
		//Determine shrink or set;
		if (count > this.count()){
			//Grow
			result = this.spawnResources(count - this.count());	
		}
		else if(count < this.count()){
			//Shrink
			result = this.killResources(this.count() - count);
		}
		vmLock.unlock();
		return result;
	}

	@Override
	public boolean shrink() throws Exception {
		//Trys to kill all idle resources
		boolean result = false;
		vmLock.lock();
		result = this.killResources(this.idle());
		vmLock.unlock();
		return result;
	}

	@Override
	public void setMaxResources(int count) {
		_maxResources = count;
	}

	private Collection<VMStat> getIdleVMS(){
		vmLock.lock();

		ArrayList<VMStat> tList = new ArrayList<VMStat>();
		ArrayList<VMStat> idleList = new ArrayList<VMStat>();
		tList.addAll(_vms.values());

		for (VMStat tStat : tList){
			if ((tStat.getLoad() == 0) && tStat.isReady()){
				idleList.add(tStat);		
			}
		}

		vmLock.unlock();
		return idleList;
	}


	@Override
	public boolean killResources(int count) throws Exception {

		vmLock.lock();
		boolean result = false;
		ArrayList<VMStat> killList = new ArrayList<VMStat>();
		killList.addAll(this.getIdleVMS());

		if (killList.size() >= count){
			result = _controller.killResources(killList.subList(0, count));
		}

		for (VMStat tStat : killList.subList(0, count)){
			CloudMonitor.deleteResource(tStat.getID(), _besid);
			_vms.remove(tStat.getID());

		}


		vmLock.unlock();
		return result;
	}

	@Override
	public int count() {
		return _vms.size();
	}

	@Override
	public int idle() {
		return getIdleVMS().size();
	}

	@Override
	public CloudStat getStatus() throws Exception {
		//Get status of resources
		if (_controller != null){
			vmLock.lock();
			_controller.updateState(_vms.values());
			vmLock.unlock();
		}

		return new CloudStat(this.available(), this.count(), this.busy(),
				this.pending(), this.preparing(), _type, _desc);
	}

	private VMStat getResource(String id){
		return _vms.get(id);
	}


	@Override
	public boolean sendFileTo(String resourceID, String localPath,
			String remotePath) throws Exception {
		
		
		VMStat tStat = this.getResource(resourceID);

		if (tStat != null){
			synchronized(tStat){
				ResourceController tCont = this.getVMController(tStat);
				if (tCont != null)
					return tCont.sendFileTo(localPath, remotePath);
			}
		}
		return false;
	}
	
	@Override
	public boolean checkFile(String resourceID, String path) throws Exception {
		
		
		VMStat tStat = this.getResource(resourceID);

		if (tStat != null){
			synchronized(tStat){
				ResourceController tCont = this.getVMController(tStat);
				if (tCont != null)
					return tCont.fileExists(path);
			}
		}
		return false;
	}

	@Override
	public boolean recieveFileFrom(String resourceID, String localPath,
			String remotePath) throws Exception {
		
		VMStat tStat = this.getResource(resourceID);

		if (tStat != null){
			synchronized(tStat){
				ResourceController tCont = this.getVMController(tStat);
				if (tCont != null)
					return tCont.recieveFileFrom(localPath, remotePath);
			}
		}
		return false;
	}

	@Override
	public int sendCommand(String resourceID, String command,
			OutputStream out, OutputStream err) throws Exception {
		
		
		VMStat tStat = this.getResource(resourceID);

		if (tStat != null){

			synchronized(tStat){
				ResourceController tCont = this.getVMController(tStat);
				if (tCont != null)
					return tCont.sendCommand(command, out, err);
			}
		}
		return -1;
	}




	private boolean freeResource(String resourceID) {
		vmLock.lock();
		VMStat tStat = getResource(resourceID);
		if (tStat != null){
			tStat.removeWork();
			//Must persist to database;
			vmLock.unlock();
			return true;
		}
		vmLock.unlock();
		return false;
	}


	private Collection<VMStat> getAvailableVMS(){
		vmLock.lock();
		ArrayList<VMStat> tList = new ArrayList<VMStat>();
		ArrayList<VMStat> aList = new ArrayList<VMStat>();
		tList.addAll(_vms.values());

		for (VMStat tStat : tList){
			if ((tStat.getLoad() < _workPerVM) && tStat.isReady()){
				aList.add(tStat);
			}
		}

		vmLock.unlock();
		return aList;
	}

	private Collection<VMStat> getBusyVMS(){
		vmLock.lock();
		ArrayList<VMStat> tList = new ArrayList<VMStat>();
		ArrayList<VMStat> bList = new ArrayList<VMStat>();
		tList.addAll(_vms.values());

		for (VMStat tStat : tList){
			if ((tStat.getLoad() > 0) && tStat.isReady()){
				bList.add(tStat);
			}
		}

		vmLock.unlock();
		return bList;
	}

	private Collection<VMStat> getPendingVMS(){
		vmLock.lock();
		ArrayList<VMStat> tList = new ArrayList<VMStat>();
		ArrayList<VMStat> pList = new ArrayList<VMStat>();
		tList.addAll(_vms.values());

		for (VMStat tStat : tList){
			if (!(tStat.getState() == VMState.RUNNING)){
				pList.add(tStat);
			}
		}

		vmLock.unlock();
		return pList;
	}

	private String aquireResource() {
		vmLock.lock();
		ArrayList<VMStat> tList = new ArrayList<VMStat>();
		tList.addAll(_vms.values());
		String result;

		for (VMStat tStat : tList){
			if ((tStat.getLoad() < _workPerVM) && tStat.isReady()){
				result = tStat.getID();
				tStat.addWork();
				vmLock.unlock();
				return result;
			}
		}

		vmLock.unlock();
		return null;
	}

	@Override
	public void setController(CloudController controller) {
		_controller = controller;

	}

	@Override
	public int available() {
		return this.getAvailableVMS().size();
	}

	public int preparing(){
		vmLock.lock();
		ArrayList<VMStat> tList = new ArrayList<VMStat>();
		tList.addAll(_vms.values());
		int count = 0;
		for (VMStat tStat : tList){
			if ((tStat.getState() == VMState.RUNNING) && !tStat.isReady()){
				count++;
			}
		}
		vmLock.unlock();
		return count;
	}
	
	@Override
	public int pending() {
		return this.getPendingVMS().size();
	}



	@Override
	public int busy() {
		return this.getBusyVMS().size();
	}

	@Override
	public void setWorkPerResource(int count) {
		_workPerVM = count;
	}

	@Override
	public int getMaxResources() {
		return _maxResources;
	}





	private class StatusPoller implements Runnable
	{
		//Poll interval in seconds
		private int _poll;
		private CloudManager _tManage;

		public StatusPoller(int pollInterval, CloudManager tManage)
		{
			_poll = pollInterval;
			_tManage = tManage;
		}

		public void run()
		{
			
			while (true)
			{
				try {
					if (_controller != null){
						vmLock.lock();
						_controller.updateState(_vms.values());
						vmLock.unlock();
					}

					//Prepare VM if necessary
					if ((_script != null) || (_archive != null)){
						for (VMStat tStat : _vms.values()){
							if ((tStat.getState() == VMState.RUNNING) && !tStat.isReady()){
								if (_prepThreads < _maxPrepThreads){
									
									if(!tStat.preparing()){
									//Spawn new vm preparer
									_prepThreads++;
									tStat.setPreparing();
									_logger.info("(" + _desc +
											") Spawning prep thread for " +
											tStat.getID() + " at " +
											System.currentTimeMillis());
									VMPreparer preparer = 
										new VMPreparer(_tManage, tStat);
									Thread thread = new Thread(preparer,
											"VMPreparer " + tStat.getID());
									thread.setDaemon(true);
									thread.start();
									}
								}
							}
						}
					}
					Thread.sleep(_poll * 1000);
				} catch (Exception e) {
					_logger.error(e);
				}
			}
		}


	}
	
	
	private class VMPreparer implements Runnable
	{

		private CloudManager _tManage;
		private VMStat _tVM;
		private int _attempts = 0;
		private int _backoff = 20;
		private boolean _failed = false;

		public VMPreparer(CloudManager tManage, VMStat VM)
		{
			_tManage = tManage;
			_tVM = VM;
		}

		public void run()
		{
			
			while (true)
			{
					
				try {

					//Exponential Backoff
					if (_failed){
						long sleep = (long) ((_backoff * 1000) *
								Math.exp(.5 * _attempts));
						_logger.info("VM Preparer for " + _tVM.getID() + 
								" sleeping for " + sleep/1000 + " seconds");
						Thread.sleep(sleep);
						_failed = false;
					}

					//Prepare VM 
					_attempts++;
					if ((_tVM.getState() == VMState.RUNNING) && !_tVM.isReady()){
						_logger.info("(" + _desc + ") Preparing VM " +
								_tVM.getID() + " at " +
								System.currentTimeMillis());
						VMSetup.setupVM(_script, _archive,
								_remoteSetupDir, _tVM.getID(), _tManage);
						_tVM.setPrepared();
						_logger.info("(" + _desc + ") Prepared VM " +
								_tVM.getID() + " at " +
								System.currentTimeMillis());
						_prepThreads--;
						break;
					}
				} catch (Exception e) {
					_logger.error(e);
					_failed = true;
				}

			}
		}


	}
	
	


	@Override
	public boolean releaseResource(String activityID) throws SQLException {
		//Add vm cleanup code (terminate processes, wipe working directories)
		
		String resourceID = CloudMonitor.getResourceID(activityID);
		if (resourceID != null){
			this.freeResource(resourceID);
			CloudMonitor.removeActivity(activityID);
		}
		return false;
	}

	@Override
	public String aquireResource(
			String activityID) throws InterruptedException {
		//Puts thread to sleep if resource unavialable
		String resourceID = CloudMonitor.getResourceID(activityID);
		if (resourceID != null){
			_logger.info("CloudBES: Activity " + activityID + 
					" aquired resource " + resourceID);
			return resourceID;
		}
		while (resourceID == null){
			resourceID = this.aquireResource();
			if (resourceID != null){
				try {
					CloudMonitor.addActivity(activityID, resourceID);
					return resourceID;
				} catch (SQLException e) {
					_logger.error(e);
				}
			}
			Thread.sleep(_pollInterval * 1000); 
		}

		return null;

	}

}

