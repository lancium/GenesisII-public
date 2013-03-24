package edu.virginia.vcgr.genii.client.nativeq;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

public class ExecutableApplicationConfiguration implements Serializable, NativeQConstants
{
	static final long serialVersionUID = 0L;

	@XmlAttribute(name = "path", required = false)
	private String _pathOverride = null;

	@XmlElement(namespace = NS, name = "additional-argument", required = false, nillable = false)
	private Collection<String> _additionalArguments;

	private File findBinary(File binDirectoryOverride, String binaryName) throws FileNotFoundException
	{
		File ret = null;

		if (_pathOverride != null) {
			ret = new File(_pathOverride);
			if (!ret.exists() || !ret.canExecute())
				throw new FileNotFoundException(String.format("Can't find binary \"%s\".", _pathOverride));
		} else if (binDirectoryOverride != null) {
			ret = new File(binDirectoryOverride, binaryName);
			if (!ret.exists() || !ret.canExecute())
				throw new FileNotFoundException(String.format("Can't find binary \"%s\".", ret));
		} else {
			String pathEnvVar = System.getenv("PATH");
			if (pathEnvVar != null) {
				for (String path : pathEnvVar.split(Pattern.quote(File.pathSeparator))) {
					if (path == null)
						continue;

					path = path.trim();
					if (path.length() > 0) {
						ret = new File(path, binaryName);
						if (ret.exists() && ret.canExecute())
							return ret;
					}
				}
			}

			throw new FileNotFoundException(String.format("Can't find binary \"%s\".", binaryName));
		}

		return ret;
	}

	public ExecutableApplicationConfiguration(String pathOverride, Collection<String> additionalArguments)
	{
		_pathOverride = pathOverride;
		_additionalArguments = additionalArguments;
	}

	public ExecutableApplicationConfiguration()
	{
		this(null, new LinkedList<String>());
	}

	final public List<String> startCommandLine(File binDirectoryOverride, String defaultBinaryName)
		throws FileNotFoundException
	{
		List<String> ret = new LinkedList<String>();

		ret.add(findBinary(binDirectoryOverride, defaultBinaryName).getAbsolutePath());

		ret.addAll(_additionalArguments);

		return ret;
	}
}