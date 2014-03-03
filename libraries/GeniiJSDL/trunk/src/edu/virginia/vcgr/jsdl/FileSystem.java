/*
 * This code was developed by Mark Morgan (mmm2a@virginia.edu) at the University of Virginia and is
 * an implementation of JSDL, JSDL ParameterSweep and other JSDL related specifications from the
 * OGF.
 * 
 * Copyright 2010 University of Virginia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package edu.virginia.vcgr.jsdl;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import edu.virginia.vcgr.jsdl.rangevalue.RangeValue;

/**
 * @author Mark Morgan (mmm2a@virginia.edu)
 */
@XmlType(propOrder = { "_fsType", "_description", "_mountPoint", "_diskSpace" })
public class FileSystem extends CommonJSDLElement implements Serializable {
	static final long serialVersionUID = 0L;

	@XmlAttribute(name = "name", required = true)
	@XmlJavaTypeAdapter(NormalizedStringAdapter.class)
	private String _name;

	@XmlElement(namespace = JSDLConstants.JSDL_NS, name = "FileSystemType")
	private FileSystemType _fsType;

	@XmlElement(namespace = JSDLConstants.JSDL_NS, name = "Description")
	private String _description;

	@XmlElement(namespace = JSDLConstants.JSDL_NS, name = "MountPoint")
	private String _mountPoint = null;

	@XmlElement(namespace = JSDLConstants.JSDL_NS, name = "DiskSpace")
	private RangeValue _diskSpace = null;

	/**
	 * Only to be used by the XML Unmarshaller
	 */
	@SuppressWarnings("unused")
	private FileSystem() {
	}

	public FileSystem(String name, FileSystemType fsType) {
		if (name == null)
			throw new IllegalArgumentException(
					"Not allowed to have a null name.");

		_name = name;
		_fsType = fsType;
	}

	public FileSystem(String name) {
		this(name, null);
	}

	final public void name(String name) {
		if (name == null)
			throw new IllegalArgumentException(
					"Not allowed to have a null name.");

		_name = name;
	}

	final public String name() {
		return _name;
	}

	final public void fsType(FileSystemType fsType) {
		_fsType = fsType;
	}

	final public FileSystemType fsType() {
		return _fsType;
	}

	final public void description(String description) {
		_description = description;
	}

	final public String description() {
		return _description;
	}

	final public void mountPoint(String mountPoint) {
		_mountPoint = mountPoint;
	}

	final public String mountPoint() {
		return _mountPoint;
	}

	final public void diskSpace(RangeValue range) {
		_diskSpace = range;
	}

	final public RangeValue diskSpace() {
		return _diskSpace;
	}
}
