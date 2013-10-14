package edu.virginia.vcgr.appmgr.patch;

import java.io.File;
import java.util.jar.JarFile;

import edu.virginia.vcgr.appmgr.launcher.ApplicationDescription;

abstract class AbstractPatchOperation implements PatchOperation
{
	private PatchRestrictions _restrictions;
	private JarFile _patchFile;
	private ApplicationDescription _applicationDescription;
	private String _relativePath;

	protected File findTarget()
	{
		return new File(_applicationDescription.getApplicationDirectory(), getRelativePath());
	}

	protected JarFile getPatchFile()
	{
		return _patchFile;
	}

	protected ApplicationDescription getApplicationDescription()
	{
		return _applicationDescription;
	}

	protected String getRelativePath()
	{
		return _relativePath;
	}

	protected AbstractPatchOperation(PatchRestrictions restrictions, JarFile patchFile, String relativePath,
		ApplicationDescription applicationDescription)
	{
		_restrictions = restrictions;
		_patchFile = patchFile;
		_applicationDescription = applicationDescription;
		_relativePath = relativePath;
	}

	@Override
	public boolean satisfies()
	{
		if (_restrictions != null)
			return _restrictions.satisfies();

		return true;
	}
}