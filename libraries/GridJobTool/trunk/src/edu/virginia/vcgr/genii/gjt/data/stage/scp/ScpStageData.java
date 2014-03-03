package edu.virginia.vcgr.genii.gjt.data.stage.scp;

import javax.xml.bind.annotation.XmlAttribute;

import edu.virginia.vcgr.genii.gjt.data.analyze.Analysis;
import edu.virginia.vcgr.genii.gjt.data.stage.AbstractUsernamePasswordStageData;
import edu.virginia.vcgr.genii.gjt.data.stage.StageProtocol;

public class ScpStageData extends AbstractUsernamePasswordStageData
{
	static final public int DEFAULT_SCP_PORT = 22;

	@XmlAttribute(name = "host")
	private String _host = null;

	@XmlAttribute(name = "path")
	private String _path = null;

	@XmlAttribute(name = "port")
	private int _port = DEFAULT_SCP_PORT;

	@XmlAttribute(name = "is-sftp")
	private boolean _isSFTP = false;

	@Override
	protected void activateImpl()
	{
		fireParameterizableStringModified("", _host);
		fireParameterizableStringModified("", _path);

		fireJobDescriptionModified();
	}

	@Override
	protected void deactivateImpl()
	{
		fireParameterizableStringModified("", _host);
		fireParameterizableStringModified("", _path);

		fireJobDescriptionModified();
	}

	ScpStageData()
	{
		super(StageProtocol.scp);
	}

	final String host()
	{
		return _host;
	}

	final void host(String host)
	{
		String old = _host;
		_host = host;

		fireParameterizableStringModified(old, _host);
		fireJobDescriptionModified();
	}

	final String path()
	{
		return _path;
	}

	final void path(String path)
	{
		String old = _path;
		_path = path;

		fireParameterizableStringModified(old, _path);
		fireJobDescriptionModified();
	}

	final int port()
	{
		return _port;
	}

	final void port(int port)
	{
		_port = port;

		fireJobDescriptionModified();
	}

	final boolean isSFTP()
	{
		return _isSFTP;
	}

	final void isSFTP(boolean isSFTP)
	{
		_isSFTP = isSFTP;

		fireJobDescriptionModified();
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder(_isSFTP ? "sftp://" : "scp://");

		if (_host == null || _host.length() == 0)
			builder.append("<unknown>");
		else
			builder.append(_host);

		if (_port != DEFAULT_SCP_PORT)
			builder.append(":" + _port);

		if (_path == null || _path.length() == 0)
			builder.append("/<unknown>");
		else
			builder.append(_path.startsWith("/") ? _path : ("/" + _path));

		return builder.toString();
	}

	@Override
	public void analyze(String filename, Analysis analysis)
	{
		super.analyze(filename, analysis);

		if (_host == null || _host.length() == 0)
			analysis.addError("Hostname for data stage \"%s\" must be specified.", filename);

		if (_path == null || _path.length() == 0)
			analysis.addError("Path for data stage \"%s\" must be specified.", filename);
		else if (!_path.startsWith("/"))
			analysis.addWarning("Path for data stage \"%s\" will be made absolute", filename);
	}

	@Override
	public String getJSDLURI()
	{
		return toString();
	}
}