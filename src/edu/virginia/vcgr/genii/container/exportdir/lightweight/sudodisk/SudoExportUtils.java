package edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk;

import java.io.IOException;

import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.security.SAMLConstants;
import edu.virginia.vcgr.genii.security.credentials.CredentialWallet;

public class SudoExportUtils
{

	private static String extractUsernameFromCallingContext() throws IOException
	{

		// ** First get the calling context
		ICallingContext callContext = ContextManager.getExistingContext();

		// **Then get the credential wallet
		CredentialWallet Wallet =
			(CredentialWallet) callContext.getTransientProperty(SAMLConstants.SAML_CREDENTIALS_WALLET_PROPERTY_NAME);
		// **Then, get the list of USER names from the credential wallet
		// System.err.println("called getwallet");
		String userName = null;
		if (!Wallet.isEmpty()) {
			// Then pick the first one
			// Be careful - usernames may not always be unique - they are these
			// days, but maybe not in future
			userName = Wallet.getFirstUserName();
		}

		return userName;
	}

	/**
	 * This function extracts the first username from calling context and then looks at the gridmap
	 * file to return the corresponding local username
	 * 
	 * @return
	 * @throws IOException
	 */
	public static String getLocalUser() throws IOException
	{
		String cc_uname = extractUsernameFromCallingContext();
		if (cc_uname == null) {
			return null;
		}

		return (doGridMapping(cc_uname));
	}

	/**
	 * This function must be augmented in the future to return the local username given the username
	 * from the calling context
	 * 
	 * @param cc_uname
	 * @return
	 */
	public static String doGridMapping(String cc_uname)
	{
		return "guest1";
	}
}