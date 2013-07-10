package edu.virginia.vcgr.genii.container.byteio;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import org.morgan.util.io.GuaranteedDirectory;

import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.security.SAMLConstants;
import edu.virginia.vcgr.genii.security.credentials.CredentialWallet;

public class ByteIOFileCreator
{
	static private Random _directoryBalancer = new Random();
	static private final int DISPERSION_LEVELS = 2;
	static private final int DISPERSION_WIDTH = 32;

	/**
	 * Create a new file in the user directory for saving byteIO data.
	 */
	synchronized public static File createFile(File userDir) throws IOException
	{
		File baseDir,uroot;;
		baseDir= new GuaranteedDirectory(userDir, "rbyteio-data");
		uroot=baseDir;

		// ** First get the calling context
		//System.err.println("about to call getexistingcontext");
		ICallingContext callContext = ContextManager.getExistingContext();
		//System.err.println("called getexistingcontext");
		// **Then get the credential wallet
		CredentialWallet Wallet = (CredentialWallet) callContext
				.getTransientProperty(SAMLConstants.SAML_CREDENTIALS_WALLET_PROPERTY_NAME);
		// **Then, get the list of USER names from the credential wallet
		//System.err.println("called getwallet");
		if (!Wallet.isEmpty()) {
			// Then pick the first one
			// Be careful - usernames may not always be unique - they are these
			// days, but maybe not in future
			String userName = Wallet.getFirstUserName();
//			System.err.println("The username is: " + userName);
			baseDir = new GuaranteedDirectory(uroot, userName);
		}

		String filePrefix = "rbyteio";
		//if (fileName !=null) filePrefix=fileName+"-";
		String fileSuffix = ".dat";
		for (int lcv = 0; lcv < DISPERSION_LEVELS; lcv++) {
			int value = _directoryBalancer.nextInt(DISPERSION_WIDTH);
			baseDir = new GuaranteedDirectory(baseDir, String.format("dir.%d", value));
		}
		return File.createTempFile(filePrefix, fileSuffix, baseDir);
	}

	public static String getRelativePath(File userDir, File newFile)
	{
		String userPath = userDir.getAbsolutePath() + File.separator;
		String path = newFile.getAbsolutePath();
		if (path.startsWith(userPath)) {
			return path.substring(userPath.length());
		}
		return path;
	}

	public static File getAbsoluteFile(File userDir, String path)
	{
		File file = new File(path);
		return (file.isAbsolute() ? file : new File(userDir, path));
	}
}
