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
package edu.virginia.vcgr.jsdl.sweep;

import edu.virginia.vcgr.jsdl.JobDefinition;

/**
 * The SweepListener interface is a callback interface implemented by callers that wish to receive
 * the results of parameter sweep operations. When a caller wishes to "expand" the job definitions
 * represented by a parameter sweep, s/he uses this callback to receive the generated singleton
 * jobs.
 * 
 * @author Mark Morgan (mmm2a@virginia.edu)
 */
public interface SweepListener
{
	/**
	 * Receive a generated "singleton" parameter sweep job.
	 * 
	 * @param jobDef
	 *            The generated singleton job.
	 * @throws SweepException
	 */
	public void emitSweepInstance(JobDefinition jobDef) throws SweepException;
}