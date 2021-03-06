/*
 * This code was developed by Mark Morgan (mmm2a@virginia.edu) at the University of Virginia and is an implementation of JSDL, JSDL
 * ParameterSweep and other JSDL related specifications from the OGF.
 * 
 * Copyright 2010 University of Virginia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package edu.virginia.vcgr.jsdlval;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.w3c.dom.Element;

import edu.virginia.vcgr.jsdl.CreationFlag;
import edu.virginia.vcgr.jsdl.FileSystemType;
import edu.virginia.vcgr.jsdl.OperatingSystemNames;
import edu.virginia.vcgr.jsdl.ProcessorArchitecture;
import edu.virginia.vcgr.jsdl.GPUArchitecture;
import edu.virginia.vcgr.jsdl.hpcfse.SecurityToken;
import edu.virginia.vcgr.jsdl.rangevalue.RangeValue;
import edu.virginia.vcgr.jsdl.sweep.parameters.NamespaceBinding;

/**
 * @author Mark Morgan (mmm2a@virginia.edu)
 */
public class DefaultJSDLEventReceiver implements JSDLEventReceiver
{
	@Override
	public void handleAnyElement(XMLDocumentPath path, Element anyElement) throws JSDLValidationException
	{
		throw new JSDLComponentNotSupportedException(path);
	}

	@Override
	public void handleAnyAttribute(XMLDocumentPath path, QName attributeName, String attributeValue) throws JSDLValidationException
	{
		throw new JSDLComponentNotSupportedException(path, attributeName);
	}

	@Override
	public void startJobDefinition(XMLDocumentPath path, String id) throws JSDLValidationException
	{
		throw new JSDLComponentNotSupportedException(path);
	}

	@Override
	public void endJobDefinition(XMLDocumentPath path) throws JSDLValidationException
	{
		// We allow all "end..." methods
	}

	@Override
	public void startJobDescription(XMLDocumentPath path) throws JSDLValidationException
	{
		throw new JSDLComponentNotSupportedException(path);
	}

	@Override
	public void endJobDescription(XMLDocumentPath path) throws JSDLValidationException
	{
		// We allow all "end..." methods
	}

	@Override
	public void startJobIdentification(XMLDocumentPath path, String jobName, String jobDescription, List<String> jobAnnotations,
		List<String> jobProjects) throws JSDLValidationException
	{
		throw new JSDLComponentNotSupportedException(path);
	}

	@Override
	public void endJobIdentification(XMLDocumentPath path) throws JSDLValidationException
	{
		// We allow all "end..." methods
	}

	@Override
	public void startParameterSweep(XMLDocumentPath path) throws JSDLValidationException
	{
		throw new JSDLComponentNotSupportedException(path);
	}

	@Override
	public void endParameterSweep(XMLDocumentPath path) throws JSDLValidationException
	{
		// We allow all "end..." methods
	}

	@Override
	public void startResources(XMLDocumentPath path) throws JSDLValidationException
	{
		throw new JSDLComponentNotSupportedException(path);
	}

	@Override
	public void endResources(XMLDocumentPath path) throws JSDLValidationException
	{
		// We allow all "end..." methods
	}

	@Override
	public void startApplication(XMLDocumentPath path, String applicationName, String applicationVersion, String applicationDescription)
		throws JSDLValidationException
	{
		throw new JSDLComponentNotSupportedException(path);
	}

	@Override
	public void endApplication(XMLDocumentPath path) throws JSDLValidationException
	{
		// We allow all "end..." methods
	}

	@Override
	public void startDataStaging(XMLDocumentPath path, String stageName, String filename, String filesystemName, CreationFlag creationFlag,
		Boolean deleteOnTermination, Boolean handleAsArchive) throws JSDLValidationException
	{
		throw new JSDLComponentNotSupportedException(path);
	}

	@Override
	public void endDataStaging(XMLDocumentPath path) throws JSDLValidationException
	{
		// We allow all "end..." methods
	}

	@Override
	public void handleCandidateHost(XMLDocumentPath path, String candidateHost) throws JSDLValidationException
	{
		throw new JSDLComponentNotSupportedException(path);
	}

	@Override
	public void startFilesystem(XMLDocumentPath path, String filesystemName, FileSystemType fsType, String description, String mountPoint,
		RangeValue diskSpace) throws JSDLValidationException
	{
		throw new JSDLComponentNotSupportedException(path);
	}

	@Override
	public void endFilesystem(XMLDocumentPath path) throws JSDLValidationException
	{
		// We allow all "end..." methods
	}

	@Override
	public void handleExclusiveExecution(XMLDocumentPath path, boolean exclusiveExecution) throws JSDLValidationException
	{
		throw new JSDLComponentNotSupportedException(path);
	}

	@Override
	public void startOperatingSystem(XMLDocumentPath path, String osVersion, String description) throws JSDLValidationException
	{
		throw new JSDLComponentNotSupportedException(path);
	}

	@Override
	public void endOperatingSystem(XMLDocumentPath path) throws JSDLValidationException
	{
		// We allow all "end..." methods
	}

	@Override
	public void startOperatingSystemType(XMLDocumentPath path, OperatingSystemNames osName) throws JSDLValidationException
	{
		throw new JSDLComponentNotSupportedException(path);
	}

	@Override
	public void endOperatingSystemType(XMLDocumentPath path) throws JSDLValidationException
	{
		// We allow all "end..." methods
	}

	@Override
	public void startCPUArchitecture(XMLDocumentPath path, ProcessorArchitecture processorArchitecture) throws JSDLValidationException
	{
		throw new JSDLComponentNotSupportedException(path);
	}

	@Override
	public void endCPUArchitecture(XMLDocumentPath path) throws JSDLValidationException
	{
		// We allow all "end..." methods
	}

	@Override
    public void startGPUArchitecture(XMLDocumentPath path, GPUArchitecture gpuProcessorArchitecture) throws JSDLValidationException
    {
		throw new JSDLComponentNotSupportedException(path);
    }

	@Override
	public void endGPUArchitecture(XMLDocumentPath path) throws JSDLValidationException
	{
		// We allow all "end..." methods
	}

	@Override
	public void handleIndividualResourceRanges(XMLDocumentPath path, RangeValue individualCPUSpeed, RangeValue individualCPUTime,
		RangeValue individualCPUCount, RangeValue individualNetworkBandwidth, RangeValue individualPhysicalMemory,
		RangeValue individualVirtualMemory, RangeValue individualDiskSpace, RangeValue GPUCountPerNode, RangeValue GPUMemoryPerNode) throws JSDLValidationException
	{
		throw new JSDLComponentNotSupportedException(path);
	}

	@Override
	public void handleTotalResourceRanges(XMLDocumentPath path, RangeValue totalCPUTime, RangeValue totalCPUCount,
		RangeValue totalPhysicalMemory, RangeValue totalVirtualMemory, RangeValue totalDiskSpace, RangeValue totalResourceCount)
		throws JSDLValidationException
	{
		throw new JSDLComponentNotSupportedException(path);
	}

	@Override
	public void startSource(XMLDocumentPath path, String uri) throws JSDLValidationException
	{
		throw new JSDLComponentNotSupportedException(path);
	}

	@Override
	public void endSource(XMLDocumentPath path) throws JSDLValidationException
	{
		// We allow all "end..." methods.
	}

	@Override
	public void startTarget(XMLDocumentPath path, String uri) throws JSDLValidationException
	{
		throw new JSDLComponentNotSupportedException(path);
	}

	@Override
	public void endTarget(XMLDocumentPath path) throws JSDLValidationException
	{
		// We allow all "end..." methods
	}

	@Override
	public void startCredential(XMLDocumentPath path, List<SecurityToken> tokens) throws JSDLValidationException
	{
		throw new JSDLComponentNotSupportedException(path);
	}

	@Override
	public void endCredential(XMLDocumentPath path) throws JSDLValidationException
	{
		// We allow all "end..." methods
	}

	@Override
	public void startPOSIXApplication(XMLDocumentPath path, String name) throws JSDLValidationException
	{
		throw new JSDLComponentNotSupportedException(path);
	}

	@Override
	public void endPOSIXApplication(XMLDocumentPath path) throws JSDLValidationException
	{
		// We allow all "end..." methods
	}

	@Override
	public void handleExecutable(XMLDocumentPath path, String filename, String filesystemName, Map<QName, String> anyAttributes)
		throws JSDLValidationException
	{
		throw new JSDLComponentNotSupportedException(path);
	}

	@Override
	public void handleArgument(XMLDocumentPath path, String argument, String filesystemName, Map<QName, String> anyAttributes)
		throws JSDLValidationException
	{
		throw new JSDLComponentNotSupportedException(path);
	}

	@Override
	public void handleInput(XMLDocumentPath path, String filename, String filesystemName, Map<QName, String> anyAttributes)
		throws JSDLValidationException
	{
		throw new JSDLComponentNotSupportedException(path);
	}

	@Override
	public void handleOutput(XMLDocumentPath path, String filename, String filesystemName, Map<QName, String> anyAttributes)
		throws JSDLValidationException
	{
		throw new JSDLComponentNotSupportedException(path);
	}

	@Override
	public void handleError(XMLDocumentPath path, String filename, String filesystemName, Map<QName, String> anyAttributes)
		throws JSDLValidationException
	{
		throw new JSDLComponentNotSupportedException(path);
	}

	@Override
	public void handleWorkingDirectory(XMLDocumentPath path, String directoryName, String filesystemName, Map<QName, String> anyAttributes)
		throws JSDLValidationException
	{
		throw new JSDLComponentNotSupportedException(path);
	}

	@Override
	public void handleEnvironmentVariable(XMLDocumentPath path, String variableName, String variableValue, String filesystemName,
		Map<QName, String> anyAttributes) throws JSDLValidationException
	{
		throw new JSDLComponentNotSupportedException(path);
	}

	@Override
	public void startLimit(XMLDocumentPath path, POSIXLimitType limitType, long value) throws JSDLValidationException
	{
		throw new JSDLComponentNotSupportedException(path);
	}

	@Override
	public void endLimit(XMLDocumentPath path) throws JSDLValidationException
	{
		// We allow all "end..." methods
	}

	@Override
	public void startUserName(XMLDocumentPath path, String userName) throws JSDLValidationException
	{
		throw new JSDLComponentNotSupportedException(path);
	}

	@Override
	public void endUserName(XMLDocumentPath path) throws JSDLValidationException
	{
		// We allow all "end..." methods
	}

	@Override
	public void startGroupName(XMLDocumentPath path, String groupName) throws JSDLValidationException
	{
		throw new JSDLComponentNotSupportedException(path);
	}

	@Override
	public void endGroupName(XMLDocumentPath path) throws JSDLValidationException
	{
		// We allow all "end..." methods
	}

	@Override
	public void startHPCProfileApplication(XMLDocumentPath path, String name) throws JSDLValidationException
	{
		throw new JSDLComponentNotSupportedException(path);
	}

	@Override
	public void endHPCProfileApplication(XMLDocumentPath path) throws JSDLValidationException
	{
		// We allow all "end..." methods
	}

	@Override
	public void startSPMDApplication(XMLDocumentPath path, String name) throws JSDLValidationException
	{
		throw new JSDLComponentNotSupportedException(path);
	}

	@Override
	public void endSPMDApplication(XMLDocumentPath path) throws JSDLValidationException
	{
		// We allow all "end..." methods
	}

	@Override
	public void startNumberOfProcesses(XMLDocumentPath path, boolean actualTotalCPUCount, Long numberOfProcesses)
		throws JSDLValidationException
	{
		throw new JSDLComponentNotSupportedException(path);
	}

	@Override
	public void endNumberOfProcesses(XMLDocumentPath path) throws JSDLValidationException
	{
		// We allow all "end..." methods
	}

	@Override
	public void startProcessesPerHost(XMLDocumentPath path, long processesPerHost) throws JSDLValidationException
	{
		throw new JSDLComponentNotSupportedException(path);
	}

	@Override
	public void endProcessesPerHost(XMLDocumentPath path) throws JSDLValidationException
	{
		// We allow all "end..." methods
	}

	@Override
	public void startThreadsPerProcess(XMLDocumentPath path, boolean actualIndividualCPUCount, Long threadsPerProces)
		throws JSDLValidationException
	{
		throw new JSDLComponentNotSupportedException(path);
	}

	@Override
	public void endThreadsPerProcess(XMLDocumentPath path) throws JSDLValidationException
	{
		// We allow all "end..." methods
	}

	@Override
	public void startSweepAssignment(XMLDocumentPath path) throws JSDLValidationException
	{
		throw new JSDLComponentNotSupportedException(path);
	}

	@Override
	public void endSweepAssignment(XMLDocumentPath path) throws JSDLValidationException
	{
		// We allow all "end..." methods
	}

	@Override
	public void handleDocumentNodeSweepParameter(XMLDocumentPath path, List<NamespaceBinding> namespaceBindings, String matchExpression)
		throws JSDLValidationException
	{
		throw new JSDLComponentNotSupportedException(path);
	}

	@Override
	public void handleValuesSweepFunction(XMLDocumentPath path, List<Object> values) throws JSDLValidationException
	{
		throw new JSDLComponentNotSupportedException(path);
	}

	@Override
	public void handleLoopIntegerSweepFunction(XMLDocumentPath path, int start, int end, int step, Set<Integer> exceptions)
		throws JSDLValidationException
	{
		throw new JSDLComponentNotSupportedException(path);
	}

	@Override
	public void handleLoopDoubleSweepFunction(XMLDocumentPath path, double start, double end, double step, Set<Double> exceptions)
		throws JSDLValidationException
	{
		throw new JSDLComponentNotSupportedException(path);
	}
}
