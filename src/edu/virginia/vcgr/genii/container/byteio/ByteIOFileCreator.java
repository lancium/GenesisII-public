package edu.virginia.vcgr.genii.container.byteio;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.io.GuaranteedDirectory;

import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk.SudoExportUtils;
import edu.virginia.vcgr.genii.security.SAMLConstants;
import edu.virginia.vcgr.genii.security.credentials.CredentialWallet;

public class ByteIOFileCreator
{
	static private Log _logger = LogFactory.getLog(ByteIOFileCreator.class);

	static private Random _directoryBalancer = new Random();
	static private final int DISPERSION_LEVELS = 2;
	static private final int DISPERSION_WIDTH = 32;

	/**
	 * Create a new file in the user directory for saving byteIO data.
	 */
	synchronized public static File createFile(File userDir) throws IOException
	{
		File baseDir, uroot;
		baseDir = new GuaranteedDirectory(userDir, "rbyteio-data");
		uroot = baseDir;

		// First get the calling context.
		ICallingContext callContext = ContextManager.getExistingContext();
		// Then get the credential wallet.
		CredentialWallet Wallet =
			(CredentialWallet) callContext.getTransientProperty(SAMLConstants.SAML_CREDENTIALS_WALLET_PROPERTY_NAME);
		// Then, get the list of USER names from the credential wallet.
		if (!Wallet.isEmpty()) {
			/*
			 * Then pick the first one, but be careful - usernames may not always be unique - they are these days, but maybe not
			 * in future.
			 */
			String userName = Wallet.getFirstUserName();
			_logger.debug("username chosen for byteio file is: " + userName);
			if (userName != null) {
				baseDir = new GuaranteedDirectory(uroot, userName);
			} else {
				String msg = "failed attempting to create a byteio file without any user credentials.";
				_logger.error(msg);
				throw new IOException(msg);
			}
		}

		String filePrefix = "rbyteio";
		String fileSuffix = ".dat";
		for (int lcv = 0; lcv < DISPERSION_LEVELS; lcv++) {
			int value = _directoryBalancer.nextInt(DISPERSION_WIDTH);
			baseDir = new GuaranteedDirectory(baseDir, String.format("dir.%d", value));
		}

		File toReturn = File.createTempFile(filePrefix, fileSuffix, baseDir);
		SudoExportUtils.chownIfChownToUserEnabled(toReturn);
		return toReturn;
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
