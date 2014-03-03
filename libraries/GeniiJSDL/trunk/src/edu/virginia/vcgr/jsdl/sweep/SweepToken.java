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

/**
 * The SweepToken interface is returned to callers who use the
 * SweepUtility.performSweep operation to perform parameter sweeps. This token
 * is used because the SweepUtility performs parameter sweeps on a separate
 * worker thread and this token allows the caller to block until all of the
 * sweeps have finished being emitted.
 * 
 * @author Mark Morgan (mmm2a@virginia.edu)
 */
public interface SweepToken {
	/**
	 * "Join" a parameter sweep operation and wait for that sweep to complete.
	 * 
	 * @throws SweepException
	 * @throws InterruptedException
	 */
	public void join() throws SweepException, InterruptedException;
}