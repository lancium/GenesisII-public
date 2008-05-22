package edu.virginia.vcgr.genii.container.deployer.zipjar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.morgan.util.io.StreamUtils;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.appdesc.zipjar.ZipJarDeploymentType;
import edu.virginia.vcgr.genii.appdesc.zipjar.ZipJarEnumeration;
import edu.virginia.vcgr.genii.client.appdesc.DeploymentException;
import edu.virginia.vcgr.genii.client.io.FileSystemUtils;
import edu.virginia.vcgr.genii.container.deployer.AbstractDeploymentProvider;
import edu.virginia.vcgr.genii.container.deployer.DeployFacet;
import edu.virginia.vcgr.genii.container.deployer.DeploySnapshot;
import edu.virginia.vcgr.genii.container.deployer.IDeployerProvider;
import edu.virginia.vcgr.genii.container.deployer.IJSDLReifier;

public class ZipJarDeploymentProvider extends AbstractDeploymentProvider
	implements IDeployerProvider
{
	private ZipJarDeploymentType _deploymentDescription;
	
	public ZipJarDeploymentProvider(
		EndpointReferenceType depDescEPR,
		ZipJarDeploymentType deploymentDescription)
	{
		super(depDescEPR);
		
		_deploymentDescription = deploymentDescription;
	}
	
	public void deployApplication(File targetDirectory)
			throws DeploymentException
	{
		ZipJarEnumeration zjType;
		ZipInputStream in = null;
		ZipEntry entry;
		
		try
		{
			zjType = _deploymentDescription.getSource().getPackageType();
			if (zjType == ZipJarEnumeration.zip)
				in = new ZipInputStream(openSource(
					_deploymentDescription.getSource()));
			else
				in = new JarInputStream(openSource(
					_deploymentDescription.getSource()));
			
			while ( (entry = in.getNextEntry()) != null)
			{
				File targetFile = new File(targetDirectory, entry.getName());
				
				if (entry.isDirectory())
				{
					targetFile.mkdirs();
				} else
				{
					File parentDirectory = targetFile.getParentFile();
					parentDirectory.mkdirs();
					
					FileOutputStream fos = null;
					try
					{
						fos = new FileOutputStream(targetFile);
						StreamUtils.copyStream(in, fos);
					}
					finally
					{
						StreamUtils.close(fos);
					}
					
					targetFile.setReadOnly();
				}
			}
			
			File cwd;
			String relativeCWD = _deploymentDescription.getRelativeCwd();
			if (relativeCWD != null)
			{
				cwd = new File(targetDirectory, 
					_deploymentDescription.getRelativeCwd());
				cwd.mkdirs();
			} else
			{
				cwd = targetDirectory;
			}
			File binary = new File(cwd, _deploymentDescription.getBinaryName());
			FileSystemUtils.makeExecutable(binary);
		}
		catch (IOException ioe)
		{
			throw new DeploymentException("Unable to deploy application.", ioe);
		}
		finally
		{
			StreamUtils.close(in);
		}
	}

	public IJSDLReifier getReifier()
	{
		return new ZipJarJSDLReifier(
			_deploymentDescription.getBinaryName(),
			_deploymentDescription.getRelativeCwd());
	}
	
	@Override
	protected DeploySnapshot figureOutSnapshot() throws DeploymentException
	{
		ArrayList<DeployFacet> facets = new ArrayList<DeployFacet>();
		facets.add(getDeploymentDescriptionFacet(_deploymentDescriptionEPR));
		
		facets.add(getDeployFacet(_deploymentDescription.getSource()));
		return new DeploySnapshot(facets);
	}
}