package edu.virginia.vcgr.appmgr.patch;

import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import edu.virginia.vcgr.appmgr.launcher.ApplicationDescription;
import edu.virginia.vcgr.appmgr.os.OperatingSystemType;

abstract class AbstractDataBackedPatchOperation extends AbstractPatchOperation
{
	static final private String COMMON_PATH_HEADER = "common";

	protected AbstractDataBackedPatchOperation(PatchRestrictions restrictions, JarFile jarFile, String relativePath,
		ApplicationDescription applicationDescription)
	{
		super(restrictions, jarFile, relativePath, applicationDescription);
	}

	protected InputStream openSource() throws IOException
	{
		JarEntry entry;

		entry = getPatchFile().getJarEntry(String.format("%s/%s", OperatingSystemType.getCurrent(), getRelativePath()));
		if (entry == null) {
			entry = getPatchFile().getJarEntry(String.format("%s/%s", COMMON_PATH_HEADER, getRelativePath()));
			if (entry == null)
				throw new IOException(String.format("Unable to find source file %s in patch.", getRelativePath()));
		}

		InputStream in = getPatchFile().getInputStream(entry);
		if (in == null)
			throw new IOException(String.format("Unable to open patch entry \"%s\".", getRelativePath()));

		return in;
	}
}