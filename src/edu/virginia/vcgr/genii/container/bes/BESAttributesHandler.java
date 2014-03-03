/*
 * Copyright 2006 University of Virginia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package edu.virginia.vcgr.genii.container.bes;

import java.lang.management.ManagementFactory;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import org.apache.axis.message.MessageElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.jsdl.CPUArchitecture_Type;
import org.ggf.jsdl.OperatingSystemTypeEnumeration;
import org.ggf.jsdl.OperatingSystemType_Type;
import org.ggf.jsdl.OperatingSystem_Type;
import org.ggf.jsdl.ProcessorArchitectureEnumeration;
import org.morgan.util.io.StreamUtils;
import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.bes.BESConstants;
import edu.virginia.vcgr.genii.client.bes.BESConstructionParameters;
import edu.virginia.vcgr.genii.client.bes.BESPolicy;
import edu.virginia.vcgr.genii.client.common.ConstructionParameters;
import edu.virginia.vcgr.genii.client.configuration.Hostname;
import edu.virginia.vcgr.genii.client.configuration.Installation;
import edu.virginia.vcgr.genii.client.jsdl.JSDLUtils;
import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.resource.IResource;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.ser.ObjectDeserializer;
import edu.virginia.vcgr.genii.client.spmd.SPMDTranslatorFactories;
import edu.virginia.vcgr.genii.client.sysinfo.SystemInfoUtils;
import edu.virginia.vcgr.genii.client.utils.units.ClockSpeed;
import edu.virginia.vcgr.genii.client.utils.units.ClockSpeedUnits;
import edu.virginia.vcgr.genii.client.utils.units.Duration;
import edu.virginia.vcgr.genii.client.utils.units.DurationUnits;
import edu.virginia.vcgr.genii.client.utils.units.Size;
import edu.virginia.vcgr.genii.client.utils.units.SizeUnits;
import edu.virginia.vcgr.genii.container.attrs.AbstractAttributeHandler;
import edu.virginia.vcgr.genii.container.attrs.AttributePackage;
import edu.virginia.vcgr.genii.container.bes.resource.IBESResource;
import edu.virginia.vcgr.genii.container.resource.ResourceManager;
import edu.virginia.vcgr.jsdl.OperatingSystemNames;
import edu.virginia.vcgr.jsdl.ProcessorArchitecture;

public class BESAttributesHandler extends AbstractAttributeHandler {
	@SuppressWarnings("unused")
	static private Log _logger = LogFactory.getLog(BESAttributesHandler.class);

	static private final String _DESCRIPTION_PROPERTY = "attribute:description";
	static private final String _DEPLOYER_PROPERTY = "attribute:deployer";
	static private BESConstants consts = new BESConstants();

	public BESAttributesHandler(AttributePackage pkg)
			throws NoSuchMethodException {
		super(pkg);
	}

	@Override
	protected void registerHandlers() throws NoSuchMethodException {
		addHandler(consts.NAME_ATTR, "getNameAttr");
		addHandler(consts.TOTAL_NUMBER_OF_ACTIVITIES_ATTR,
				"getTotalNumberOfActivitiesAttr");
		addHandler(consts.ACTIVITY_REFERENCE_ATTR, "getActivityReferencesAttr");
		addHandler(consts.DESCRIPTION_ATTR, "getDescriptionAttr",
				"setDescriptionAttr");
		addHandler(consts.OPERATING_SYSTEM_ATTR, "getOperatingSystemAttr");
		addHandler(consts.CPU_ARCHITECTURE_ATTR, "getCPUArchitectureAttr");
		addHandler(consts.CPU_COUNT_ATTR, "getCPUCountAttr");
		addHandler(consts.IS_ACCEPTING_NEW_ACTIVITIES_ATTR,
				"getIsAcceptingNewActivitiesAttr");
		addHandler(consts.SPMD_PROVIDER_ATTR, "getSPMDProvidersAttr");
		addHandler(consts.BES_POLICY_ATTR, "getBESPolicyAttr",
				"setBESPolicyAttr");
		addHandler(consts.BES_THRESHOLD_ATTR, "getBESThresholdAttr",
				"setBESThresholdAttr");
		addHandler(consts.CPU_SPEED_ATTR, "getCPUSpeedAttr");
		addHandler(consts.PHYSICAL_MEMORY_ATTR, "getPhysicalMemoryAttr");
		addHandler(consts.VIRTUAL_MEMORY_ATTR, "getVirtualMemoryAttr");
		addHandler(consts.DEPLOYER_EPR_ATTR, "getDeployersAttr",
				"setDeployersAttr");
		addHandler(consts.OGRSH_VERSIONS_ATTR, "getOGRSHVersionsAttr");
		addHandler(consts.BES_WALLCLOCK_TIMELIMIT_ATTR,
				"getWallclockTimeLimitAttr");
		addHandler(consts.FILESYSTEM_SUPPORT_ATTR,
				"getSupportedFilesystemsAttr");
	}

	static public String getName() {
		return Hostname.getLocalHostname().toString();
	}

	static public int getTotalNumberOfActivities()
			throws ResourceUnknownFaultType, ResourceException,
			RemoteException, SQLException {
		return getActivityReferences().length;
	}

	static public EndpointReferenceType[] getActivityReferences()
			throws ResourceUnknownFaultType, ResourceException,
			RemoteException, SQLException {
		return new EndpointReferenceType[0];
	}

	static public String[] getSupportedFilesystems() {
		Set<String> ret = FilesystemSupportDetection.supportedFilesystemTypes();
		return ret.toArray(new String[ret.size()]);
	}

	static public String getDescription() throws ResourceException,
			ResourceUnknownFaultType {
		IBESResource resource = null;

		resource = (IBESResource) ResourceManager.getCurrentResource()
				.dereference();
		return (String) resource.getProperty(_DESCRIPTION_PROPERTY);
	}

	static public OperatingSystem_Type getOperatingSystem()
			throws RemoteException {
		IBESResource resource = null;
		resource = (IBESResource) ResourceManager.getCurrentResource()
				.dereference();
		ConstructionParameters cParams = resource
				.constructionParameters(GeniiBESServiceImpl.class);
		BESConstructionParameters besParams = (BESConstructionParameters) cParams;

		OperatingSystemNames osType = besParams.getResourceOverrides()
				.operatingSystemName();
		String osVersion = besParams.getResourceOverrides()
				.operatingSystemVersion();

		OperatingSystem_Type ret = JSDLUtils.getLocalOperatingSystem();
		if (osType != null)
			ret.setOperatingSystemType(new OperatingSystemType_Type(
					OperatingSystemTypeEnumeration.fromString(osType.name()),
					null));
		if (osVersion != null)
			ret.setOperatingSystemVersion(osVersion);

		return ret;
	}

	static public CPUArchitecture_Type getCPUArchitecture()
			throws RemoteException {
		IBESResource resource = null;
		resource = (IBESResource) ResourceManager.getCurrentResource()
				.dereference();
		ConstructionParameters cParams = resource
				.constructionParameters(GeniiBESServiceImpl.class);
		BESConstructionParameters besParams = (BESConstructionParameters) cParams;
		ProcessorArchitecture override = besParams.getResourceOverrides()
				.cpuArchitecture();

		if (override != null)
			return new CPUArchitecture_Type(
					ProcessorArchitectureEnumeration
							.fromString(override.name()),
					null);

		return JSDLUtils.getLocalCPUArchitecture();
	}

	static public int getCPUCount() throws RemoteException {
		IBESResource resource = null;
		resource = (IBESResource) ResourceManager.getCurrentResource()
				.dereference();
		ConstructionParameters cParams = resource
				.constructionParameters(GeniiBESServiceImpl.class);
		BESConstructionParameters besParams = (BESConstructionParameters) cParams;

		Integer i = besParams.getResourceOverrides().cpuCount();
		if (i != null)
			return i.intValue();

		return ManagementFactory.getOperatingSystemMXBean()
				.getAvailableProcessors();
	}

	static public Boolean getIsAcceptingNewActivities()
			throws ResourceException, ResourceUnknownFaultType, RemoteException {
		IBESResource resource = null;

		resource = (IBESResource) ResourceManager.getCurrentResource()
				.dereference();
		Boolean isAccepting = new Boolean(resource.isAcceptingNewActivities());

		return isAccepting;
	}

	static public Collection<String> getSPMDProviders() throws RemoteException {
		return SPMDTranslatorFactories.listSPMDTranslatorFactories();
	}

	static public long getCPUSpeed() throws RemoteException {
		IBESResource resource = null;
		resource = (IBESResource) ResourceManager.getCurrentResource()
				.dereference();
		ConstructionParameters cParams = resource
				.constructionParameters(GeniiBESServiceImpl.class);
		BESConstructionParameters besParams = (BESConstructionParameters) cParams;

		ClockSpeed cs = besParams.getResourceOverrides().cpuSpeed();
		if (cs != null)
			return (long) cs.as(ClockSpeedUnits.Hertz);

		return SystemInfoUtils.getIndividualCPUSpeed();
	}

	static public Long getWallclockTimeLimit() throws ResourceUnknownFaultType,
			ResourceException {
		IBESResource resource = null;
		resource = (IBESResource) ResourceManager.getCurrentResource()
				.dereference();
		ConstructionParameters cParams = resource
				.constructionParameters(GeniiBESServiceImpl.class);
		BESConstructionParameters besParams = (BESConstructionParameters) cParams;

		Duration duration = besParams.getResourceOverrides()
				.wallclockTimeLimit();
		if (duration != null)
			return (long) duration.as(DurationUnits.Seconds);

		return null;
	}

	static public long getPhysicalMemory() throws RemoteException {
		IBESResource resource = null;
		resource = (IBESResource) ResourceManager.getCurrentResource()
				.dereference();
		ConstructionParameters cParams = resource
				.constructionParameters(GeniiBESServiceImpl.class);
		BESConstructionParameters besParams = (BESConstructionParameters) cParams;
		Size override = besParams.getResourceOverrides().physicalMemory();
		if (override != null)
			return (long) override.as(SizeUnits.Bytes);

		return SystemInfoUtils.getPhysicalMemory();
	}

	static public long getVirtualMemory() throws RemoteException {
		IBESResource resource = null;
		resource = (IBESResource) ResourceManager.getCurrentResource()
				.dereference();
		ConstructionParameters cParams = resource
				.constructionParameters(GeniiBESServiceImpl.class);
		BESConstructionParameters besParams = (BESConstructionParameters) cParams;
		Size override = besParams.getResourceOverrides().virtualMemory();
		if (override != null)
			return (long) override.as(SizeUnits.Bytes);

		return SystemInfoUtils.getVirtualMemory();
	}

	public void setDescription(String description) throws ResourceException,
			ResourceUnknownFaultType {
		IBESResource resource = null;

		try {
			resource = (IBESResource) ResourceManager.getCurrentResource()
					.dereference();
			resource.setProperty(_DESCRIPTION_PROPERTY, description);
			resource.commit();
		} finally {
			StreamUtils.close(resource);
		}
	}

	@SuppressWarnings("unchecked")
	public Collection<EndpointReferenceType> getDeployers()
			throws ResourceException, ResourceUnknownFaultType {
		ArrayList<EndpointReferenceType> eprs = new ArrayList<EndpointReferenceType>();
		IResource resource;
		resource = ResourceManager.getCurrentResource().dereference();
		Collection<byte[]> ret = (Collection<byte[]>) resource
				.getProperty(_DEPLOYER_PROPERTY);
		if (ret != null) {
			for (byte[] bytes : ret)
				eprs.add(EPRUtils.fromBytes(bytes));
		}

		return eprs;
	}

	public void setDeployers(Collection<EndpointReferenceType> deployers)
			throws ResourceException, ResourceUnknownFaultType {
		ArrayList<byte[]> toStore = new ArrayList<byte[]>();
		for (EndpointReferenceType deployer : deployers)
			toStore.add(EPRUtils.toBytes(deployer));

		IResource resource;
		resource = ResourceManager.getCurrentResource().dereference();
		resource.setProperty(_DEPLOYER_PROPERTY, toStore);
		resource.commit();
	}

	public MessageElement getIsAcceptingNewActivitiesAttr()
			throws ResourceUnknownFaultType, ResourceException, RemoteException {
		return new MessageElement(consts.IS_ACCEPTING_NEW_ACTIVITIES_ATTR,
				getIsAcceptingNewActivities());
	}

	public MessageElement getBESPolicyAttr() throws ResourceUnknownFaultType,
			ResourceException, RemoteException {
		IBESResource resource;
		resource = (IBESResource) ResourceManager.getCurrentResource()
				.dereference();
		return resource.getPolicy().toMessageElement(consts.BES_POLICY_ATTR);
	}

	public void setBESPolicyAttr(MessageElement policy)
			throws ResourceUnknownFaultType, ResourceException, RemoteException {
		BESPolicy p = BESPolicy.fromMessageElement(policy);
		IBESResource resource;
		resource = (IBESResource) ResourceManager.getCurrentResource()
				.dereference();
		resource.setPolicy(p);
		resource.commit();
	}

	public MessageElement getBESThresholdAttr()
			throws ResourceUnknownFaultType, ResourceException, RemoteException {
		IBESResource resource;
		resource = (IBESResource) ResourceManager.getCurrentResource()
				.dereference();
		Integer threshold = (Integer) resource
				.getProperty(IBESResource.THRESHOLD_DB_PROPERTY_NAME);
		return new MessageElement(consts.BES_THRESHOLD_ATTR, threshold);
	}

	public void setBESThresholdAttr(MessageElement policy)
			throws ResourceUnknownFaultType, ResourceException, RemoteException {
		Integer threshold = Integer.class.cast(ObjectDeserializer.toObject(
				policy, Integer.class));

		IBESResource resource;
		resource = (IBESResource) ResourceManager.getCurrentResource()
				.dereference();
		resource.setProperty(IBESResource.THRESHOLD_DB_PROPERTY_NAME, threshold);
		resource.commit();
	}

	public MessageElement getNameAttr() {
		return new MessageElement(consts.NAME_ATTR, getName());
	}

	public MessageElement getTotalNumberOfActivitiesAttr()
			throws ResourceException, ResourceUnknownFaultType,
			RemoteException, SQLException {
		return new MessageElement(consts.TOTAL_NUMBER_OF_ACTIVITIES_ATTR,
				getTotalNumberOfActivities());
	}

	public ArrayList<MessageElement> getSPMDProvidersAttr()
			throws RemoteException {
		Collection<String> spmdProviders = getSPMDProviders();
		ArrayList<MessageElement> ret = new ArrayList<MessageElement>(
				spmdProviders.size());
		for (String provider : spmdProviders) {
			ret.add(new MessageElement(consts.SPMD_PROVIDER_ATTR, provider));
		}

		return ret;
	}

	public ArrayList<MessageElement> getActivityReferencesAttr()
			throws ResourceException, ResourceUnknownFaultType,
			RemoteException, SQLException {
		EndpointReferenceType[] eprs = getActivityReferences();
		ArrayList<MessageElement> ret = new ArrayList<MessageElement>(
				eprs.length);
		for (int lcv = 0; lcv < eprs.length; lcv++) {
			ret.add(new MessageElement(consts.ACTIVITY_REFERENCE_ATTR,
					eprs[lcv]));
		}

		return ret;
	}

	public ArrayList<MessageElement> getOGRSHVersionsAttr() {
		ArrayList<MessageElement> ret = new ArrayList<MessageElement>();
		for (String version : Installation.getOGRSH().getInstalledVersions()
				.keySet()) {
			ret.add(new MessageElement(consts.OGRSH_VERSIONS_ATTR, version));
		}

		return ret;
	}

	public ArrayList<MessageElement> getDescriptionAttr()
			throws ResourceException, ResourceUnknownFaultType {
		ArrayList<MessageElement> result = new ArrayList<MessageElement>();
		String desc = getDescription();
		if (desc != null)
			result.add(new MessageElement(consts.DESCRIPTION_ATTR, desc));

		return result;
	}

	public void setDescriptionAttr(MessageElement element)
			throws ResourceException, ResourceUnknownFaultType {
		setDescription(element.getValue());
	}

	public MessageElement getOperatingSystemAttr() throws RemoteException {
		return new MessageElement(consts.OPERATING_SYSTEM_ATTR,
				getOperatingSystem());
	}

	public MessageElement getCPUArchitectureAttr() throws RemoteException {
		return new MessageElement(consts.CPU_ARCHITECTURE_ATTR,
				getCPUArchitecture());
	}

	public MessageElement getCPUCountAttr() throws RemoteException {
		return new MessageElement(consts.CPU_COUNT_ATTR, getCPUCount());
	}

	public MessageElement getCPUSpeedAttr() throws RemoteException {
		return new MessageElement(consts.CPU_SPEED_ATTR, getCPUSpeed());
	}

	static public MessageElement getWallclockTimeLimitAttr()
			throws ResourceUnknownFaultType, ResourceException {
		BESConstants sconsts = new BESConstants();
		Long value = getWallclockTimeLimit();
		if (value != null)
			return new MessageElement(sconsts.BES_WALLCLOCK_TIMELIMIT_ATTR,
					value);
		return null;
	}

	public MessageElement getPhysicalMemoryAttr() throws RemoteException {
		return new MessageElement(consts.PHYSICAL_MEMORY_ATTR,
				getPhysicalMemory());
	}

	public MessageElement getVirtualMemoryAttr() throws RemoteException {
		return new MessageElement(consts.VIRTUAL_MEMORY_ATTR,
				getVirtualMemory());
	}

	public ArrayList<MessageElement> getDeployersAttr()
			throws ResourceException, ResourceUnknownFaultType {
		ArrayList<MessageElement> deployers = new ArrayList<MessageElement>();
		for (EndpointReferenceType deployer : getDeployers()) {
			deployers
					.add(new MessageElement(consts.DEPLOYER_EPR_ATTR, deployer));
		}

		return deployers;
	}

	public void setDeployersAttr(Collection<MessageElement> deployers)
			throws ResourceException, ResourceUnknownFaultType {
		Collection<EndpointReferenceType> deployerEPRs = new ArrayList<EndpointReferenceType>();

		for (MessageElement deployerM : deployers) {
			EndpointReferenceType deployer = ObjectDeserializer.toObject(
					deployerM, EndpointReferenceType.class);
			deployerEPRs.add(deployer);
		}

		setDeployers(deployerEPRs);
	}

	static public ArrayList<MessageElement> getSupportedFilesystemsAttr() {
		BESConstants sconsts = new BESConstants();

		String[] supported = getSupportedFilesystems();
		ArrayList<MessageElement> ret = new ArrayList<MessageElement>(
				supported.length);
		for (String sup : supported)
			ret.add(new MessageElement(sconsts.FILESYSTEM_SUPPORT_ATTR, sup));

		return ret;
	}
}