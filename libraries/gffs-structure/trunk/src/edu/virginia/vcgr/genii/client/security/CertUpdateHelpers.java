package edu.virginia.vcgr.genii.client.security;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.client.InstallationProperties;

public class CertUpdateHelpers
{
	static private Log _logger = LogFactory.getLog(CertUpdateHelpers.class);

	// the name of a property file that tracks our grid certificate update process.
	static public final String UPDATE_GRID_CERTS_PROPERTY_FILE = "update-grid-certs.properties";

	static final public String UPDATE_INTERVAL_PROPNAME = "UpdateInterval";
	static final public String NEXT_UPDATE_PROPNAME = "NextUpdate";
	static final public String LAST_UPDATE_PACKAGE_PROPNAME = "LastCertPackageName";

	static final public Long DEFAULT_CERT_UPDATE_INTERVAL = new Long(24 * 60 * 60 * 1000);

	// the location in the grid where we expect to find certificate packages.
	static final public String RNS_CERTS_FOLDER = "/etc/grid-security/certificates";

	/**
	 * returns the currently active certificate update properties. the user must lock the
	 * consistency lock file before invoking this.
	 */
	public static Properties getCertUpdateProperties()
	{
		File propFile = getUpdateGridCertsFile();
		Properties props = readProperties(propFile);
		return props;
	}

	/**
	 * updates the certificate properties file with the properties provided. the user must lock the
	 * consistency lock file before invoking this.
	 */
	public static boolean putCertUpdateProperties(Properties toWrite)
	{
		File propFile = getUpdateGridCertsFile();
		boolean toReturn = writeProperties(propFile, toWrite);
		return toReturn;
	}

	// service functions below.

	/**
	 * returns the file used for grid certificate update properties, which we both read and write.
	 */
	public static File getUpdateGridCertsFile()
	{
		String stateDir = InstallationProperties.getUserDir();
		File propsFile = new File(stateDir + "/" + UPDATE_GRID_CERTS_PROPERTY_FILE);
		return propsFile;
	}

	/**
	 * assuming the consistency lock is held, this reads the certificate update properties from the
	 * config file.
	 */
	private static Properties readProperties(File propFile)
	{
		_logger.debug("into read properties on " + propFile);
		if (!propFile.isFile() || !propFile.canRead()) {
			writeDefaultProperties(propFile);
			_logger.debug("wrote default properties on " + propFile);
		}
		Properties toReturn = new Properties();
		InputStream in = null;
		try {
			in = new FileInputStream(propFile);
			toReturn.load(in);
		} catch (IOException e) {
			_logger.error("failure loading properties from '" + propFile + "'", e);
			return null;
		} finally {
			StreamUtils.close(in);
		}
		return toReturn;
	}

	/**
	 * assuming that the consistency lock is already grabbed, this will write the default version of
	 * the cert update properties file.
	 */
	public static void writeDefaultProperties(File propFile)
	{
		try {
			Properties props = new Properties();

			Long defDuration = new Long(DEFAULT_CERT_UPDATE_INTERVAL);
			props.setProperty(UPDATE_INTERVAL_PROPNAME, defDuration.toString());
			long timestamp = new Date().getTime();
			// force an update soon.
			props.setProperty(NEXT_UPDATE_PROPNAME, "" + (timestamp - 10000));
			props.setProperty(LAST_UPDATE_PACKAGE_PROPNAME, "none");

			File f = getUpdateGridCertsFile();
			OutputStream out = new FileOutputStream(f);
			props.store(out, "certificate update properties");
		} catch (Exception e) {
			_logger.error("failed to write default version of properties for certificate updates");
		}
	}

	/**
	 * assuming that the consistency lock is established, this will write out the properties
	 * provided to the cert update properties file.
	 */
	public static boolean writeProperties(File propFile, Properties data)
	{
		try {
			OutputStream out = new FileOutputStream(propFile);
			data.store(out, "certificate update properties");
		} catch (Exception e) {
			_logger.error("failed to write updated properties for certificate updates");
			return false;
		}
		return true;
	}
}
