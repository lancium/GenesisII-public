package edu.virginia.vcgr.genii.cloud;

import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class VMStat {

	private String _id;
	private String _besid;
	private VMState _state = null;
	private String _host;
	private int _port = 22;
	private int _load = 0;
	private boolean _prepared = false;
	private boolean _preparing = false;
	
	public void setPreparing(){
		_preparing = true;
	}
	
	public boolean preparing(){
		return _preparing;
	}

	static private Log _logger = LogFactory.getLog(VMStat.class);
	
	public VMStat(){

	}

	public VMStat(String id, String host, int port,
			int load, int setup, String besid) {
	
		_id = id;
		_host = host;
		_port = port;
		_load = load;
		_besid = besid;
		if (setup == 1)
			_prepared = true;

	}

	public void setPrepared(){
		_prepared = true;
		updateResource();
	}

	
	public void setHost(String host){
		_host = host;
	}
	
	public void setID(String id){
		_id = id;
	}
	
	public void setBESID(String besid){
		_besid = besid;
	}
	
	public String getID() {
		return _id;
	}

	public VMState getState() {
		return _state;
	}
	public void setState(VMState state) {
		_state = state;
	}
	public String getHost() {
		return _host;
	}
	public int getPort() {
		return _port;
	}

	public int getLoad() {
		return _load;
	}
	public void setLoad(int load) {
		_load = load;
		updateResource();
	}

	public void addWork(){
		_load += 1;
		updateResource();
	}

	public void removeWork(){
		_load -= 1;
		updateResource();
	}

	public boolean isReady(){
		return ((_state == VMState.RUNNING) && _prepared);
	}


	public void updateResource(){
		int prepared = 0;
		if (_prepared)
			prepared=1;
		try {
			CloudMonitor.updateResource(_besid, _id, _load, prepared);
		} catch (SQLException e) {
			_logger.error(e);
		}

	}

}
