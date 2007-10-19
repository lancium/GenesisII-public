package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.IOException;
import java.util.regex.*;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.oasis_open.docs.wsrf.rp_2.GetMultipleResourcePropertiesResponse;
import org.oasis_open.docs.wsrf.rp_2.GetResourcePropertyResponse;

import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;
import edu.virginia.vcgr.genii.common.GeniiCommon;

public class GetResourcePropertiesTool extends BaseGridTool
{
	static final private String _DESCRIPTION =
		"Retrieves and prints requsted attributes for a target.";
	static final private String _USAGE =
		"get-resourceProps <target> {namespaceURI}localpart [{namespaceURI}localpart [...]]";
	
	public GetResourcePropertiesTool()
	{
		super(_DESCRIPTION, _USAGE, false);
	}
	
	@Override
	protected int runCommand() throws Throwable
	{
		//get path to specified target
		RNSPath path = RNSPath.getCurrent();
		path = path.lookup(getArgument(0), RNSPathQueryFlags.MUST_EXIST);
		
		//extract qnames from agruments
		String []userInput = new String[numArguments()-1];
		QName[] qNames = new QName[numArguments()-1];
		Pattern leftBrace = Pattern.compile("\\{");
		Pattern rightBrace = Pattern.compile("\\}");
		
		for (int i = 1; i < numArguments(); i++)
		{
			userInput[i-1] = getArgument(i);
			String []splitOnLeftBrace = leftBrace.split(userInput[i-1], 2);
			String []splitOnRightBrace = rightBrace.split(splitOnLeftBrace[1], 2);
			qNames[i-1] = new QName(splitOnRightBrace[0], splitOnRightBrace[1]);
			stdout.println("Namespace: " +splitOnRightBrace[0] + 
					"\nLocal Part: "+splitOnRightBrace[1]);
		}
		
		//send request and output response
		GeniiCommon common = ClientUtils.createProxy(GeniiCommon.class,
			path.getEndpoint());
		
		if (numArguments() == 2){
			//get single resource property
			GetResourcePropertyResponse rpResponse;
			try{
				rpResponse = common.getResourceProperty(qNames[0]);
				
				for(MessageElement out : rpResponse.get_any()){
					stdout.println(out);
				}
			}
			catch(IOException ioe){
				stdout.println("GetResourceProperties failed: " + ioe.toString());
				return 0;
			}
		}
		else{
			//get multiple properties
			GetMultipleResourcePropertiesResponse rmpResponse;
			try{
				rmpResponse = common.getMultipleResourceProperties(qNames);
				
				for(MessageElement out : rmpResponse.get_any()){
					stdout.println(out);
				}
			}
			catch(IOException ioe){
				stdout.println("GetMultipleResourceProperties failed: " + ioe.toString());
				return 0;
			}
		}
		return 0;
	}

	@Override
	protected void verify() throws ToolException
	{
		if (numArguments() <= 1)
			throw new InvalidToolUsageException();
	}
}