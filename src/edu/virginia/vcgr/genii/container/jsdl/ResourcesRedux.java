/*
 * Copyright 2006 University of Virginia
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package edu.virginia.vcgr.genii.container.jsdl;

import org.apache.axis.message.MessageElement;
import org.ggf.jsdl.CPUArchitecture_Type;
import org.ggf.jsdl.OperatingSystem_Type;
import org.ggf.jsdl.ProcessorArchitectureEnumeration;
import org.ggf.jsdl.RangeValue_Type;
import org.ggf.jsdl.Resources_Type;

import edu.virginia.vcgr.genii.client.configuration.Hostname;
import edu.virginia.vcgr.genii.client.jsdl.JSDLUtils;

public class ResourcesRedux extends BaseRedux
{
	private Resources_Type _resources;
	
	private FileSystemRedux _filesystem = null;
	
	public ResourcesRedux(IJobPlanProvider provider, 
		Resources_Type resources)
	{
		super(provider);
		
		_resources = resources;
	}
	
	public Resources_Type getResources()
	{
		return _resources;
	}
	
	public FileSystemRedux getFileSystem()
	{
		return _filesystem;
	}
	
	public void consume() throws JSDLException
	{
		if (_resources != null)
		{
			understandCandidateHosts(_resources.getCandidateHosts());
			understandCPUArchitecture(_resources.getCPUArchitecture());
			understandExclusiveExecution(_resources.getExclusiveExecution());
			
			_filesystem = getProvider().createFileSystem(_resources.getFileSystem());
			_filesystem.consume();

			understandIndividualCPUCount(_resources.getIndividualCPUCount());
			understandIndividualCPUSpeed(_resources.getIndividualCPUSpeed());
			understandIndividualCPUTime(_resources.getIndividualCPUTime());
			understandIndividualDiskSpace(_resources.getIndividualDiskSpace());
			understandIndividualNetworkBandwidth(
					_resources.getIndividualNetworkBandwidth());
			understandIndividualPhsyicalMemory(
					_resources.getIndividualPhysicalMemory());
			understandIndividualVirtualMemory(
					_resources.getIndividualVirtualMemory());
			understandOperatingSystem(_resources.getOperatingSystem());
			understandTotalCPUCount(_resources.getTotalCPUCount());
			understandTotalCPUTime(_resources.getTotalCPUTime());
			understandTotalDiskSpace(_resources.getTotalDiskSpace());
			understandTotalPhysicalMemory(_resources.getTotalPhysicalMemory());
			understandTotalResourceCount(_resources.getTotalResourceCount());
			understandTotalVirtualMemory(_resources.getTotalVirtualMemory());
			
			MessageElement []any = _resources.get_any();
			if (any != null && any.length > 0)
				throw new UnsupportedJSDLElement(any[0].getQName());
		}
	}
	
	public void verifyComplete() throws JSDLException
	{
		if (_filesystem != null)
			_filesystem.verifyComplete();
	}
	
	protected void understandCandidateHosts(String []candidateHosts)
		throws JSDLException
	{
		if (candidateHosts != null)
		{
			String myHostName = Hostname.getLocalHostname().toString();
			for (String candidate : candidateHosts)
			{
				if (candidate.equals(myHostName))
					return;
			}
				
			throw new JSDLMatchException(JobPlan.toJSDLQName("CandidateHosts"));
		}
	}
	
	protected void understandCPUArchitecture(CPUArchitecture_Type cpuArchitecture)
		throws JSDLException
	{
		if (cpuArchitecture != null)
		{
			ProcessorArchitectureEnumeration procArchEnum =
				cpuArchitecture.getCPUArchitectureName();
			
			if (!procArchEnum.equals(JSDLUtils.getLocalCPUArchitecture()))
				throw new JSDLMatchException(JobPlan.toJSDLQName("CPUArchitecture"));
			
			MessageElement []any = cpuArchitecture.get_any();
			if (any != null && any.length > 0)
				throw new UnsupportedJSDLElement(any[0].getQName());
		}
	}
	
	protected void understandExclusiveExecution(Boolean exclusiveExecution)
		throws JSDLException
	{
		if (exclusiveExecution != null && exclusiveExecution.booleanValue())
			throw new JSDLMatchException(JobPlan.toJSDLQName("ExclusiveExecution"));
	}
	
	protected void understandIndividualCPUCount(RangeValue_Type cpuCount)
		throws JSDLException
	{
		if (cpuCount != null)
		{
			if (!JSDLUtils.satisfiesRange(1.0, cpuCount))
				throw new JSDLMatchException(
					JobPlan.toJSDLQName("IndividualCPUCount"));
		}
	}
	
	protected void understandIndividualCPUSpeed(RangeValue_Type cpuSpeed)
		throws JSDLException
	{
		if (cpuSpeed != null)
			throw new UnsupportedJSDLElement(
				JobPlan.toJSDLQName("IndividualCPUSpeed"));
	}
	
	protected void understandIndividualCPUTime(RangeValue_Type cpuTime)
		throws JSDLException
	{
	}
	
	protected void understandIndividualDiskSpace(RangeValue_Type diskSpace)
		throws JSDLException
	{
		if (diskSpace != null)
			throw new UnsupportedJSDLElement(
				JobPlan.toJSDLQName("IndividualDiskSpace"));
	}
	
	protected void understandIndividualNetworkBandwidth(
		RangeValue_Type networkBandwidth)
		throws JSDLException
	{
		if (networkBandwidth != null)
			throw new UnsupportedJSDLElement(
				JobPlan.toJSDLQName("IndividualNetworkBandwidth"));
	}
	
	protected void understandIndividualPhsyicalMemory(
		RangeValue_Type physicalMemory)
		throws JSDLException
	{
		if (physicalMemory != null)
			throw new UnsupportedJSDLElement(
				JobPlan.toJSDLQName("IndividualPhysicalMemory"));
	}
	
	protected void understandIndividualVirtualMemory(
		RangeValue_Type individualMemory)
		throws JSDLException
	{
		if (individualMemory != null)
			throw new UnsupportedJSDLElement(
				JobPlan.toJSDLQName("IndividualVirtualMemory"));
	}
	
	protected void understandOperatingSystem(OperatingSystem_Type operatingSystem)
		throws JSDLException
	{
		if (operatingSystem != null)
		{
			if (!JSDLUtils.satisfiesOS(JSDLUtils.getLocalOperatingSystem(),
				operatingSystem))
			{
				throw new JSDLMatchException(
					JobPlan.toJSDLQName("OperatingSystem"));
			}
		}
	}
	
	protected void understandTotalCPUCount(RangeValue_Type cpuCount)
		throws JSDLException
	{
		if (cpuCount != null)
		{
			if (!JSDLUtils.satisfiesRange(1.0, cpuCount))
				throw new JSDLMatchException(
					JobPlan.toJSDLQName("TotalCPUCount"));
		}	
	}
	
	protected void understandTotalCPUTime(RangeValue_Type cpuTime)
		throws JSDLException
	{
	}
	
	protected void understandTotalDiskSpace(RangeValue_Type diskSpace)
		throws JSDLException
	{
		if (diskSpace != null)
			throw new UnsupportedJSDLElement(JobPlan.toJSDLQName("TotalDiskSpace"));
	}
	
	protected void understandTotalPhysicalMemory(RangeValue_Type phsicalMemory)
		throws JSDLException
	{
		if (phsicalMemory != null)
			throw new UnsupportedJSDLElement(
				JobPlan.toJSDLQName("TotalPhysicalMemory"));
	}
	
	protected void understandTotalResourceCount(RangeValue_Type resourceCount)
		throws JSDLException
	{
		if (resourceCount != null)
			throw new UnsupportedJSDLElement(
				JobPlan.toJSDLQName("TotalResourceCount"));
	}
	
	protected void understandTotalVirtualMemory(RangeValue_Type virtualMemory)
		throws JSDLException
	{
		if (virtualMemory != null)
			throw new UnsupportedJSDLElement(
				JobPlan.toJSDLQName("TotalVirtualMemory"));
	}
}
