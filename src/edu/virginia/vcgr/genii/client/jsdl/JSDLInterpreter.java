package edu.virginia.vcgr.genii.client.jsdl;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.LinkedList;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.apache.axis.types.NormalizedString;
import org.ggf.jsdl.Application_Type;
import org.ggf.jsdl.CPUArchitecture_Type;
import org.ggf.jsdl.CreationFlagEnumeration;
import org.ggf.jsdl.DataStaging_Type;
import org.ggf.jsdl.FileSystemTypeEnumeration;
import org.ggf.jsdl.FileSystem_Type;
import org.ggf.jsdl.JobDefinition_Type;
import org.ggf.jsdl.JobDescription_Type;
import org.ggf.jsdl.JobIdentification_Type;
import org.ggf.jsdl.OperatingSystemTypeEnumeration;
import org.ggf.jsdl.OperatingSystemType_Type;
import org.ggf.jsdl.OperatingSystem_Type;
import org.ggf.jsdl.ProcessorArchitectureEnumeration;
import org.ggf.jsdl.Resources_Type;
import org.ggf.jsdl.SourceTarget_Type;
import org.ggf.jsdl.hpcp.HPCProfileApplication_Type;
import org.ggf.jsdl.posix.Argument_Type;
import org.ggf.jsdl.posix.DirectoryName_Type;
import org.ggf.jsdl.posix.Environment_Type;
import org.ggf.jsdl.posix.FileName_Type;
import org.ggf.jsdl.posix.GroupName_Type;
import org.ggf.jsdl.posix.Limits_Type;
import org.ggf.jsdl.posix.POSIXApplication_Type;
import org.ggf.jsdl.posix.UserName_Type;

import edu.virginia.vcgr.genii.client.jsdl.hpc.HPCConstants;
import edu.virginia.vcgr.genii.client.jsdl.personality.ApplicationFacet;
import edu.virginia.vcgr.genii.client.jsdl.personality.CPUArchitectureFacet;
import edu.virginia.vcgr.genii.client.jsdl.personality.CandidateHostsFacet;
import edu.virginia.vcgr.genii.client.jsdl.personality.DataStagingFacet;
import edu.virginia.vcgr.genii.client.jsdl.personality.FileSystemFacet;
import edu.virginia.vcgr.genii.client.jsdl.personality.HPCApplicationFacet;
import edu.virginia.vcgr.genii.client.jsdl.personality.JobDefinitionFacet;
import edu.virginia.vcgr.genii.client.jsdl.personality.JobDescriptionFacet;
import edu.virginia.vcgr.genii.client.jsdl.personality.JobIdentificationFacet;
import edu.virginia.vcgr.genii.client.jsdl.personality.OperatingSystemFacet;
import edu.virginia.vcgr.genii.client.jsdl.personality.OperatingSystemTypeFacet;
import edu.virginia.vcgr.genii.client.jsdl.personality.POSIXApplicationFacet;
import edu.virginia.vcgr.genii.client.jsdl.personality.PersonalityFacet;
import edu.virginia.vcgr.genii.client.jsdl.personality.PersonalityProvider;
import edu.virginia.vcgr.genii.client.jsdl.personality.ResourcesFacet;
import edu.virginia.vcgr.genii.client.jsdl.personality.SourceURIFacet;
import edu.virginia.vcgr.genii.client.jsdl.personality.TargetURIFacet;
import edu.virginia.vcgr.genii.client.jsdl.posix.JSDLPosixConstants;
import edu.virginia.vcgr.genii.client.jsdl.range.RangeExpression;
import edu.virginia.vcgr.genii.client.jsdl.range.RangeFactory;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.ser.ObjectDeserializer;

public class JSDLInterpreter
{
	static public Object interpretJSDL(
		PersonalityProvider provider, JobDefinition_Type jsdl) 
			throws JSDLException
	{
		Object understanding = provider.createNewUnderstanding();
		
		understand(provider, understanding, jsdl);
		return understanding;
	}
	
	static private void understandAny(
		PersonalityFacet facet, Object understanding, MessageElement []any)
			throws JSDLException
	{
		if (any != null)
		{
			for (MessageElement a : any)
			{
				if (a != null)
					facet.consumeAny(understanding, a);
			}
		}
	}

	static private void understand(PersonalityProvider provider, 
		Object parentUnderstanding, JobDefinition_Type def)
			throws JSDLException
	{
		if (def == null)
			return;
		
		JobDefinitionFacet facet = provider.getJobDefinitionFacet(
			parentUnderstanding);
		Object understanding = 
			facet.createFacetUnderstanding(parentUnderstanding);
		
		NormalizedString id = def.getId();
		if (id != null)
			facet.consumeID(understanding, id.toString());
		
		understandAny(facet, understanding, def.get_any());
		
		understand(provider, understanding, def.getJobDescription());
		facet.completeFacet(parentUnderstanding, understanding);
	}
	
	static private void understand(PersonalityProvider provider, 
		Object parentUnderstanding, JobDescription_Type desc) 
			throws JSDLException
	{
		if (desc == null)
			return;
		
		JobDescriptionFacet facet = provider.getJobDescriptionFacet(
			parentUnderstanding);
		Object understanding = facet.createFacetUnderstanding(
			parentUnderstanding);
		
		understandAny(facet, understanding, desc.get_any());
		
		understand(provider, understanding, desc.getJobIdentification());
		understand(provider, understanding, desc.getApplication());
		understand(provider, understanding, desc.getResources());
		understand(provider, understanding, desc.getDataStaging());
		
		facet.completeFacet(parentUnderstanding, understanding);
	}
	
	static private void understand(PersonalityProvider provider, 
		Object parentUnderstanding, JobIdentification_Type ident)
			throws JSDLException
	{
		if (ident == null)
			return;
		
		JobIdentificationFacet facet = provider.getJobIdentificationFacet(
			parentUnderstanding);
		Object understanding = facet.createFacetUnderstanding(
			parentUnderstanding);
		
		understandAny(facet, understanding, ident.get_any());
		
		String str = ident.getJobName();
		if (str != null)
			facet.consumeJobName(understanding, str);
		
		str = ident.getDescription();
		if (str != null)
			facet.consumeDescription(understanding, str);
		
		String []annotArray = ident.getJobAnnotation();
		if (annotArray != null)
		{
			for (String annot : annotArray)
			{
				if (annot != null)
					facet.consumeJobAnnotation(understanding, annot);
			}
		}
		
		String []projectArray = ident.getJobProject();
		if (projectArray != null)
		{
			for (String project : projectArray)
			{
				if (project != null)
					facet.consumeJobProject(understanding, project);
			}
		}
		
		facet.completeFacet(parentUnderstanding, understanding);
	}

	static private void understand(PersonalityProvider provider, 
		Object parentUnderstanding, Application_Type app)
			throws JSDLException
	{
		Collection<MessageElement> any = new LinkedList<MessageElement>();
		
		if (app == null)
			return;
		
		ApplicationFacet facet = provider.getApplicationFacet(
			parentUnderstanding);
		Object understanding = facet.createFacetUnderstanding(
			parentUnderstanding);
		
		String str = app.getApplicationName();
		if (str != null)
			facet.consumeApplicationName(understanding, str);
		
		str = app.getApplicationVersion();
		if (str != null)
			facet.consumeApplicationVersion(understanding, str);
		
		str = app.getDescription();
		if (str != null)
			facet.consumeDescription(understanding, str);
		
		MessageElement []anyArray = app.get_any();
		if (anyArray != null)
		{
			for (MessageElement a : anyArray)
			{
				QName elementName = a.getQName();
				if (elementName.equals(
					JSDLPosixConstants.JSDL_POSIX_APPLICATION_QNAME))
				{
					try
					{
						POSIXApplication_Type pat = ObjectDeserializer.toObject(
							a, POSIXApplication_Type.class);
						understand(provider, understanding, pat);
					}
					catch (ResourceException re)
					{
						throw new InvalidJSDLException(
							"Unable to parse JSDL Application element " +
								"into POSIXApplication element.", re);
					}
				} else if (elementName.equals(
					HPCConstants.HPC_APPLICATION_QNAME))
				{
					try
					{
						HPCProfileApplication_Type hat = ObjectDeserializer.toObject(
							a, HPCProfileApplication_Type.class);
						understand(provider, understanding, hat);
					}
					catch (ResourceException re)
					{
						throw new InvalidJSDLException(
							"Unable to parse JSDL Application element " +
								"into HPCProfileApplication element.", re);
					}
				} else
				{
					any.add(a);
				}
			}
		}
		understandAny(facet, understanding, any.toArray(new MessageElement[0]));
		
		facet.completeFacet(parentUnderstanding, understanding);
	}

	static private void understand(PersonalityProvider provider, 
		Object parentUnderstanding, Resources_Type resources)
			throws JSDLException
	{
		if (resources == null)
			return;
		
		ResourcesFacet facet = provider.getResourcesFacet(
			parentUnderstanding);
		Object understanding = facet.createFacetUnderstanding(
			parentUnderstanding);
				
		understandAny(facet, understanding, resources.get_any());
		
		understandCandidateHosts(provider, understanding, 
			resources.getCandidateHosts());
		understand(provider, understanding, resources.getFileSystem());
		
		Boolean b = resources.getExclusiveExecution();
		if (b != null)
		facet.consumeExclusiveExecution(understanding, b.booleanValue());
		
		understand(provider, understanding, resources.getOperatingSystem());
		understand(provider, understanding, resources.getCPUArchitecture());
		
		RangeExpression range = RangeFactory.parse(
			resources.getIndividualCPUSpeed());
		if (range != null)
			facet.consumeIndividualCPUSpeed(understanding, range);
		
		range = RangeFactory.parse(resources.getIndividualCPUTime());
		if (range != null)
			facet.consumeIndividualCPUTime(understanding, range);
		
		range = RangeFactory.parse(resources.getIndividualCPUCount());
		if (range != null)
			facet.consumeIndividualCPUCount(understanding, range);
		
		range = RangeFactory.parse(resources.getIndividualNetworkBandwidth());
		if (range != null)
			facet.consumeIndividualNetworkBandwidth(understanding, range);
		
		range = RangeFactory.parse(resources.getIndividualPhysicalMemory());
		if (range != null)
			facet.consumeIndividualPhysicalMemory(understanding, range);
		
		range = RangeFactory.parse(resources.getIndividualVirtualMemory());
		if (range != null)
			facet.consumeIndividualVirtualMemory(understanding, range);
		
		range = RangeFactory.parse(resources.getIndividualDiskSpace());
		if (range != null)
			facet.consumeIndividualDiskSpace(understanding, range);
		
		range = RangeFactory.parse(resources.getTotalCPUTime());
		if (range != null)
			facet.consumeTotalCPUTime(understanding, range);
		
		range = RangeFactory.parse(resources.getTotalCPUCount());
		if (range != null)
			facet.consumeTotalCPUCount(understanding, range);
		
		range = RangeFactory.parse(resources.getTotalPhysicalMemory());
		if (range != null)
			facet.consumeTotalPhysicalMemory(understanding, range);

		range = RangeFactory.parse(resources.getTotalVirtualMemory());
		if (range != null)
			facet.consumeTotalVirtualMemory(understanding, range);

		range = RangeFactory.parse(resources.getTotalDiskSpace());
		if (range != null)
			facet.consumeTotalDiskSpace(understanding, range);
		
		range = RangeFactory.parse(resources.getTotalResourceCount());
		if (range != null)
			facet.consumeTotalResourceCount(understanding, range);
		
		facet.completeFacet(parentUnderstanding, understanding);
	}

	static private void understand(PersonalityProvider provider, 
		Object parentUnderstanding, DataStaging_Type []staging)
			throws JSDLException
	{
		if (staging == null)
			return;
		
		for (DataStaging_Type stage : staging)
		{
			understand(provider, parentUnderstanding, stage);
		}
	}
	
	static private void understand(PersonalityProvider provider,
		Object parentUnderstanding, POSIXApplication_Type pat)
			throws JSDLException
	{
		if (pat == null)
			return;
		
		POSIXApplicationFacet facet = provider.getPOSIXApplicationFacet(
			parentUnderstanding);
		Object understanding = facet.createFacetUnderstanding(
			parentUnderstanding);
		
		NormalizedString nStr;
		
		FileName_Type file = pat.getExecutable();
		if (file != null)
		{
			nStr = file.getFilesystemName();
			facet.consumeExecutable(understanding, 
				nStr != null ? nStr.toString() : null, file.get_value());
		}
		
		Argument_Type []args = pat.getArgument();
		if (args != null)
		{
			for (Argument_Type arg : args)
			{
				nStr = arg.getFilesystemName();
				NormalizedString nStr2 = arg.get_value();
				facet.consumeArgument(understanding, 
					nStr != null ? nStr.toString() : null, 
					nStr2 != null ? nStr2.toString() : null);
			}
		}
		
		file = pat.getInput();
		if (file != null)
		{
			nStr = file.getFilesystemName();
			facet.consumeInput(understanding, nStr != null ? nStr.toString() : null, 
				file.get_value());
		}
		
		file = pat.getOutput();
		if (file != null)
		{
			nStr = file.getFilesystemName();
			facet.consumeOutput(understanding, nStr != null ? nStr.toString() : null, 
				file.get_value());
		}
		file = pat.getError();
		if (file != null)
		{
			nStr = file.getFilesystemName();
			facet.consumeError(understanding, nStr != null ? nStr.toString() : null, 
				file.get_value());
		}
		
		DirectoryName_Type dir = pat.getWorkingDirectory();
		if (dir != null)
		{
			nStr = dir.getFilesystemName();
			facet.consumeWorkingDirectory(understanding, 
				nStr != null ? nStr.toString() : null, dir.get_value());
		}
		
		Environment_Type []env = pat.getEnvironment();
		if (env != null)
		{
			for (Environment_Type e : env)
			{
				nStr = e.getName();
				NormalizedString nStr2 = e.getFilesystemName();
				facet.consumeEnvironment(understanding, nStr != null ? nStr.toString() : null, 
					nStr2 != null ? nStr2.toString() : null, e.get_value());
			}
		}
		
		Limits_Type limit = pat.getWallTimeLimit();
		if (limit != null)
			facet.consumeWallTimeLimit(understanding, 
				limit.get_value().longValue());
		
		limit = pat.getFileSizeLimit();
		if (limit != null)
			facet.consumeFileSizeLimit(understanding, 
				limit.get_value().longValue());
		
		limit = pat.getCoreDumpLimit();
		if (limit != null)
			facet.consumeCoreDumpLimit(understanding, 
				limit.get_value().longValue());
		
		limit = pat.getDataSegmentLimit();
		if (limit != null)
			facet.consumeDataSegmentLimit(understanding, 
				limit.get_value().longValue());
		
		limit = pat.getLockedMemoryLimit();
		if (limit != null)
			facet.consumeLockedMemoryLimit(understanding, 
				limit.get_value().longValue());
		
		limit = pat.getMemoryLimit();
		if (limit != null)
			facet.consumeMemoryLimit(understanding, 
				limit.get_value().longValue());
		
		limit = pat.getOpenDescriptorsLimit();
		if (limit != null)
			facet.consumeOpenDescriptorsLimit(understanding, 
				limit.get_value().longValue());
		
		limit = pat.getPipeSizeLimit();
		if (limit != null)
			facet.consumePipeSizeLimit(understanding, 
				limit.get_value().longValue());
		
		limit = pat.getStackSizeLimit();
		if (limit != null)
			facet.consumeStackSizeLimit(understanding, 
				limit.get_value().longValue());
		
		limit = pat.getCPUTimeLimit();
		if (limit != null)
			facet.consumeCPUTimeLimit(understanding, 
				limit.get_value().longValue());
		
		limit = pat.getProcessCountLimit();
		if (limit != null)
			facet.consumeProcessCountLimit(understanding, 
				limit.get_value().longValue());
		
		limit = pat.getVirtualMemoryLimit();
		if (limit != null)
			facet.consumeVirtualMemoryLimit(understanding, 
				limit.get_value().longValue());
		
		limit = pat.getThreadCountLimit();
		if (limit != null)
			facet.consumeThreadCountLimit(understanding, 
				limit.get_value().longValue());
		
		UserName_Type user = pat.getUserName();
		if (user != null)
			facet.consumeUserName(understanding, user.get_value());
		
		GroupName_Type group = pat.getGroupName();
		if (group != null)
			facet.consumeGroupName(understanding, group.get_value());
		
		facet.completeFacet(parentUnderstanding, understanding);
	}
	
	static private void understand(PersonalityProvider provider, 
		Object parentUnderstanding, 
		HPCProfileApplication_Type hat) throws JSDLException
	{
		if (hat == null)
			return;
		
		HPCApplicationFacet facet = provider.getHPCApplicationFacet(
			parentUnderstanding);	
		Object understanding = facet.createFacetUnderstanding(
			parentUnderstanding);
		
		NormalizedString ns = hat.getName();
		if (ns != null)
			facet.consumeName(understanding, ns.toString());
		
		org.ggf.jsdl.hpcp.FileName_Type ft = hat.getExecutable();
		if (ft != null)
			facet.consumeExecutable(understanding, ft.get_value());
		
		org.ggf.jsdl.hpcp.Argument_Type []args = hat.getArgument();
		if (args != null)
		{
			for (org.ggf.jsdl.hpcp.Argument_Type arg : args)
			{
				facet.consumeArgument(understanding, arg.get_value());
			}
		}
		
		ft = hat.getInput();
		if (ft != null)
			facet.consumeInput(understanding, ft.get_value());
		ft = hat.getOutput();
		if (ft != null)
			facet.consumeOutput(understanding, ft.get_value());
		ft = hat.getError();
		if (ft != null)
			facet.consumeError(understanding, ft.get_value());
		
		org.ggf.jsdl.hpcp.DirectoryName_Type dt = hat.getWorkingDirectory();
		if (dt != null)
			facet.consumeWorkingDirectory(understanding, dt.get_value());
		
		org.ggf.jsdl.hpcp.Environment_Type []env = hat.getEnvironment();
		if (env != null)
		{
			for (org.ggf.jsdl.hpcp.Environment_Type e : env)
			{
				facet.consumeEnvironment(understanding, 
					e.getName().toString(), e.get_value());
			}
		}
		
		org.ggf.jsdl.hpcp.UserName_Type uName = hat.getUserName();
		if (uName != null)
			facet.consumeUserName(understanding, uName.get_value());
		
		facet.completeFacet(parentUnderstanding, understanding);
	}
	
	static private void understandCandidateHosts(PersonalityProvider provider, 
		Object parentUnderstanding, String []candidateHosts) 
			throws JSDLException
	{
		if (candidateHosts == null)
			return;
		
		CandidateHostsFacet facet = provider.getCandidateHostsFacet(
			parentUnderstanding);
		Object understanding = facet.createFacetUnderstanding(
			parentUnderstanding);
		
		for (String host : candidateHosts)
		{
			if (host != null)
				facet.consumeHostName(understanding, host);
		}
		
		facet.completeFacet(parentUnderstanding, understanding);
	}

	static private void understand(PersonalityProvider provider, 
		Object understanding, FileSystem_Type []fs) throws JSDLException
	{
		if (fs == null)
			return;
		
		for (FileSystem_Type fst : fs)
		{
			understand(provider, understanding, fst);
		}
	}
	
	static private void understand(PersonalityProvider provider,
		Object parentUnderstanding, FileSystem_Type fst) throws JSDLException
	{
		if (fst == null)
			return;
		
		FileSystemFacet facet = provider.getFileSystemFacet(
			parentUnderstanding);
		Object understanding = facet.createFacetUnderstanding(
			parentUnderstanding);
		
		understandAny(facet, understanding, fst.get_any());
		
		NormalizedString nString = fst.getName();
		if (nString != null)
			facet.consumeName(understanding, nString.toString());
		
		String str = fst.getDescription();
		if (str != null)
			facet.consumeDescription(understanding, str);
		
		str = fst.getMountPoint();
		if (str != null)
			facet.consumeMountPoint(understanding, str);
		
		/* MountSource doesn't actually exist.
			facet.consumeMountSource(understanding, fst.getMountSource());
		*/
		
		RangeExpression range = RangeFactory.parse(fst.getDiskSpace());
		if (range != null)
			facet.consumeDiskSpace(understanding, range);
		
		FileSystemTypeEnumeration fste = fst.getFileSystemType();
		if (fste != null)
			facet.consumeFileSystemType(understanding, fste);
		
		facet.completeFacet(parentUnderstanding, understanding);
	}
	
	static private void understand(PersonalityProvider provider, 
		Object parentUnderstanding, OperatingSystem_Type osType)
			throws JSDLException
	{
		if (osType == null)
			return;
		
		OperatingSystemFacet facet = provider.getOperatingSystemFacet(
			parentUnderstanding);
		Object understanding = facet.createFacetUnderstanding(
			parentUnderstanding);
		
		understandAny(facet, understanding, osType.get_any());
		
		String str = osType.getDescription();
		if (str != null)
			facet.consumeDescription(understanding, str);
		
		str = osType.getOperatingSystemVersion();
		if (str != null)
			facet.consumeOperatingSystemVersion(understanding, str);
		
		understand(provider, understanding, osType.getOperatingSystemType());
		
		facet.completeFacet(parentUnderstanding, understanding);
	}

	static private void understand(PersonalityProvider provider, 
		Object parentUnderstanding, OperatingSystemType_Type osType)
			throws JSDLException
	{
		if (osType == null)
			return;
		
		OperatingSystemTypeFacet facet = provider.getOperatingSystemTypeFacet(
			parentUnderstanding);
		Object understanding = facet.createFacetUnderstanding(
			parentUnderstanding);
		
		understandAny(facet, understanding, osType.get_any());
		
		OperatingSystemTypeEnumeration e = osType.getOperatingSystemName();
		if (e != null)
			facet.consumeOperatingSystemName(understanding, e);
		
		facet.completeFacet(parentUnderstanding, understanding);
	}
	
	static private void understand(PersonalityProvider provider, 
		Object parentUnderstanding, CPUArchitecture_Type arch)
			throws JSDLException
	{
		if (arch == null)
			return;
		
		CPUArchitectureFacet facet = provider.getCPUArchitectureFacet(
			parentUnderstanding);
		Object understanding = facet.createFacetUnderstanding(
			parentUnderstanding);
		
		understandAny(facet, understanding, arch.get_any());
		
		ProcessorArchitectureEnumeration e = arch.getCPUArchitectureName();
		if (e != null)
			facet.consumeCPUArchitectureName(understanding, e);
		
		facet.completeFacet(parentUnderstanding, understanding);
	}
	
	static private void understand(PersonalityProvider provider, 
		Object parentUnderstanding, DataStaging_Type stage)
			throws JSDLException
	{
		if (stage == null)
			return;
		
		DataStagingFacet facet = provider.getDataStagingFacet(
			parentUnderstanding);
		Object understanding = facet.createFacetUnderstanding(
			parentUnderstanding);
		
		understandAny(facet, understanding, stage.get_any());
		
		String str = stage.getFileName();
		if (str != null)
			facet.consumeFileName(understanding, str);
		
		NormalizedString nStr = stage.getFilesystemName();
		if (nStr != null)
			facet.consumeFileSystemName(understanding, nStr.toString());
		
		CreationFlagEnumeration e = stage.getCreationFlag();
		if (e != null)
			facet.consumeCreationFlag(understanding, e);
		
		Boolean b = stage.getDeleteOnTermination();
		if (b != null)
			facet.consumeDeleteOnTerminateFlag(understanding, b.booleanValue());
		
		understandSource(provider, understanding, stage.getSource());
		understandTarget(provider, understanding, stage.getTarget());
		
		facet.completeFacet(parentUnderstanding, understanding);
	}
	
	static private void understandSource(PersonalityProvider provider,
		Object parentUnderstanding, SourceTarget_Type source) 
			throws JSDLException
	{
		if (source == null)
			return;
		
		SourceURIFacet facet = provider.getSourceURIFacet(
			parentUnderstanding);
		Object understanding = facet.createFacetUnderstanding(
			parentUnderstanding);
		
		understandAny(facet, understanding, source.get_any());
		
		try
		{
			facet.consumeURI(understanding, 
				new URI(source.getURI().toString()));
		}
		catch (URISyntaxException use)
		{
			throw new InvalidJSDLException(
				"Unable to parse URI in data staging element.");
		}
		
		facet.completeFacet(parentUnderstanding, understanding);
	}
	
	static private void understandTarget(PersonalityProvider provider,
		Object parentUnderstanding, SourceTarget_Type target) 
			throws JSDLException
	{
		if (target == null)
			return;
		
		TargetURIFacet facet = provider.getTargetURIFacet(
			parentUnderstanding);
		Object understanding = facet.createFacetUnderstanding(
			parentUnderstanding);
		
		understandAny(facet, understanding, target.get_any());
		
		try
		{
			facet.consumeURI(understanding, 
				new URI(target.getURI().toString()));
		}
		catch (URISyntaxException use)
		{
			throw new InvalidJSDLException(
				"Unable to parse URI in data staging element.");
		}
		
		facet.completeFacet(parentUnderstanding, understanding);
	}
}
