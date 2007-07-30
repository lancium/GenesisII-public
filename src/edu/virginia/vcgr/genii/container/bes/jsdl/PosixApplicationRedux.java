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
package edu.virginia.vcgr.genii.container.bes.jsdl;

import java.io.File;
import java.util.HashMap;

import javax.xml.namespace.QName;

import org.apache.axis.types.NCName;
import org.ggf.jsdl.posix.Argument_Type;
import org.ggf.jsdl.posix.DirectoryName_Type;
import org.ggf.jsdl.posix.Environment_Type;
import org.ggf.jsdl.posix.FileName_Type;
import org.ggf.jsdl.posix.GroupName_Type;
import org.ggf.jsdl.posix.Limits_Type;
import org.ggf.jsdl.posix.POSIXApplication_Type;
import org.ggf.jsdl.posix.UserName_Type;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.container.jsdl.BaseRedux;
import edu.virginia.vcgr.genii.container.jsdl.IJobPlanProvider;
import edu.virginia.vcgr.genii.container.jsdl.JSDLException;
import edu.virginia.vcgr.genii.container.jsdl.UnsupportedJSDLElement;

public class PosixApplicationRedux extends BaseRedux
{
	private POSIXApplication_Type _posixApplication;
	
	private String _executable;
	private String []_arguments;
	private HashMap<String, String> _environment;
	private String _stdin = null;
	private String _stdout = null;
	private String _stderr = null;
	private String _workingDir = null;
	
	public PosixApplicationRedux(IJobPlanProvider provider, 
		POSIXApplication_Type posixApplication)
	{
		super(provider);
		
		_posixApplication = posixApplication;
	}
	
	public POSIXApplication_Type getPosixApplication()
	{
		return _posixApplication;
	}
	
	public String getExecutable()
	{
		return _executable;
	}
	
	public String[] getArguments()
	{
		return _arguments;
	}
	
	public HashMap<String, String> getEnvironment()
	{
		return _environment;
	}
	
	public File getStdin(File baseDir)
	{
		if (_stdin != null)
			return mergePaths(baseDir, _stdin);
		return null;
	}
	
	public File getStdout(File baseDir)
	{
		if (_stdout != null)
			return mergePaths(baseDir, _stdout);
		return null;
	}
	
	public File getStderr(File baseDir)
	{
		if (_stderr != null)
			return mergePaths(baseDir, _stderr);
		return null;
	}
	
	public File getWorkingDirectory(File baseDir)
	{
		if (_workingDir == null)
			return baseDir;

		return new File(_workingDir);
	}
	
	@Override
	public void consume() throws JSDLException
	{
		if (_posixApplication != null)
		{
			understandArguments(_posixApplication.getArgument());
			understandCoreDumpLimit(_posixApplication.getCoreDumpLimit());
			understandCPUTimeLimit(_posixApplication.getCPUTimeLimit());
			understandDataSegmentLimit(_posixApplication.getDataSegmentLimit());
			understandEnvironment(_posixApplication.getEnvironment());
			understandError(_posixApplication.getError());
			understandExecutable(_posixApplication.getExecutable());
			understandFileSizeLimit(_posixApplication.getFileSizeLimit());
			understandGroupName(_posixApplication.getGroupName());
			understandInput(_posixApplication.getInput());
			understandLockedMemoryLimit(_posixApplication.getLockedMemoryLimit());
			understandMemoryLimit(_posixApplication.getMemoryLimit());
			understandName(_posixApplication.getName());
			understandOpenDescriptorLimit(
				_posixApplication.getOpenDescriptorsLimit());
			understandOutput(_posixApplication.getOutput());
			understandPipeSizeLimit(_posixApplication.getPipeSizeLimit());
			understandProcessCountLimit(_posixApplication.getProcessCountLimit());
			understandStackSizeLimit(_posixApplication.getStackSizeLimit());
			understandThreadCountLimit(_posixApplication.getThreadCountLimit());
			understandUserName(_posixApplication.getUserName());
			understandVirtualMemoryLimit(_posixApplication.getVirtualMemoryLimit());
			understandWallTimeLimit(_posixApplication.getWallTimeLimit());
			understandWorkingDirectory(_posixApplication.getWorkingDirectory());
		}
	}
	
	protected void understandArguments(Argument_Type []arguments)
		throws JSDLException
	{
		if (arguments != null)
		{
			_arguments = new String[arguments.length];
			int lcv = 0;
			for (Argument_Type argument : arguments)
			{
				if (argument.getFilesystemName() != null)
					throw new UnsupportedJSDLElement(
						"filesystemName attribute for \"" +
						toQName("Argument") + 
							"\" element not supported.",
						toQName("Argument"));
				_arguments[lcv++] = argument.get_value().toString();
			}
		}
	}
	
	protected void understandCoreDumpLimit(Limits_Type coreDumpLimit)
		throws JSDLException
	{
		if (coreDumpLimit != null)
			throw new UnsupportedJSDLElement(toQName("CoreDumpLimit"));
	}
	
	protected void understandCPUTimeLimit(Limits_Type cpuTimeLimit)
		throws JSDLException
	{
		if (cpuTimeLimit != null)
			throw new UnsupportedJSDLElement(toQName("CPUTimeLimit"));
	}
	
	protected void understandDataSegmentLimit(Limits_Type dataSegmentLimit)
		throws JSDLException
	{
		if (dataSegmentLimit != null)
			throw new UnsupportedJSDLElement(toQName("DataSegmentLimit"));
	}
	
	protected void understandEnvironment(Environment_Type []environment)
		throws JSDLException
	{
		if (environment != null)
		{
			_environment = new HashMap<String, String>();
			
			for (Environment_Type env : environment)
			{
				if (env.getFilesystemName() != null)
					throw new UnsupportedJSDLElement(
						"filesystemName attribute for \"" +
						toQName("Environment") + 
							"\" element not supported.",
						toQName("Environment"));
				
				_environment.put(env.getName().toString(), env.get_value());
			}
		}
	}
	
	protected void understandError(FileName_Type errorFile)
		throws JSDLException
	{
		if (errorFile != null)
			_stderr = errorFile.get_value();
	}
	
	protected void understandExecutable(FileName_Type executable)
	throws JSDLException
	{
		if (executable != null)
		{
			if (executable.getFilesystemName() != null)
				throw new UnsupportedJSDLElement(
					"filesystemName attribute for \"" +
					toQName("Executable") + 
						"\" element not supported.",
					toQName("Executable"));
			_executable = executable.get_value();
		}
	}
	
	protected void understandFileSizeLimit(Limits_Type fileSizeLimit)
		throws JSDLException
	{
		if (fileSizeLimit != null)
			throw new UnsupportedJSDLElement(toQName("FileSizeLimit"));
	}
	
	protected void understandGroupName(GroupName_Type groupName)
		throws JSDLException
	{
		if (groupName != null)
			throw new UnsupportedJSDLElement(toQName("GroupName"));
	}
	
	protected void understandInput(FileName_Type inputFile)
		throws JSDLException
	{
		if (inputFile != null)
			_stdin = inputFile.get_value();
	}
	
	protected void understandLockedMemoryLimit(Limits_Type lockedMemoryLimit)
		throws JSDLException
	{
		if (lockedMemoryLimit != null)
			throw new UnsupportedJSDLElement(toQName("LockedMemoryLimit"));
	}
	
	protected void understandMemoryLimit(Limits_Type memoryLimit)
		throws JSDLException
	{
		if (memoryLimit != null)
			throw new UnsupportedJSDLElement(toQName("MemoryLimit"));
	}
	
	protected void understandName(NCName name)
		throws JSDLException
	{
		if (name != null)
			throw new UnsupportedJSDLElement(toQName("Name"));
	}
	
	protected void understandOpenDescriptorLimit(Limits_Type openDescriptorLimit)
		throws JSDLException
	{
		if (openDescriptorLimit != null)
			throw new UnsupportedJSDLElement(toQName("OpenDescriptorLimit"));
	}
	
	protected void understandOutput(FileName_Type outputFile)
		throws JSDLException
	{
		if (outputFile != null)
			_stdout = outputFile.get_value();
	}
	
	protected void understandPipeSizeLimit(Limits_Type pipeSizeLimit)
		throws JSDLException
	{
		if (pipeSizeLimit != null)
			throw new UnsupportedJSDLElement(toQName("PipeSizeLimit"));
	}
	
	protected void understandProcessCountLimit(Limits_Type processCountLimit)
		throws JSDLException
	{
		if (processCountLimit != null)
			throw new UnsupportedJSDLElement(toQName("ProcessCountLimit"));
	}
	
	protected void understandStackSizeLimit(Limits_Type stackSizeLimit)
		throws JSDLException
	{
		if (stackSizeLimit != null)
			throw new UnsupportedJSDLElement(toQName("StackSizeLimit"));
	}
	
	protected void understandThreadCountLimit(Limits_Type threadCountLimit)
		throws JSDLException
	{
		if (threadCountLimit != null)
			throw new UnsupportedJSDLElement(toQName("ThreadCountLimit"));
	}
	
	protected void understandUserName(UserName_Type userName)
		throws JSDLException
	{
		if (userName != null)
			throw new UnsupportedJSDLElement(toQName("UserName"));
	}
	
	protected void understandVirtualMemoryLimit(Limits_Type virtualMemoryLimit)
		throws JSDLException
	{
		if (virtualMemoryLimit != null)
			throw new UnsupportedJSDLElement(toQName("VirtualMemoryLimit"));
	}
	
	protected void understandWallTimeLimit(Limits_Type wallTimeLimit)
		throws JSDLException
	{
		if (wallTimeLimit != null)
			throw new UnsupportedJSDLElement(toQName("WallTimeLimit"));
	}
	
	protected void understandWorkingDirectory(DirectoryName_Type workingDirectory)
		throws JSDLException
	{
		if (workingDirectory != null)
			_workingDir = workingDirectory.get_value();
	}
	
	static public QName toQName(String elementName)
	{
		return new QName(GenesisIIConstants.JSDL_POSIX_NS, elementName);
	}
}
