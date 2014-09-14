package edu.virginia.vcgr.genii.gjt.data.stage.http;

import javax.xml.bind.annotation.XmlAttribute;

import edu.virginia.vcgr.genii.gjt.data.analyze.Analysis;
import edu.virginia.vcgr.genii.gjt.data.stage.AbstractStageData;
import edu.virginia.vcgr.genii.gjt.data.stage.StageProtocol;

public class HttpStageData extends AbstractStageData
{
	static final public int DEFAULT_HTTP_PORT = 80;

	@XmlAttribute(name = "hostname")
	private String _hostname = null;

	@XmlAttribute(name = "port")
	private int _port = DEFAULT_HTTP_PORT;

	@XmlAttribute(name = "path")
	private String _path = null;

	@Override
	protected void activateImpl()
	{
		fireParameterizableStringModified("", _hostname);
		fireParameterizableStringModified("", _path);
		fireJobDescriptionModified();
	}

	@Override
	protected void deactivateImpl()
	{
		fireParameterizableStringModified(_hostname, "");
		fireParameterizableStringModified(_path, "");
		fireJobDescriptionModified();
	}

	HttpStageData()
	{
		super(StageProtocol.http);
	}

	final String hostname()
	{
		return _hostname;
	}

	final void hostname(String hostname)
	{
		String old = _hostname;
		_hostname = hostname;
		fireParameterizableStringModified(old, hostname);
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

	final String path()
	{
		return _path;
	}

	final void path(String path)
	{
		String old = _path;
		_path = path;

		fireParameterizableStringModified(old, path);
		fireJobDescriptionModified();
	}

	@Override
	public String toString()
	{
		String hostname = _hostname;
		if (hostname == null || hostname.equals(""))
			hostname = "<unknown>";

		String path = _path;
		if (path == null || path.equals(""))
			path = "<unknown>";

		if (_port == DEFAULT_HTTP_PORT)
			return String.format("http://%s/%s", hostname, path);
		else
			return String.format("http://%s:%d/%s", hostname, _port, path);
	}

	@Override
	public void analyze(String filename, Analysis analysis)
	{
		if (_hostname == null || _hostname.equals(""))
			analysis.addError("Data stage for file \"%s\" uses the " + "http protocol without a hostname.", filename);

		if (_path == null || _path.equals(""))
			analysis.addError("Data stage for file \"%s\" uses the " + "http protocol without a path.", filename);
		else if (_path.startsWith("/"))
			analysis.addError("Path for data stage \"%s\" cannot start with /.", filename);
	}

	@Override
	public String getJSDLURI()
	{
		return String.format("http://%s:%d/%s", _hostname, _port, _path);
	}
}