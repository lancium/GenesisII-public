package edu.virginia.vcgr.genii.container.deployer.bin;

import java.io.File;
import java.io.Serializable;

import edu.virginia.vcgr.genii.container.deployer.AbstractReifier;
import edu.virginia.vcgr.genii.container.deployer.IJSDLReifier;

public class BinJSDLReifier 
	extends AbstractReifier implements IJSDLReifier, Serializable
{
	static final long serialVersionUID = 0L;
	
	private String _binaryName;
	private String _relativeCWD;
	
	public BinJSDLReifier(String binaryName, String relativeCWD)
	{
		_binaryName = binaryName;
		_relativeCWD = relativeCWD;
	}

	@Override
	public String[] getAdditionalLibraryPaths(File deployDirectory)
	{
		return new String [] {
			new File(deployDirectory, "lib").getAbsolutePath()
		};
	}

	@Override
	public String[] getAdditionalPaths(File deployDirectory)
	{
		return new String[] {
			new File(deployDirectory, "bin").getAbsolutePath()
		};
	}

	@Override
	public String getBinaryName(File deployDirectory)
	{
		return _binaryName;
	}

	@Override
	public String getCWD(File deployDirectory)
	{
		File dir = deployDirectory;
		if (_relativeCWD != null)
			dir = new File(dir, _relativeCWD);
		
		return dir.getAbsolutePath();
	}
}