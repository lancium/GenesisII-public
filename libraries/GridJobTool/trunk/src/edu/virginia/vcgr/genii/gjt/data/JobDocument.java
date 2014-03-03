package edu.virginia.vcgr.genii.gjt.data;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import edu.virginia.vcgr.genii.gjt.conf.SPMDVariation;
import edu.virginia.vcgr.genii.gjt.data.analyze.Analysis;
import edu.virginia.vcgr.genii.gjt.data.fs.Filesystem;
import edu.virginia.vcgr.genii.gjt.data.fs.FilesystemListener;
import edu.virginia.vcgr.genii.gjt.data.fs.FilesystemMap;
import edu.virginia.vcgr.genii.gjt.data.fs.FilesystemRecents;
import edu.virginia.vcgr.genii.gjt.data.fs.FilesystemType;
import edu.virginia.vcgr.genii.gjt.data.recent.Recents;
import edu.virginia.vcgr.genii.gjt.data.stage.DataStage;
import edu.virginia.vcgr.genii.gjt.data.stage.StageList;
import edu.virginia.vcgr.genii.gjt.data.variables.ParameterizableBroker;
import edu.virginia.vcgr.genii.gjt.data.variables.SerializableVariablesAdapter;
import edu.virginia.vcgr.genii.gjt.data.variables.VariableDefinition;
import edu.virginia.vcgr.genii.gjt.data.variables.VariableDefinitionType;
import edu.virginia.vcgr.genii.gjt.data.variables.VariableInformation;
import edu.virginia.vcgr.genii.gjt.data.variables.VariableManager;
import edu.virginia.vcgr.genii.gjt.data.xml.PostUnmarshallListener;
import edu.virginia.vcgr.genii.gjt.data.xml.UnmarshallListener;
import edu.virginia.vcgr.genii.gjt.data.xpath.DefaultXPathAttributeNode;
import edu.virginia.vcgr.genii.gjt.data.xpath.DefaultXPathIterableNode;
import edu.virginia.vcgr.genii.gjt.data.xpath.DefaultXPathNode;
import edu.virginia.vcgr.genii.gjt.data.xpath.XPathBuilder;
import edu.virginia.vcgr.genii.gjt.util.Duple;
import edu.virginia.vcgr.genii.gjt.util.Triple;
import edu.virginia.vcgr.jsdl.Application;
import edu.virginia.vcgr.jsdl.ApplicationBase;
import edu.virginia.vcgr.jsdl.CPUArchitecture;
import edu.virginia.vcgr.jsdl.DataStaging;
import edu.virginia.vcgr.jsdl.JSDLConstants;
import edu.virginia.vcgr.jsdl.JobDefinition;
import edu.virginia.vcgr.jsdl.JobDescription;
import edu.virginia.vcgr.jsdl.JobIdentification;
import edu.virginia.vcgr.jsdl.MatchingParameter;
import edu.virginia.vcgr.jsdl.OperatingSystem;
import edu.virginia.vcgr.jsdl.OperatingSystemNames;
import edu.virginia.vcgr.jsdl.OperatingSystemType;
import edu.virginia.vcgr.jsdl.ProcessorArchitecture;
import edu.virginia.vcgr.jsdl.Resources;
import edu.virginia.vcgr.jsdl.SourceTarget;
import edu.virginia.vcgr.jsdl.posix.Argument;
import edu.virginia.vcgr.jsdl.posix.Environment;
import edu.virginia.vcgr.jsdl.posix.FileName;
import edu.virginia.vcgr.jsdl.posix.POSIXApplication;
import edu.virginia.vcgr.jsdl.rangevalue.Boundary;
import edu.virginia.vcgr.jsdl.rangevalue.RangeValue;
import edu.virginia.vcgr.jsdl.spmd.NumberOfProcesses;
import edu.virginia.vcgr.jsdl.spmd.ProcessesPerHost;
import edu.virginia.vcgr.jsdl.spmd.SPMDApplication;
import edu.virginia.vcgr.jsdl.spmd.SPMDConstants;
import edu.virginia.vcgr.jsdl.sweep.Sweep;
import edu.virginia.vcgr.jsdl.sweep.SweepAssignment;
import edu.virginia.vcgr.jsdl.sweep.SweepFunction;
import edu.virginia.vcgr.jsdl.sweep.SweepParameter;

@XmlRootElement(namespace = JobDocumentConstants.DOCUMENT_NAMESPACE, name = "job-project")
public class JobDocument implements PostUnmarshallListener {
	@XmlTransient
	private ModificationBroker _mBroker;

	@XmlElement(namespace = JobDocumentConstants.DOCUMENT_NAMESPACE, name = "Variables")
	@XmlJavaTypeAdapter(SerializableVariablesAdapter.class)
	private Map<String, VariableDefinition> _variables = new HashMap<String, VariableDefinition>();

	@XmlElement(namespace = JobDocumentConstants.DOCUMENT_NAMESPACE, name = "filesystem-map")
	private FilesystemMap _filesystemMap;

	@XmlElement(namespace = JobDocumentConstants.DOCUMENT_NAMESPACE, name = "job-name")
	private ParameterizableString _jobName;

	@XmlElement(namespace = JobDocumentConstants.DOCUMENT_NAMESPACE, name = "job-projects")
	private ParameterizableStringList _jobProjects;

	@XmlElement(namespace = JobDocumentConstants.DOCUMENT_NAMESPACE, name = "job-annotations")
	private ParameterizableStringList _jobAnnotations;

	@XmlElement(namespace = JobDocumentConstants.DOCUMENT_NAMESPACE, name = "job-description")
	private ParameterizableString _jobDescription;

	@XmlElement(namespace = JobDocumentConstants.DOCUMENT_NAMESPACE, name = "executable")
	private FilesystemAssociatedString _executable;

	@XmlElement(namespace = JobDocumentConstants.DOCUMENT_NAMESPACE, name = "arguments")
	private FilesystemAssociatedStringList _arguments;

	@XmlElement(namespace = JobDocumentConstants.DOCUMENT_NAMESPACE, name = "environment")
	private EnvironmentList _environment;

	@XmlElement(namespace = JobDocumentConstants.DOCUMENT_NAMESPACE, name = "standard-input")
	private FilesystemAssociatedString _standardInput;

	@XmlElement(namespace = JobDocumentConstants.DOCUMENT_NAMESPACE, name = "standard-output")
	private FilesystemAssociatedString _standardOutput;

	@XmlElement(namespace = JobDocumentConstants.DOCUMENT_NAMESPACE, name = "standard-error")
	private FilesystemAssociatedString _standardError;

	@XmlElement(namespace = JobDocumentConstants.DOCUMENT_NAMESPACE, name = "operating-system")
	private OperatingSystemNames _operatingSystem;

	@XmlElement(namespace = JobDocumentConstants.DOCUMENT_NAMESPACE, name = "operating-system-version")
	private ParameterizableString _operatingSystemVersion;

	@XmlElement(namespace = JobDocumentConstants.DOCUMENT_NAMESPACE, name = "processor-architecture")
	private ProcessorArchitecture _processorArchitecture;

	@XmlElement(namespace = JobDocumentConstants.DOCUMENT_NAMESPACE, name = "memory-upper-bound")
	private SizeValue _memoryUpperBound;

	@XmlElement(namespace = JobDocumentConstants.DOCUMENT_NAMESPACE, name = "wallclock-upper-bound")
	private TimeValue _wallClockUpperBound;

	@XmlElement(namespace = JobDocumentConstants.DOCUMENT_NAMESPACE, name = "matching-parameters")
	private MatchingParameterList _matchingParameters;

	@XmlElement(namespace = JobDocumentConstants.DOCUMENT_NAMESPACE, name = "spmd-variation")
	private SPMDVariation _spmdVariation = null;

	@XmlElement(namespace = JobDocumentConstants.DOCUMENT_NAMESPACE, name = "number-of-processes")
	private SettableLong _numberOfProcesses;

	@XmlElement(namespace = JobDocumentConstants.DOCUMENT_NAMESPACE, name = "processes-per-host")
	private SettableLong _processesPerHost;

	@XmlElement(namespace = JobDocumentConstants.DOCUMENT_NAMESPACE, name = "stage-in")
	private StageList _stageIns;

	@XmlElement(namespace = JobDocumentConstants.DOCUMENT_NAMESPACE, name = "stage-out")
	private StageList _stageOuts;

	private JobIdentification generateJobIdentification(XPathBuilder builder,
			Map<String, List<SweepParameter>> variables) {
		String value;
		List<String> values;
		boolean modified = false;

		builder.push(new DefaultXPathNode(JSDLConstants.JSDL_NS,
				"JobIdentification"));
		JobIdentification jobIdent = new JobIdentification();

		builder.push(new DefaultXPathNode(JSDLConstants.JSDL_NS, "JobName"));
		value = JSDLGenerator.generate(_jobName, builder, variables);
		if (value != null) {
			jobIdent.jobName(value);
			modified = true;
		}
		builder.pop();

		builder.push(new DefaultXPathIterableNode(JSDLConstants.JSDL_NS,
				"JobAnnotation"));
		values = JSDLGenerator.generate(_jobAnnotations, builder, variables);
		if (values != null) {
			jobIdent.annotations().addAll(values);
			modified = true;
		}
		builder.pop();

		builder.push(new DefaultXPathIterableNode(JSDLConstants.JSDL_NS,
				"JobProject"));
		values = JSDLGenerator.generate(_jobProjects, builder, variables);
		if (values != null) {
			jobIdent.projects().addAll(values);
			modified = true;
		}
		builder.pop();

		builder.push(new DefaultXPathNode(JSDLConstants.JSDL_NS,
				"JobDescription"));
		value = JSDLGenerator.generate(_jobDescription, builder, variables);
		if (value != null) {
			jobIdent.description(value);
			modified = true;
		}
		builder.pop();

		builder.pop();
		if (modified)
			return jobIdent;

		return null;
	}

	private Application generateApplication(XPathBuilder builder,
			Map<String, List<SweepParameter>> variables,
			Set<FilesystemType> filesystemSet) {
		ApplicationBase app = null;

		builder.push(new DefaultXPathNode(JSDLConstants.JSDL_NS, "Application"));

		if (isSequentialApplication()) {
			app = generatePOSIXApplication(builder, variables, filesystemSet);
		} else
			app = generateSPMDApplication(builder, variables, filesystemSet);

		builder.pop();

		if (app != null)
			return new Application(app);

		return null;
	}

	private Resources generateResources(XPathBuilder builder,
			Map<String, List<SweepParameter>> variables,
			Set<FilesystemType> filesystemSet) {
		Resources resources = null;

		builder.push(new DefaultXPathNode(JSDLConstants.JSDL_NS, "Resources"));

		if (!filesystemSet.isEmpty()) {
			if (resources == null)
				resources = new Resources();

			for (FilesystemType filesystemType : filesystemSet) {
				Filesystem filesystem = _filesystemMap.get(filesystemType);
				resources.filesystems().add(filesystem.toJSDLFilesystem());
			}
		}

		if (_operatingSystem != null || !_operatingSystemVersion.isEmpty()) {
			if (resources == null)
				resources = new Resources();

			OperatingSystem os = new OperatingSystem();

			builder.push(new DefaultXPathNode(JSDLConstants.JSDL_NS,
					"OperatingSystem"));

			if (_operatingSystem != null) {
				builder.push(new DefaultXPathNode(JSDLConstants.JSDL_NS,
						"OperatingSystemType"));
				builder.push(new DefaultXPathNode(JSDLConstants.JSDL_NS,
						"OperatingSystemName"));
				os.osType(new OperatingSystemType(_operatingSystem));
				builder.pop();
				builder.pop();
			}

			if (_operatingSystemVersion != null) {
				builder.push(new DefaultXPathNode(JSDLConstants.JSDL_NS,
						"OperatingSystemVersion"));

				String value = JSDLGenerator.generate(_operatingSystemVersion,
						builder, variables);
				if (value != null)
					os.osVersion(value);

				builder.pop();
			}

			resources.operatingSystem(os);
			builder.pop();
		}

		if (_processorArchitecture != null) {
			if (resources == null)
				resources = new Resources();

			builder.push(new DefaultXPathNode(JSDLConstants.JSDL_NS,
					"CPUArchitecture"));
			builder.push(new DefaultXPathNode(JSDLConstants.JSDL_NS,
					"CPUArchitectureName"));
			resources.cpuArchitecture(new CPUArchitecture(
					_processorArchitecture));
			builder.pop();
			builder.pop();
		}

		if (_memoryUpperBound.value() != null) {
			if (resources == null)
				resources = new Resources();

			builder.push(new DefaultXPathNode(JSDLConstants.JSDL_NS,
					"TotalPhysicalMemory"));
			builder.push(new DefaultXPathNode(JSDLConstants.JSDL_NS,
					"UpperBoundedRange"));

			RangeValue rv = new RangeValue();
			rv.upperBoundedRange(new Boundary(_memoryUpperBound.units()
					.toBytes(_memoryUpperBound.value())));
			resources.totalPhysicalMemory(rv);

			builder.pop();
			builder.pop();
		}

		if (_wallClockUpperBound.value() != null) {
			if (resources == null)
				resources = new Resources();

			builder.push(new DefaultXPathNode(
					"http://vcgr.cs.virginia.edu/jsdl/genii", "WallclockTime"));
			builder.push(new DefaultXPathNode(JSDLConstants.JSDL_NS,
					"UpperBoundedRange"));

			RangeValue rv = new RangeValue();
			rv.upperBoundedRange(new Boundary(_wallClockUpperBound.units()
					.toSeconds(_wallClockUpperBound.value())));
			resources.wallclockTime(rv);

			builder.pop();
			builder.pop();
		}

		if (_numberOfProcesses.value() != null) {
			if (resources == null)
				resources = new Resources();
			builder.push(new DefaultXPathNode(
					"http://vcgr.cs.virginia.edu/jsdl/genii", "TotalCPUCount"));
			builder.push(new DefaultXPathNode(JSDLConstants.JSDL_NS,
					"UpperBoundedRange"));

			RangeValue rv = new RangeValue();
			rv.upperBoundedRange(new Boundary(_numberOfProcesses.value()));
			resources.totalCPUCount(rv);

			builder.pop();
			builder.pop();
		}

		if (_processesPerHost.value() != null) {
			if (resources == null)
				resources = new Resources();
			builder.push(new DefaultXPathNode(
					"http://vcgr.cs.virginia.edu/jsdl/genii",
					"IndividualCPUCount"));
			builder.push(new DefaultXPathNode(JSDLConstants.JSDL_NS,
					"UpperBoundedRange"));

			RangeValue rv = new RangeValue();

			rv.upperBoundedRange(new Boundary(_processesPerHost.value()));
			resources.individualCPUCount(rv);

			builder.pop();
			builder.pop();
		}

		if (_matchingParameters.size() > 0) {
			if (resources == null)
				resources = new Resources();

			builder.push(new DefaultXPathIterableNode(
					"http://vcgr.cs.virginia.edu/jsdl/genii", "property"));

			for (StringStringPair parameter : _matchingParameters) {
				String name;
				String value;

				builder.setAttribute(new DefaultXPathAttributeNode(null, "name"));
				Duple<String, List<VariableInformation>> keyResults = VariableManager
						.findVariables(parameter.name());
				for (VariableInformation info : keyResults.second()) {
					List<SweepParameter> parameters = variables.get(info
							.variable());
					if (parameters == null)
						variables.put(info.variable(),
								parameters = new Vector<SweepParameter>());

					parameters.add(builder.toSubstringParameter(
							info.offset() + 1, info.variable().length()));
				}
				name = keyResults.first();
				builder.clearAttribute();

				builder.setAttribute(new DefaultXPathAttributeNode(null,
						"value"));
				Duple<String, List<VariableInformation>> valueResults = VariableManager
						.findVariables(parameter.value());
				for (VariableInformation info : valueResults.second()) {
					List<SweepParameter> parameters = variables.get(info
							.variable());
					if (parameters == null)
						variables.put(info.variable(),
								parameters = new Vector<SweepParameter>());

					parameters.add(builder.toSubstringParameter(
							info.offset() + 1, info.variable().length()));
				}
				value = valueResults.first();
				builder.clearAttribute();

				resources.matchingParameters().add(
						new MatchingParameter(name, value));

				builder.iterate();
			}

			builder.pop();
		}

		builder.pop();

		return resources;
	}

	private boolean isSequentialApplication() {
		return _spmdVariation == null;
	}

	private POSIXApplication generatePOSIXApplication(XPathBuilder builder,
			Map<String, List<SweepParameter>> variables,
			Set<FilesystemType> filesystemSet) {
		Duple<String, FilesystemType> value;
		List<Duple<String, FilesystemType>> values;
		List<Triple<String, String, FilesystemType>> tValues;
		boolean modified = false;

		POSIXApplication application = new POSIXApplication();

		builder.push(new DefaultXPathNode(JSDLConstants.JSDL_POSIX_NS,
				"POSIXApplication"));

		builder.push(new DefaultXPathNode(JSDLConstants.JSDL_POSIX_NS,
				"Executable"));
		value = JSDLGenerator.generate(_executable, builder, variables);
		if (value != null) {
			FileName name = new FileName(value.first());
			if (JSDLGenerator.indicatesFilesystem(value.second())) {
				name.filesystemName(value.second().jsdlName());
				filesystemSet.add(value.second());
			}

			application.executable(name);
			modified = true;
		}
		builder.pop();

		builder.push(new DefaultXPathNode(JSDLConstants.JSDL_POSIX_NS, "Input"));
		value = JSDLGenerator.generate(_standardInput, builder, variables);
		if (value != null) {
			FileName name = new FileName(value.first());
			if (JSDLGenerator.indicatesFilesystem(value.second())) {
				name.filesystemName(value.second().jsdlName());
				filesystemSet.add(value.second());
			}

			application.input(name);
			modified = true;
		}
		builder.pop();

		builder.push(new DefaultXPathNode(JSDLConstants.JSDL_POSIX_NS, "Output"));
		value = JSDLGenerator.generate(_standardOutput, builder, variables);
		if (value != null) {
			FileName name = new FileName(value.first());
			if (JSDLGenerator.indicatesFilesystem(value.second())) {
				name.filesystemName(value.second().jsdlName());
				filesystemSet.add(value.second());
			}

			application.output(name);
			modified = true;
		}
		builder.pop();

		builder.push(new DefaultXPathNode(JSDLConstants.JSDL_POSIX_NS, "Error"));
		value = JSDLGenerator.generate(_standardError, builder, variables);
		if (value != null) {
			FileName name = new FileName(value.first());
			if (JSDLGenerator.indicatesFilesystem(value.second())) {
				name.filesystemName(value.second().jsdlName());
				filesystemSet.add(value.second());
			}

			application.error(name);
			modified = true;
		}
		builder.pop();

		builder.push(new DefaultXPathIterableNode(JSDLConstants.JSDL_POSIX_NS,
				"Argument"));
		values = JSDLGenerator.generate(_arguments, builder, variables);
		if (values != null) {
			for (Duple<String, FilesystemType> item : values) {
				Argument name = new Argument(item.first());
				if (JSDLGenerator.indicatesFilesystem(item.second())) {
					name.filesystemName(item.second().jsdlName());
					filesystemSet.add(item.second());
				}

				application.arguments().add(name);
				modified = true;
			}
		}
		builder.pop();

		builder.push(new DefaultXPathIterableNode(JSDLConstants.JSDL_POSIX_NS,
				"Environment"));
		tValues = JSDLGenerator.generate(_environment, builder, variables);
		if (tValues != null) {
			for (Triple<String, String, FilesystemType> item : tValues) {
				String name = item.first();
				String val = item.second();
				if (val != null && val.equals(""))
					val = null;

				Environment env = new Environment(name, val);
				if (JSDLGenerator.indicatesFilesystem(item.third())) {
					env.filesystemName(item.third().jsdlName());
					filesystemSet.add(item.third());
				}

				application.environmentVariables().add(env);
				modified = true;
			}
		}
		builder.pop();

		builder.pop();

		if (modified)
			return application;

		return null;
	}

	private SPMDApplication generateSPMDApplication(XPathBuilder builder,
			Map<String, List<SweepParameter>> variables,
			Set<FilesystemType> filesystemSet) {
		Duple<String, FilesystemType> value;
		List<Duple<String, FilesystemType>> values;
		List<Triple<String, String, FilesystemType>> tValues;
		boolean modified = false;

		SPMDApplication application = new SPMDApplication();

		builder.push(new DefaultXPathNode(SPMDConstants.JSDL_SPMD_NS,
				"SPMDApplication"));

		builder.push(new DefaultXPathNode(JSDLConstants.JSDL_POSIX_NS,
				"Executable"));
		value = JSDLGenerator.generate(_executable, builder, variables);
		if (value != null) {
			FileName name = new FileName(value.first());
			if (JSDLGenerator.indicatesFilesystem(value.second())) {
				name.filesystemName(value.second().jsdlName());
				filesystemSet.add(value.second());
			}

			application.executable(name);
			modified = true;
		}
		builder.pop();

		builder.push(new DefaultXPathNode(JSDLConstants.JSDL_POSIX_NS, "Input"));
		value = JSDLGenerator.generate(_standardInput, builder, variables);
		if (value != null) {
			FileName name = new FileName(value.first());
			if (JSDLGenerator.indicatesFilesystem(value.second())) {
				name.filesystemName(value.second().jsdlName());
				filesystemSet.add(value.second());
			}

			application.input(name);
			modified = true;
		}
		builder.pop();

		builder.push(new DefaultXPathNode(JSDLConstants.JSDL_POSIX_NS, "Output"));
		value = JSDLGenerator.generate(_standardOutput, builder, variables);
		if (value != null) {
			FileName name = new FileName(value.first());
			if (JSDLGenerator.indicatesFilesystem(value.second())) {
				name.filesystemName(value.second().jsdlName());
				filesystemSet.add(value.second());
			}

			application.output(name);
			modified = true;
		}
		builder.pop();

		builder.push(new DefaultXPathNode(JSDLConstants.JSDL_POSIX_NS, "Error"));
		value = JSDLGenerator.generate(_standardError, builder, variables);
		if (value != null) {
			FileName name = new FileName(value.first());
			if (JSDLGenerator.indicatesFilesystem(value.second())) {
				name.filesystemName(value.second().jsdlName());
				filesystemSet.add(value.second());
			}

			application.error(name);
			modified = true;
		}
		builder.pop();

		builder.push(new DefaultXPathIterableNode(JSDLConstants.JSDL_POSIX_NS,
				"Argument"));
		values = JSDLGenerator.generate(_arguments, builder, variables);
		if (values != null) {
			for (Duple<String, FilesystemType> item : values) {
				Argument name = new Argument(item.first());
				if (JSDLGenerator.indicatesFilesystem(item.second())) {
					name.filesystemName(item.second().jsdlName());
					filesystemSet.add(item.second());
				}

				application.arguments().add(name);
				modified = true;
			}
		}
		builder.pop();

		builder.push(new DefaultXPathIterableNode(JSDLConstants.JSDL_POSIX_NS,
				"Environment"));
		tValues = JSDLGenerator.generate(_environment, builder, variables);
		if (tValues != null) {
			for (Triple<String, String, FilesystemType> item : tValues) {
				String name = item.first();
				String val = item.second();
				if (val != null && val.equals(""))
					val = null;

				Environment env = new Environment(name, val);
				if (JSDLGenerator.indicatesFilesystem(item.third())) {
					env.filesystemName(item.third().jsdlName());
					filesystemSet.add(item.third());
				}

				application.environmentVariables().add(env);
				modified = true;
			}
		}
		builder.pop();

		/*
		 * We don't actually have to deal with the builder or variables for
		 * these elements as they can't have variables inside of them
		 */
		application.spmdVariation(_spmdVariation.variationURI());
		application.numberOfProcesses(NumberOfProcesses
				.numberOfProccesses(_numberOfProcesses.value()));
		if (_processesPerHost.value() != null)
			application.processesPerHost(new ProcessesPerHost(_processesPerHost
					.value()));
		builder.pop();

		if (modified)
			return application;

		return null;
	}

	private SourceTarget generateSourceTarget(DataStage dataStage,
			boolean isStageIn, XPathBuilder builder,
			Map<String, List<SweepParameter>> variables) {
		builder.push(new DefaultXPathNode(JSDLConstants.JSDL_NS,
				isStageIn ? "Source" : "Target"));
		builder.push(new DefaultXPathNode(JSDLConstants.JSDL_NS, "URI"));

		Duple<String, List<VariableInformation>> results = VariableManager
				.findVariables(dataStage.current().getJSDLURI());
		for (VariableInformation info : results.second()) {
			List<SweepParameter> parameters = variables.get(info.variable());
			if (parameters == null)
				variables.put(info.variable(),
						parameters = new Vector<SweepParameter>());

			parameters.add(builder.toSubstringParameter(info.offset() + 1, info
					.variable().length()));
		}

		builder.pop();
		builder.pop();

		return new SourceTarget(results.first());
	}

	private DataStaging generateSingleDataStage(DataStage dataStage,
			XPathBuilder builder, Map<String, List<SweepParameter>> variables,
			Set<FilesystemType> filesystemSet, boolean isStageIn) {
		String filename = dataStage.filename();

		builder.push(new DefaultXPathNode(JSDLConstants.JSDL_NS, "FileName"));

		Duple<String, List<VariableInformation>> results = VariableManager
				.findVariables(filename);
		for (VariableInformation info : results.second()) {
			List<SweepParameter> parameters = variables.get(info.variable());
			if (parameters == null)
				variables.put(info.variable(),
						parameters = new Vector<SweepParameter>());

			parameters.add(builder.toSubstringParameter(info.offset() + 1, info
					.variable().length()));
		}

		builder.pop();

		DataStaging staging = new DataStaging(results.first(),
				dataStage.creationFlag(), dataStage.deleteOnTerminate());

		FilesystemType fsType = dataStage.filesystemType();
		if (JSDLGenerator.indicatesFilesystem(fsType)) {
			filesystemSet.add(fsType);
			staging.filesystemName(fsType.jsdlName());
		}

		SourceTarget sourceTarget = generateSourceTarget(dataStage, isStageIn,
				builder, variables);
		if (isStageIn)
			staging.source(sourceTarget);
		else
			staging.target(sourceTarget);

		dataStage.current().generateAdditionalJSDL(staging, builder, variables);

		return staging;
	}

	private Collection<DataStaging> generateDataStaging(XPathBuilder builder,
			Map<String, List<SweepParameter>> variables,
			Set<FilesystemType> filesystemSet) {
		Collection<DataStaging> ret = new LinkedList<DataStaging>();

		builder.push(new DefaultXPathIterableNode(JSDLConstants.JSDL_NS,
				"DataStaging"));

		for (DataStage dataStage : _stageIns) {
			ret.add(generateSingleDataStage(dataStage, builder, variables,
					filesystemSet, true));

			builder.iterate();
		}

		for (DataStage dataStage : _stageOuts) {
			ret.add(generateSingleDataStage(dataStage, builder, variables,
					filesystemSet, false));

			builder.iterate();
		}

		builder.pop();
		return ret;
	}

	@Override
	public void postUnmarshall(ParameterizableBroker parameterBroker,
			ModificationBroker modificationBroker) {
		_mBroker = modificationBroker;

		if (_variables == null)
			_variables = new HashMap<String, VariableDefinition>();

		if (_filesystemMap == null)
			_filesystemMap = new FilesystemMap();
		_filesystemMap.addFilesystemListener(new NewFilesystemListener());

		if (_jobName == null)
			_jobName = new ParameterizableString(parameterBroker,
					modificationBroker);

		if (_jobAnnotations == null)
			_jobAnnotations = new ParameterizableStringList(parameterBroker,
					modificationBroker);

		if (_jobProjects == null)
			_jobProjects = new ParameterizableStringList(parameterBroker,
					modificationBroker);

		if (_jobDescription == null)
			_jobDescription = new ParameterizableString(parameterBroker,
					modificationBroker);

		if (_executable == null)
			_executable = new FilesystemAssociatedString(parameterBroker,
					modificationBroker);

		if (_arguments == null)
			_arguments = new FilesystemAssociatedStringList(parameterBroker,
					modificationBroker);

		if (_environment == null)
			_environment = new EnvironmentList(parameterBroker,
					modificationBroker);

		if (_standardInput == null)
			_standardInput = new FilesystemAssociatedString(parameterBroker,
					modificationBroker);

		if (_standardOutput == null)
			_standardOutput = new FilesystemAssociatedString(parameterBroker,
					modificationBroker);

		if (_standardError == null)
			_standardError = new FilesystemAssociatedString(parameterBroker,
					modificationBroker);

		if (_operatingSystemVersion == null)
			_operatingSystemVersion = new ParameterizableString(
					parameterBroker, modificationBroker);

		if (_memoryUpperBound == null)
			_memoryUpperBound = new SizeValue(parameterBroker,
					modificationBroker);

		if (_wallClockUpperBound == null)
			_wallClockUpperBound = new TimeValue(parameterBroker,
					modificationBroker);

		if (_matchingParameters == null)
			_matchingParameters = new MatchingParameterList(parameterBroker,
					modificationBroker);

		if (_numberOfProcesses == null)
			_numberOfProcesses = new SettableLong(parameterBroker,
					modificationBroker);

		if (_processesPerHost == null)
			_processesPerHost = new SettableLong(parameterBroker,
					modificationBroker);

		if (_stageIns == null)
			_stageIns = new StageList(parameterBroker, modificationBroker);

		if (_stageOuts == null)
			_stageOuts = new StageList(parameterBroker, modificationBroker);
	}

	public Map<String, VariableDefinition> variables() {
		return _variables;
	}

	public FilesystemMap filesystemMap() {
		return _filesystemMap;
	}

	public ParameterizableString jobName() {
		return _jobName;
	}

	public ParameterizableStringList jobProjects() {
		return _jobProjects;
	}

	public ParameterizableStringList jobAnnotations() {
		return _jobAnnotations;
	}

	public ParameterizableString jobDescription() {
		return _jobDescription;
	}

	public FilesystemAssociatedString executable() {
		return _executable;
	}

	public FilesystemAssociatedStringList arguments() {
		return _arguments;
	}

	public EnvironmentList environment() {
		return _environment;
	}

	public FilesystemAssociatedString standardInput() {
		return _standardInput;
	}

	public FilesystemAssociatedString standardOutput() {
		return _standardOutput;
	}

	public FilesystemAssociatedString standardError() {
		return _standardError;
	}

	public OperatingSystemNames operatingSystem() {
		return _operatingSystem;
	}

	public void operatingSystem(OperatingSystemNames operatingSystem) {
		if (_operatingSystem != operatingSystem) {
			_operatingSystem = operatingSystem;
			_mBroker.jobDescriptionModified();
		}
	}

	public ParameterizableString operatingSystemVersion() {
		return _operatingSystemVersion;
	}

	public ProcessorArchitecture processorArchitecture() {
		return _processorArchitecture;
	}

	public void processorArchitecture(ProcessorArchitecture arch) {
		if (arch != _processorArchitecture) {
			_processorArchitecture = arch;
			_mBroker.fireJobDescriptionModified();
		}
	}

	public SizeValue memoryUpperBound() {
		return _memoryUpperBound;
	}

	public TimeValue wallclockUpperBound() {
		return _wallClockUpperBound;
	}

	public MatchingParameterList matchingParameters() {
		return _matchingParameters;
	}

	public SPMDVariation spmdVariation() {
		return _spmdVariation;
	}

	public void spmdVariation(SPMDVariation variation) {
		if (variation != _spmdVariation) {
			_spmdVariation = variation;
			_mBroker.fireJobDescriptionModified();
		}
	}

	public SettableLong numberOfProcesses() {
		return _numberOfProcesses;
	}

	public SettableLong processesPerHost() {
		return _processesPerHost;
	}

	public StageList stageIns() {
		return _stageIns;
	}

	public StageList stageOuts() {
		return _stageOuts;
	}

	public Analysis analyze() {
		Analysis analysis = new Analysis();

		if (_executable.isEmpty())
			analysis.addError("Executable cannot be left empty");

		for (StringStringFilesystemTriple var : _environment) {
			String key = var.getKey();
			if (key == null || key.length() == 0)
				analysis.addError("Environment variable name cannot be left empty.");
		}

		for (StringStringPair param : _matchingParameters) {
			String name = param.name();
			String value = param.value();

			if (name == null || name.length() == 0) {
				analysis.addError("Matching Parameter name cannot be left empty.");
				continue;
			}

			if (value == null || value.length() == 0) {
				analysis.addError(
						"Value for matching parameter \"%s\" cannot be left empty.",
						name);
			}
		}

		if (_spmdVariation != null) {
			if (_numberOfProcesses.value() == null) {
				analysis.addError("SPMD Variation chosen without the "
						+ "number of processes indicated.");
			}
		}

		Set<String> stageInFilenames = new HashSet<String>(_stageIns.size());
		Set<String> stageOutFilenames = new HashSet<String>(_stageOuts.size());

		for (DataStage stage : _stageIns) {
			stage.analyze(analysis);
			stageInFilenames.add(stage.filename());
		}

		for (DataStage stage : _stageOuts) {
			stage.analyze(analysis);
			stageOutFilenames.add(stage.filename());
		}

		if (!_standardInput.isEmpty()
				&& !stageInFilenames.contains(_standardInput.get())) {
			analysis.addWarning("No input data stage defined that matches the "
					+ "standard input redirection.  Is this intentional?");
		}

		if (!_standardOutput.isEmpty()
				&& !stageOutFilenames.contains(_standardOutput.get())) {
			analysis.addWarning("No output data stage defined that matches the "
					+ "standard output redirection.  Is this intentional?");
		}

		if (!_standardError.isEmpty()
				&& !stageOutFilenames.contains(_standardError.get())) {
			analysis.addWarning("No output data stage defined that matches the "
					+ "standard error redirection.  Is this intentional?");
		}

		if (!_operatingSystemVersion.isEmpty()) {
			analysis.addWarning("It is recommended that you leave the"
					+ " operating system version empty.");
		}

		for (Map.Entry<String, VariableDefinition> variable : _variables
				.entrySet()) {
			VariableDefinition def = variable.getValue();
			if (def == null
					|| def.type() == VariableDefinitionType.UndefinedVariable)
				analysis.addError("Variable \"%s\" is undefined!",
						variable.getKey());
		}

		return analysis;
	}

	public JobDefinition generate() {
		Map<String, List<SweepParameter>> variables = new HashMap<String, List<SweepParameter>>();
		EnumSet<FilesystemType> filesystemSet = EnumSet
				.noneOf(FilesystemType.class);

		XPathBuilder builder = new XPathBuilder();

		builder.push(new DefaultXPathNode(JSDLConstants.JSDL_NS,
				"JobDefinition"));
		builder.push(new DefaultXPathNode(JSDLConstants.JSDL_NS,
				"JobDescription"));

		JobIdentification jobIdent = generateJobIdentification(builder,
				variables);
		Application application = generateApplication(builder, variables,
				filesystemSet);
		Resources resources = generateResources(builder, variables,
				filesystemSet);

		JobDescription jobDesc = new JobDescription(jobIdent, application,
				resources);

		jobDesc.staging().addAll(
				generateDataStaging(builder, variables, filesystemSet));

		builder.pop();

		JobDefinition jobDef = new JobDefinition(jobDesc);
		builder.pop();

		Sweep root = null;

		if (!variables.isEmpty()) {
			Sweep sweep = null;
			for (Map.Entry<String, List<SweepParameter>> variable : variables
					.entrySet()) {
				Sweep newSweep = new Sweep();
				if (root == null)
					root = newSweep;
				if (sweep != null)
					sweep.addSubSweep(newSweep);
				sweep = newSweep;

				SweepFunction function = _variables.get(variable.getKey())
						.generateFunction();
				SweepAssignment assignment = new SweepAssignment(function);
				for (SweepParameter parameter : variable.getValue())
					assignment.addParameter(parameter);
				sweep.addAssignment(assignment);
			}

			jobDef.parameterSweeps().add(root);
		}

		return jobDef;
	}

	static public JobDocument load(File source,
			ParameterizableBroker parameterBroker,
			ModificationBroker modificationBroker) throws IOException {
		JobDocument result;

		try {
			JAXBContext context = JAXBContext.newInstance(JobDocument.class);
			Unmarshaller unmarshaller = context.createUnmarshaller();
			unmarshaller.setListener(new UnmarshallListener(parameterBroker,
					modificationBroker));
			result = (JobDocument) unmarshaller.unmarshal(source);
			Recents.instance.addRecent(source);
			return result;
		} catch (JAXBException e) {
			throw new IOException(String.format(
					"Unable to load project from file \"%s\".", source), e);
		}
	}

	static public void store(JobDocument document, File target)
			throws IOException {
		try {
			JAXBContext context = JAXBContext.newInstance(JobDocument.class);
			Marshaller marshaller = context.createMarshaller();
			marshaller.marshal(document, target);
			Recents.instance.addRecent(target);
		} catch (JAXBException e) {
			throw new IOException(String.format(
					"Unable to save project to file \"%s\".", target), e);
		}
	}

	static private class NewFilesystemListener implements FilesystemListener {
		@Override
		public void filesystemDefined(FilesystemMap filesystemMap,
				Filesystem newFilesystem) {
			FilesystemType filesystemType = newFilesystem.filesystemType();

			if (filesystemType.canEdit())
				FilesystemRecents.instance(filesystemType).add(newFilesystem);
		}
	}
}
