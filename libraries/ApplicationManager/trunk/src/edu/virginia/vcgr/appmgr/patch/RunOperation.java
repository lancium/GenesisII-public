package edu.virginia.vcgr.appmgr.patch;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Properties;
import java.util.jar.JarFile;

import edu.virginia.vcgr.appmgr.launcher.ApplicationDescription;
import edu.virginia.vcgr.appmgr.patch.par.ParFile;

class RunOperation extends AbstractDataBackedPatchOperation
{
	private String _className;
	private Properties _runProperties;

	RunOperation(PatchRestrictions restrictions, JarFile patchFile, String relativePath, String className,
		Properties runProperties, ApplicationDescription applicationDescription)
	{
		super(restrictions, patchFile, relativePath, applicationDescription);

		_className = className;
		_runProperties = runProperties;
	}

	@Override
	public PatchOperationTransaction perform(PrintStream log) throws IOException
	{
		InputStream in = null;

		log.format("    Running %s.\n", _className);
		log.flush();

		try {
			in = openSource();
			ParFile parFile = new ParFile(getApplicationDescription(), in);
			parFile.run(_className, _runProperties);
			return new NullPatchOperationTransaction();
		} catch (IOException ioe) {
			throw ioe;
		} catch (Throwable cause) {
			throw new IOException("Unable to run from par file.  " + cause.getLocalizedMessage());
		} finally {
			if (in != null)
				in.close();
		}
	}

	@Override
	public String toString()
	{
		return String.format("Run [%s]%s", getRelativePath(), _className);
	}
}