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
import javax.xml.bind.annotation.XmlElement;

/**
 * @author Mark Morgan (mmm2a@virginia.edu)
 */
public class CPUArchitecture extends CommonJSDLElement implements Serializable {
	static final long serialVersionUID = 0L;

	@XmlElement(namespace = JSDLConstants.JSDL_NS, name = "CPUArchitectureName", required = true)
	private ProcessorArchitecture _processorArchitecture;

	/**
	 * Only to be used by XML unmarshalling.
	 */
	@SuppressWarnings("unused")
	private CPUArchitecture() {
	}

	public CPUArchitecture(ProcessorArchitecture processorArchitecture) {
		if (processorArchitecture == null)
			throw new IllegalArgumentException(
					"ProcessorArchitecture cannot be null.");

		_processorArchitecture = processorArchitecture;
	}

	final public void processorArchitecture(
			ProcessorArchitecture processorArchitecture) {
		_processorArchitecture = processorArchitecture;
	}

	final public ProcessorArchitecture processorArchitecture() {
		return _processorArchitecture;
	}
}
