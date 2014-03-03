package edu.virginia.vcgr.genii.gjt.data.fs;

import java.awt.Window;

import javax.xml.bind.annotation.XmlSeeAlso;

import edu.virginia.vcgr.genii.gjt.data.fs.def.DefaultFilesystem;
import edu.virginia.vcgr.genii.gjt.data.fs.grid.GridFilesystem;
import edu.virginia.vcgr.genii.gjt.data.fs.scratch.ScratchFilesystem;
import edu.virginia.vcgr.jsdl.FileSystem;

@XmlSeeAlso({ DefaultFilesystem.class, GridFilesystem.class,
		ScratchFilesystem.class })
public interface Filesystem extends Cloneable {
	public FilesystemType filesystemType();

	public boolean edit(Window owner);

	public Object clone();

	public FileSystem toJSDLFilesystem();
}