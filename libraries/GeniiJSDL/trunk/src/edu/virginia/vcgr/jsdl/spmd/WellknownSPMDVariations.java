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
package edu.virginia.vcgr.jsdl.spmd;

import java.net.URI;

/**
 * @author Mark Morgan (mmm2a@virginia.edu)
 */
public interface WellknownSPMDVariations
{
	static final String BASE_URI = "http://www.ogf/org/jsdl/2007/02/jsdl-spmd/";

	static final public URI MPI = URI.create(BASE_URI + "MPI");
	static final public URI GridMPI = URI.create(BASE_URI + "GridMPI");
	static final public URI IntelMPI = URI.create(BASE_URI + "IntelMPI");
	static final public URI LAM_MPI = URI.create(BASE_URI + "LAM-MPI");
	static final public URI MPICH1 = URI.create(BASE_URI + "MPICH1");
	static final public URI MPICH2 = URI.create(BASE_URI + "MPICH2");
	static final public URI MPICH_GM = URI.create(BASE_URI + "MPICH-GM");
	static final public URI MPICH_MX = URI.create(BASE_URI + "MPICH-MX");
	static final public URI MVAPICH = URI.create(BASE_URI + "MVAPICH");
	static final public URI MVAPICH2 = URI.create(BASE_URI + "MVAPICH2");
	static final public URI OpenMPI = URI.create(BASE_URI + "OpenMPI");
	static final public URI POE = URI.create(BASE_URI + "POE");
	static final public URI PVM = URI.create(BASE_URI + "PVM");
}
