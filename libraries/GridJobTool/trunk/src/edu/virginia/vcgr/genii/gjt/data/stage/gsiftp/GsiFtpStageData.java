package edu.virginia.vcgr.genii.gjt.data.stage.gsiftp;

import javax.xml.bind.annotation.XmlAttribute;

import edu.virginia.vcgr.genii.gjt.data.analyze.Analysis;
import edu.virginia.vcgr.genii.gjt.data.stage.AbstractStageData;
import edu.virginia.vcgr.genii.gjt.data.stage.StageProtocol;

public class GsiFtpStageData extends AbstractStageData
{
	static final public int DEFAULT_GSIFTP_PORT = 5821;

	@XmlAttribute(name = "host")
	private String _host = null;

	@XmlAttribute(name = "path")
	private String _path = null;

	@XmlAttribute(name = "port")
	private int _port = DEFAULT_GSIFTP_PORT;

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

	GsiFtpStageData()
	{
		super(StageProtocol.gsiftp);
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

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder("gsiftp://");

		if (_host == null || _host.length() == 0)
			builder.append("<unknown>");
		else
			builder.append(_host);

		if (_port != DEFAULT_GSIFTP_PORT)
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
		if (_host == null || _host.length() == 0)
			analysis.addError("Hostname for data stage \"%s\" must be specified.", filename);

		if (_path == null || _path.length() == 0)
			analysis.addError("Path for data stage \"%s\" must be specified.", filename);
	}

	@Override
	public String getJSDLURI()
	{
		return toString();
	}
}