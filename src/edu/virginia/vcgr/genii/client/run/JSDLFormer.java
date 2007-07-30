package edu.virginia.vcgr.genii.client.run;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.axis.types.NCName;
import org.apache.axis.types.URI;
import org.ggf.jsdl.Application_Type;
import org.ggf.jsdl.CreationFlagEnumeration;
import org.ggf.jsdl.DataStaging_Type;
import org.ggf.jsdl.JobDefinition_Type;
import org.ggf.jsdl.JobDescription_Type;
import org.ggf.jsdl.JobIdentification_Type;
import org.ggf.jsdl.SourceTarget_Type;
import org.ggf.jsdl.posix.Argument_Type;
import org.ggf.jsdl.posix.Environment_Type;
import org.ggf.jsdl.posix.FileName_Type;
import org.ggf.jsdl.posix.POSIXApplication_Type;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.ser.AnyHelper;
import edu.virginia.vcgr.genii.client.ser.ObjectSerializer;

public class JSDLFormer
{
	private String _jobName = null;
	private List<String> _commandLine = null;
	
	private String _stdout = null;
	private String _stderr = null;
	private String _stdin = null;
	
	private Map<String, String> _environmentVariables =
		new HashMap<String, String>();
	
	private Map<String, URI> _stageIn =
		new HashMap<String, URI>();
	private Map<String, URI> _stageOut =
		new HashMap<String, URI>();
	
	public JSDLFormer(String jobName, List<String> commandLine)
	{
		if (commandLine == null || commandLine.size() == 0)
			throw new IllegalArgumentException(
				"\"commandLine\" parameter cannot be null or empty.");
		
		if (jobName == null)
		{
			jobName = commandLine.get(0);
			int index = jobName.lastIndexOf('/');
			if (index >= 0)
				jobName = jobName.substring(index + 1);
			index = jobName.lastIndexOf('\\');
			if (index >= 0)
				jobName = jobName.substring(index + 1);
		}
		
		_jobName = jobName;
		_commandLine = new ArrayList<String>(commandLine);
	}
	
	public JSDLFormer(List<String> commandLine)
	{
		this(null, commandLine);
	}
	
	public void redirectStdout(String targetFileName)
	{
		_stdout = targetFileName;
	}
	
	public void redirectStderr(String targetFileName)
	{
		_stderr = targetFileName;
	}
	
	public void redirectStdin(String sourceFileName)
	{
		_stdin = sourceFileName;
	}
	
	public Map<String, String> environment()
	{
		return _environmentVariables;
	}
	
	public Map<String, URI> inDataStages()
	{
		return _stageIn;
	}
	
	public Map<String, URI> outDataStages()
	{
		return _stageOut;
	}
	
	public JobDefinition_Type formJSDL()
	{
		JobIdentification_Type jobIdentification;
		Application_Type application;
		DataStaging_Type []dataStages;
		POSIXApplication_Type posixApplication;
		
		jobIdentification = new JobIdentification_Type(_jobName,
			"Automatically generated JSDL document by Genesis II system.",
			null, null, null);
		
		posixApplication = createPOSIXApplication();
		application = new Application_Type(
			_commandLine.get(0), null, null,
			AnyHelper.toAnyArray(posixApplication));
		application.get_any()[0].setQName(new QName(
			GenesisIIConstants.JSDL_POSIX_NS, "POSIXApplication"));
		
		dataStages = createDataStages();
		
		return new JobDefinition_Type(
			new JobDescription_Type(
				jobIdentification, application, null, dataStages, null), 
			null, null);
	}
	
	public void writeJSDL(OutputStream out) throws IOException
	{
		JobDefinition_Type jobDef = formJSDL();
		Writer writer = new OutputStreamWriter(out);
		ObjectSerializer.serialize(writer, jobDef,
			new QName(GenesisIIConstants.GENESISII_NS, "jsdl-document"));
		writer.flush();
	}
	
	private POSIXApplication_Type createPOSIXApplication()
	{
		FileName_Type executable = new FileName_Type(_commandLine.get(0));
		Argument_Type []arguments = new Argument_Type[_commandLine.size() - 1];
		for (int lcv = 0; lcv < arguments.length; lcv++)
			arguments[lcv] = new Argument_Type(_commandLine.get(lcv + 1));
		FileName_Type stdin = (_stdin != null) ? new FileName_Type(_stdin) : null;
		FileName_Type stdout = (_stdout != null) ? new FileName_Type(_stdout) : null;
		FileName_Type stderr = (_stderr != null) ? new FileName_Type(_stderr) : null;
		
		Environment_Type []envOverload = null;
		
		if (_environmentVariables.size() > 0)
		{
			envOverload = new Environment_Type[_environmentVariables.size()];
			int lcv = 0;
			for (String key : _environmentVariables.keySet())
			{
				Environment_Type env = new Environment_Type(
					_environmentVariables.get(key));
				env.setName(new NCName(key));
				envOverload[lcv++] = env;
			}
		}
		
		return new POSIXApplication_Type(
			executable, arguments, stdin, stdout, stderr, null, envOverload,
			null, null, null, null, null, null, null,
			null, null, null, null, null, null, null, null, null);
	}
	
	private DataStaging_Type[] createDataStages()
	{
		HashMap<String, DataStaging_Type> stages =
			new HashMap<String, DataStaging_Type>();
			
		for (String stage : _stageIn.keySet())
		{
			stages.put(stage, new DataStaging_Type(
				stage, null, CreationFlagEnumeration.overwrite,
				Boolean.TRUE,
				new SourceTarget_Type(_stageIn.get(stage), null),
				null, null, null));
		}
		
		for (String stage : _stageOut.keySet())
		{
			DataStaging_Type stageElement = stages.get(stage);
			if (stageElement == null)
			{
				stages.put(stage, new DataStaging_Type(
					stage, null, CreationFlagEnumeration.overwrite,
					Boolean.TRUE, null, 
					new SourceTarget_Type(_stageOut.get(stage), null), null, null));
			} else
			{
				stageElement.setTarget(
					new SourceTarget_Type(_stageOut.get(stage), null));
			}
		}
		
		return stages.values().toArray(new DataStaging_Type[0]);
	}
}