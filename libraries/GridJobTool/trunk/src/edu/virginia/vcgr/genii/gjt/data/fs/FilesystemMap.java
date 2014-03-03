package edu.virginia.vcgr.genii.gjt.data.fs;

import java.awt.Window;
import java.util.Collection;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Vector;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import edu.virginia.vcgr.genii.gjt.data.JobDocumentConstants;

public class FilesystemMap {
	@XmlTransient
	private Collection<FilesystemListener> _listeners = new LinkedList<FilesystemListener>();

	@XmlElement(namespace = JobDocumentConstants.DOCUMENT_NAMESPACE, name = "filesystems")
	@XmlJavaTypeAdapter(FilesystemMapAdapter.class)
	private Map<FilesystemType, Filesystem> _filesystemMap = new EnumMap<FilesystemType, Filesystem>(
			FilesystemType.class);

	protected void fireFilesystemDefined(Filesystem newFilesystem) {
		Collection<FilesystemListener> listeners;

		synchronized (_listeners) {
			listeners = new Vector<FilesystemListener>(_listeners);
		}

		for (FilesystemListener listener : listeners)
			listener.filesystemDefined(this, newFilesystem);
	}

	public FilesystemMap() {
		get(null, FilesystemType.Default);
	}

	public void addFilesystemListener(FilesystemListener listener) {
		synchronized (_listeners) {
			_listeners.add(listener);
		}
	}

	public void removeFilesystemListener(FilesystemListener listener) {
		synchronized (_listeners) {
			_listeners.remove(listener);
		}
	}

	public Filesystem set(Filesystem filesystem) {
		Filesystem ret = _filesystemMap.put(filesystem.filesystemType(),
				filesystem);
		fireFilesystemDefined(filesystem);
		return ret;
	}

	public Filesystem get(FilesystemType type) {
		return _filesystemMap.get(type);
	}

	public Filesystem get(Window owner, FilesystemType type) {
		Filesystem ret = _filesystemMap.get(type);
		if (ret == null) {
			ret = type.factory().instantiate(owner);
			if (ret != null) {
				_filesystemMap.put(type, ret);
				fireFilesystemDefined(ret);
			}
		}

		return ret;
	}
}