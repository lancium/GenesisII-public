package edu.virginia.vcgr.appmgr.launcher;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.security.cert.CertificateException;
import java.util.Collection;
import java.util.LinkedList;

import edu.virginia.vcgr.appmgr.io.IOUtils;
// import edu.virginia.vcgr.appmgr.io.ScratchSpaceManager;
// import edu.virginia.vcgr.appmgr.security.Verifier;
// import edu.virginia.vcgr.appmgr.security.VerifierFactory;
import edu.virginia.vcgr.appmgr.util.HierarchicalProperties;
import edu.virginia.vcgr.appmgr.util.PropertyUtilities;
import edu.virginia.vcgr.appmgr.version.VersionManager;

// import edu.virginia.vcgr.appmgr.version.VersionManager;

public class ApplicationDescription
{
	static final private String BASE_PROP_NAME = "edu.virginia.vcgr.appwatcher.";

	static final private String DEPENDENT_PROPERTY = BASE_PROP_NAME + "dependent-properties-file";

	static final private String PATCH_SIGNER_CERTIFICATE_PROPERTY_BASE = BASE_PROP_NAME + "patch-signer-certificate";
	static final private String APPLICATION_NAME_PROPERTY = BASE_PROP_NAME + "application-name";
	static final private String APPLICATION_DIRECTORY_PROPERTY = BASE_PROP_NAME + "application-directory";
	// static final private String UPDATE_DIRECTORY_PROPERTY = BASE_PROP_NAME + "update-directory";
	static final private String APPLICATION_URL_PROPERTY_BASE = BASE_PROP_NAME + "application-url";
	static final private String JAR_DESCRIPTION_FILE_PROPERTY = BASE_PROP_NAME + "jar-description-file";
	static final private String APPLICATION_CLASS_PROPERTY = BASE_PROP_NAME + "application-class";
	static final private String FAKE_UPDATER_CLASS_VALUE = "edu.virginia.vcgr.appmgr.update.UpdaterClass";

	// private Verifier _patchVerifier;
	private VersionManager _versionManager;
	private String _applicationName;
	private File _applicationDirectory;
	private File _updateDirectory;
	// private ScratchSpaceManager _scratchManager;
	private Collection<URL> _applicationDownloadURLs;
	private File _jarDescriptionFile;
	private String _applicationClassName;
	private Boolean _updateDisabled = false;

	static private HierarchicalProperties readPropertiesFile(File propFile) throws FileNotFoundException, IOException
	{
		HierarchicalProperties tmp = new HierarchicalProperties();
		FileInputStream fin = null;

		try {
			fin = new FileInputStream(propFile);
			tmp.load(fin);

			String dependentFile = tmp.getProperty(DEPENDENT_PROPERTY);
			if (dependentFile != null) {
				HierarchicalProperties parent = readPropertiesFile(new File(dependentFile));
				tmp.setParent(parent);
			}

			return tmp;
		} finally {
			IOUtils.close(fin);
		}
	}

	private void initialize(String applicationClassName, HierarchicalProperties properties) throws IOException,
		CertificateException
	{
		_applicationName = PropertyUtilities.getRequiredProperty(properties, APPLICATION_NAME_PROPERTY);
		_applicationDirectory = new File(PropertyUtilities.getRequiredProperty(properties, APPLICATION_DIRECTORY_PROPERTY));
		if (!_applicationDirectory.exists())
			throw new FileNotFoundException(String.format("Couldn't find application directory \"%s\".", _applicationDirectory));

		// _updateDirectory = new File(PropertyUtilities.getRequiredProperty(properties,
		// UPDATE_DIRECTORY_PROPERTY));
		// _scratchManager = new ScratchSpaceManager(new File(_updateDirectory, "scratch"));

		_jarDescriptionFile = new File(PropertyUtilities.getRequiredProperty(properties, JAR_DESCRIPTION_FILE_PROPERTY));
		if (!_jarDescriptionFile.exists())
			throw new FileNotFoundException(String.format("Couldn't find jar description file \"%s\".", _jarDescriptionFile));
		_applicationClassName = applicationClassName;

		if (_applicationClassName == null)
			_applicationClassName = PropertyUtilities.getRequiredProperty(properties, APPLICATION_CLASS_PROPERTY);

		_versionManager = new VersionManager(_updateDirectory);

		_applicationDownloadURLs = new LinkedList<URL>();

		for (String url : PropertyUtilities.getPropertyList(properties, APPLICATION_URL_PROPERTY_BASE)) {
			try {
				_applicationDownloadURLs.add(new URL(url));
			} catch (java.net.MalformedURLException ex) {
				_updateDisabled = true;
				break;
			}
		}

		Collection<File> certificateFiles = new LinkedList<File>();
		for (String certFile : PropertyUtilities.getPropertyList(properties, PATCH_SIGNER_CERTIFICATE_PROPERTY_BASE))
			certificateFiles.add(new File(certFile));
		// _patchVerifier = VerifierFactory.createCertificateVerifier(certificateFiles.toArray(new
		// File[0]));
	}

	public ApplicationDescription(String applicationClassName, File propertiesFile) throws IOException, CertificateException
	{
		HierarchicalProperties properties = readPropertiesFile(propertiesFile);
		initialize(applicationClassName, properties);
	}

	public ApplicationDescription(String applicationClassName, String propertiesFileName) throws IOException,
		CertificateException
	{
		this(applicationClassName, new File(propertiesFileName));
	}

	// public Verifier getPatchVerifier()
	// {
	// return _patchVerifier;
	// }

	public String getApplicationName()
	{
		return _applicationName;
	}

	public File getApplicationDirectory()
	{
		return _applicationDirectory;
	}

	public boolean updateDisabled()
	{
		return _updateDisabled;
	}

	public Collection<URL> getApplicationDownloadURLs()
	{
		return _applicationDownloadURLs;
	}

	/*
	 * public ScratchSpaceManager getScratchSpaceManager() { return _scratchManager; }
	 */

	public VersionManager getVersionManager()
	{
		return _versionManager;
	}

	public File getJarDescriptionFile()
	{
		return _jarDescriptionFile;
	}

	public String getApplicationClassName()
	{
		return _applicationClassName;
	}

	public boolean isUpdateRequest()
	{
		return _applicationClassName.equals(FAKE_UPDATER_CLASS_VALUE);
	}
}