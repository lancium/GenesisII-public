package edu.virginia.vcgr.genii.client.cmd.tools;

import java.net.URI;
import java.util.ArrayList;
import java.util.Date;



import org.ws.addressing.EndpointReferenceType;
import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.comm.SecurityUpdateResults;
import edu.virginia.vcgr.genii.client.context.CallingContextImpl;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.dialog.DialogFactory;
import edu.virginia.vcgr.genii.client.dialog.DialogProvider;
import edu.virginia.vcgr.genii.client.dialog.InputDialog;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;
import edu.virginia.vcgr.genii.client.security.credentials.GIICredential;
import edu.virginia.vcgr.genii.client.security.credentials.TransientCredentials;
import edu.virginia.vcgr.genii.client.security.credentials.identity.UsernamePasswordIdentity;
import edu.virginia.vcgr.genii.client.security.x509.KeyAndCertMaterial;
import edu.virginia.vcgr.genii.client.utils.PathUtils;
import edu.virginia.vcgr.genii.client.utils.units.Duration;
import edu.virginia.vcgr.genii.client.utils.units.DurationUnits;
import edu.virginia.vcgr.genii.context.ContextType;


public class LoginTool  extends BaseLoginTool {


	static private final String _DESCRIPTION = "Inserts authentication information into the user's context.";
	static private final String _USAGE_RESOURCE = 
		"login [--username=<username>] [--password=<password>] [--validDuration=<duration-string>] [rns:<identity provider path>]";

	protected LoginTool(String description, String usage, boolean isHidden) {
		super(description, usage, isHidden);
	}

	public LoginTool() {
		super(_DESCRIPTION, _USAGE_RESOURCE, false);
	}


	@Override
	protected int runCommand() throws Throwable
	{

		
		// get the local identity's key material (or create one if necessary)
		ICallingContext callContext = ContextManager.getCurrentContext(false);
		if (callContext == null) {
			callContext = new CallingContextImpl(new ContextType());
		}
		
		DialogProvider provider = DialogFactory.getProvider(
				stdout, stderr, stdin, useGui());


		if (_username == null){
			InputDialog usernameDialog = provider.createInputDialog(
					"Username", 
			"Please enter username.");
			usernameDialog.showDialog();
			_username = usernameDialog.getAnswer();
		}


		//Look for IDP path
		if (numArguments() == 0){
			//Assume in /users/userid
			_authnUri = "rns:/users/" + _username;
		}
		else{
			_authnUri = getArgument(0);	 
		}
		
	

		URI authnSource = PathUtils.pathToURI(_authnUri);

		//Check if idp exists else prompt for it
		if (!callContext.getCurrentPath().lookup(authnSource.getSchemeSpecificPart()).exists()){
			InputDialog IDPDialog = provider.createInputDialog(
					"IDP", 
			"Please enter rns path to your IDP.");
			IDPDialog.showDialog();
			_authnUri = IDPDialog.getAnswer();
			authnSource = PathUtils.pathToURI(_authnUri);
		}
		
		


		//Do password Login
		UsernamePasswordIdentity utCredential = new PasswordLoginTool().doPasswordLogin(_username, _password);

		if (utCredential != null){	

			TransientCredentials transientCredentials = TransientCredentials
			.getTransientCredentials(callContext);
			transientCredentials._credentials.add(utCredential);

			ContextManager.storeCurrentContext(callContext);
			
			
			// we're going to use the WS-TRUST token-issue operation
			// to log in to a security tokens service
			KeyAndCertMaterial clientKeyMaterial = 
				ClientUtils.checkAndRenewCredentials(callContext, 
						new Date(), new SecurityUpdateResults());

			RNSPath authnPath = callContext.getCurrentPath().lookup(
					authnSource.getSchemeSpecificPart(),
					RNSPathQueryFlags.MUST_EXIST);
			EndpointReferenceType epr = authnPath.getEndpoint();


			try {

				//Do IDP login
				ArrayList<GIICredential> signedAssertions = IDPLoginTool.doIdpLogin(epr, _validMillis, clientKeyMaterial._clientCertChain);

				if (signedAssertions == null) {
					return 0;
				}

				// insert the assertion into the calling context's transient creds
				transientCredentials._credentials.addAll(signedAssertions);

			} finally {

				if (utCredential != null) {
					// the UT credential was used only to log into the IDP, remove it
					transientCredentials._credentials.remove(utCredential);
					TransientCredentials._logger.debug("Removing temporary username-token credential from current calling context credentials.");
				}
			}


		}


		ContextManager.storeCurrentContext(callContext);

		return 0;
	}

	@Override
	protected void verify() throws ToolException 
	{
		int numArgs = numArguments();
		if (numArgs > 1) 
			throw new InvalidToolUsageException();

		if (_durationString != null)
		{
			try
			{
				_validMillis = (long)new Duration(
						_durationString).as(DurationUnits.Milliseconds);
			}
			catch (IllegalArgumentException pe)
			{
				throw new ToolException("Invalid duration string given.", pe);
			}
		}

	}


}
