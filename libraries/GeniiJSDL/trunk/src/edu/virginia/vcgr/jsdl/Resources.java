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
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;

import edu.virginia.vcgr.jsdl.rangevalue.RangeValue;

/**
 * @author Mark Morgan (mmm2a@virginia.edu)
 */
@XmlType(propOrder = { "_candidateHosts", "_filesystems",
		"_exclusiveExecution", "_os", "_arch", "_individualCPUSpeed",
		"_individualCPUTime", "_individualCPUCount",
		"_individualNetworkBandwidth", "_individualPhysicalMemory",
		"_individualVirtualMemory", "_individualDiskSpace", "_totalCPUTime",
		"_totalCPUCount", "_totalPhysicalMemory", "_totalVirtualMemory",
		"_totalDiskSpace", "_totalResourceCount", "_wallclockTime",
		"_matchingParameters" })
public class Resources extends CommonJSDLElement implements Serializable {
	static final long serialVersionUID = 0L;

	@XmlElementWrapper(namespace = JSDLConstants.JSDL_NS, name = "CandidateHosts")
	@XmlElement(namespace = JSDLConstants.JSDL_NS, name = "HostName")
	private List<String> _candidateHosts = null;

	@XmlElement(namespace = JSDLConstants.JSDL_NS, name = "FileSystem")
	private List<FileSystem> _filesystems = new LinkedList<FileSystem>();

	@XmlElement(namespace = JSDLConstants.JSDL_NS, name = "ExclusiveExecution")
	private Boolean _exclusiveExecution = null;

	@XmlElement(namespace = JSDLConstants.JSDL_NS, name = "OperatingSystem")
	private OperatingSystem _os = null;

	@XmlElement(namespace = JSDLConstants.JSDL_NS, name = "CPUArchitecture")
	private CPUArchitecture _arch = null;

	@XmlElement(namespace = JSDLConstants.JSDL_NS, name = "IndividualCPUSpeed")
	private RangeValue _individualCPUSpeed = null;

	@XmlElement(namespace = JSDLConstants.JSDL_NS, name = "IndividualCPUTime")
	private RangeValue _individualCPUTime = null;

	@XmlElement(namespace = JSDLConstants.JSDL_NS, name = "IndividualCPUCount")
	private RangeValue _individualCPUCount = null;

	@XmlElement(namespace = JSDLConstants.JSDL_NS, name = "IndividualNetworkBandwidth")
	private RangeValue _individualNetworkBandwidth = null;

	@XmlElement(namespace = JSDLConstants.JSDL_NS, name = "IndividualPhysicalMemory")
	private RangeValue _individualPhysicalMemory = null;

	@XmlElement(namespace = JSDLConstants.JSDL_NS, name = "IndividualVirtualMemory")
	private RangeValue _individualVirtualMemory = null;

	@XmlElement(namespace = JSDLConstants.JSDL_NS, name = "IndividualDiskSpace")
	private RangeValue _individualDiskSpace = null;

	@XmlElement(namespace = JSDLConstants.JSDL_NS, name = "TotalCPUTime")
	private RangeValue _totalCPUTime = null;

	@XmlElement(namespace = JSDLConstants.JSDL_NS, name = "TotalCPUCount")
	private RangeValue _totalCPUCount = null;

	@XmlElement(namespace = JSDLConstants.JSDL_NS, name = "TotalPhysicalMemory")
	private RangeValue _totalPhysicalMemory = null;

	@XmlElement(namespace = JSDLConstants.JSDL_NS, name = "TotalVirtualMemory")
	private RangeValue _totalVirtualMemory = null;

	@XmlElement(namespace = JSDLConstants.JSDL_NS, name = "TotalDiskSpace")
	private RangeValue _totalDiskSpace = null;

	@XmlElement(namespace = JSDLConstants.JSDL_NS, name = "TotalResourceCount")
	private RangeValue _totalResourceCount = null;

	@XmlElement(namespace = "http://vcgr.cs.virginia.edu/jsdl/genii", name = "WallclockTime")
	private RangeValue _wallclockTime = null;

	@XmlElement(namespace = "http://vcgr.cs.virginia.edu/jsdl/genii", name = "property")
	private List<MatchingParameter> _matchingParameters = new Vector<MatchingParameter>();

	public Resources() {
	}

	final public void candidateHosts(List<String> candidateHosts) {
		_candidateHosts = candidateHosts;
	}

	final public List<String> candidateHosts() {
		return _candidateHosts;
	}

	final public List<FileSystem> filesystems() {
		return _filesystems;
	}

	final public void exclusiveExecution(boolean value) {
		_exclusiveExecution = value;
	}

	final public boolean exclusiveExecution() {
		return (_exclusiveExecution == null) ? false : _exclusiveExecution;
	}

	final public void operatingSystem(OperatingSystem os) {
		_os = os;
	}

	final public OperatingSystem operatingSystem() {
		return _os;
	}

	final public void cpuArchitecture(CPUArchitecture arch) {
		_arch = arch;
	}

	final public CPUArchitecture cpuArchitecture() {
		return _arch;
	}

	final public void individualCPUSpeed(RangeValue rangeValue) {
		_individualCPUSpeed = rangeValue;
	}

	final public RangeValue individualCPUSpeed() {
		return _individualCPUSpeed;
	}

	final public void individualCPUTime(RangeValue rangeValue) {
		_individualCPUTime = rangeValue;
	}

	final public RangeValue individualCPUTime() {
		return _individualCPUTime;
	}

	final public void individualCPUCount(RangeValue rangeValue) {
		_individualCPUCount = rangeValue;
	}

	final public RangeValue individualCPUCount() {
		return _individualCPUCount;
	}

	final public void individualNetworkBandwidth(RangeValue rangeValue) {
		_individualNetworkBandwidth = rangeValue;
	}

	final public RangeValue individualNetworkBandwidth() {
		return _individualNetworkBandwidth;
	}

	final public void individualPhysicalMemory(RangeValue rangeValue) {
		_individualPhysicalMemory = rangeValue;
	}

	final public RangeValue individualPhysicalMemory() {
		return _individualPhysicalMemory;
	}

	final public void individualVirtualMemory(RangeValue rangeValue) {
		_individualVirtualMemory = rangeValue;
	}

	final public RangeValue individualVirtualMemory() {
		return _individualVirtualMemory;
	}

	final public void individualDiskSpace(RangeValue rangeValue) {
		_individualDiskSpace = rangeValue;
	}

	final public RangeValue individualDiskSpace() {
		return _individualDiskSpace;
	}

	final public void totalCPUTime(RangeValue rangeValue) {
		_totalCPUTime = rangeValue;
	}

	final public RangeValue totalCPUTime() {
		return _totalCPUTime;
	}

	final public void totalCPUCount(RangeValue rangeValue) {
		_totalCPUCount = rangeValue;
	}

	final public RangeValue totalCPUCount() {
		return _totalCPUCount;
	}

	final public void totalPhysicalMemory(RangeValue rangeValue) {
		_totalPhysicalMemory = rangeValue;
	}

	final public RangeValue totalPhysicalMemory() {
		return _totalPhysicalMemory;
	}

	final public void totalVirtualMemory(RangeValue rangeValue) {
		_totalVirtualMemory = rangeValue;
	}

	final public RangeValue totalVirtualMemory() {
		return _totalVirtualMemory;
	}

	final public void totalDiskSpace(RangeValue rangeValue) {
		_totalDiskSpace = rangeValue;
	}

	final public RangeValue totalDiskSpace() {
		return _totalDiskSpace;
	}

	final public void totalResourceCount(RangeValue rangeValue) {
		_totalResourceCount = rangeValue;
	}

	final public RangeValue totalResourceCount() {
		return _totalResourceCount;
	}

	final public void wallclockTime(RangeValue rangeValue) {
		_wallclockTime = rangeValue;
	}

	final public RangeValue wallclockTime() {
		return _wallclockTime;
	}

	final public List<MatchingParameter> matchingParameters() {
		return _matchingParameters;
	}
}
