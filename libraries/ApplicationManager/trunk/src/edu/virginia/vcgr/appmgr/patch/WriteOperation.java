package edu.virginia.vcgr.appmgr.patch;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.jar.JarFile;

import edu.virginia.vcgr.appmgr.io.IOUtils;
import edu.virginia.vcgr.appmgr.launcher.ApplicationDescription;

class WriteOperation extends AbstractDataBackedPatchOperation
{
	private String _desiredPermissions;

	WriteOperation(PatchRestrictions restrictions, JarFile patchFile, String desiredPermissions, String relativePath,
		ApplicationDescription applicationDescription)
	{
		super(restrictions, patchFile, relativePath, applicationDescription);

		_desiredPermissions = desiredPermissions;
	}

	@Override
	public PatchOperationTransaction perform(PrintStream log) throws IOException
	{
		log.format("    Writing \"%s\".\n", getRelativePath());
		log.flush();

		File target = findTarget();
		File backup = null;
		if (target.exists())
			backup = getApplicationDescription().getScratchSpaceManager().backup(target);
		else {
			File parent = target.getParentFile();
			if (!parent.exists()) {
				if (!parent.mkdirs())
					throw new IOException(String.format("Unable to make parent directory \"%s\".", parent));
			}

			if (!parent.isDirectory())
				throw new IOException(String.format("Parent path \"%s\" is not a directory.", parent));
		}

		FileMoverPatchOperationTransaction transaction = new FileMoverPatchOperationTransaction(target, backup);

		FileOutputStream fos = null;
		InputStream in = null;

		try {
			fos = new FileOutputStream(target);
			in = openSource();
			IOUtils.copy(in, fos);
			fos.flush();
			fos.close();
			fos = null;

			if (_desiredPermissions != null) {
				Permissions p = new Permissions(target);
				p.override(_desiredPermissions);
				p.enforce();
			}

			return transaction;
		} catch (IOException ioe) {
			transaction.rollback();
			throw ioe;
		} catch (RuntimeException re) {
			transaction.rollback();
			throw re;
		} finally {
			IOUtils.close(in);
			IOUtils.close(fos);
		}
	}

	@Override
	public String toString()
	{
		return String.format("Write %s%s", (_desiredPermissions != null) ? String.format("[%s]", _desiredPermissions) : "",
			getRelativePath());
	}
}