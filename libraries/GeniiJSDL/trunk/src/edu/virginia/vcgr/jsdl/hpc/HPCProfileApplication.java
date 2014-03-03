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
package edu.virginia.vcgr.jsdl.hpc;

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

/**
 * @author Mark Morgan (mmm2a@virginia.edu)
 */
@XmlType(propOrder = { "_executable", "_arguments", "_input", "_output", "_error", "_workingDirectory",
	"_environmentVariables", "_userName" })
@XmlRootElement(namespace = HPCConstants.HPCPA_NS, name = "HPCProfileApplication")
public class HPCProfileApplication implements ApplicationBase, Serializable
{
	static final long serialVersionUID = 0L;

	@XmlAnyAttribute
	private Map<QName, String> _anyAttributes = new HashMap<QName, String>();

	@XmlAttribute(name = "name", required = false)
	@XmlJavaTypeAdapter(NormalizedStringAdapter.class)
	private String _name = null;

	@XmlElement(namespace = HPCConstants.HPCPA_NS, name = "Executable", required = false)
	private FileName _executable = null;

	@XmlElement(namespace = HPCConstants.HPCPA_NS, name = "Argument", required = false)
	private List<Argument> _arguments = new Vector<Argument>();

	@XmlElement(namespace = HPCConstants.HPCPA_NS, name = "Input", required = false)
	private FileName _input = null;

	@XmlElement(namespace = HPCConstants.HPCPA_NS, name = "Output", required = false)
	private FileName _output = null;

	@XmlElement(namespace = HPCConstants.HPCPA_NS, name = "Error", required = false)
	private FileName _error = null;

	@XmlElement(namespace = HPCConstants.HPCPA_NS, name = "WorkingDirectory", required = false)
	private DirectoryName _workingDirectory = null;

	@XmlElement(namespace = HPCConstants.HPCPA_NS, name = "Environment", required = false)
	private List<Environment> _environmentVariables = new Vector<Environment>();

	@XmlElement(namespace = HPCConstants.HPCPA_NS, name = "UserName", required = false)
	private UserName _userName = null;

	public HPCProfileApplication(String executable, String... arguments)
	{
		_executable = new FileName(executable);
		for (String arg : arguments)
			_arguments.add(new Argument(arg));
	}

	public HPCProfileApplication()
	{
	}

	final public void name(String name)
	{
		_name = name;
	}

	final public String name()
	{
		return _name;
	}

	final public void executable(FileName executable)
	{
		_executable = executable;
	}

	final public FileName executable()
	{
		return _executable;
	}

	final public List<Argument> arguments()
	{
		return _arguments;
	}

	final public void input(FileName input)
	{
		_input = input;
	}

	final public FileName input()
	{
		return _input;
	}

	final public void output(FileName output)
	{
		_output = output;
	}

	final public FileName output()
	{
		return _output;
	}

	final public void error(FileName error)
	{
		_error = error;
	}

	final public FileName error()
	{
		return _error;
	}

	final public void workingDirectory(DirectoryName workingDirectory)
	{
		_workingDirectory = workingDirectory;
	}

	final public DirectoryName workingDirectory()
	{
		return _workingDirectory;
	}

	final public List<Environment> environmentVariables()
	{
		return _environmentVariables;
	}

	final public void userName(UserName userName)
	{
		_userName = userName;
	}

	final public UserName userName()
	{
		return _userName;
	}

	final public Map<QName, String> anyAttributes()
	{
		return _anyAttributes;
	}
}
