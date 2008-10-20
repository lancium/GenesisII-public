package edu.virginia.vcgr.genii.client.jni.gIIlib.miscellaneous;


import java.io.BufferedReader;
import java.io.InputStreamReader;

import edu.virginia.vcgr.genii.client.cmd.CommandLineRunner;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.jni.gIIlib.JNILibraryBase;
import edu.virginia.vcgr.genii.client.security.gamlauthz.TransientCredentials;


public class JNILoginTool extends JNILibraryBase 
{	
	public static Boolean login(String keystorePath, String password, String certPattern){		
		tryToInitialize();
		if(ENABLE_LOCAL_TEST){
			return true;
		}
		
		CommandLineRunner runner = new CommandLineRunner();
		String[] args = {"login"};						
		
		try{
			runner.runCommand(args, System.out, System.err, new BufferedReader(new InputStreamReader(System.in)));
			
			// Checks to make sure login worked
			ICallingContext callContext = ContextManager.getCurrentContext(false);
			TransientCredentials transientCredentials = TransientCredentials
			.getTransientCredentials(callContext);
			
			if(transientCredentials != null && transientCredentials._credentials != null &&
					transientCredentials._credentials.size() > 0){
				return true;
			}
			else{
				return false;
			}		
		}catch(Throwable e){
			e.printStackTrace();
			return false;			
		}
	}		
}