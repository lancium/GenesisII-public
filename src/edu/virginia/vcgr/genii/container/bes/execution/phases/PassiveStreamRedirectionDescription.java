package edu.virginia.vcgr.genii.container.bes.execution.phases;

import java.io.File;
import java.io.Serializable;

import org.morgan.util.GUID;

public class PassiveStreamRedirectionDescription
	implements Serializable
{
	static final long serialVersionUID = 0L;
	
	private File _stdinSource = null;
	private File _stdoutSink = null;
	private File _stderrSink = null;
	
	public PassiveStreamRedirectionDescription(File stdinSource,
		File stdoutSink, File stderrSink)
	{
		_stdinSource = stdinSource;
		_stdoutSink = stdoutSink;
		_stderrSink = stderrSink;
	}
	
	final public File stdinSource()
	{
		return _stdinSource;
	}
	
	final public File stdoutSink()
	{
		return _stdoutSink;
	}
	
	final public File stderrSink(File workingDirectory)
	{
		if (_stderrSink != null)
			return _stderrSink;
		else
		{
			if (workingDirectory != null)
				return new File(workingDirectory, String.format("%s.stderr",
					new GUID()));
		}
		
		return null;
	}
}