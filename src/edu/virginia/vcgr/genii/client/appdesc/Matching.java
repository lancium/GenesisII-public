package edu.virginia.vcgr.genii.client.appdesc;

import org.apache.axis.types.URI;
import org.ggf.jsdl.CPUArchitecture_Type;
import org.ggf.jsdl.OperatingSystemTypeEnumeration;
import org.ggf.jsdl.OperatingSystemType_Type;
import org.ggf.jsdl.OperatingSystem_Type;
import org.ggf.jsdl.ProcessorArchitectureEnumeration;

import edu.virginia.vcgr.genii.appdesc.PlatformDescriptionType;
import edu.virginia.vcgr.genii.appdesc.SupportDocumentType;

public class Matching
{
	static private boolean matches(ProcessorArchitectureEnumeration osname,
		SupportDocumentType potential)
	{
		PlatformDescriptionType []platforms = potential.getPlatformDescription();
		if (platforms == null || platforms.length == 0)
			return true;
		
		for (PlatformDescriptionType platform : platforms)
		{
			CPUArchitecture_Type []arches = platform.getCPUArchitecture();
			if (arches == null || arches.length == 0)
				return true;
			
			for (CPUArchitecture_Type arch : arches)
			{
				ProcessorArchitectureEnumeration targetName = 
					arch.getCPUArchitectureName();
				if (targetName == osname)
					return true;
			}
		}
		
		return false;
	}
	
	static private boolean matches(CPUArchitecture_Type []desired,
		SupportDocumentType potential)
	{
		if (desired == null || desired.length == 0)
			return true;
		
		for (CPUArchitecture_Type desiredArch : desired)
		{
			ProcessorArchitectureEnumeration osname =
				desiredArch.getCPUArchitectureName();
			
			if (matches(osname, potential))
				return true;
		}
		
		return false;
	}
	
	static private boolean matches(OperatingSystemType_Type desired,
		SupportDocumentType potential)
	{
		OperatingSystemTypeEnumeration osname = desired.getOperatingSystemName();
		if (osname == null)
			return true;
		
		PlatformDescriptionType []targetPlatforms =
			potential.getPlatformDescription();
		if (targetPlatforms == null || targetPlatforms.length == 0)
			return true;
		
		for (PlatformDescriptionType target : targetPlatforms)
		{
			OperatingSystem_Type []osTypes = target.getOperatingSystem();
			if (osTypes == null || osTypes.length == 0)
				return true;
			
			for (OperatingSystem_Type osType : osTypes)
			{
				OperatingSystemType_Type osTypeType =
					osType.getOperatingSystemType();
				if (osTypeType == null)
					return true;
				
				if (osTypeType.getOperatingSystemName().equals(
					desired.getOperatingSystemName()))
					return true;
			}
		}
		
		return false;
	}
	
	static private boolean versionMatches(String desired,
		SupportDocumentType potential)
	{
		if (desired == null)
			return true;
		
		PlatformDescriptionType []targetPlatforms =
			potential.getPlatformDescription();
		if (targetPlatforms == null || targetPlatforms.length == 0)
			return true;
		
		for (PlatformDescriptionType target : targetPlatforms)
		{
			OperatingSystem_Type []osTypes = target.getOperatingSystem();
			if (osTypes == null || osTypes.length == 0)
				return true;
			
			for (OperatingSystem_Type osType : osTypes)
			{
				String version = osType.getOperatingSystemVersion();
				if (version == null)
					return true;
				
				if (version.equals(desired))
					return true;
			}
		}
		
		return false;
	}
	
	static private boolean matches(OperatingSystem_Type []desired,
		SupportDocumentType potential)
	{
		if (desired == null || desired.length == 0)
			return true;
		
		for (OperatingSystem_Type desiredOS : desired)
		{
			OperatingSystemType_Type osType =
				desiredOS.getOperatingSystemType();
			if (osType != null)
			{
				if (!matches(osType, potential))
					continue;
			}
			
			String version = desiredOS.getOperatingSystemVersion();
			if (version != null)
			{
				if (!versionMatches(version, potential))
					continue;
			}
			
			return true;
		}
		
		return false;
	}
	
	static public boolean matches(SupportDocumentType want,
		SupportDocumentType []have)
	{
		URI desiredDeploymentType = want.getDeploymentType();
		PlatformDescriptionType []desiredPlatforms = 
			want.getPlatformDescription();
		
		for (SupportDocumentType potential : have)
		{
			if (!potential.getDeploymentType().equals(desiredDeploymentType))
				continue;
			
			if (desiredPlatforms == null || desiredPlatforms.length == 0)
				return true;
			
			for (PlatformDescriptionType desiredPlatform : desiredPlatforms)
			{
				if (!matches(desiredPlatform.getCPUArchitecture(), potential))
					continue;
				if (!matches(desiredPlatform.getOperatingSystem(), potential))
					continue;
				
				return true;
			}
		}
		
		return false;
	}
}