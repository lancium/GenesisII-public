package edu.virginia.vcgr.genii.client.cmd.tools;

import java.rmi.RemoteException;

import org.ggf.rns.*;
import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.naming.EPRUtils;

public class RNSInteropTool extends BaseGridTool
{
	static final private String _DESCRIPTION =
		"Runs EGEE RNS interop tests.";
	
	static final private String _USAGE =
		"rns " +
		"[--serviceURL=<rns-service-url>]" +
		"[--addName=<entry-name>]" +
		"[--ls=<entry-name>]" +
		"[--rm=<entry-name>]";
	
	private RNSPortType rnsPort;
	
	private String _srvURL = null;
	private String _addName = null;
	private String _ls = null;
	private String _rm = null;
	
	public RNSInteropTool(){
		super(_DESCRIPTION, _USAGE, false);
	}
	
	public void setServiceURL(String serviceURL){
		_srvURL = serviceURL;
	}
	
	public void setAddName(String entryName){
		_addName = entryName;
	}
	
	public void setLs(String entryName){
		_ls = entryName;
	}
	
	public void setRm(String entryName){
		_rm = entryName;
	}

	@Override
	protected int runCommand() throws Throwable
	{
		stdout.println("***Welcome to RNS Interop testing***");
		
		if ((_addName == null) && (_rm == null) && (_ls == null))
			stdout.println("No tests selected.");
		else
			setupConnection();
		
		if (_addName != null){
			stdout.println("Running add test");
		}
		
		if (_ls != null){
			stdout.println("Running list test");
			testLIST();
		}
		
		if (_rm != null){
			stdout.println("Running remove test");
		}
			
		stdout.println("***Testing completed***");
		
		return 0;
	}
	
	/**
	 * setup connection
	 */
	
	protected void setupConnection()
		throws Throwable
	{
		//get epr from command line info or set default
		EndpointReferenceType serviceEPR = null;
		
		String serviceURL;
		if (_srvURL != null)
			serviceURL = _srvURL;
		//by default assume localhost running on port 9090
		else {
			serviceURL = "http://localhost:9090/axis2/services/rns";
			stdout.println("Using defaults for RNS service epr.");
		}
		
		//create epr to specified rns service
		serviceEPR = EPRUtils.makeEPR(serviceURL, false);
		
		// create proxy to specified rns service
		rnsPort = ClientUtils.createProxy(
				RNSPortType.class, serviceEPR);
		
		stdout.println("Successfully created proxy to service.");
		
		return;
	}
	/**
	 * Test LIST - send list request to service host
	 * @throws Throwable
	 */

	protected Boolean testLIST()
		throws ResourceUnknownFaultType, RemoteException
	{
		//create list request
		List listRequest = new List(_ls);
		ListResponse listResponse;
		
		try{
			listResponse = rnsPort.list(listRequest);
		}
		catch(Exception e){
			stdout.println("List test failed: " + e.getMessage());
			return false;
		}
		
		//process response
		for (EntryType entry : listResponse.getEntryList()){
			stdout.println(entry.getEntry_name());
		}
		
		return true;
	}
	
	@Override
	protected void verify() throws ToolException {
		if (numArguments() != 0) {
			stdout.println("Arguments specified incorrectly.");
			throw new InvalidToolUsageException();
		}
	}
	

}
