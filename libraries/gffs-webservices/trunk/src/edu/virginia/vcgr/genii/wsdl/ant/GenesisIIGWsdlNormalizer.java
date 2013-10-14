package edu.virginia.vcgr.genii.wsdl.ant;

import java.io.File;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import edu.virginia.vcgr.genii.wsdl.WsdlDocument;

public class GenesisIIGWsdlNormalizer extends Task
{
	static private final String _SOURCE_FILE_PROPERTY = "source";
	static private final String _TARGET_FILE_PROPERTY = "target";

	protected File _source = null;
	protected File _target = null;

	public void setSource(File source)
	{
		_source = source;
	}

	public void setTarget(File target)
	{
		_target = target;
	}

	public void validate() throws BuildException
	{
		if (_source == null)
			throw new BuildException("\"" + _SOURCE_FILE_PROPERTY + "\" attribute not specified.");
		if (_target == null)
			throw new BuildException("\"" + _TARGET_FILE_PROPERTY + "\" attribute not specified.");
	}

	protected void internalExecute() throws BuildException
	{
		try {
			WsdlDocument doc = new WsdlDocument(_source);
			doc.normalize();
			doc.write(_target);
		} catch (Exception e) {
			throw new BuildException(e.getLocalizedMessage(), e);
		}
	}

	public void execute() throws BuildException
	{
		validate();
		internalExecute();
	}
}