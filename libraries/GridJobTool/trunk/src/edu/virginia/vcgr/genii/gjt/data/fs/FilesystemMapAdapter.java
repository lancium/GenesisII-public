package edu.virginia.vcgr.genii.gjt.data.fs;

import java.util.Map;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class FilesystemMapAdapter extends
		XmlAdapter<SerializableFilesystemMap, Map<FilesystemType, Filesystem>> {
	@Override
	public SerializableFilesystemMap marshal(Map<FilesystemType, Filesystem> v)
			throws Exception {
		return new SerializableFilesystemMap(v);
	}

	@Override
	public Map<FilesystemType, Filesystem> unmarshal(SerializableFilesystemMap v)
			throws Exception {
		return v.createMap();
	}
}