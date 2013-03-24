/**
 * @author kkk5z
 * 
 * This class creates the XQuery string used to query the XML database for
 * BES containers that have the properties required by the user.
 */
package edu.virginia.vcgr.genii.client.informationService;

public class QueryBuilder
{

	public QueryBuilder()
	{

	}

	public String BuildQueryFromGUI(GUIInternalStruct GUIStructure)
	{

		String resultingQuery = "";

		String OSTypeQuery = "";
		String OSVersionQuery;
		String CPUArchitectureNameQuery = "";
		String CPUCountQuery = "";
		String CPUSpeedQuery = "";
		String physicalMemoryQuery = "";
		String virtualMemoryQuery = "";
		String commonNameQuery = "";
		String totalNumberOfActivitiesQuery = "";
		String localResourceManagerQuery = "";
		String namingProfileQuery = "";
		String isAcceptingNewActivitiesQuery = "";

		/*
		 * Determining the conditions and forming the partial queries Dependion on what values the
		 * user has entered in the GUI a corresponding XQuery query is formed.
		 */
		if (!GUIStructure.getOSTypeValue().equals("")) {
			OSTypeQuery = "$i/ns1:bes-factory-attributes/ns2:FactoryResourceAttributesDocument/"
				+ "ns2:OperatingSystem/ns3:OperatingSystemType[ns3:OperatingSystemName = '" + GUIStructure.getOSTypeValue()
				+ "'] ";
			if (resultingQuery.equals(""))
				resultingQuery = resultingQuery.concat(OSTypeQuery);
			else
				resultingQuery = resultingQuery.concat(" and " + OSTypeQuery);
		}

		if (!GUIStructure.getOSVersionValue().equals("")) {
			OSVersionQuery = "$i/ns1:bes-factory-attributes/ns2:FactoryResourceAttributesDocument/"
				+ "ns2:OperatingSystem/ns3:OperatingSystemType[ns3:OperatingSystemVersion = '"
				+ GUIStructure.getOSVersionValue() + "'] ";
			if (resultingQuery.equals(""))
				resultingQuery = resultingQuery.concat(OSVersionQuery);
			else
				resultingQuery = resultingQuery.concat(" and " + OSVersionQuery);
		}

		if (!GUIStructure.getCPUArchitectureNameValue().equals("")) {
			CPUArchitectureNameQuery = "$i/ns1:bes-factory-attributes/ns2:FactoryResourceAttributesDocument/"
				+ "ns2:CPUArchitecture[ns4:CPUArchitectureName= '" + GUIStructure.getCPUArchitectureNameValue() + "'] ";
			if (resultingQuery.equals(""))
				resultingQuery = resultingQuery.concat(CPUArchitectureNameQuery);
			else
				resultingQuery = resultingQuery.concat(" and " + CPUArchitectureNameQuery);
		}

		if (!GUIStructure.getCPUCountValue().equals("")) {
			CPUCountQuery = "$i/ns1:bes-factory-attributes/ns2:FactoryResourceAttributesDocument" + "[ns2:CPUCount >= '"
				+ GUIStructure.getCPUCountValue() + "']";
			if (resultingQuery.equals(""))
				resultingQuery = resultingQuery.concat(CPUCountQuery);
			else
				resultingQuery = resultingQuery.concat(" and " + CPUCountQuery);
		}

		if (!GUIStructure.getCPUSpeedValue().equals("")) {
			CPUSpeedQuery = "$i/ns1:bes-factory-attributes/ns2:FactoryResourceAttributesDocument" + "[ns2:CPUSpeed >= '"
				+ GUIStructure.getCPUSpeedValue() + "']";
			if (resultingQuery.equals(""))
				resultingQuery = resultingQuery.concat(CPUSpeedQuery);
			else
				resultingQuery = resultingQuery.concat(" and " + CPUSpeedQuery);
		}

		if (!GUIStructure.getPhysicalMemoryValue().equals("")) {
			physicalMemoryQuery = "$i/ns1:bes-factory-attributes/ns2:FactoryResourceAttributesDocument"
				+ "[ns2:PhysicalMemory >= '" + GUIStructure.getPhysicalMemoryValue() + "']";
			if (resultingQuery.equals(""))
				resultingQuery = resultingQuery.concat(physicalMemoryQuery);
			else
				resultingQuery = resultingQuery.concat(" and " + physicalMemoryQuery);
		}

		if (!GUIStructure.getVirtualMemoryValue().equals("")) {
			virtualMemoryQuery = "$i/ns1:bes-factory-attributes/ns2:FactoryResourceAttributesDocument"
				+ "[ns2:VirtualMemory >= '" + GUIStructure.getVirtualMemoryValue() + "']";
			if (resultingQuery.equals(""))
				resultingQuery = resultingQuery.concat(virtualMemoryQuery);
			else
				resultingQuery = resultingQuery.concat(" and " + virtualMemoryQuery);
		}

		if (!GUIStructure.getCommonNameValue().equals("")) {
			commonNameQuery = "$i/ns1:bes-factory-attributes/ns2:FactoryResourceAttributesDocument" + "[ns2:CommonName='"
				+ GUIStructure.getCommonNameValue() + "']";
			if (resultingQuery.equals(""))
				resultingQuery = resultingQuery.concat(commonNameQuery);
			else
				resultingQuery = resultingQuery.concat(" and " + commonNameQuery);
		}

		if (!GUIStructure.getTotalNumberOfActivitiesValue().equals("")) {
			totalNumberOfActivitiesQuery = "$i/ns1:bes-factory-attributes/ns2:FactoryResourceAttributesDocument"
				+ "[ns2:CommonName ='" + GUIStructure.getTotalNumberOfActivitiesValue() + "']";
			if (resultingQuery.equals(""))
				resultingQuery = resultingQuery.concat(totalNumberOfActivitiesQuery);
			else
				resultingQuery = resultingQuery.concat(" and " + totalNumberOfActivitiesQuery);
		}

		if (!GUIStructure.getLocalResourcemanagerValue().equals("")) {
			localResourceManagerQuery = "$i/ns1:bes-factory-attributes/ns2:FactoryResourceAttributesDocument"
				+ "[ns2:LocalResourceManagerType='" + GUIStructure.getLocalResourcemanagerValue() + "']";
			if (resultingQuery.equals(""))
				resultingQuery = resultingQuery.concat(localResourceManagerQuery);
			else
				resultingQuery = resultingQuery.concat(" and " + localResourceManagerQuery);
		}

		if (!GUIStructure.getNamingProfilevalue().equals("")) {
			namingProfileQuery = "$i/ns1:bes-factory-attributes/ns2:FactoryResourceAttributesDocument" + "[ns2:NamingProfile='"
				+ GUIStructure.getNamingProfilevalue() + "']";
			if (resultingQuery.equals(""))
				resultingQuery = resultingQuery.concat(namingProfileQuery);
			else
				resultingQuery = resultingQuery.concat(" and " + namingProfileQuery);
		}

		// if (!GUIStructure.getIsAcceptingNewActivitiesValue().equals(""))
		isAcceptingNewActivitiesQuery = "$i/ns1:bes-factory-attributes/ns2:FactoryResourceAttributesDocument"
			+ "[ns2:IsAcceptingNewActivities='" + GUIStructure.getIsAcceptingNewActivitiesValue() + "']";
		if (resultingQuery.equals(""))
			resultingQuery = resultingQuery.concat(isAcceptingNewActivitiesQuery);
		else
			resultingQuery = resultingQuery.concat(" and " + isAcceptingNewActivitiesQuery);

		return resultingQuery;
	}

}
