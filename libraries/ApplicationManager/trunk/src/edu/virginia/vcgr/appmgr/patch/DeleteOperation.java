package edu.virginia.vcgr.appmgr.patch;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.jar.JarFile;

import edu.virginia.vcgr.appmgr.launcher.ApplicationDescription;

class DeleteOperation extends AbstractPatchOperation
{
	DeleteOperation(PatchRestrictions patchRestrictions, JarFile patchFile, String relativePath,
		ApplicationDescription applicationDescription)
	{
		super(patchRestrictions, patchFile, relativePath, applicationDescription);
	}

	@Override
	public PatchOperationTransaction perform(PrintStream log) throws IOException
	{
		log.format("    Deleting \"%s\".\n", getRelativePath());
		log.flush();

		File target = findTarget();
		if (!target.exists())
			return new NullPatchOperationTransaction();

		File backup = getApplicationDescription().getScratchSpaceManager().backup(target);
		return new FileMoverPatchOperationTransaction(target, backup);
	}

	@Override
	public String toString()
	{
		return String.format("Delete %s", getRelativePath());
	}
}