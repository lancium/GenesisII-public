package edu.virginia.vcgr.genii.gjt.data.fs;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;

import edu.virginia.vcgr.genii.gjt.data.JobDocumentConstants;
import edu.virginia.vcgr.genii.gjt.data.fs.def.DefaultFilesystem;
import edu.virginia.vcgr.genii.gjt.data.fs.grid.GridFilesystem;
import edu.virginia.vcgr.genii.gjt.data.fs.scratch.ScratchFilesystem;

public class SerializableFilesystemMap
{
	@XmlElements({
		@XmlElement(namespace = JobDocumentConstants.DOCUMENT_NAMESPACE, name = "default-filesystem",
			type = DefaultFilesystem.class),
		@XmlElement(namespace = JobDocumentConstants.DOCUMENT_NAMESPACE, name = "grid-filesystem", type = GridFilesystem.class),
		@XmlElement(namespace = JobDocumentConstants.DOCUMENT_NAMESPACE, name = "scratch-filesystem",
			type = ScratchFilesystem.class) })
	private List<Filesystem> _filesystems;

	public SerializableFilesystemMap(Map<FilesystemType, Filesystem> map)
	{
		_filesystems = new Vector<Filesystem>(map.size());
		for (FilesystemType type : FilesystemType.values()) {
			Filesystem fs = map.get(type);
			if (fs != null)
				_filesystems.add(fs);
		}
	}

	public SerializableFilesystemMap()
	{
		_filesystems = new Vector<Filesystem>();
	}

	public Map<FilesystemType, Filesystem> createMap()
	{
		Map<FilesystemType, Filesystem> ret = new EnumMap<FilesystemType, Filesystem>(FilesystemType.class);

		for (Filesystem fs : _filesystems)
			ret.put(fs.filesystemType(), fs);

		return ret;
	}
}