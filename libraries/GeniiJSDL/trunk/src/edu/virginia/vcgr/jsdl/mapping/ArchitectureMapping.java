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
package edu.virginia.vcgr.jsdl.mapping;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import edu.virginia.vcgr.jsdl.ProcessorArchitecture;

/**
 * @author Mark Morgan (mmm2a@virginia.edu)
 */
@XmlRootElement(namespace = MappingConstants.GENII_NS, name = "arch-map")
class ArchitectureMapping
{
	@XmlElement(namespace = MappingConstants.GENII_NS, name = "arch-mapping", required = false, nillable = true)
	private List<ArchitectureMappingElement> _mappings = new LinkedList<ArchitectureMappingElement>();

	/**
	 * For use only by XML unmarshalling.
	 */
	private ArchitectureMapping()
	{
	}

	Map<String, ProcessorArchitecture> mapping()
	{
		Map<String, ProcessorArchitecture> map = new HashMap<String, ProcessorArchitecture>(_mappings.size());

		for (ArchitectureMappingElement e : _mappings)
			map.put(e.javaValue(), e.architecture());

		return map;
	}
}
