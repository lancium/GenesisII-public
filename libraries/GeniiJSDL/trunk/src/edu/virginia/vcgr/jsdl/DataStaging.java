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

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import edu.virginia.vcgr.jsdl.hpcfse.Credential;
import edu.virginia.vcgr.jsdl.hpcfse.HPCFSEConstants;

/**
 * @author Mark Morgan (mmm2a@virginia.edu)
 */
@XmlType(propOrder = { "_filename", "_filesystemName", "_creationFlag",
		"_deleteOnTermination", "_source", "_target", "_credentials" })
public class DataStaging extends CommonJSDLElement {
	static final long serialVersionUID = 0L;

	@XmlAttribute(name = "name", required = false)
	@XmlJavaTypeAdapter(NormalizedStringAdapter.class)
	private String _name;

	@XmlElement(namespace = JSDLConstants.JSDL_NS, name = "FileName", required = true)
	private String _filename;

	@XmlJavaTypeAdapter(NormalizedStringAdapter.class)
	@XmlElement(namespace = JSDLConstants.JSDL_NS, name = "FilesystemName")
	private String _filesystemName;

	@XmlElement(namespace = JSDLConstants.JSDL_NS, name = "CreationFlag", required = true)
	private CreationFlag _creationFlag;

	@XmlElement(namespace = JSDLConstants.JSDL_NS, name = "DeleteOnTermination")
	private Boolean _deleteOnTermination;

	@XmlElement(namespace = JSDLConstants.JSDL_NS, name = "Source")
	private SourceTarget _source;

	@XmlElement(namespace = JSDLConstants.JSDL_NS, name = "Target")
	private SourceTarget _target;

	@XmlElement(namespace = HPCFSEConstants.HPCFSE_NS, name = "Credential")
	private Credential _credentials = null;

	/**
	 * To be used only be xml unmarshalling.
	 */
	@SuppressWarnings("unused")
	private DataStaging() {
	}

	public DataStaging(String name, String filename, String filesystemName,
			CreationFlag creationFlag, boolean deleteOnTermination,
			SourceTarget source, SourceTarget target) {
		if (filename == null)
			throw new IllegalArgumentException("Filename cannot be null.");

		if (creationFlag == null)
			throw new IllegalArgumentException("Creation flag cannot be null.");

		_name = name;
		_filename = filename;
		_filesystemName = filesystemName;
		_creationFlag = creationFlag;
		_deleteOnTermination = deleteOnTermination;
		_source = source;
		_target = target;
	}

	public DataStaging(String filename, String filesystemName,
			CreationFlag creationFlag, boolean deleteOnTermination,
			SourceTarget source, SourceTarget target) {
		this(null, filename, filesystemName, creationFlag, deleteOnTermination,
				source, target);
	}

	public DataStaging(String filename, CreationFlag creationFlag,
			boolean deleteOnTermination, SourceTarget source,
			SourceTarget target) {
		this(filename, null, creationFlag, deleteOnTermination, source, target);
	}

	public DataStaging(String filename, CreationFlag creationFlag,
			boolean deleteOnTerminate) {
		this(filename, creationFlag, deleteOnTerminate, null, null);
	}

	final public void name(String name) {
		_name = name;
	}

	final public String name() {
		return _name;
	}

	final public void filename(String filename) {
		if (filename == null)
			throw new IllegalArgumentException("Filename cannot be null.");

		_filename = filename;
	}

	final public String filename() {
		return _filename;
	}

	final public void filesystemName(String fsName) {
		_filesystemName = fsName;
	}

	final public String filesystemName() {
		return _filesystemName;
	}

	final public void creationFlag(CreationFlag creationFlag) {
		if (creationFlag == null)
			throw new IllegalArgumentException("Creation flag cannot be null.");

		_creationFlag = creationFlag;
	}

	final public CreationFlag creationFlag() {
		return _creationFlag;
	}

	final public void deleteOnTermination(Boolean deleteOnTermination) {
		_deleteOnTermination = deleteOnTermination;
	}

	final public Boolean deleteOnTermionation() {
		return _deleteOnTermination;
	}

	final public void source(SourceTarget source) {
		_source = source;
	}

	final public SourceTarget source() {
		return _source;
	}

	final public void target(SourceTarget target) {
		_target = target;
	}

	final public SourceTarget target() {
		return _target;
	}

	final public void credentials(Credential credentials) {
		_credentials = credentials;
	}

	final public Credential credentials() {
		return _credentials;
	}
}
