package edu.virginia.vcgr.genii.container.jsdl;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class JobRequest implements Serializable
{
	static final long serialVersionUID = 0L;

	private String _jobAnnotation = null;
	private String _jobName = null;

	private Restrictions _restrictions = new Restrictions();

	private SPMDInformation _spmdInformation = null;

	private Map<String, Filesystem> _filesystems = new HashMap<String, Filesystem>();
	private Map<String, FilesystemRelative<String>> _environment = new HashMap<String, FilesystemRelative<String>>();

	private FilesystemRelative<String> _workingDirectory = null;

	private FilesystemRelative<String> _executable = null;
	private List<FilesystemRelative<String>> _arguments = new LinkedList<FilesystemRelative<String>>();

	private FilesystemRelative<String> _stdinRedirect = null;
	private FilesystemRelative<String> _stdoutRedirect = null;
	private FilesystemRelative<String> _stderrRedirect = null;

	private Collection<DataStage> _stageIns = new LinkedList<DataStage>();
	private Collection<DataStage> _stageOuts = new LinkedList<DataStage>();

	final public void setJobName(String jobName)
	{
		_jobName = jobName;
	}

	final public String getJobName()
	{
		return _jobName;
	}

	final public Restrictions getRestrictions()
	{
		return _restrictions;
	}

	final public void setJobAnnotation(String annotation)
	{
		_jobAnnotation = annotation;
	}

	final public String getJobAnnotation()
	{
		return _jobAnnotation;
	}

	final public void setSPMDInformation(SPMDInformation spmdInformation)
	{
		_spmdInformation = spmdInformation;
	}

	final public SPMDInformation getSPMDInformation()
	{
		return _spmdInformation;
	}

	final public void addFilesystem(Filesystem filesystem)
	{
		_filesystems.put(filesystem.getName(), filesystem);
	}

	final public Map<String, Filesystem> getFilesystems()
	{
		return _filesystems;
	}

	final public void addEnvironmentVariable(String variable, FilesystemRelative<String> value)
	{
		_environment.put(variable, value);
	}

	final public Map<String, FilesystemRelative<String>> getEnvironment()
	{
		return _environment;
	}

	final public void setWorkingDirectory(FilesystemRelative<String> workingDirectory)
	{
		_workingDirectory = workingDirectory;
	}

	final public FilesystemRelative<String> getWorkingDirectory()
	{
		return _workingDirectory;
	}

	final public void setExecutable(FilesystemRelative<String> executable)
	{
		_executable = executable;
	}

	final public FilesystemRelative<String> getExecutable()
	{
		return _executable;
	}

	final public void addArgument(FilesystemRelative<String> argument)
	{
		_arguments.add(argument);
	}

	final public List<FilesystemRelative<String>> getArguments()
	{
		return _arguments;
	}

	final public void setStdinRedirect(FilesystemRelative<String> redirect)
	{
		_stdinRedirect = redirect;
	}

	final public FilesystemRelative<String> getStdinRedirect()
	{
		return _stdinRedirect;
	}

	final public void setStdoutRedirect(FilesystemRelative<String> redirect)
	{
		_stdoutRedirect = redirect;
	}

	final public FilesystemRelative<String> getStdoutRedirect()
	{
		return _stdoutRedirect;
	}

	final public void setStderrRedirect(FilesystemRelative<String> redirect)
	{
		_stderrRedirect = redirect;
	}

	final public FilesystemRelative<String> getStderrRedirect()
	{
		return _stderrRedirect;
	}

	final public void addDataStage(DataStage stage)
	{
		if (stage.getSourceURI() != null)
			_stageIns.add(stage);
		if (stage.getTargetURI() != null)
			_stageOuts.add(stage);
	}

	final public Collection<DataStage> getStageIns()
	{
		return _stageIns;
	}

	final public Collection<DataStage> getStageOuts()
	{
		return _stageOuts;
	}
}