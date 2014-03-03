/*
 * This code was developed by Mark Morgan (mmm2a@virginia.edu) at the University of Virginia and is
 * an implementation of JSDL, JSDL ParameterSweep and other JSDL related specifications from the
 * OGF.
 * 
 * Copyright 2010 University of Virginia
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
import edu.virginia.vcgr.jsdl.hpcfse.SecurityToken;
import edu.virginia.vcgr.jsdl.rangevalue.RangeValue;
import edu.virginia.vcgr.jsdl.sweep.parameters.NamespaceBinding;

/**
 * @author Mark Morgan (mmm2a@virginia.edu)
 */
public interface JSDLEventReceiver {
	public void handleAnyElement(XMLDocumentPath path, Element anyElement)
			throws JSDLValidationException;

	public void handleAnyAttribute(XMLDocumentPath path, QName attributeName,
			String attributeValue) throws JSDLValidationException;

	public void startJobDefinition(XMLDocumentPath path, String id)
			throws JSDLValidationException;

	public void endJobDefinition(XMLDocumentPath path)
			throws JSDLValidationException;

	public void startJobDescription(XMLDocumentPath path)
			throws JSDLValidationException;

	public void endJobDescription(XMLDocumentPath path)
			throws JSDLValidationException;

	public void startJobIdentification(XMLDocumentPath path, String jobName,
			String jobDescription, List<String> jobAnnotations,
			List<String> jobProjects) throws JSDLValidationException;

	public void endJobIdentification(XMLDocumentPath path)
			throws JSDLValidationException;

	public void startParameterSweep(XMLDocumentPath path)
			throws JSDLValidationException;

	public void endParameterSweep(XMLDocumentPath path)
			throws JSDLValidationException;

	public void startResources(XMLDocumentPath path)
			throws JSDLValidationException;

	public void endResources(XMLDocumentPath path)
			throws JSDLValidationException;

	public void startApplication(XMLDocumentPath path, String applicationName,
			String applicationVersion, String applicationDescription)
			throws JSDLValidationException;

	public void endApplication(XMLDocumentPath path)
			throws JSDLValidationException;

	public void startDataStaging(XMLDocumentPath path, String stageName,
			String filename, String filesystemName, CreationFlag creationFlag,
			Boolean deleteOnTermination) throws JSDLValidationException;

	public void endDataStaging(XMLDocumentPath path)
			throws JSDLValidationException;

	public void handleCandidateHost(XMLDocumentPath path, String candidateHost)
			throws JSDLValidationException;

	public void startFilesystem(XMLDocumentPath path, String filesystemName,
			FileSystemType fsType, String description, String mountPoint,
			RangeValue diskSpace) throws JSDLValidationException;

	public void endFilesystem(XMLDocumentPath path)
			throws JSDLValidationException;

	public void handleExclusiveExecution(XMLDocumentPath path,
			boolean exclusiveExecution) throws JSDLValidationException;

	public void startOperatingSystem(XMLDocumentPath path, String osVersion,
			String description) throws JSDLValidationException;

	public void endOperatingSystem(XMLDocumentPath path)
			throws JSDLValidationException;

	public void startOperatingSystemType(XMLDocumentPath path,
			OperatingSystemNames osName) throws JSDLValidationException;

	public void endOperatingSystemType(XMLDocumentPath path)
			throws JSDLValidationException;

	public void startCPUArchitecture(XMLDocumentPath path,
			ProcessorArchitecture processorArchitecture)
			throws JSDLValidationException;

	public void endCPUArchitecture(XMLDocumentPath path)
			throws JSDLValidationException;

	public void handleIndividualResourceRanges(XMLDocumentPath path,
			RangeValue individualCPUSpeed, RangeValue individualCPUTime,
			RangeValue individualCPUCount,
			RangeValue individualNetworkBandwidth,
			RangeValue individualPhysicalMemory,
			RangeValue individualVirtualMemory, RangeValue individualDiskSpace)
			throws JSDLValidationException;

	public void handleTotalResourceRanges(XMLDocumentPath path,
			RangeValue totalCPUTime, RangeValue totalCPUCount,
			RangeValue totalPhysicalMemory, RangeValue totalVirtualMemory,
			RangeValue totalDiskSpace, RangeValue totalResourceCount)
			throws JSDLValidationException;

	public void startSource(XMLDocumentPath path, String uri)
			throws JSDLValidationException;

	public void endSource(XMLDocumentPath path) throws JSDLValidationException;

	public void startTarget(XMLDocumentPath path, String uri)
			throws JSDLValidationException;

	public void endTarget(XMLDocumentPath path) throws JSDLValidationException;

	public void startCredential(XMLDocumentPath path, List<SecurityToken> tokens)
			throws JSDLValidationException;

	public void endCredential(XMLDocumentPath path)
			throws JSDLValidationException;

	public void startPOSIXApplication(XMLDocumentPath path, String name)
			throws JSDLValidationException;

	public void endPOSIXApplication(XMLDocumentPath path)
			throws JSDLValidationException;

	public void handleExecutable(XMLDocumentPath path, String filename,
			String filesystemName, Map<QName, String> anyAttributes)
			throws JSDLValidationException;

	public void handleArgument(XMLDocumentPath path, String argument,
			String filesystemName, Map<QName, String> anyAttributes)
			throws JSDLValidationException;

	public void handleInput(XMLDocumentPath path, String filename,
			String filesystemName, Map<QName, String> anyAttributes)
			throws JSDLValidationException;

	public void handleOutput(XMLDocumentPath path, String filename,
			String filesystemName, Map<QName, String> anyAttributes)
			throws JSDLValidationException;

	public void handleError(XMLDocumentPath path, String filename,
			String filesystemName, Map<QName, String> anyAttributes)
			throws JSDLValidationException;

	public void handleWorkingDirectory(XMLDocumentPath path,
			String directoryName, String filesystemName,
			Map<QName, String> anyAttributes) throws JSDLValidationException;

	public void handleEnvironmentVariable(XMLDocumentPath path,
			String variableName, String variableValue, String filesystemName,
			Map<QName, String> anyAttributes) throws JSDLValidationException;

	public void startLimit(XMLDocumentPath path, POSIXLimitType limitType,
			long value) throws JSDLValidationException;

	public void endLimit(XMLDocumentPath path) throws JSDLValidationException;

	public void startUserName(XMLDocumentPath path, String userName)
			throws JSDLValidationException;

	public void endUserName(XMLDocumentPath path)
			throws JSDLValidationException;

	public void startGroupName(XMLDocumentPath path, String groupName)
			throws JSDLValidationException;

	public void endGroupName(XMLDocumentPath path)
			throws JSDLValidationException;

	public void startHPCProfileApplication(XMLDocumentPath path, String name)
			throws JSDLValidationException;

	public void endHPCProfileApplication(XMLDocumentPath path)
			throws JSDLValidationException;

	public void startSPMDApplication(XMLDocumentPath path, String name)
			throws JSDLValidationException;

	public void endSPMDApplication(XMLDocumentPath path)
			throws JSDLValidationException;

	public void startNumberOfProcesses(XMLDocumentPath path,
			boolean actualTotalCPUCount, Long numberOfProcesses)
			throws JSDLValidationException;

	public void endNumberOfProcesses(XMLDocumentPath path)
			throws JSDLValidationException;

	public void startProcessesPerHost(XMLDocumentPath path,
			long processesPerHost) throws JSDLValidationException;

	public void endProcessesPerHost(XMLDocumentPath path)
			throws JSDLValidationException;

	public void startThreadsPerProcess(XMLDocumentPath path,
			boolean actualIndividualCPUCount, Long threadsPerProces)
			throws JSDLValidationException;

	public void endThreadsPerProcess(XMLDocumentPath path)
			throws JSDLValidationException;

	public void startSweepAssignment(XMLDocumentPath path)
			throws JSDLValidationException;

	public void endSweepAssignment(XMLDocumentPath path)
			throws JSDLValidationException;

	public void handleDocumentNodeSweepParameter(XMLDocumentPath path,
			List<NamespaceBinding> namespaceBindings, String matchExpression)
			throws JSDLValidationException;

	public void handleValuesSweepFunction(XMLDocumentPath path,
			List<Object> values) throws JSDLValidationException;

	public void handleLoopIntegerSweepFunction(XMLDocumentPath path, int start,
			int end, int step, Set<Integer> exceptions)
			throws JSDLValidationException;

	public void handleLoopDoubleSweepFunction(XMLDocumentPath path,
			double start, double end, double step, Set<Double> exceptions)
			throws JSDLValidationException;
}
