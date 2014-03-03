package edu.virginia.vcgr.genii.gjt.data.fs;

import javax.swing.Icon;

import edu.virginia.vcgr.genii.gjt.data.fs.def.DefaultFilesystem;
import edu.virginia.vcgr.genii.gjt.data.fs.def.DefaultFilesystemFactory;
import edu.virginia.vcgr.genii.gjt.data.fs.grid.GridFilesystem;
import edu.virginia.vcgr.genii.gjt.data.fs.grid.GridFilesystemFactory;
import edu.virginia.vcgr.genii.gjt.data.fs.scratch.ScratchFilesystem;
import edu.virginia.vcgr.genii.gjt.data.fs.scratch.ScratchFilesystemFactory;

public enum FilesystemType {
	Default(DefaultFilesystem.COMMON_NAME, DefaultFilesystem.JSDL_NAME, DefaultFilesystem.DESCRIPTION, DefaultFilesystem
		.getIcon(), new DefaultFilesystemFactory(), DefaultFilesystem.CAN_EDIT),
	Grid(GridFilesystem.COMMON_NAME, GridFilesystem.JSDL_NAME, GridFilesystem.DESCRIPTION, GridFilesystem.getIcon(),
		new GridFilesystemFactory(), GridFilesystem.CAN_EDIT),
	Scratch(ScratchFilesystem.COMMON_NAME, ScratchFilesystem.JSDL_NAME, ScratchFilesystem.DESCRIPTION, ScratchFilesystem
		.getIcon(), new ScratchFilesystemFactory(), ScratchFilesystem.CAN_EDIT);

	private String _commonName;
	private String _jsdlName;
	private String _description;
	private boolean _canEdit;
	private Icon _icon;

	private FilesystemFactory _factory;

	private FilesystemType(String commonName, String jsdlName, String description, Icon icon, FilesystemFactory factory,
		boolean canEdit)
	{
		_commonName = commonName;
		_jsdlName = jsdlName;
		_description = description;
		_factory = factory;
		_icon = icon;
		_canEdit = canEdit;
	}

	public String jsdlName()
	{
		return _jsdlName;
	}

	public String description()
	{
		return _description;
	}

	public FilesystemFactory factory()
	{
		return _factory;
	}

	public Icon icon()
	{
		return _icon;
	}

	public boolean canEdit()
	{
		return _canEdit;
	}

	public String toString(FilesystemMap map)
	{
		Filesystem filesystem = map.get(this);
		if (filesystem != null)
			return filesystem.toString();

		return toString();
	}

	@Override
	public String toString()
	{
		return _commonName;
	}
}