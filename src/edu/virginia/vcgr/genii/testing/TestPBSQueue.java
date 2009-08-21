package edu.virginia.vcgr.genii.testing;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import org.morgan.util.io.GuaranteedDirectory;

import edu.virginia.vcgr.genii.client.jsdl.spmd.SPMDConstants;
import edu.virginia.vcgr.genii.client.nativeq.ApplicationDescription;
import edu.virginia.vcgr.genii.client.nativeq.JobToken;
import edu.virginia.vcgr.genii.client.nativeq.NativeQueue;
import edu.virginia.vcgr.genii.client.nativeq.NativeQueueConnection;
import edu.virginia.vcgr.genii.client.nativeq.NativeQueues;

public class TestPBSQueue
{
	static public void main(String []args) throws Throwable
	{
		Collection<String> arggs = new ArrayList<String>();
		arggs.add("-s");
		arggs.add("Testing");
		arggs.add("mmm2a@virginia.edu");
		File workingDir = new GuaranteedDirectory("/home/mmm2a/queue");
		ApplicationDescription application =  new ApplicationDescription(
				SPMDConstants.ANY_MPI, new Integer(5), new Integer(1),
				"mail", arggs, null, "/etc/passwd", "/dev/null", "/dev/null", 
				new Double(5000000));
		NativeQueue queue = NativeQueues.getNativeQueue("pbs");
		
		NativeQueueConnection connection = queue.connect(workingDir, null);
		JobToken token = connection.submit(application);
		System.out.println("Job Token:  \"" + token + "\".");
		System.out.println("Job Status:  " + connection.getStatus(token));
		connection.cancel(token);
	}
}