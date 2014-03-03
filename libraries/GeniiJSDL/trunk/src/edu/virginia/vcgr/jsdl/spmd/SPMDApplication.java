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
package edu.virginia.vcgr.jsdl.spmd;

import java.io.Serializable;
import java.net.URI;
import java.util.HashMap;
import java.util.LinkedList;
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

import org.w3c.dom.Element;

import edu.virginia.vcgr.jsdl.ApplicationBase;
import edu.virginia.vcgr.jsdl.JSDLConstants;
import edu.virginia.vcgr.jsdl.posix.Argument;
import edu.virginia.vcgr.jsdl.posix.DirectoryName;
import edu.virginia.vcgr.jsdl.posix.Environment;
import edu.virginia.vcgr.jsdl.posix.FileName;
import edu.virginia.vcgr.jsdl.posix.UserName;

/**
 * @author Mark Morgan (mmm2a@virginia.edu)
 */
@XmlRootElement(namespace = SPMDConstants.JSDL_SPMD_NS, name = "SPMDApplication")
@XmlType(propOrder = { "_executable", "_arguments", "_input", "_output", "_error", "_workingDirectory",
	"_environmentVariables", "_userName", "_numberOfProcesses", "_processesPerHost", "_threadsPerProcess", "_spmdVariation" })
public class SPMDApplication implements ApplicationBase, Serializable
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
	private List<Environment> _environmentVariables = new LinkedList<Environment>();

	@XmlElement(namespace = JSDLConstants.JSDL_POSIX_NS, name = "UserName", required = false)
	private UserName _userName = null;

	@XmlElement(namespace = SPMDConstants.JSDL_SPMD_NS, name = "NumberOfProcesses", required = false, nillable = true)
	private NumberOfProcesses _numberOfProcesses = null;

	@XmlElement(namespace = SPMDConstants.JSDL_SPMD_NS, name = "ProcessesPerHost", required = false, nillable = false)
	private ProcessesPerHost _processesPerHost = null;

	@XmlElement(namespace = SPMDConstants.JSDL_SPMD_NS, name = "ThreadsPerProcess", required = false, nillable = true)
	private ThreadsPerProcess _threadsPerProcess = null;

	@XmlElement(namespace = SPMDConstants.JSDL_SPMD_NS, name = "SPMDVariation", required = true, nillable = false)
	private URI _spmdVariation;

	private List<Element> _any = new LinkedList<Element>();

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

	final public void userName(UserName value)
	{
		_userName = value;
	}

	final public UserName userName()
	{
		return _userName;
	}

	final public void numberOfProcesses(NumberOfProcesses numberOfProcesses)
	{
		_numberOfProcesses = numberOfProcesses;
	}

	final public NumberOfProcesses numberOfProcesses()
	{
		return _numberOfProcesses;
	}

	final public void processesPerHost(ProcessesPerHost processesPerHost)
	{
		_processesPerHost = processesPerHost;
	}

	final public ProcessesPerHost processesPerHost()
	{
		return _processesPerHost;
	}

	final public void threadsPerProcess(ThreadsPerProcess threadsPerProcess)
	{
		_threadsPerProcess = threadsPerProcess;
	}

	final public ThreadsPerProcess threadsPerProcess()
	{
		return _threadsPerProcess;
	}

	final public void spmdVariation(URI variation)
	{
		_spmdVariation = variation;
	}

	final public URI spmdVariation()
	{
		return _spmdVariation;
	}

	final public List<Element> any()
	{
		return _any;
	}

	final public Map<QName, String> anyAttributes()
	{
		return _anyAttributes;
	}
}
