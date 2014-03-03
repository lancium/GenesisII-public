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

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;

import edu.virginia.vcgr.jsdl.OperatingSystemNames;
import edu.virginia.vcgr.jsdl.ProcessorArchitecture;

/**
 * @author Mark Morgan (mmm2a@virginia.edu)
 */
public class Mappings {
	static private Logger _logger = Logger.getLogger(Mappings.class);

	static private Map<String, ProcessorArchitecture> _archMapping;
	static private Map<String, OperatingSystemNames> _osMapping;

	static private Object loadMapping(JAXBContext context, String resourceName) {
		InputStream in = null;

		try {
			in = Mappings.class.getResourceAsStream(resourceName);
			if (in == null)
				throw new FileNotFoundException(String.format(
						"Couldn't find mapping resource \"%s\".", resourceName));

			return context.createUnmarshaller().unmarshal(in);
		} catch (JAXBException e) {
			_logger.error(String.format("Unable to parse mapping file %s.",
					resourceName), e);
		} catch (FileNotFoundException fnfe) {
			_logger.error(
					String.format("Unable to find resource %s.", resourceName),
					fnfe);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (Throwable cause) {
				}
			}
		}

		return null;
	}

	static private ArchitectureMapping loadArchitectureMapping(
			JAXBContext context) {
		return (ArchitectureMapping) loadMapping(context, "arch-map.xml");
	}

	static private OSMapping loadOSMapping(JAXBContext context) {
		return (OSMapping) loadMapping(context, "os-map.xml");
	}

	static {
		try {
			JAXBContext context = JAXBContext.newInstance(
					ArchitectureMapping.class, OSMapping.class);

			ArchitectureMapping aMapping = loadArchitectureMapping(context);
			OSMapping oMapping = loadOSMapping(context);

			if (aMapping == null)
				_archMapping = new HashMap<String, ProcessorArchitecture>();
			else
				_archMapping = aMapping.mapping();

			if (oMapping == null)
				_osMapping = new HashMap<String, OperatingSystemNames>();
			else
				_osMapping = oMapping.mapping();
		} catch (JAXBException e) {
			_logger.error("Unable to create JAXB Context.", e);
		}
	}

	static public Map<String, OperatingSystemNames> osMap() {
		return _osMapping;
	}

	static public Map<String, ProcessorArchitecture> architectureMap() {
		return _archMapping;
	}
}
