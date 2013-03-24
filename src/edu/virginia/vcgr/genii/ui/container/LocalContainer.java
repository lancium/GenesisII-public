package edu.virginia.vcgr.genii.ui.container;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.install.ContainerInformation;
import edu.virginia.vcgr.genii.client.install.InstallationState;
import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.utils.flock.FileLockException;
import edu.virginia.vcgr.genii.ui.UIContext;
import edu.virginia.vcgr.genii.ui.prefs.general.GeneralUIPreferenceSet;

public class LocalContainer
{
	static final private String SERVICE_PATH_FORMAT = "%s/axis/services/%s";
	static final private String CONTAINER_SERVICE_NAME = "VCGRContainerPortType";

	private ContainerInformation _localContainer = null;

	public LocalContainer(UIContext context)
	{
		GeneralUIPreferenceSet general = context.preferences().preferenceSet(GeneralUIPreferenceSet.class);
		String name = general.localContainerName();
		if (name != null) {
			try {
				HashMap<String, ContainerInformation> containers = InstallationState.getRunningContainers();
				if (containers != null) {
					if (!containers.containsKey(name)) {
						if (containers.size() > 0) {
							List<String> names = new Vector<String>(containers.keySet());
							Collections.sort(names);
							name = names.get(0);
						}
					}

					_localContainer = containers.get(name);
				}
			} catch (FileLockException fle) {
				// Can't do anything about this right now.
			}
		}
	}

	public boolean isContainerRunning()
	{
		return _localContainer != null;
	}

	public EndpointReferenceType getEndpoint(String serviceName) throws ContainerNotRunningException
	{
		if (_localContainer == null)
			throw new ContainerNotRunningException();

		String urlString = String.format(SERVICE_PATH_FORMAT, _localContainer.getContainerURL(),
			serviceName == null ? CONTAINER_SERVICE_NAME : serviceName);

		return EPRUtils.makeEPR(urlString, false);
	}

	public EndpointReferenceType getEndpoint() throws ContainerNotRunningException
	{
		return getEndpoint(null);
	}
}