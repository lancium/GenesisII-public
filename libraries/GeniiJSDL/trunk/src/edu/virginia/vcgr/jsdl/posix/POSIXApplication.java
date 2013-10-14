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
package edu.virginia.vcgr.jsdl.posix;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.namespace.QName;

import edu.virginia.vcgr.jsdl.ApplicationBase;
import edu.virginia.vcgr.jsdl.JSDLConstants;

/**
 * @author Mark Morgan (mmm2a@virginia.edu)
 */
@XmlType(propOrder = { "_executable", "_arguments", "_input", "_output", "_error", "_workingDirectory",
	"_environmentVariables", "_wallTimeLimit", "_fileSizeLimit", "_coreDumpLimit", "_dataSegmentLimit", "_lockedMemoryLimit",
	"_memoryLimit", "_openDescriptorsLimit", "_pipeSizeLimit", "_stackSizeLimit", "_cpuTimeLimit", "_processCountLimit",
	"_virtualMemoryLimit", "_threadCountLimit", "_userName", "_groupName" })
@XmlRootElement(namespace = JSDLConstants.JSDL_POSIX_NS, name = "POSIXApplication")
public class POSIXApplication implements ApplicationBase, Serializable
{
	static final long serialVersionUID = 0L;

	@XmlAnyAttribute
	private Map<QName, String> _anyAttributes = new HashMap<QName, String>();

	@XmlAttribute(name = "name", required = false)
	@XmlJavaTypeAdapter(NormalizedStringAdapter.class)
	private String _name = null;

	@XmlElement(namespace = JSDLConstants.JSDL_POSIX_NS, name = "Executable", required = false)
	private FileName _executable = null;

	@XmlElement(namespace = JSDLConstants.JSDL_POSIX_NS, name = "Argument", required = false)
	private List<Argument> _arguments = new Vector<Argument>();

	@XmlElement(namespace = JSDLConstants.JSDL_POSIX_NS, name = "Input", required = false)
	private FileName _input = null;

	@XmlElement(namespace = JSDLConstants.JSDL_POSIX_NS, name = "Output", required = false)
	private FileName _output = null;

	@XmlElement(namespace = JSDLConstants.JSDL_POSIX_NS, name = "Error", required = false)
	private FileName _error = null;

	@XmlElement(namespace = JSDLConstants.JSDL_POSIX_NS, name = "WorkingDirectory", required = false)
	private DirectoryName _workingDirectory = null;

	@XmlElement(namespace = JSDLConstants.JSDL_POSIX_NS, name = "Environment", required = false)
	private List<Environment> _environmentVariables = new Vector<Environment>();

	@XmlElement(namespace = JSDLConstants.JSDL_POSIX_NS, name = "WallTimeLimit", required = false)
	private Limits _wallTimeLimit = null;

	@XmlElement(namespace = JSDLConstants.JSDL_POSIX_NS, name = "FileSizeLimit", required = false)
	private Limits _fileSizeLimit = null;

	@XmlElement(namespace = JSDLConstants.JSDL_POSIX_NS, name = "CoreDumpLimit", required = false)
	private Limits _coreDumpLimit = null;

	@XmlElement(namespace = JSDLConstants.JSDL_POSIX_NS, name = "DataSegmentLimit", required = false)
	private Limits _dataSegmentLimit = null;

	@XmlElement(namespace = JSDLConstants.JSDL_POSIX_NS, name = "LockedMemoryLimit", required = false)
	private Limits _lockedMemoryLimit = null;

	@XmlElement(namespace = JSDLConstants.JSDL_POSIX_NS, name = "MemoryLimit", required = false)
	private Limits _memoryLimit = null;

	@XmlElement(namespace = JSDLConstants.JSDL_POSIX_NS, name = "OpenDescriptorsLimit", required = false)
	private Limits _openDescriptorsLimit = null;

	@XmlElement(namespace = JSDLConstants.JSDL_POSIX_NS, name = "PipeSizeLimit", required = false)
	private Limits _pipeSizeLimit = null;

	@XmlElement(namespace = JSDLConstants.JSDL_POSIX_NS, name = "StackSizeLimit", required = false)
	private Limits _stackSizeLimit = null;

	@XmlElement(namespace = JSDLConstants.JSDL_POSIX_NS, name = "CPUTimeLimit", required = false)
	private Limits _cpuTimeLimit = null;

	@XmlElement(namespace = JSDLConstants.JSDL_POSIX_NS, name = "ProcessCountLimit", required = false)
	private Limits _processCountLimit = null;

	@XmlElement(namespace = JSDLConstants.JSDL_POSIX_NS, name = "VirtualMemoryLimit", required = false)
	private Limits _virtualMemoryLimit = null;

	@XmlElement(namespace = JSDLConstants.JSDL_POSIX_NS, name = "ThreadCountLimit", required = false)
	private Limits _threadCountLimit = null;

	@XmlElement(namespace = JSDLConstants.JSDL_POSIX_NS, name = "UserName", required = false)
	private UserName _userName = null;

	@XmlElement(namespace = JSDLConstants.JSDL_POSIX_NS, name = "GroupName", required = false)
	private GroupName _groupName = null;

	public POSIXApplication(String executable, String... arguments)
	{
		_executable = new FileName(executable);
		for (String arg : arguments)
			_arguments.add(new Argument(arg));
	}

	public POSIXApplication()
	{
	}

	final public void name(String value)
	{
		_name = value;
	}

	final public String name()
	{
		return _name;
	}

	final public void executable(FileName value)
	{
		_executable = value;
	}

	final public FileName executable()
	{
		return _executable;
	}

	final public List<Argument> arguments()
	{
		return _arguments;
	}

	final public void input(FileName value)
	{
		_input = value;
	}

	final public FileName input()
	{
		return _input;
	}

	final public void output(FileName value)
	{
		_output = value;
	}

	final public FileName output()
	{
		return _output;
	}

	final public void error(FileName value)
	{
		_error = value;
	}

	final public FileName error()
	{
		return _error;
	}

	final public void workingDirectory(DirectoryName value)
	{
		_workingDirectory = value;
	}

	final public DirectoryName workingDirectory()
	{
		return _workingDirectory;
	}

	final public List<Environment> environmentVariables()
	{
		return _environmentVariables;
	}

	final public void wallTimeLimit(Limits value)
	{
		_wallTimeLimit = value;
	}

	final public Limits wallTimeLimit()
	{
		return _wallTimeLimit;
	}

	final public void fileSizeLimit(Limits value)
	{
		_fileSizeLimit = value;
	}

	final public Limits fileSizeLimit()
	{
		return _fileSizeLimit;
	}

	final public void coreDumpLimit(Limits value)
	{
		_coreDumpLimit = value;
	}

	final public Limits coreDumpLimit()
	{
		return _coreDumpLimit;
	}

	final public void dataSegmentLimit(Limits value)
	{
		_dataSegmentLimit = value;
	}

	final public Limits dataSegmentLimit()
	{
		return _dataSegmentLimit;
	}

	final public void lockedMemoryLimit(Limits value)
	{
		_lockedMemoryLimit = value;
	}

	final public Limits lockedMemoryLimit()
	{
		return _lockedMemoryLimit;
	}

	final public void memoryLimit(Limits value)
	{
		_memoryLimit = value;
	}

	final public Limits memoryLimit()
	{
		return _memoryLimit;
	}

	final public void openDescriptorsLimit(Limits value)
	{
		_openDescriptorsLimit = value;
	}

	final public Limits openDescriptorsLimit()
	{
		return _openDescriptorsLimit;
	}

	final public void pipeSizeLimit(Limits value)
	{
		_pipeSizeLimit = value;
	}

	final public Limits pipeSizeLimit()
	{
		return _pipeSizeLimit;
	}

	final public void stackSizeLimit(Limits value)
	{
		_stackSizeLimit = value;
	}

	final public Limits stackSizeLimit()
	{
		return _stackSizeLimit;
	}

	final public void cpuTimeLimit(Limits value)
	{
		_cpuTimeLimit = value;
	}

	final public Limits cpuTimeLimit()
	{
		return _cpuTimeLimit;
	}

	final public void processCountLimit(Limits value)
	{
		_processCountLimit = value;
	}

	final public Limits processCountLimit()
	{
		return _processCountLimit;
	}

	final public void virtualMemoryLimit(Limits value)
	{
		_virtualMemoryLimit = value;
	}

	final public Limits virtualMemoryLimit()
	{
		return _virtualMemoryLimit;
	}

	final public void threadCountLimit(Limits value)
	{
		_threadCountLimit = value;
	}

	final public Limits threadCountLimit()
	{
		return _threadCountLimit;
	}

	final public void userName(UserName value)
	{
		_userName = value;
	}

	final public UserName userName()
	{
		return _userName;
	}

	final public void groupName(GroupName value)
	{
		_groupName = value;
	}

	final public GroupName groupName()
	{
		return _groupName;
	}

	final public Map<QName, String> anyAttributes()
	{
		return _anyAttributes;
	}
}
