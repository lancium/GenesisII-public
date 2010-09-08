package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.security.cert.X509Certificate;
import java.util.*;

import org.apache.axis.message.MessageElement;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.byteio.ByteIOStreamFactory;
import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.security.*;
import edu.virginia.vcgr.genii.client.security.credentials.GIICredential;
import edu.virginia.vcgr.genii.client.security.credentials.identity.UsernamePasswordIdentity;
import edu.virginia.vcgr.genii.client.utils.units.Duration;
import edu.virginia.vcgr.genii.client.utils.units.DurationUnits;
import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.*;
import edu.virginia.vcgr.genii.client.gpath.GeniiPath;
import edu.virginia.vcgr.genii.client.gpath.GeniiPathType;


public class IdpTool extends BaseLoginTool {

	static private final String _DESCRIPTION = "Creates a proxy authentication object to delegate an X.509 identity";
	static private final String _USAGE_RESOURCE = "idp "
		+ "[--storetype=<PKCS12|JKS|WIN>] "
		+ "[--password=<keystore-password>] " 
		+ "[--alias] "
		+ "[--pattern=<certificate/token pattern>] "
		+ "[--validMillis=<valid milliseconds>] " 
		+ "[<authentication source URL>] "
		+ "<IDP service path> "
		+ "<new IDP name>";


	protected IdpTool(String description, String usage, boolean isHidden) {
		super(description, usage, isHidden);
	}

	public IdpTool() {
		super(_DESCRIPTION, _USAGE_RESOURCE, false);
		// set valid millis to 180 days
		_validMillis = 1000L * 60 * 60 * 24 * 180;		
	}

	@Override
	protected int runCommand() throws Throwable
	{
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
		GeniiPath gPath = new GeniiPath(idpServiceRelPath);
		if(gPath.pathType() != GeniiPathType.Grid)
			throw new InvalidToolUsageException("idpServicePath must be a grid path. ");
		RNSPath idpService = lookup(gPath,
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

			KeystoreLoginTool tool = new KeystoreLoginTool();
			BaseLoginTool.copyCreds(this, tool);


			ArrayList<GIICredential> assertions = null;
			GeniiPath tPath = new GeniiPath(_authnUri);

			if (_authnUri == null){
				//Keystore login
				assertions = tool.doKeystoreLogin(null, ContextManager.getCurrentContext(), idpCertChain);
			}
			else{
				if (tPath.pathType() == GeniiPathType.Local){
					//Keystore Login
					BufferedInputStream fis = new BufferedInputStream(
							new FileInputStream(tPath.path()));
					try {
						assertions = tool.doKeystoreLogin(fis, ContextManager.getCurrentContext(), idpCertChain);
					} finally {
						fis.close();
					}
				   
				}
				else {
					RNSPath authnPath = ContextManager.getCurrentContext().getCurrentPath().lookup(
							authnSource.getSchemeSpecificPart(),
							RNSPathQueryFlags.MUST_EXIST);
					EndpointReferenceType epr = authnPath.getEndpoint();
					TypeInformation type = new TypeInformation(epr);
					if (type.isIDP()){
						//IDP Login
						assertions = IDPLoginTool.doIdpLogin(epr, this._validMillis, idpCertChain);
					}
					else if (type.isByteIO()){
						// log into keystore from rns path to keystore file
						InputStream in = ByteIOStreamFactory.createInputStream(epr);
						try {
							assertions = tool.doKeystoreLogin(in, ContextManager.getCurrentContext(), idpCertChain);
						} finally {
							in.close();
						}
					}
				}		
			}


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

		if (_durationString != null)
		{
			try
			{
				_validMillis = (long)new Duration(_durationString).as(DurationUnits.Milliseconds);
			}
			catch (IllegalArgumentException pe)
			{
				throw new ToolException("Invalid duration string given.", pe);
			}
		}
	}
}