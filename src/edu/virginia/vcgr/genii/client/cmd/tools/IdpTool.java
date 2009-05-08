package edu.virginia.vcgr.genii.client.cmd.tools;

import java.net.URI;
import java.security.cert.X509Certificate;
import java.util.*;

import org.apache.axis.message.MessageElement;

import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.security.*;
import edu.virginia.vcgr.genii.client.security.credentials.GIICredential;
import edu.virginia.vcgr.genii.client.security.credentials.identity.UsernamePasswordIdentity;
import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.rns.*;

public class IdpTool extends GamlLoginTool {

	static private final String _DESCRIPTION2 = 
		"Creates a proxy authentication object to delegate an X.509 identity";
	static private final String _USAGE2 = "idp "
		+ "[--storetype=<PKCS12|JKS|WIN>] "
		+ "[--password=<keystore-password>] " 
		+ "[--alias] "
		+ "[--pattern=<certificate/token pattern>] "
		+ "[--validMillis=<valid milliseconds>] " 
		+ "[<authentication source URL>] "
		+ "<IDP service path> "
		+ "<new IDP name>";


	public IdpTool() { 
		super(_DESCRIPTION2, _USAGE2, false);
		
		// set valid millis to 180 days
		_validMillis = 1000L * 60 * 60 * 24 * 180;		
	}

	@Override
	protected int runCommand() throws Throwable {
		String idpServiceRelPath = null;
		String newIdpName = null;
		
		switch (numArguments()) {
		case 2:
			idpServiceRelPath = this.getArgument(0);
			newIdpName = this.getArgument(1);
			break;
		case 3:
			_authnUri = getArgument(0);
			idpServiceRelPath = this.getArgument(1);
			newIdpName = this.getArgument(2);
			break;
		}
		
		// get rns path to idp service
		RNSPath idpService = RNSPath.getCurrent().lookup(idpServiceRelPath,
				RNSPathQueryFlags.MUST_EXIST);

		// get the identity of the idp service
		X509Certificate[] idpCertChain = EPRUtils.extractCertChain(idpService
				.getEndpoint());
		if (idpCertChain == null) {
			throw new RNSException("Entry \"" + idpServiceRelPath
					+ "\" is not an IDP service.");
		}

		MessageElement[] constructionParms = null;
		MessageElement newIdpNameParm = new MessageElement(
				SecurityConstants.NEW_IDP_NAME_QNAME, newIdpName);

		
		if ((_authnUri == null) && (_storeType == null)) {

			MessageElement validMillisParm = new MessageElement(
					SecurityConstants.IDP_VALID_MILLIS_QNAME, _validMillis);

			if ((_username != null) && (_password != null)) {
				
				// actually create a new idp that delegates a 
				// usernametoken credential
				UsernamePasswordIdentity ut = 
					new UsernamePasswordIdentity(_username, _password);
				MessageElement delegatedIdentParm = new MessageElement(
						SecurityConstants.IDP_DELEGATED_CREDENTIAL_QNAME);
				delegatedIdentParm.addChild(ut.toMessageElement());
				
				constructionParms = 
					new MessageElement[] { delegatedIdentParm, newIdpNameParm, validMillisParm};
				
			} else {

				// we're creating a new-identity from scratch, not 
				// delegating one into the grid
				
				constructionParms = 
					new MessageElement[] { validMillisParm, newIdpNameParm };
			}
			
		} else {
		
			// create a new IDP that further delegates a delegated token
			
			// log in
			URI authnSource = (_authnUri == null) ? null : new URI(_authnUri);
			ArrayList<GIICredential> assertions = delegateToIdentity(
					authnSource, 
					ContextManager.getCurrentContext(), 
					idpCertChain);
	
			if ((assertions == null) || (assertions.size() == 0)) {
				return 0;
			}
			
			stdout.println("Creating idp for attribute for \""
					+ assertions.get(0) + "\".");
	
			// serialize the delegatedAssertion and put into construction params
			MessageElement delegatedIdentParm = new MessageElement(
					SecurityConstants.IDP_DELEGATED_CREDENTIAL_QNAME);
			delegatedIdentParm.addChild(assertions.get(0).toMessageElement());
			constructionParms = 
				new MessageElement[] { delegatedIdentParm, newIdpNameParm };
	
		}

		// create the new idp resource and link it into context space
		CreateResourceTool.createInstance(
				idpService.getEndpoint(),
				null,						// no link needed 
				constructionParms);

		return 0;
	}

	@Override
	protected void verify() throws ToolException
	{
		int numArgs = numArguments();
		if ((numArgs < 2) || (numArgs > 3))
			throw new InvalidToolUsageException();
		
	}

}