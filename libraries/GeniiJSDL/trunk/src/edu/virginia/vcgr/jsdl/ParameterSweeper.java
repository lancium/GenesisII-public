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

import java.io.File;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import edu.virginia.vcgr.jsdl.sweep.SweepException;
import edu.virginia.vcgr.jsdl.sweep.SweepListener;
import edu.virginia.vcgr.jsdl.sweep.SweepUtility;

/**
 * @author Mark Morgan (mmm2a@virginia.edu)
 */
public class ParameterSweeper {
	static public void main(String[] args) {
		if (args.length < 1 || args.length > 2) {
			System.err
					.println("USAGE:  ParameterSweeper <input-jsdl> <output-directory>");
			System.exit(1);
		}

		File source = new File(args[0]);
		File targetDir = new File(args[1]);

		if (!source.exists() || !source.isFile()) {
			System.err.format("JSDL File %s does not exist.\n", args[0]);
			System.exit(1);
		}

		if (!targetDir.exists()) {
			System.err.format("Target directory %s does not exist.\n", args[1]);
			System.exit(1);
		}

		if (!targetDir.isDirectory()) {
			System.err.format("Target %s is not a directory.\n", args[1]);
			System.exit(1);
		}

		try {
			Unmarshaller unmarshaller = JSDLUtility.JSDLContext
					.createUnmarshaller();
			JobDefinition jobDef = (JobDefinition) unmarshaller
					.unmarshal(source);

			System.out.format("Generating %d jobs from sweep file %s\n",
					SweepUtility.sweepSize(jobDef), source);
			SweepUtility.performSweep(jobDef, new SweepListenerImpl(source,
					targetDir));
		} catch (Throwable e) {
			System.err.format("Unable to sweep JSDL file:  %s\n", e);
			System.exit(1);
		}
	}

	static private class SweepListenerImpl implements SweepListener {
		private Marshaller _marshaller;
		private File _source;
		private File _targetDir;
		private int _nextInstance;

		private SweepListenerImpl(File source, File targetDir)
				throws JAXBException {
			_marshaller = JSDLUtility.JSDLContext.createMarshaller();
			_marshaller.setProperty("jaxb.formatted.output", Boolean.TRUE);
			_source = source;
			_targetDir = targetDir;
			_nextInstance = 0;
		}

		@Override
		public void emitSweepInstance(JobDefinition jobDef)
				throws SweepException {
			try {
				File output = new File(_targetDir, String.format("%s.%d",
						_source.getName(), _nextInstance++));
				_marshaller.marshal(jobDef, output);
			} catch (JAXBException e) {
				throw new SweepException("Unable to marshall Job Definition.",
						e);
			}
		}
	}
}
