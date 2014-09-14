package edu.virginia.vcgr.genii.gjt.data.stage.grid;

import javax.xml.bind.annotation.XmlAttribute;

import edu.virginia.vcgr.genii.gjt.data.analyze.Analysis;
import edu.virginia.vcgr.genii.gjt.data.stage.AbstractStageData;
import edu.virginia.vcgr.genii.gjt.data.stage.StageProtocol;

public class GridStageData extends AbstractStageData
{
	@XmlAttribute(name = "path")
	private String _path = null;

	@Override
	protected void activateImpl()
	{
		fireParameterizableStringModified("", _path);
		fireJobDescriptionModified();
	}

	@Override
	protected void deactivateImpl()
	{
		fireParameterizableStringModified(_path, "");
		fireJobDescriptionModified();
	}

	GridStageData()
	{
		super(StageProtocol.grid);
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
		return String.format("grid://%s", ((_path == null) || (_path.equals(""))) ? "<unknown>" : _path);
	}

	@Override
	public void analyze(String filename, Analysis analysis)
	{
		if (_path == null || _path.equals(""))
			analysis.addError("Data stage for file \"%s\" does not specify a path.", filename);
		else if (!_path.startsWith("/"))
			analysis.addWarning("Data stage for file \"%s\" uses a relative" + " path which is discouraged.", filename);
	}

	@Override
	public String getJSDLURI()
	{
		return String.format("rns:%s", _path);
	}
}