package edu.virginia.vcgr.genii.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.io.StreamUtils;

/**
 * manages the properties for gffs exports for the container. this class should only be used by the
 * container-side code, although due to current organizational constraints, it's in gffs-structure
 * which is nominally a client-side library.
 * 
 * @author Chris Koeritz
 */
public class ExportProperties
{
	static private Log _logger = LogFactory.getLog(ExportProperties.class);

	// the config file that this class reads is set here.
	static public final String EXPORT_PROPERTIES_FILENAME = "export.properties";

	private Properties _exportProps = null;
	
	static private ExportProperties _realExportProperties = null;

	/**
	 * for all normal run-time classes, the export properties are accessed this way.
	 */
	static public ExportProperties getExportProperties()
	{
		synchronized (InstallationProperties.class) {
			if (_realExportProperties == null) {
				_realExportProperties = new ExportProperties();
			}
		}
		return _realExportProperties;
	}

	/**
	 * provides access to all of the properties that control exports and certain other storage
	 * attributes.
	 */
	private ExportProperties()
	{
		openProperties();
		if (_exportProps == null) {
			_logger.error("failed to load export properties configuration.");
		}
	}

	// symbols for property names defined in the design document...

	// the mechanism tells the container how to perform exports.
	public static final String EXPORT_MECHANISM_PROPERTY = "export.Mechanism";
	// can export configuration be overridden in construction properties of exported resource?
	public static final String EXPORT_ALLOW_OVERRIDE_PROPERTY = "export.AllowOverride";
	// will random byteio files be stored in user's home dir?
	public static final String BYTEIO_IN_USERHOME_PROPERTY = "byteio.InUserHome";
	// who will own the byteio files, the gffs container or the creating user?
	public static final String BYTEIO_STORAGE_PROPERTY = "byteio.Storage";
	// the location where the gffschown application (or script) is stored.
	public static final String EXPORT_GFFSCHOWN_APP_PROPERTY = "export.GffsChownApp";
	// the path to the proxy IO launcher app / script.
	public static final String EXPORT_PROXYIO_APP_PROPERTY = "export.ProxyIOApp";
	// the grid map file maps gffs grid users to local unix users.
	public static final String EXPORT_GRIDMAPFILE_LOCATION_PROPERTY = "export.GridMapFile";

	// enumerations based on the design doc...

	/**
	 * enumerates the export mechanisms supported (or to be supported) by the container.
	 */
	public static enum ExportMechanisms {
		EXPORT_MECH_ACL("ACL"),
		EXPORT_MECH_ACLANDCHOWN("ACLandChown"),
		EXPORT_MECH_PROXYIO("ProxyIO"),
		EXPORT_MECH_ARCHIVE("Archive");

		private String _text = null;

		private ExportMechanisms(String text)
		{
			_text = text;
		}

		@Override
		public String toString()
		{
			return _text;
		}

		/**
		 * returns an ExportMechanisms enum from a string if possible. if not possible, null is
		 * returned.
		 */
		public static ExportMechanisms parse(String fromString)
		{
			if (fromString == null)
				return null;
			if (fromString.equalsIgnoreCase(EXPORT_MECH_ACL.toString()))
				return EXPORT_MECH_ACL;
			if (fromString.equalsIgnoreCase(EXPORT_MECH_ACLANDCHOWN.toString()))
				return EXPORT_MECH_ACLANDCHOWN;
			if (fromString.equalsIgnoreCase(EXPORT_MECH_PROXYIO.toString()))
				return EXPORT_MECH_PROXYIO;
			if (fromString.equalsIgnoreCase(EXPORT_MECH_ARCHIVE.toString()))
				return EXPORT_MECH_ARCHIVE;
			return null;
		}
	};

	/**
	 * enumerates the possible types of ownership for random byteIO files.
	 */
	public static enum OwnershipForByteIO {
		BYTEIO_GFFS_OWNED("OwnedByGFFS"),
		BYTEIO_CHOWNTOUSER("ChownToUser");

		private String _text = null;

		private OwnershipForByteIO(String text)
		{
			_text = text;
		}

		@Override
		public String toString()
		{
			return _text;
		}

		public static OwnershipForByteIO parse(String fromString)
		{
			if (fromString == null)
				return null;
			if (fromString.equalsIgnoreCase(BYTEIO_GFFS_OWNED.toString()))
				return BYTEIO_GFFS_OWNED;
			if (fromString.equalsIgnoreCase(BYTEIO_CHOWNTOUSER.toString()))
				return BYTEIO_CHOWNTOUSER;
			return null;
		}
	};

	/*
	 * the main informational functions follow. these all come from the export.properties file.
	 */
	// hmmm: be careful if we need to rename that export.properties file again.

	// hmmm: throughout--need error reporting if not right flag.

	// hmmm: throughout, warn that we return null on failure.

	public ExportMechanisms getExportMechanism()
	{
		if (_exportProps == null) {
			_logger.error("export property configuration is not defined.");
			return null;
		}
		String r = _exportProps.getProperty(EXPORT_MECHANISM_PROPERTY);
		return ExportMechanisms.parse(r);
	}

	public boolean getByteIOInUserHome()
	{
		if (_exportProps == null) {
			_logger.error("export property configuration is not defined.");
			return false;
		}
		String r = _exportProps.getProperty(BYTEIO_IN_USERHOME_PROPERTY);
		return r.equalsIgnoreCase("true");
	}

	// hmmm: if we implement this as per design, it may actually be required on client side.
	// hoping to provide alternate approach.
	//
	// public File getGffsExportsFile()

	public OwnershipForByteIO getByteIOStorage()
	{
		if (_exportProps == null) {
			_logger.error("export property configuration is not defined.");
			return null;
		}
		String r = _exportProps.getProperty(BYTEIO_STORAGE_PROPERTY);
		return OwnershipForByteIO.parse(r);
	}

	public boolean getAllowOverride()
	{
		if (_exportProps == null) {
			_logger.error("export property configuration is not defined.");
			return false;
		}
		String r = _exportProps.getProperty(EXPORT_ALLOW_OVERRIDE_PROPERTY);
		return r.equalsIgnoreCase("true");
	}

	public String getGffsChownFilePath()
	{
		if (_exportProps == null) {
			_logger.error("export property configuration is not defined.");
			return null;
		}
		return _exportProps.getProperty(EXPORT_GFFSCHOWN_APP_PROPERTY);
	}

	public String getProxyIOLauncherFilePath()
	{
		if (_exportProps == null) {
			_logger.error("export property configuration is not defined.");
			return null;
		}
		return _exportProps.getProperty(EXPORT_PROXYIO_APP_PROPERTY);
	}

	public String getGridMapFile()
	{
		if (_exportProps == null) {
			_logger.error("export property configuration is not defined.");
			return null;
		}
		return _exportProps.getProperty(EXPORT_GRIDMAPFILE_LOCATION_PROPERTY);
	}

	// hmmm: need to add a stat check that lets us know if the grid map file's changed since our last
	// checking on it.

	// dingy basement of support functions...

	private void openProperties()
	{
		if (_exportProps != null) {
			// just use what we had before if it's already constructed.
			return;
		}
		File exportPropsFile = InstallationProperties.getInstallationProperties().getExportPropertiesFile();
		if (exportPropsFile == null) {
			// we failed to find an appropriate export property file.
			_exportProps = null;
			_logger.error("failed to locate export properties configuration file");
			return;
		}
		if (!exportPropsFile.isFile() || !exportPropsFile.canRead()) {
			_exportProps = null;
			_logger.error("failed to read the export properties configuration file");
			return;
		}
		_exportProps = new Properties();
		InputStream in = null;
		try {
			in = new FileInputStream(exportPropsFile);
			_exportProps.load(in);
		} catch (IOException e) {
			_logger.error("failure loading export properties", e);
			_exportProps = null;
		} finally {
			StreamUtils.close(in);
		}
	}

	/**
	 * returns a file pointer pointing at the grid-mapfile, if possible.
	 */
	public File openGridMapFile()
	{
		String fileName = getGridMapFile();
		if (fileName == null)
			return null;
		return new File(fileName);
	}

}
