package edu.virginia.vcgr.genii.client.jsdl;

import java.io.IOException;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.jsdl.JobDefinition_Type;

import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.jsdl.Application;
import edu.virginia.vcgr.jsdl.Common;
import edu.virginia.vcgr.jsdl.DataStaging;
import edu.virginia.vcgr.jsdl.JobDefinition;
import edu.virginia.vcgr.jsdl.JobDescription;
import edu.virginia.vcgr.jsdl.JobIdentification;
import edu.virginia.vcgr.jsdl.Resources;
import edu.virginia.vcgr.jsdl.sweep.Sweep;

public class JSDLTransformer
{
	static private Log _logger = LogFactory.getLog(JSDLTransformer.class);

	/*
	 * This method transforms a JSDL document with more than one job description into a JSDL document with only a single job description and
	 * returns the latter.
	 * 
	 * @param jsdlType The original JSDL document
	 * 
	 * @param index An integer representing the job description to put in the transformed JSDL document
	 * 
	 * @return The "transformed" JSDL document with only one job description
	 */
	static public JobDefinition_Type transform(JobDefinition_Type jsdlType, int index) throws ResourceException
	{
		if (jsdlType == null)
			return null;

		try {

			// Convert to JobDefinition object
			JobDefinition oldJSDL = JSDLUtils.convert(jsdlType);

			// Retrieve the list of job descriptions from the old JSDL document
			List<JobDescription> jobList = oldJSDL.jobDescription();

			// No transformation is needed for JSDL's that have only one job description
			if (jobList.size() == 1)
				return null;

			// Retrieve the list of parameter sweeps from the old JSDL document
			List<Sweep> oldParameterSweeps = oldJSDL.parameterSweeps();

			// Construct a new JSDL document with the same id as the old one
			JobDefinition newJSDL = new JobDefinition(oldJSDL.id());

			// Get the list of job descriptions in the new JSDL (which is initially empty)
			List<JobDescription> jobList2 = newJSDL.jobDescription();

			// Add the correct job description (determined by the matching process in the Scheduler)
			// to the new JSDL document
			jobList2.add(jobList.get(index));

			// If we have parameter sweeps, add them to the new JSDL
			int size = oldParameterSweeps.size();
			if (size > 0) {
				for (int i = 0; i < size; i++) {
					newJSDL.parameterSweeps().add(oldParameterSweeps.get(i));
				}
			}

			// Convert the new JSDL to a JobDefinition_Type object
			JobDefinition_Type jsdlType2 = JSDLUtils.convert(newJSDL);

			// Return the new JSDL
			return jsdlType2;
		} catch (JAXBException je) {
			_logger.warn("Unable to parse using JAXB.", je);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/*
	 * This method extracts the information in the common block of a JSDL document and inserts it into all of the job descriptions.
	 * 
	 * @param commonJSDLType The JSDL document containing a common block in its definition
	 * 
	 * @return The JSDL document with all elements in the common block inserted into all of the job descriptions
	 */
	static public JobDefinition_Type extractCommon(JobDefinition_Type commonJSDLType) throws ResourceException
	{
		// System.out.println("Extract Common e dhuklam");

		try {
			// Convert to JobDefinition and get the job descriptions
			JobDefinition commonJSDL = JSDLUtils.convert(commonJSDLType);
			List<JobDescription> jobDescList = commonJSDL.jobDescription();

			// Get all of the information in the common block
			Common commonBlock = commonJSDL.common();
			JobIdentification commonJobID = commonBlock.jobIdentification();
			Application commonApps = commonBlock.application();
			Resources commonResources = commonBlock.resources();
			List<DataStaging> commonDataStages = commonBlock.staging();

			// Insert identification information into the job descriptions
			if (commonJobID != null) {
				int i = 0;
				for (JobDescription jobDesc : jobDescList) {
					if (jobDesc.jobIdentification() != null) {
						System.out.println("The jobID " + i + " has an application");
					} else {
						jobDesc.jobIdentification(commonJobID);
					}
					i++;
				}

			}

			// Insert application information into the job descriptions
			if (commonApps != null) {
				int i = 0;
				for (JobDescription jobDesc : jobDescList) {
					if (jobDesc.application() != null) {
						System.out.println("The jobDesc " + i + " has an application");

					} else {
						jobDesc.application(commonApps);
					}
					i++;
				}

			}

			// Insert resource requirements into the job descriptions
			if (commonResources != null) {
				int i = 0;
				for (JobDescription jobDesc : jobDescList) {
					if (jobDesc.resources() != null) {
						System.out.println("The jobID " + i + " has an application");
					} else {
						jobDesc.resources(commonResources);
					}
					i++;

				}

			}

			// Insert data staging requirements into the job descriptions
			if (commonDataStages != null && commonDataStages.size() > 0) {
				for (JobDescription jobDesc : jobDescList) {
					List<DataStaging> dataStages = jobDesc.staging();
					for (int i = 0; i < commonDataStages.size(); i++)
						dataStages.add(commonDataStages.get(i));
				}

			}

			JobDefinition_Type normalJSDL = JSDLUtils.convert(commonJSDL);
			return normalJSDL;

		} catch (JAXBException je) {
			_logger.warn("Unable to parse using JAXB.", je);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;

	}

}
