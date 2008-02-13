package edu.virginia.vcgr.genii.client.jni.gIIlib;


import java.io.BufferedReader;
import java.io.InputStreamReader;

import edu.virginia.vcgr.genii.client.cmd.CommandLineRunner;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.security.gamlauthz.TransientCredentials;


public class JNILoginTool extends JNILibraryBase 
{

	private static boolean useGui = true; 
	
	public static Boolean login(String keystorePath, String password, String certPattern){
		tryToInitialize();
		
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

/*
			if(keystorePath == null || keystorePath.length() == 0){	
			System.out.println("Running GamlLoginTool");
			GamlLoginTool loginTool = new GamlLoginTool();	
			loginTool.setStoretype("WIN");
			loginTool.run(System.out, System.err, 
				new BufferedReader(new InputStreamReader(System.in)));
			
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
		}
		else{								
			AbstractGamlLoginHandler handler = null;
					
			if (!useGui)
				handler = new TextGamlLoginHandler(System.out, System.err, 
						new BufferedReader(new InputStreamReader(System.in)));
			else
				handler = new GuiGamlLoginHandler(System.out, System.err, 
						new BufferedReader(new InputStreamReader(System.in)));
			
			CertEntry certEntry = handler.selectCert(new FileInputStream(keystorePath), null, 
					password, false, certPattern);
			
			if (certEntry == null)
				return false;
			
			// Create identity assertion
			RenewableIdentityAttribute identityAttr = new RenewableIdentityAttribute(
				new BasicConstraints(
						System.currentTimeMillis() - (1000L * 60 * 15), // 15 minutes ago
						1000 * 60 * 60 * 2,								// valid 2 hours
						10),
				new X509Identity(certEntry._certChain));
			RenewableAttributeAssertion identityAssertion =
				new RenewableAttributeAssertion(identityAttr, certEntry._privateKey);
			
			// get the calling context (or create one if necessary)
			ICallingContext callContext = ContextManager.getCurrentContext(false);
			if (callContext == null)
				callContext = new CallingContextImpl(new ContextType());
			
			// Delegate the identity assertion to the temporary client
			// identity
			RenewableClientAttribute delegatedAttr = new RenewableClientAttribute(
				null, identityAssertion, callContext);
			RenewableClientAssertion delegatedAssertion = new RenewableClientAssertion(
				delegatedAttr, certEntry._privateKey);
			
			// insert the assertion into the calling context's transient creds
			TransientCredentials transientCredentials =
				TransientCredentials.getTransientCredentials(callContext);
			transientCredentials._credentials.add(delegatedAssertion);
			
			ContextManager.storeCurrentContext(callContext);
		}
*/