package edu.virginia.vcgr.genii.container.deployer.zipjar;

import java.io.File;
import java.io.Serializable;

import edu.virginia.vcgr.genii.container.deployer.AbstractReifier;
import edu.virginia.vcgr.genii.container.deployer.IJSDLReifier;

public class ZipJarJSDLReifier
	extends AbstractReifier implements IJSDLReifier, Serializable
{
	static final long serialVersionUID = 0L;
	
	private String _binaryName;
	private String _relativeCWD;
	
	public ZipJarJSDLReifier(String binaryName, String relativeCWD)
	{
		_binaryName = binaryName;
		_relativeCWD = relativeCWD;
	}

	@Override
	public String[] getAdditionalLibraryPaths(File deployDirectory)
	{
		return new String[0];
	}

	@Override
	public String[] getAdditionalPaths(File deployDirectory)
	{
		return new String[] { getCWD(deployDirectory) };
	}

	@Override
	public String getBinaryName(File deployDirectory)
	{
		return _binaryName;
	}

	@Override
	public String getCWD(File deployDirectory)
	{
		if (_relativeCWD == null)
			return deployDirectory.getAbsolutePath();
		else
			return new File(deployDirectory, _relativeCWD).getAbsolutePath();
	}
}