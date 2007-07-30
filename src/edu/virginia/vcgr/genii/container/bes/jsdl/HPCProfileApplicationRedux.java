package edu.virginia.vcgr.genii.container.bes.jsdl;

import java.io.File;
import java.util.HashMap;

import javax.xml.namespace.QName;

import org.ggf.jsdl.hpcp.Argument_Type;
import org.ggf.jsdl.hpcp.DirectoryName_Type;
import org.ggf.jsdl.hpcp.Environment_Type;
import org.ggf.jsdl.hpcp.FileName_Type;
import org.ggf.jsdl.hpcp.HPCProfileApplication_Type;
import org.ggf.jsdl.hpcp.UserName_Type;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.container.jsdl.BaseRedux;
import edu.virginia.vcgr.genii.container.jsdl.IJobPlanProvider;
import edu.virginia.vcgr.genii.container.jsdl.JSDLException;
import edu.virginia.vcgr.genii.container.jsdl.UnsupportedJSDLElement;

public class HPCProfileApplicationRedux extends BaseRedux
{
	private HPCProfileApplication_Type _hpcApplication;
	
	private String _executable;
	private String []_arguments;
	private HashMap<String, String> _environment;
	private String _stdin = null;
	private String _stdout = null;
	private String _stderr = null;
	private String _workingDir = null;
	
	public HPCProfileApplicationRedux(IJobPlanProvider provider, 
		HPCProfileApplication_Type hpcApplication)
	{
		super(provider);
		
		_hpcApplication = hpcApplication;
	}
	
	public HPCProfileApplication_Type getPosixApplication()
	{
		return _hpcApplication;
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
			return mergePaths(getWorkingDirectory(baseDir), _stdin);
		return null;
	}
	
	public File getStdout(File baseDir)
	{
		if (_stdout != null)
			return mergePaths(getWorkingDirectory(baseDir), _stdout);
		return null;
	}
	
	public File getStderr(File baseDir)
	{
		if (_stderr != null)
			return mergePaths(getWorkingDirectory(baseDir), _stderr);
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
		if (_hpcApplication != null)
		{
			understandArguments(_hpcApplication.getArgument());
			understandEnvironment(_hpcApplication.getEnvironment());
			understandError(_hpcApplication.getError());
			understandExecutable(_hpcApplication.getExecutable());
			understandInput(_hpcApplication.getInput());
			understandOutput(_hpcApplication.getOutput());
			understandUserName(_hpcApplication.getUserName());
			understandWorkingDirectory(_hpcApplication.getWorkingDirectory());
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
				_arguments[lcv++] = argument.get_value().toString();
			}
		}
	}
	
	protected void understandEnvironment(Environment_Type []environment)
		throws JSDLException
	{
		if (environment != null)
		{
			_environment = new HashMap<String, String>();
			
			for (Environment_Type env : environment)
			{	
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
			_executable = executable.get_value();
		}
	}
	
	protected void understandInput(FileName_Type inputFile)
		throws JSDLException
	{
		if (inputFile != null)
			_stdin = inputFile.get_value();
	}
	
	protected void understandOutput(FileName_Type outputFile)
		throws JSDLException
	{
		if (outputFile != null)
			_stdout = outputFile.get_value();
	}
	
	protected void understandUserName(UserName_Type userName)
		throws JSDLException
	{
		if (userName != null)
			throw new UnsupportedJSDLElement(toQName("UserName"));
	}
	
	protected void understandWorkingDirectory(DirectoryName_Type workingDirectory)
		throws JSDLException
	{
		if (workingDirectory != null)
			_workingDir = workingDirectory.get_value();
	}
	
	static public QName toQName(String elementName)
	{
		return new QName(GenesisIIConstants.JSDL_HPC_NS, elementName);
	}
}