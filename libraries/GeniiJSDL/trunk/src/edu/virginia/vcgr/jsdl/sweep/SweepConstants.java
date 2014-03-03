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

import javax.xml.namespace.QName;

/**
 * @author Mark Morgan (mmm2a@virginia.edu)
 */
public interface SweepConstants {
	static final public String SWEEP_NS = "http://schemas.ogf.org/jsdl/2009/03/sweep";
	static final public String SWEEP_FUNC_NS = "http://schemas.ogf.org/jsdl/2009/03/sweep/functions";

	static final public String SWEEP_NAME = "Sweep";

	static final public QName SWEEP_QNAME = new QName(SWEEP_NS, SWEEP_NAME);
}
