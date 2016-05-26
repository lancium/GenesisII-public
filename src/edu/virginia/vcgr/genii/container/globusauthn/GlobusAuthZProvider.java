/*
 * Copyright 2006 University of Virginia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */

package edu.virginia.vcgr.genii.container.globusauthn;

import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bouncycastle.util.encoders.Base64;
import org.jsoup.Connection;
import org.jsoup.Connection.Method;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.virginia.vcgr.genii.client.comm.axis.security.VcgrSslSocketFactory;
import edu.virginia.vcgr.genii.client.configuration.ContainerConfiguration;
import edu.virginia.vcgr.genii.client.configuration.SslInformation;
import edu.virginia.vcgr.genii.client.configuration.SslInformation.GlobusAuthClientSecrets;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.resource.IResource;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.security.axis.AuthZSecurityException;
import edu.virginia.vcgr.genii.container.security.authz.providers.AclAuthZProvider;
import edu.virginia.vcgr.genii.security.RWXCategory;
import edu.virginia.vcgr.genii.security.SAMLConstants;
import edu.virginia.vcgr.genii.security.SecurityConstants;
import edu.virginia.vcgr.genii.security.credentials.NuCredential;
import edu.virginia.vcgr.genii.security.credentials.identity.UsernamePasswordIdentity;

/**
 * Globus Auth access control implementation, extends default ACL authz provider
 * 
 * @author ckoeritz (originally based on Kerberos implementation.)
 */
public class GlobusAuthZProvider extends AclAuthZProvider
{
	static private Log _logger = LogFactory.getLog(GlobusAuthZProvider.class);

	public GlobusAuthZProvider() throws AuthZSecurityException, IOException
	{
	}

	@Override
	public boolean checkAccess(Collection<NuCredential> authenticatedCallerCredentials, IResource resource, Class<?> serviceClass,
		java.lang.reflect.Method operation)
	{
		// Try regular ACLs for administrative access.
		try {
			/*
			 * we cannot let the credentials be used intact, because the myproxy identity could be in here, which would lead us to think we
			 * authenticated already when we have not yet. thus we strip out that credential if we see it and force the user/password
			 * authentication process to occur.
			 */
			ArrayList<NuCredential> prunedCredentials = new ArrayList<NuCredential>();
			X509Certificate[] resourceCertChain = null;
			try {
				resourceCertChain = (X509Certificate[]) resource.getProperty(IResource.CERTIFICATE_CHAIN_PROPERTY_NAME);
			} catch (ResourceException e) {
				_logger.error("failed to load resource certificate chain for globus auth.  resource is: " + resource.toString());
				// this seems really pretty bad. the resource is bogus.
				return false;
			}
			for (NuCredential cred : authenticatedCallerCredentials) {
				if (cred.getOriginalAsserter().equals(resourceCertChain)) {
					_logger.debug("dropping own identity from cred set so we can authorize properly.");
					continue;
				}
				prunedCredentials.add(cred);
			}

			/*
			 * we must check that the resource is writable if we're going to skip authentication. this must only be true for the admin of the
			 * STS.
			 */
			boolean accessOkay = checkAccess(prunedCredentials, resource, RWXCategory.WRITE);
			if (accessOkay) {
				_logger.debug("skipping globus auth authentication due to administrative access to resource.");
				if (_logger.isTraceEnabled()) {
					blurtCredentials("credentials that enabled globus auth authz skip are: ", prunedCredentials);
				}
				return true;
			}
		} catch (Exception AclException) {
			/*
			 * we assume we will need the sequel of the function now, since admin ACLs didn't work.
			 */
		}

		// try the globus auth server. this is the main case for this method.
		boolean globusAuthOkay = testGlobusAuthAuthorization(resource);
		if (globusAuthOkay) {
			return true;
		}

		// just try a traditional access check now. the globus auth attempt did not succeed.
		boolean accessOkay = super.checkAccess(authenticatedCallerCredentials, resource, serviceClass, operation);
		if (accessOkay) {
			if (_logger.isDebugEnabled())
				_logger.debug("allowing globus auth due to base class permission.");
			return true;
		}

		// Nobody appreciates us.
		String assetName = resource.toString();
		try {
			String addIn = (String) resource.getProperty(SecurityConstants.NEW_IDP_NAME_QNAME.getLocalPart());
			if (addIn != null)
				assetName.concat(" -- " + addIn);
		} catch (ResourceException e) {
			// ignore.
		}
		_logger.error("failure to authorize " + operation.getName() + " for " + assetName);
		return false;
	}

	/**
	 * performs the actual two-legged oauth authentication of the user's name and password against globus auth. adapted from code donated by
	 * the XSEDE Portal Group (Maytal Dahan and Alex Rocha).
	 */
	static public boolean performGlobusAuthentication(String username, String password, String server, String clientID, String clientSecret)
	{
		try {
			String encoded = new String(Base64.encode((clientID + ":" + clientSecret).getBytes()));
			Connection conn = Jsoup.connect(server).method(Method.POST).header("Content-Type", "application/x-www-form-urlencoded")
				.header("Authorization", "Basic " + encoded)
				
				//new addition from alex:				
				.ignoreHttpErrors(true).ignoreContentType(true)			
				//end new additions.
				
				.timeout(10000);
			
			conn.data("grant_type", "password");
			conn.data("username", username + "@xsede.org");
			conn.data("password", password);
			_logger.debug("Start login and token request for: " + username);
			org.jsoup.Connection.Response resp = conn.execute();
			_logger.debug("Finish login and token request for: " + username);
			Document respDoc = resp.parse();
			String jsonResp = respDoc.getElementsByTag("body").first().text();
			_logger.debug("status code: " + resp.statusCode() + " response body: \n" + jsonResp);
			int result = resp.statusCode();
			if (result == 200) {
				ObjectMapper mapper = new ObjectMapper();
				JsonNode obj = mapper.readTree(jsonResp);
				String accessToken = obj.get("access_token").asText();
				String expiresIn = obj.get("expires_in").asText();

				// future: we can record the tokens here if we need them, but currently we drop them on the floor.

				// hmmm: do not want to log these secrets here!!! just for debugging!!!!!!!
				_logger.debug("REMOVE THIS LINE: access token: '" + accessToken + "' expires in: '" + expiresIn + "'");

				return true;
			} else if (result == 403) {
				
				//hmmm: REMOVE THIS IMMEDIATELY WHEN THEY GET THE CONSENT ISSUE FIXED.
				
				_logger.warn("FAILED TO GAIN ACCESS DUE TO 403 ERROR, PROBABLY CONSENT ISSUE.  DEBUGGING MODE ALLOWING THIS!!!!");
				return true;
			} else {
				_logger.debug("failed to authenticate to globus auth with http code=" + result);
			}

		} catch (Exception e) {
			_logger.error("exception during attempt to authenticate via globus auth", e);
		}
		// any paths reaching here were not successful at logging in.
		return false;
	}

	static public boolean testGlobusAuthAuthorization(IResource resource)
	{
		// server is hardcoded for now, since it's a well known item for globus auth.
		String server = "https://auth.globus.org/v2/oauth2/token";
		String username = "";

		GlobusAuthClientSecrets secrets = null;
		try {
			username = (String) resource.getProperty(SecurityConstants.NEW_IDP_NAME_QNAME.getLocalPart());

			SslInformation sslinfo = ContainerConfiguration.getTheContainerConfig().getSslInformation();
			secrets = sslinfo.loadGlobusAuthSecrets();
		} catch (Throwable e) {
			_logger.error("failed to retrieve globus auth properties: " + e.getMessage(), e);
			return false;
		}

		if ((secrets == null) || (secrets._clientId == null) || (secrets._clientSecret == null)) {
			_logger.error("Insufficient Globus Auth configuration; either clientId or clientSecret is missing.");
			return false;
		}

		ICallingContext callingContext;
		try {
			callingContext = ContextManager.getExistingContext();
		} catch (IOException e) {
			_logger.error("Calling context exception", e);
			return false;
		}
		
		// kludge required to get the jsoup connector able to see our calling context.
		VcgrSslSocketFactory.extraneousCallingContextForSocketFactory = callingContext;

		// try each identity in the caller's credentials
		@SuppressWarnings("unchecked")
		ArrayList<NuCredential> callerCredentials =
			(ArrayList<NuCredential>) callingContext.getTransientProperty(SAMLConstants.CALLER_CREDENTIALS_PROPERTY);
		for (NuCredential cred : callerCredentials) {
			if (cred instanceof UsernamePasswordIdentity) {
				// Grab password from usernametoken (but use the username that is our resource name)
				UsernamePasswordIdentity utIdentity = (UsernamePasswordIdentity) cred;
				String password = utIdentity.getPassword();
				
				boolean success = performGlobusAuthentication(username, password, server, secrets._clientId, secrets._clientSecret);
				if (success == true) {
					return success;
				}

			}
		}
		
		// reset this kludge.
		VcgrSslSocketFactory.extraneousCallingContextForSocketFactory = null;

		// if we have achieved this location in the code, we have failed to authorize the user by any means.
		return false;
	}
}
