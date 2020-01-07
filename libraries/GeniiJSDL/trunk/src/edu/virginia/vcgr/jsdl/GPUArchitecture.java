/*
 * This code was developed by Vanamala Venkataswamy (vv3xu@virginia.edu) at the University of Virginia and is an implementation of JSDL, JSDL
 * ParameterSweep and other JSDL related specifications from the OGF.
 * 
 * Copyright 2010 University of Virginia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package edu.virginia.vcgr.jsdl;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlElement;

/**
 * @author Vanamala Venkataswamyn (vv3xu@virginia.edu)
 */
public class GPUArchitecture extends CommonJSDLElement implements Serializable
{
	static final long serialVersionUID = 0L;

	@XmlElement(namespace = JSDLConstants.JSDL_NS, name = "GPUArchitectureName", required = true)
	private GPUArchitecture _gpuArchitecture;

	/**
	 * Only to be used by XML unmarshalling.
	 */
	@SuppressWarnings("unused")
	private GPUArchitecture()
	{
	}

	public GPUArchitecture(GPUArchitecture gpuArchitecture)
	{
		if (gpuArchitecture == null)
			throw new IllegalArgumentException("GPUArchitecture cannot be null.");

		_gpuArchitecture = gpuArchitecture;
	}

	final public void gpuArchitecture(GPUArchitecture gpuArchitecture)
	{
		_gpuArchitecture = gpuArchitecture;
	}

	final public GPUArchitecture getGpuProcessorArchitecture()
	{
		return _gpuArchitecture;
	}
}
