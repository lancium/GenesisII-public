package edu.virginia.vcgr.externalapp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.client.byteio.ByteIOStreamFactory;
import edu.virginia.vcgr.genii.client.context.GridUserEnvironment;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rns.RNSPath;

public class ApplicationDatabase
{
	static private Log _logger = LogFactory.getLog(ApplicationDatabase.class);

	private ApplicationRegistry _defaultRegistry;
	private ApplicationRegistry _gridOverrideRegistry;
	private ApplicationRegistry _localOverrideRegistry;

	protected ApplicationRegistry load(InputStream in) throws JAXBException
	{
		JAXBContext context = JAXBContext.newInstance(ApplicationRegistry.class);

		Unmarshaller u = context.createUnmarshaller();
		return (ApplicationRegistry) u.unmarshal(in);
	}

	protected ApplicationRegistry loadDefaultRegistry()
	{
		InputStream in = null;

		try {
			in = ApplicationDatabase.class.getResourceAsStream("default-config.xml");
			if (in == null)
				throw new FileNotFoundException("Unable to find default application configuration resource.");
			return load(in);
		} catch (Throwable cause) {
			_logger.warn("Unable to load default application registry.", cause);
			return new ApplicationRegistry();
		} finally {
			StreamUtils.close(in);
		}
	}

	protected ApplicationRegistry loadGridOverrideRegistry()
	{
		InputStream in = null;

		try {
			Map<String, String> env = GridUserEnvironment.getGridUserEnvironment();
			String path = env.get("HOME");
			if (path != null) {
				RNSPath current = RNSPath.getCurrent();
				in = ByteIOStreamFactory.createInputStream(current.lookup(path));
				return load(in);
			}
		} catch (FileNotFoundException e) {
			if (_logger.isDebugEnabled())
				_logger.debug("Unable to locate grid external application override file.", e);
		} catch (RemoteException e) {
			_logger.warn("Unable to load grid external application override file.", e);
		} catch (RNSException e) {
			_logger.warn("Unable to load grid external application override file.", e);
		} catch (IOException e) {
			_logger.warn("Unable to load grid external application override file.", e);
		} catch (JAXBException e) {
			_logger.warn("Unable to load grid external application override file.", e);
		} finally {
			StreamUtils.close(in);
		}

		return new ApplicationRegistry();
	}

	protected ApplicationRegistry loadLocalOverrideRegistry()
	{
		String localHome = System.getProperty("user.home");
		if (localHome != null) {
			File path = new File(localHome);
			path = new File(path, ".grid-applications.xml");
			InputStream in = null;
			try {
				in = new FileInputStream(path);
				return load(in);
			} catch (FileNotFoundException e) {
				if (_logger.isDebugEnabled())
					_logger.debug("Unable to locate local external application override.", e);
			} catch (JAXBException e) {
				_logger.warn("Unable to load local external application override.", e);
			} finally {
				StreamUtils.close(in);

			}
		}

		return new ApplicationRegistry();
	}

	private ApplicationDatabase()
	{
		_defaultRegistry = loadDefaultRegistry();
		_gridOverrideRegistry = loadGridOverrideRegistry();
		_localOverrideRegistry = loadLocalOverrideRegistry();
	}

	private ExternalApplication getExternalApplication(String mimeType, boolean allowDefault)
	{
		ExternalApplication app;

		app = _localOverrideRegistry.getApplication(mimeType, allowDefault);
		if (app == null) {
			app = _gridOverrideRegistry.getApplication(mimeType, allowDefault);
			if (app == null)
				app = _defaultRegistry.getApplication(mimeType, allowDefault);
		}

		return app;
	}

	public ExternalApplication getExternalApplication(String mimeType)
	{
		ExternalApplication app = getExternalApplication(mimeType, false);
		if (app != null)
			return app;

		return getExternalApplication(mimeType, true);
	}

	static private ApplicationDatabase _database = null;

	synchronized static public ApplicationDatabase database()
	{
		if (_database == null)
			_database = new ApplicationDatabase();

		return _database;
	}
}