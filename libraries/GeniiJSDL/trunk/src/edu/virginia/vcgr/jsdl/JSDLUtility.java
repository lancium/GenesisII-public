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

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import edu.virginia.vcgr.jsdl.hpc.HPCProfileApplication;
import edu.virginia.vcgr.jsdl.posix.POSIXApplication;
import edu.virginia.vcgr.jsdl.spmd.SPMDApplication;
import edu.virginia.vcgr.jsdl.sweep.Sweep;

/**
 * This utility class provides a singleton JAXBContext that can be used to create JAXB Marshallers
 * and Unmarshallers for XML serializing and deserializing of JSDL documents.
 * 
 * @author Mark Morgan (mmm2a@virginia.edu)
 */
public class JSDLUtility
{
	/**
	 * The singleton JAXBContext that one should by default use to create JSDL Marshallers and
	 * Unmarshallers.
	 */
	static public JAXBContext JSDLContext;

	static {
		try {
			JSDLContext =
				JAXBContext.newInstance(JobDefinition.class, Sweep.class, POSIXApplication.class, HPCProfileApplication.class,
					SPMDApplication.class);
		} catch (JAXBException e) {
			throw new RuntimeException("Unable to configure JSDL JAXB Context.", e);
		}
	}
}