package edu.virginia.vcgr.genii.container.deployer.bin;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.appdesc.SourceElementType;
import edu.virginia.vcgr.genii.appdesc.bin.BinDeploymentType;
import edu.virginia.vcgr.genii.appdesc.bin.NamedSourceType;
import edu.virginia.vcgr.genii.appdesc.bin.RelativeNamedSourceType;
import edu.virginia.vcgr.genii.client.appdesc.DeploymentException;
import edu.virginia.vcgr.genii.container.deployer.AbstractDeploymentProvider;
import edu.virginia.vcgr.genii.container.deployer.DeployFacet;
import edu.virginia.vcgr.genii.container.deployer.DeploySnapshot;
import edu.virginia.vcgr.genii.container.deployer.IDeployerProvider;
import edu.virginia.vcgr.genii.container.deployer.IJSDLReifier;

public class BinDeploymentProvider extends AbstractDeploymentProvider implements IDeployerProvider
{
	private BinDeploymentType _deploymentDescription;

	public BinDeploymentProvider(EndpointReferenceType depDescEPR, BinDeploymentType deploymentDescription)
	{
		super(depDescEPR);

		_deploymentDescription = deploymentDescription;
	}

	public void deployApplication(File targetDirectory) throws DeploymentException
	{
		HashMap<String, File> relativeMap = new HashMap<String, File>();

		File binDir = new File(targetDirectory, "bin");
		File libDir = new File(targetDirectory, "lib");

		binDir.mkdirs();
		libDir.mkdirs();

		if (_deploymentDescription.getBinary() != null) {
			for (NamedSourceType source : _deploymentDescription.getBinary()) {
				File target = new File(binDir, source.getName());

				downloadFile(source, target, true, true);
				relativeMap.put(source.getName(), target);
			}
		}

		if (_deploymentDescription.getSharedLibrary() != null) {
			for (NamedSourceType source : _deploymentDescription.getSharedLibrary()) {
				File target = new File(libDir, source.getName());

				downloadFile(source, target, true, false);
				relativeMap.put(source.getName(), target);
			}
		}

		if (_deploymentDescription.getStaticFile() != null) {
			for (RelativeNamedSourceType source : _deploymentDescription.getStaticFile()) {
				File target;

				Boolean relativeToComponent = source.getRelativeToComponent();
				if (relativeToComponent != null && relativeToComponent.booleanValue()) {
					String componentName = source.getComponentName();
					if (componentName == null)
						throw new DeploymentException("Can't place a component relative to another "
							+ "without giving the other component's name.");

					File other = relativeMap.get(componentName);
					if (other == null)
						throw new DeploymentException("Can't find other component \"" + componentName + "\".");

					target = new File(other.getParentFile(), source.getName());
				} else {
					// Relative to cwd
					target = new File(targetDirectory, source.getName());
				}

				target.getParentFile().mkdirs();
				downloadFile(source, target, true, false);
				relativeMap.put(source.getName(), target);
			}
		}
	}

	public IJSDLReifier getReifier()
	{
		return new BinJSDLReifier(_deploymentDescription.getBinaryName(), _deploymentDescription.getRelativeCwd());
	}

	protected DeploySnapshot figureOutSnapshot() throws DeploymentException
	{
		EndpointReferenceType depDescEPR = _deploymentDescriptionEPR;
		BinDeploymentType description = _deploymentDescription;

		ArrayList<DeployFacet> facets = new ArrayList<DeployFacet>();

		facets.add(getDeploymentDescriptionFacet(depDescEPR));

		if (description.getBinary() != null) {
			for (SourceElementType source : description.getBinary()) {
				facets.add(getDeployFacet(source));
			}
		}

		if (description.getSharedLibrary() != null) {
			for (SourceElementType source : description.getSharedLibrary()) {
				facets.add(getDeployFacet(source));
			}
		}

		if (description.getStaticFile() != null) {
			for (SourceElementType source : description.getStaticFile()) {
				facets.add(getDeployFacet(source));
			}
		}

		return new DeploySnapshot(facets);
	}
}