package edu.virginia.vcgr.appmgr.patch;

import java.util.Properties;
import java.util.jar.JarFile;

import edu.virginia.vcgr.appmgr.launcher.ApplicationDescription;

class OperationFactory
{
	private JarFile _patchFile;
	private ApplicationDescription _applicationDescription;

	OperationFactory(ApplicationDescription applicationDescription, JarFile patchFile)
	{
		_patchFile = patchFile;
		_applicationDescription = applicationDescription;
	}

	PatchOperation createWriteOperation(PatchRestrictions restrictions, String desiredPermissions, String relativePath)
	{
		return new WriteOperation(restrictions, _patchFile, desiredPermissions, relativePath, _applicationDescription);
	}

	PatchOperation createDeleteOperation(PatchRestrictions restrictions, String relativePath)
	{
		return new DeleteOperation(restrictions, _patchFile, relativePath, _applicationDescription);
	}

	PatchOperation createRunOperation(PatchRestrictions restrictions, String relativePath, String className,
		Properties runProperties)
	{
		return new RunOperation(restrictions, _patchFile, relativePath, className, runProperties, _applicationDescription);
	}
}