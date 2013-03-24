/*
 * Copyright 2006 University of Virginia
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package edu.virginia.vcgr.genii.client.jsdl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.jsdl.Boundary_Type;
import org.ggf.jsdl.CPUArchitecture_Type;
import org.ggf.jsdl.Exact_Type;
import org.ggf.jsdl.JobDefinition_Type;
import org.ggf.jsdl.OperatingSystemTypeEnumeration;
import org.ggf.jsdl.OperatingSystemType_Type;
import org.ggf.jsdl.OperatingSystem_Type;
import org.ggf.jsdl.ProcessorArchitectureEnumeration;
import org.ggf.jsdl.RangeValue_Type;
import org.ggf.jsdl.Range_Type;
import org.w3c.dom.Element;

import edu.virginia.vcgr.appmgr.os.OperatingSystemType;
import edu.virginia.vcgr.genii.client.jni.JNIContainerBaseClass;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.ser.ObjectDeserializer;
import edu.virginia.vcgr.genii.client.ser.ObjectSerializer;
import edu.virginia.vcgr.jsdl.JSDLConstants;
import edu.virginia.vcgr.jsdl.JSDLUtility;
import edu.virginia.vcgr.jsdl.JobDefinition;

public class JSDLUtils extends JNIContainerBaseClass
{
	static private Log _logger = LogFactory.getLog(JSDLUtils.class);

	static public CPUArchitecture_Type getLocalCPUArchitecture()
	{
		String arch = System.getProperty("os.arch");
		if (arch != null) {
			if (_logger.isDebugEnabled())
				_logger.debug("Determined that the local CPU Type is \"" + arch + "\".");
			if (arch.equals(ProcessorArchitectureEnumeration._x86))
				return new CPUArchitecture_Type(ProcessorArchitectureEnumeration.x86, null);
			else if (arch.equals("i386"))
				return new CPUArchitecture_Type(ProcessorArchitectureEnumeration.x86, null);
			else if (arch.equals(ProcessorArchitectureEnumeration._x86_64))
				return new CPUArchitecture_Type(ProcessorArchitectureEnumeration.x86, null);
			// this clause was added because we often are told the arch is amd64, but the
			// arch enum from the org.ggf.jsdl spec doesn't have that listed.
			else if (arch.equals("amd64"))
				return new CPUArchitecture_Type(ProcessorArchitectureEnumeration.x86, null);
		}

		return new CPUArchitecture_Type(ProcessorArchitectureEnumeration.other, null);
	}

	static public OperatingSystem_Type getLocalOperatingSystem()
	{
		return new OperatingSystem_Type(getLocalOperatingSystemType(), System.getProperty("os.version"), null, null);

	}

	static public OperatingSystemType_Type getLocalOperatingSystemType()
	{
		OperatingSystemType os = OperatingSystemType.getCurrent();

		if (_logger.isDebugEnabled())
			_logger.debug("Determined that the local OS Type is \"" + os + "\".");
		if (os.equals(OperatingSystemType.Windows_7) || os.equals(OperatingSystemType.Windows_VISTA)) {
			if (_logger.isDebugEnabled())
				_logger.debug("Considering " + os + " as roughly equivalent to XP.");
			os = OperatingSystemType.Windows_XP;
		}
		return new OperatingSystemType_Type(OperatingSystemTypeEnumeration.fromString(os.name()), null);
	}

	static public boolean satisfiesRange(double value, RangeValue_Type rangeValue)
	{
		Boundary_Type boundaryType;
		Boolean bool;

		boundaryType = rangeValue.getUpperBoundedRange();
		if (boundaryType != null) {
			bool = boundaryType.getExclusiveBound();
			if (bool != null && bool.booleanValue()) {
				if (value < boundaryType.get_value())
					return true;
			} else {
				if (value <= boundaryType.get_value())
					return true;
			}
		}

		boundaryType = rangeValue.getLowerBoundedRange();
		if (boundaryType != null) {
			bool = boundaryType.getExclusiveBound();
			if (bool != null && bool.booleanValue()) {
				if (value > boundaryType.get_value())
					return true;
			} else {
				if (value >= boundaryType.get_value())
					return true;
			}
		}

		Exact_Type[] exacts = rangeValue.getExact();
		if (exacts != null) {
			for (Exact_Type exact : exacts) {
				Double eps = exact.getEpsilon();
				double epsilon = 0.0;
				if (eps != null)
					epsilon = eps.doubleValue();

				double lower = exact.get_value() - epsilon;
				double upper = exact.get_value() + epsilon;

				return (value >= lower && value <= upper);
			}
		}

		Range_Type[] ranges = rangeValue.getRange();
		if (ranges != null) {
			Boundary_Type lower;
			Boundary_Type upper;

			for (Range_Type range : ranges) {
				lower = range.getLowerBound();
				upper = range.getUpperBound();

				if (lower == null || upper == null)
					continue;

				Boolean exclusive = lower.getExclusiveBound();
				if (exclusive != null && exclusive.booleanValue()) {
					if (value <= lower.get_value())
						continue;
				} else {
					if (value < lower.get_value())
						continue;
				}

				exclusive = upper.getExclusiveBound();
				if (exclusive != null && exclusive.booleanValue()) {
					if (value >= upper.get_value())
						continue;
				} else {
					if (value > upper.get_value())
						continue;
				}

				return true;
			}
		}

		return false;
	}

	static public boolean satisfiesOS(OperatingSystem_Type myOS, OperatingSystem_Type targetOS)
	{
		OperatingSystemType_Type myType = myOS.getOperatingSystemType();
		OperatingSystemType_Type targetType = targetOS.getOperatingSystemType();

		if (targetType != null) {
			if (myType == null)
				return false;

			if (!targetType.getOperatingSystemName().equals(myType.getOperatingSystemName()))
				return false;

			String myVersion = myOS.getOperatingSystemVersion();
			String targetVersion = targetOS.getOperatingSystemVersion();

			if (targetVersion != null) {
				if (myVersion == null)
					return false;

				return myVersion.equals(targetVersion);
			}
		}

		return true;
	}

	static public JobDefinition convert(JobDefinition_Type axisType) throws ResourceException, JAXBException
	{
		Element element = ObjectSerializer.toElement(axisType, new QName(JSDLConstants.JSDL_NS, "JobDefinition"));
		return (JobDefinition) JSDLUtility.JSDLContext.createUnmarshaller().unmarshal(element);
	}

	static public JobDefinition_Type convert(JobDefinition jaxbType) throws JAXBException, IOException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		JSDLUtility.JSDLContext.createMarshaller().marshal(jaxbType, baos);
		baos.close();
		return ObjectDeserializer.fromBytes(JobDefinition_Type.class, baos.toByteArray());
	}
}
