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

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import org.w3c.dom.Element;

import edu.virginia.vcgr.jsdl.Application;
import edu.virginia.vcgr.jsdl.ApplicationBase;
import edu.virginia.vcgr.jsdl.CPUArchitecture;
import edu.virginia.vcgr.jsdl.DataStaging;
import edu.virginia.vcgr.jsdl.FileSystem;
import edu.virginia.vcgr.jsdl.JSDLConstants;
import edu.virginia.vcgr.jsdl.JobDefinition;
import edu.virginia.vcgr.jsdl.JobDescription;
import edu.virginia.vcgr.jsdl.JobIdentification;
import edu.virginia.vcgr.jsdl.OperatingSystem;
import edu.virginia.vcgr.jsdl.OperatingSystemType;
import edu.virginia.vcgr.jsdl.Resources;
import edu.virginia.vcgr.jsdl.SourceTarget;
import edu.virginia.vcgr.jsdl.hpc.HPCConstants;
import edu.virginia.vcgr.jsdl.hpc.HPCProfileApplication;
import edu.virginia.vcgr.jsdl.hpcfse.Credential;
import edu.virginia.vcgr.jsdl.hpcfse.HPCFSEConstants;
import edu.virginia.vcgr.jsdl.posix.Argument;
import edu.virginia.vcgr.jsdl.posix.DirectoryName;
import edu.virginia.vcgr.jsdl.posix.Environment;
import edu.virginia.vcgr.jsdl.posix.FileName;
import edu.virginia.vcgr.jsdl.posix.GroupName;
import edu.virginia.vcgr.jsdl.posix.Limits;
import edu.virginia.vcgr.jsdl.posix.POSIXApplication;
import edu.virginia.vcgr.jsdl.posix.UserName;
import edu.virginia.vcgr.jsdl.spmd.NumberOfProcesses;
import edu.virginia.vcgr.jsdl.spmd.ProcessesPerHost;
import edu.virginia.vcgr.jsdl.spmd.SPMDApplication;
import edu.virginia.vcgr.jsdl.spmd.SPMDConstants;
import edu.virginia.vcgr.jsdl.spmd.ThreadsPerProcess;
import edu.virginia.vcgr.jsdl.sweep.Sweep;
import edu.virginia.vcgr.jsdl.sweep.SweepAssignment;
import edu.virginia.vcgr.jsdl.sweep.SweepConstants;
import edu.virginia.vcgr.jsdl.sweep.SweepFunction;
import edu.virginia.vcgr.jsdl.sweep.SweepParameter;
import edu.virginia.vcgr.jsdl.sweep.functions.LoopDoubleSweepFunction;
import edu.virginia.vcgr.jsdl.sweep.functions.LoopIntegerSweepFunction;
import edu.virginia.vcgr.jsdl.sweep.functions.ValuesSweepFunction;
import edu.virginia.vcgr.jsdl.sweep.parameters.DocumentNodeSweepParameter;

/**
 * @author Mark Morgan (mmm2a@virginia.edu)
 */
public class JSDLEventGenerator
{
	static private void handleAnys(XMLDocumentPathImpl path, List<Element> anyElements, Map<QName, String> anyAttributes,
		JSDLEventReceiver receiver) throws JSDLValidationException
	{
		if (anyAttributes != null) {
			for (Map.Entry<QName, String> attribute : anyAttributes.entrySet()) {
				receiver.handleAnyAttribute(path, attribute.getKey(), attribute.getValue());
			}
		}

		if (anyElements != null) {
			for (Element any : anyElements) {
				String ns = any.getNamespaceURI();
				String prefix = any.getPrefix();
				QName name =
					new QName(ns == null ? XMLConstants.NULL_NS_URI : ns, any.getLocalName(),
						prefix == null ? XMLConstants.DEFAULT_NS_PREFIX : prefix);
				path.push(name);

				receiver.handleAnyElement(path, any);

				path.pop();
			}
		}
	}

	static private void generateJobIdentificationEvents(XMLDocumentPathImpl path, JobIdentification jobIdentification,
		JSDLEventReceiver receiver) throws JSDLValidationException
	{
		if (jobIdentification != null) {
			path.push(path.formQNameFromPrevious("JobIdentification"));
			receiver.startJobIdentification(path, jobIdentification.jobName(), jobIdentification.description(),
				jobIdentification.annotations(), jobIdentification.projects());
			handleAnys(path, jobIdentification.any(), jobIdentification.anyAttributes(), receiver);

			receiver.endJobIdentification(path);
			path.pop();
		}
	}

	static private void generateFilesystemEvents(XMLDocumentPathImpl path, List<FileSystem> filesystems,
		JSDLEventReceiver receiver) throws JSDLValidationException
	{
		if (filesystems != null) {
			for (FileSystem filesystem : filesystems) {
				path.push(path.formQNameFromPrevious("FileSystem"));
				receiver.startFilesystem(path, filesystem.name(), filesystem.fsType(), filesystem.description(),
					filesystem.mountPoint(), filesystem.diskSpace());
				handleAnys(path, filesystem.any(), filesystem.anyAttributes(), receiver);
				receiver.endFilesystem(path);
				path.pop();
			}
		}
	}

	static private void generateOperatingSystemEvents(XMLDocumentPathImpl path, OperatingSystem operatingSystem,
		JSDLEventReceiver receiver) throws JSDLValidationException
	{
		if (operatingSystem != null) {
			path.push(path.formQNameFromPrevious("OperatingSystem"));
			receiver.startOperatingSystem(path, operatingSystem.osVersion(), operatingSystem.description());
			handleAnys(path, operatingSystem.any(), operatingSystem.anyAttributes(), receiver);

			OperatingSystemType osType = operatingSystem.osType();
			if (osType != null) {
				path.push(path.formQNameFromPrevious("OperatingSystemType"));
				receiver.startOperatingSystemType(path, osType.osName());
				handleAnys(path, osType.any(), osType.anyAttributes(), receiver);
				receiver.endOperatingSystemType(path);
				path.pop();
			}

			receiver.endOperatingSystem(path);
			path.pop();
		}
	}

	static private void generateCPUArchitectureEvents(XMLDocumentPathImpl path, CPUArchitecture cpuArch,
		JSDLEventReceiver receiver) throws JSDLValidationException
	{
		if (cpuArch != null) {
			path.push(path.formQNameFromPrevious("CPUArchitecture"));
			receiver.startCPUArchitecture(path, cpuArch.processorArchitecture());
			handleAnys(path, cpuArch.any(), cpuArch.anyAttributes(), receiver);
			receiver.endCPUArchitecture(path);
			path.pop();
		}
	}

	static private void generateResourcesEvents(XMLDocumentPathImpl path, Resources resources, JSDLEventReceiver receiver)
		throws JSDLValidationException
	{
		if (resources != null) {
			path.push(path.formQNameFromPrevious("Resources"));
			receiver.startResources(path);
			handleAnys(path, resources.any(), resources.anyAttributes(), receiver);

			List<String> candidateHosts = resources.candidateHosts();
			if (candidateHosts != null) {
				for (String host : candidateHosts) {
					path.push(path.formQNameFromPrevious("CandidateHosts"));
					path.push(path.formQNameFromPrevious("HostName"));

					receiver.handleCandidateHost(path, host);

					path.pop();
					path.pop();
				}
			}

			generateFilesystemEvents(path, resources.filesystems(), receiver);

			Boolean exclusiveExecution = resources.exclusiveExecution();
			if (exclusiveExecution != null) {
				path.push(path.formQNameFromPrevious("ExclusiveExecution"));
				receiver.handleExclusiveExecution(path, exclusiveExecution.booleanValue());
				path.pop();
			}

			generateOperatingSystemEvents(path, resources.operatingSystem(), receiver);
			generateCPUArchitectureEvents(path, resources.cpuArchitecture(), receiver);

			receiver.handleIndividualResourceRanges(path, resources.individualCPUSpeed(), resources.individualCPUTime(),
				resources.individualCPUCount(), resources.individualNetworkBandwidth(), resources.individualPhysicalMemory(),
				resources.individualVirtualMemory(), resources.individualDiskSpace());
			receiver.handleTotalResourceRanges(path, resources.totalCPUTime(), resources.totalCPUCount(),
				resources.totalPhysicalMemory(), resources.totalVirtualMemory(), resources.totalDiskSpace(),
				resources.totalResourceCount());

			receiver.endResources(path);
			path.pop();
		}
	}

	static private void generateLimitEvents(XMLDocumentPathImpl path, POSIXLimitType limitType, Limits limit,
		JSDLEventReceiver receiver) throws JSDLValidationException
	{
		if (limit != null) {
			path.push(path.formQNameFromPrevious(limitType.toString()));
			receiver.startLimit(path, limitType, limit.value());
			handleAnys(path, null, limit.anyAttributes(), receiver);
			receiver.endLimit(path);
			path.pop();
		}
	}

	static private void generateUserNameEvents(XMLDocumentPathImpl path, UserName userName, JSDLEventReceiver receiver)
		throws JSDLValidationException
	{
		if (userName != null) {
			path.push(path.formQNameFromPrevious("UserName"));
			receiver.startUserName(path, userName.value());
			handleAnys(path, null, userName.anyAttributes(), receiver);
			receiver.endUserName(path);
			path.pop();
		}
	}

	static private void generateGroupNameEvents(XMLDocumentPathImpl path, GroupName groupName, JSDLEventReceiver receiver)
		throws JSDLValidationException
	{
		if (groupName != null) {
			path.push(path.formQNameFromPrevious("GroupName"));
			receiver.startGroupName(path, groupName.value());
			handleAnys(path, null, groupName.anyAttributes(), receiver);
			receiver.endGroupName(path);
			path.pop();
		}
	}

	static private void generatePOSIXApplicationEvents(XMLDocumentPathImpl path, POSIXApplication posixApplication,
		JSDLEventReceiver receiver) throws JSDLValidationException
	{
		path.push(new QName(JSDLConstants.JSDL_POSIX_NS, "POSIXApplication", "jsdl-posix"));
		receiver.startPOSIXApplication(path, posixApplication.name());
		handleAnys(path, null, posixApplication.anyAttributes(), receiver);

		FileName executable = posixApplication.executable();
		if (executable != null)
			receiver.handleExecutable(path, executable.get(), executable.filesystemName(), executable.anyAttributes());
		List<Argument> args = posixApplication.arguments();
		if (args != null) {
			for (Argument arg : args)
				receiver.handleArgument(path, arg.get(), arg.filesystemName(), arg.anyAttributes());
		}

		FileName stream = posixApplication.input();
		if (stream != null)
			receiver.handleInput(path, stream.get(), stream.filesystemName(), stream.anyAttributes());
		stream = posixApplication.output();
		if (stream != null)
			receiver.handleOutput(path, stream.get(), stream.filesystemName(), stream.anyAttributes());
		stream = posixApplication.error();
		if (stream != null)
			receiver.handleError(path, stream.get(), stream.filesystemName(), stream.anyAttributes());

		DirectoryName workingDir = posixApplication.workingDirectory();
		if (workingDir != null)
			receiver.handleWorkingDirectory(path, workingDir.get(), workingDir.filesystemName(), workingDir.anyAttributes());

		List<Environment> environmentVariables = posixApplication.environmentVariables();
		if (environmentVariables != null) {
			for (Environment env : environmentVariables)
				receiver.handleEnvironmentVariable(path, env.name(), env.get(), env.filesystemName(), env.anyAttributes());
		}

		generateLimitEvents(path, POSIXLimitType.WallTimeLimit, posixApplication.wallTimeLimit(), receiver);
		generateLimitEvents(path, POSIXLimitType.FileSizeLimit, posixApplication.fileSizeLimit(), receiver);
		generateLimitEvents(path, POSIXLimitType.CoreDumpLimit, posixApplication.coreDumpLimit(), receiver);
		generateLimitEvents(path, POSIXLimitType.DataSegmentLimit, posixApplication.dataSegmentLimit(), receiver);
		generateLimitEvents(path, POSIXLimitType.LockedMemoryLimit, posixApplication.lockedMemoryLimit(), receiver);
		generateLimitEvents(path, POSIXLimitType.MemoryLimit, posixApplication.memoryLimit(), receiver);
		generateLimitEvents(path, POSIXLimitType.OpenDescriptorsLimit, posixApplication.openDescriptorsLimit(), receiver);
		generateLimitEvents(path, POSIXLimitType.PipeSizeLimit, posixApplication.pipeSizeLimit(), receiver);
		generateLimitEvents(path, POSIXLimitType.StackSizeLimit, posixApplication.stackSizeLimit(), receiver);
		generateLimitEvents(path, POSIXLimitType.CPUTimeLimit, posixApplication.cpuTimeLimit(), receiver);
		generateLimitEvents(path, POSIXLimitType.ProcessCountLimit, posixApplication.processCountLimit(), receiver);
		generateLimitEvents(path, POSIXLimitType.VirtualMemoryLimit, posixApplication.virtualMemoryLimit(), receiver);
		generateLimitEvents(path, POSIXLimitType.ThreadCountLimit, posixApplication.threadCountLimit(), receiver);

		generateUserNameEvents(path, posixApplication.userName(), receiver);
		generateGroupNameEvents(path, posixApplication.groupName(), receiver);

		receiver.endPOSIXApplication(path);
		path.pop();
	}

	static private void generateUserNameEvents(XMLDocumentPathImpl path, edu.virginia.vcgr.jsdl.hpc.UserName userName,
		JSDLEventReceiver receiver) throws JSDLValidationException
	{
		if (userName != null) {
			path.push(path.formQNameFromPrevious("UserName"));
			receiver.startUserName(path, userName.value());
			handleAnys(path, null, userName.anyAttributes(), receiver);
			receiver.endUserName(path);
			path.pop();
		}
	}

	static private void generateHPCProfileApplicationEvents(XMLDocumentPathImpl path, HPCProfileApplication hpcApplication,
		JSDLEventReceiver receiver) throws JSDLValidationException
	{
		path.push(new QName(HPCConstants.HPCPA_NS, "HPCProfileApplication", "hpcp"));
		receiver.startHPCProfileApplication(path, hpcApplication.name());
		handleAnys(path, null, hpcApplication.anyAttributes(), receiver);

		edu.virginia.vcgr.jsdl.hpc.FileName executable = hpcApplication.executable();
		if (executable != null)
			receiver.handleExecutable(path, executable.value(), null, executable.anyAttributes());
		List<edu.virginia.vcgr.jsdl.hpc.Argument> args = hpcApplication.arguments();
		if (args != null) {
			for (edu.virginia.vcgr.jsdl.hpc.Argument arg : args)
				receiver.handleArgument(path, arg.value(), null, arg.anyAttributes());
		}

		edu.virginia.vcgr.jsdl.hpc.FileName stream = hpcApplication.input();
		if (stream != null)
			receiver.handleInput(path, stream.value(), null, stream.anyAttributes());
		stream = hpcApplication.output();
		if (stream != null)
			receiver.handleOutput(path, stream.value(), null, stream.anyAttributes());
		stream = hpcApplication.error();
		if (stream != null)
			receiver.handleError(path, stream.value(), null, stream.anyAttributes());

		edu.virginia.vcgr.jsdl.hpc.DirectoryName workingDir = hpcApplication.workingDirectory();
		if (workingDir != null)
			receiver.handleWorkingDirectory(path, workingDir.value(), null, workingDir.anyAttributes());

		List<edu.virginia.vcgr.jsdl.hpc.Environment> environmentVariables = hpcApplication.environmentVariables();
		if (environmentVariables != null) {
			for (edu.virginia.vcgr.jsdl.hpc.Environment env : environmentVariables)
				receiver.handleEnvironmentVariable(path, env.name(), env.value(), null, env.anyAttributes());
		}

		generateUserNameEvents(path, hpcApplication.userName(), receiver);

		receiver.endHPCProfileApplication(path);
		path.pop();
	}

	static private void generateNumberOfProcessesEvents(XMLDocumentPathImpl path, NumberOfProcesses numberOfProcesses,
		JSDLEventReceiver receiver) throws JSDLValidationException
	{
		if (numberOfProcesses != null) {
			path.push(path.formQNameFromPrevious("NumberOfProcesses"));
			receiver.startNumberOfProcesses(path, numberOfProcesses.actualTotalCPUCount(), numberOfProcesses.value());
			handleAnys(path, null, numberOfProcesses.anyAttributes(), receiver);
			receiver.endNumberOfProcesses(path);
			path.pop();
		}
	}

	static private void generateProcessesPerHostEvents(XMLDocumentPathImpl path, ProcessesPerHost processesPerHost,
		JSDLEventReceiver receiver) throws JSDLValidationException
	{
		if (processesPerHost != null) {
			path.push(path.formQNameFromPrevious("ProcessesPerHost"));
			receiver.startProcessesPerHost(path, processesPerHost.value());
			handleAnys(path, null, processesPerHost.anyAttributes(), receiver);
			receiver.endProcessesPerHost(path);
			path.pop();
		}
	}

	static private void generateThreadsPerProcessEvents(XMLDocumentPathImpl path, ThreadsPerProcess threadsPerProcess,
		JSDLEventReceiver receiver) throws JSDLValidationException
	{
		if (threadsPerProcess != null) {
			path.push(path.formQNameFromPrevious("ThreadsPerProcess"));
			receiver.startThreadsPerProcess(path, threadsPerProcess.actualIndividualCPUCount(), threadsPerProcess.value());
			handleAnys(path, null, threadsPerProcess.anyAttributes(), receiver);
			receiver.endThreadsPerProcess(path);
			path.pop();
		}
	}

	static private void generateSPMDApplicationEvents(XMLDocumentPathImpl path, SPMDApplication spmdApplication,
		JSDLEventReceiver receiver) throws JSDLValidationException
	{
		path.push(new QName(SPMDConstants.JSDL_SPMD_NS, "SPMDApplication", "jsdl-spmd"));
		receiver.startSPMDApplication(path, spmdApplication.name());
		handleAnys(path, null, spmdApplication.anyAttributes(), receiver);

		FileName executable = spmdApplication.executable();
		if (executable != null)
			receiver.handleExecutable(path, executable.get(), executable.filesystemName(), executable.anyAttributes());
		List<Argument> args = spmdApplication.arguments();
		if (args != null) {
			for (Argument arg : args)
				receiver.handleArgument(path, arg.get(), arg.filesystemName(), arg.anyAttributes());
		}

		FileName stream = spmdApplication.input();
		if (stream != null)
			receiver.handleInput(path, stream.get(), stream.filesystemName(), stream.anyAttributes());
		stream = spmdApplication.output();
		if (stream != null)
			receiver.handleOutput(path, stream.get(), stream.filesystemName(), stream.anyAttributes());
		stream = spmdApplication.error();
		if (stream != null)
			receiver.handleError(path, stream.get(), stream.filesystemName(), stream.anyAttributes());

		DirectoryName workingDir = spmdApplication.workingDirectory();
		if (workingDir != null)
			receiver.handleWorkingDirectory(path, workingDir.get(), workingDir.filesystemName(), workingDir.anyAttributes());

		List<Environment> environmentVariables = spmdApplication.environmentVariables();
		if (environmentVariables != null) {
			for (Environment env : environmentVariables)
				receiver.handleEnvironmentVariable(path, env.name(), env.get(), env.filesystemName(), env.anyAttributes());
		}

		generateUserNameEvents(path, spmdApplication.userName(), receiver);

		generateNumberOfProcessesEvents(path, spmdApplication.numberOfProcesses(), receiver);
		generateProcessesPerHostEvents(path, spmdApplication.processesPerHost(), receiver);
		generateThreadsPerProcessEvents(path, spmdApplication.threadsPerProcess(), receiver);

		receiver.endSPMDApplication(path);
		path.pop();
	}

	static private void
		generateApplicationEvents(XMLDocumentPathImpl path, Application application, JSDLEventReceiver receiver)
			throws JSDLValidationException
	{
		if (application != null) {
			path.push(path.formQNameFromPrevious("Application"));
			receiver.startApplication(path, application.applicationName(), application.applicationVersion(),
				application.description());
			handleAnys(path, application.any(), application.anyAttributes(), receiver);

			ApplicationBase base = application.application();
			if (base != null) {
				if (base instanceof POSIXApplication)
					generatePOSIXApplicationEvents(path, (POSIXApplication) base, receiver);
				else if (base instanceof HPCProfileApplication)
					generateHPCProfileApplicationEvents(path, (HPCProfileApplication) base, receiver);
				else if (base instanceof SPMDApplication)
					generateSPMDApplicationEvents(path, (SPMDApplication) base, receiver);
				else
					throw new JSDLValidationException(String.format("Unexpected application type %s while validating JSDL.",
						base.getClass()));
			}

			receiver.endApplication(path);
			path.pop();
		}
	}

	static private void generateSourceEvents(XMLDocumentPathImpl path, SourceTarget source, JSDLEventReceiver receiver)
		throws JSDLValidationException
	{
		if (source != null) {
			path.push(path.formQNameFromPrevious("Source"));
			receiver.startSource(path, source.uri());
			handleAnys(path, source.any(), source.anyAttributes(), receiver);
			receiver.endSource(path);
			path.pop();
		}
	}

	static private void generateTargetEvents(XMLDocumentPathImpl path, SourceTarget target, JSDLEventReceiver receiver)
		throws JSDLValidationException
	{
		if (target != null) {
			path.push(path.formQNameFromPrevious("Source"));
			receiver.startTarget(path, target.uri());
			handleAnys(path, target.any(), target.anyAttributes(), receiver);
			receiver.endTarget(path);
			path.pop();
		}
	}

	static private void generateCredentialsEvents(XMLDocumentPathImpl path, Credential credentials, JSDLEventReceiver receiver)
		throws JSDLValidationException
	{
		if (credentials != null) {
			path.push(new QName(HPCFSEConstants.HPCFSE_NS, "Credential", "hpc-fse"));
			receiver.startCredential(path, credentials.tokens());
			handleAnys(path, credentials.any(), null, receiver);
			receiver.endCredential(path);
			path.pop();
		}
	}

	static private void
		generateDataStagingEvents(XMLDocumentPathImpl path, List<DataStaging> stages, JSDLEventReceiver receiver)
			throws JSDLValidationException
	{
		if (stages != null) {
			for (DataStaging stage : stages) {
				path.push(path.formQNameFromPrevious("DataStaging"));
				receiver.startDataStaging(path, stage.name(), stage.filename(), stage.filesystemName(), stage.creationFlag(),
					stage.deleteOnTermionation());
				handleAnys(path, stage.any(), stage.anyAttributes(), receiver);

				generateSourceEvents(path, stage.source(), receiver);
				generateTargetEvents(path, stage.target(), receiver);
				generateCredentialsEvents(path, stage.credentials(), receiver);

				receiver.endDataStaging(path);
				path.pop();
			}
		}
	}

	static private void generateDocumentNodeSweepParameterEvents(XMLDocumentPathImpl path,
		DocumentNodeSweepParameter parameter, JSDLEventReceiver receiver) throws JSDLValidationException
	{
		path.push(new QName(SweepConstants.SWEEP_NS, "DocumentNode", "spmd"));

		receiver.handleDocumentNodeSweepParameter(path, parameter.bindings(), parameter.matchExpression());

		path.pop();
	}

	static private void generateSweepParameterEvents(XMLDocumentPathImpl path, List<SweepParameter> parameters,
		JSDLEventReceiver receiver) throws JSDLValidationException
	{
		if (parameters != null) {
			for (SweepParameter parameter : parameters) {
				if (parameter instanceof DocumentNodeSweepParameter) {
					generateDocumentNodeSweepParameterEvents(path, (DocumentNodeSweepParameter) parameter, receiver);
				} else
					throw new JSDLValidationException(String.format(
						"Unexpected sweep parameter type %s while validating JSDL.", parameter.getClass()));
			}
		}
	}

	static private void generateValuesSweepFunctionEvents(XMLDocumentPathImpl path, ValuesSweepFunction function,
		JSDLEventReceiver receiver) throws JSDLValidationException
	{
		path.push(new QName(SweepConstants.SWEEP_FUNC_NS, "Values", "sweep-func"));

		receiver.handleValuesSweepFunction(path, function.values());

		path.pop();
	}

	static private void generateLoopIntegerSweepFunctionEvents(XMLDocumentPathImpl path, LoopIntegerSweepFunction function,
		JSDLEventReceiver receiver) throws JSDLValidationException
	{
		path.push(new QName(SweepConstants.SWEEP_FUNC_NS, "LoopInteger", "sweep-func"));

		receiver.handleLoopIntegerSweepFunction(path, function.start(), function.end(), function.step(), function.exceptions());

		path.pop();
	}

	static private void generateLoopDoubleSweepFunctionEvents(XMLDocumentPathImpl path, LoopDoubleSweepFunction function,
		JSDLEventReceiver receiver) throws JSDLValidationException
	{
		path.push(new QName(SweepConstants.SWEEP_FUNC_NS, "LoopDouble", "sweep-func"));

		receiver.handleLoopDoubleSweepFunction(path, function.start(), function.end(), function.step(), function.exceptions());

		path.pop();
	}

	static private void
		generateSweepFunctionEvents(XMLDocumentPathImpl path, SweepFunction function, JSDLEventReceiver receiver)
			throws JSDLValidationException
	{
		if (function != null) {
			if (function instanceof ValuesSweepFunction) {
				generateValuesSweepFunctionEvents(path, (ValuesSweepFunction) function, receiver);
			} else if (function instanceof LoopIntegerSweepFunction) {
				generateLoopIntegerSweepFunctionEvents(path, (LoopIntegerSweepFunction) function, receiver);
			} else if (function instanceof LoopDoubleSweepFunction) {
				generateLoopDoubleSweepFunctionEvents(path, (LoopDoubleSweepFunction) function, receiver);
			} else
				throw new JSDLValidationException(String.format("Unexpected sweep function type %s while validating JSDL.",
					function.getClass()));
		}
	}

	static private void generateSweepAssignmentEvents(XMLDocumentPathImpl path, List<SweepAssignment> assignments,
		JSDLEventReceiver receiver) throws JSDLValidationException
	{
		if (assignments != null) {
			for (SweepAssignment assignment : assignments) {
				path.push(path.formQNameFromPrevious("Assignment"));
				receiver.startSweepAssignment(path);

				generateSweepParameterEvents(path, assignment.sweepParameters(), receiver);
				generateSweepFunctionEvents(path, assignment.sweepFunction(), receiver);

				receiver.endSweepAssignment(path);
				path.pop();
			}
		}
	}

	static private void generateSweepEvents(XMLDocumentPathImpl path, List<Sweep> sweeps, JSDLEventReceiver receiver)
		throws JSDLValidationException
	{
		if (sweeps != null) {
			for (Sweep sweep : sweeps) {
				path.push(new QName(SweepConstants.SWEEP_NS, SweepConstants.SWEEP_NAME, "sweep"));
				receiver.startParameterSweep(path);

				generateSweepAssignmentEvents(path, sweep.assignments(), receiver);
				generateSweepEvents(path, sweep.subSweeps(), receiver);

				receiver.endParameterSweep(path);
				path.pop();
			}
		}
	}

	static public void generateJSDLEvents(JobDefinition jobDef, JSDLEventReceiver receiver) throws JSDLValidationException
	{
		XMLDocumentPathImpl xmlPath = new XMLDocumentPathImpl();

		if (jobDef != null) {
			xmlPath.push(new QName(JSDLConstants.JSDL_NS, "JobDefinition", "jsdl"));
			receiver.startJobDefinition(xmlPath, jobDef.id());
			handleAnys(xmlPath, jobDef.any(), jobDef.anyAttributes(), receiver);

			JobDescription jobDesc = jobDef.jobDescription();
			if (jobDesc != null) {
				xmlPath.push(xmlPath.formQNameFromPrevious("JobDescription"));
				receiver.startJobDescription(xmlPath);
				handleAnys(xmlPath, jobDesc.any(), jobDesc.anyAttributes(), receiver);

				generateJobIdentificationEvents(xmlPath, jobDesc.jobIdentification(), receiver);
				generateResourcesEvents(xmlPath, jobDesc.resources(), receiver);
				generateApplicationEvents(xmlPath, jobDesc.application(), receiver);
				generateDataStagingEvents(xmlPath, jobDesc.staging(), receiver);

				receiver.endJobDescription(xmlPath);
				xmlPath.pop();
			}

			generateSweepEvents(xmlPath, jobDef.parameterSweeps(), receiver);

			receiver.endJobDefinition(xmlPath);
			xmlPath.pop();
		}
	}
}
