package edu.virginia.vcgr.genii.client.configuration;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.configuration.ConfigurationException;

@XmlRootElement(name = "deployment-conf")
class DeploymentConf
{
	static final private String FILENAME = "deployment-conf.xml";
	static private Log _logger = LogFactory.getLog(DeploymentConf.class);

	static private JAXBContext CONTEXT;

	static {
		try {
			CONTEXT = JAXBContext.newInstance(DeploymentConf.class);
		} catch (JAXBException e) {
			throw new ConfigurationException("Unable to load JAXBContext for DeploymentConf.");
		}
	}

	@XmlAttribute(name = "based-on", required = true)
	private String _basedOn = null;

	final public String basedOn()
	{
		return _basedOn;
	}

	static public File basedOn(File parentDir)
	{
		try {
			File file = new File(parentDir, FILENAME);
			if (file.exists()) {
				Unmarshaller u = CONTEXT.createUnmarshaller();
				DeploymentConf conf = (DeploymentConf) u.unmarshal(file);
				if (conf != null) {
					String basedOn = conf.basedOn();
					if (basedOn != null) {
						File basedOnFile = new File(parentDir.getParentFile(), basedOn);
						if (basedOnFile.exists() && basedOnFile.isDirectory())
							return basedOnFile;
					}
				}
			}
		} catch (Throwable cause) {
			_logger.warn(String.format("Error trying to read dependent deployment from \"%s\".", parentDir), cause);
		}

		return null;
	}
}
