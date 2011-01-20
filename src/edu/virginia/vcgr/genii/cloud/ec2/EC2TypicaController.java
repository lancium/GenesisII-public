package edu.virginia.vcgr.genii.cloud.ec2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import com.xerox.amazonws.ec2.EC2Exception;
import com.xerox.amazonws.ec2.InstanceType;
import com.xerox.amazonws.ec2.Jec2;
import com.xerox.amazonws.ec2.LaunchConfiguration;
import com.xerox.amazonws.ec2.ReservationDescription;
import com.xerox.amazonws.ec2.ReservationDescription.Instance;

import edu.virginia.vcgr.genii.cloud.CloudController;
import edu.virginia.vcgr.genii.cloud.VMStat;
import edu.virginia.vcgr.genii.cloud.VMState;

public class EC2TypicaController implements CloudController{


	private String _publicKey;
	private String _secretKey;
	private String _endpoint;
	private int _port;
	private boolean _secure;
	private Jec2 _ec2;
	private boolean _eucalyptus;
	private String _imageID;
	private InstanceType _type = InstanceType.DEFAULT;
	private String _keyPair;
	private int _backoff = 20;



	public EC2TypicaController(
			String publicKey, String secretKey,String endpoint, int port,
			boolean https, boolean euca, String keyPair){
		
		_publicKey = publicKey;
		_secretKey = secretKey;
		_endpoint = endpoint;
		_port = port;
		_secure = https;
		_eucalyptus = euca;
		_keyPair = keyPair;

		
		//Security (SSL) for api calls to cloud temporarily disabled
		//Possible for replay attacks but secret key not compromised
		//Issue with Socket factory to be resolved in the future (mts5x - 1/2011)
		_secure = false;
		
		if(_eucalyptus){
			_ec2 = new Jec2(_publicKey, _secretKey, _secure, _endpoint, _port);
			_ec2.setResourcePrefix("/services/Eucalyptus");
		}
		else{
			_ec2 = new Jec2(_publicKey, _secretKey, _secure, _endpoint);
		}
	}

	@Override
	public Collection<VMStat> spawnResources(int count) throws Exception {
		LaunchConfiguration tConfig =
			new LaunchConfiguration(_imageID, count, count);
		
		tConfig.setInstanceType(_type);
		tConfig.setKeyName(_keyPair);
		ReservationDescription tDesc;
		tDesc = _ec2.runInstances(tConfig);
	
		int retries = 0;
		
		//Ensure can update state, if api call is lagging try several times
		//then send terminate instances command to ensure no orphans
		while (true){
			try{
			return this.updateState(tDesc);
			}catch(EC2Exception e){
				if (retries < 3){
					retries++;
					long sleep = (long) ((_backoff * 1000) *
							Math.exp(.5 * retries));
					Thread.sleep(sleep);
				}
				else{
					//Need to ensure this succeeds
					this.killResources(tDesc);
					throw e;
				}
			}
		}
	}

	private Collection<VMStat> updateState(
			ReservationDescription tDesc) throws EC2Exception{

		List<VMStat> vmList = new ArrayList<VMStat>();
		List<String> idList = new ArrayList<String>();

		for (Instance tInstance : tDesc.getInstances()){
			idList.add(tInstance.getInstanceId());
		}

		List<ReservationDescription> rList;
		rList = _ec2.describeInstances(idList);

		List<Instance> instList = new ArrayList<Instance>();

		for (ReservationDescription desc : rList){
			for (Instance tInstance : desc.getInstances()){
				instList.add(tInstance);	
			}
		}

		for (Instance tInstance : instList){
			VMStat tStat = new VMStat();
			this.getVMData(tInstance, tStat);
			vmList.add(tStat);
		}

		return vmList;
	}

	private void getVMData(Instance inst, VMStat vm){
		vm.setHost(inst.getDnsName());
		vm.setID(inst.getInstanceId());
		vm.setState(parseState(inst.getState()));
	}


	//Add more states later
	private VMState parseState(String state){

		if (state.equals("pending"))
			return VMState.PENDING;			
		else if (state.equals("shutting-down"))
			return VMState.TERMINATING;
		else if (state.equals("running"))
			return VMState.RUNNING;


		return VMState.PENDING;
	}

	@Override
	//Work on this make sure reliable?
	public boolean killResources(Collection<VMStat> vms) throws Exception {
		List<String> idList = new ArrayList<String>();
		for (VMStat tStat : vms){
			idList.add(tStat.getID());
		}

		try{
			_ec2.terminateInstances(idList);
		}catch(Exception ex){
			return true;
		}

		return false;
	}
	
	private boolean killResources(ReservationDescription tDesc) throws Exception{
		List<String> idList = new ArrayList<String>();
		for (Instance t : tDesc.getInstances()){
			idList.add(t.getInstanceId());
		}

		try{
			_ec2.terminateInstances(idList);
		}catch(Exception ex){
			return true;
		}

		return false;
	}

	@Override
	public boolean updateState(VMStat vm) throws Exception {
		String[] idArray = new String[1];
		idArray[0] = vm.getID();

		List<ReservationDescription> rList;
		rList = _ec2.describeInstances(idArray);

		if (rList.size() > 0){
			if (rList.get(0).getInstances().size() > 0){
				this.getVMData(rList.get(0).getInstances().get(0), vm);
				return true;
			}
		}
		return false;


	}

	@Override
	public boolean updateState(Collection<VMStat> vms) throws Exception {
		List<String> idList = new ArrayList<String>();

		for (VMStat vm : vms){
			idList.add(vm.getID());
		}

		List<ReservationDescription> rList;

		if (idList.size() > 0){
			rList = _ec2.describeInstances(idList);

			List<Instance> instList = new ArrayList<Instance>();

			for (ReservationDescription desc : rList){
				for (Instance tInstance : desc.getInstances()){
					instList.add(tInstance);	
				}
			}

			VMStat tStat;
			HashMap<String, VMStat> vmMap = this.buildMap(vms);

			for (Instance tInstance : instList){
				tStat = vmMap.get(tInstance.getInstanceId());
				if (tStat != null){
					this.getVMData(tInstance, tStat);
				}
			}
		}

		//Add checks to ensure success
		return true;
	}

	private HashMap<String, VMStat> buildMap(Collection<VMStat> vms){
		HashMap<String, VMStat> vmMap = new HashMap<String, VMStat>();

		for (VMStat vm : vms){
			vmMap.put(vm.getID(), vm);
		}

		return vmMap;
	}


	public String get_imageID() {
		return _imageID;
	}

	public void set_imageID(String _imageID) {
		this._imageID = _imageID;
	}

	public void chooseInstanceType(int size){
		switch(size){
		case 1: _type = InstanceType.MEDIUM_HCPU;break;
		case 2: _type = InstanceType.LARGE;break;
		case 3: _type = InstanceType.XLARGE;break;
		default: break;
		}
	}

}
