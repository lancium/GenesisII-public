package edu.virginia.vcgr.genii.client.jsdl.spmd;

import java.net.URI;

import javax.xml.namespace.QName;

public interface SPMDConstants
{
	static public final String JSDL_SPMD_NS =
		"http://schemas.ogf.org/jsdl/2007/02/jsdl-spmd";
	static public final String JSDL_SPMD_APPLICATION_NAME = 
		"SPMDApplication";
	
	static public final QName JSDL_SPMD_APPLICATION_QNAME =
		new QName(JSDL_SPMD_NS, JSDL_SPMD_APPLICATION_NAME);

	static public final URI ANY_MPI = URI.create(
		"http://www.ogf.org/jsdl/2007/02/jsdl-spmd/MPI");
	static public final URI GRID = URI.create(
		"http://www.ogf.org/jsdl/2007/02/jsdl-spmd/GridMPI");
	static public final URI INTEL = URI.create(
		"http://www.ogf.org/jsdl/2007/02/jsdl-spmd/IntelMPI");
	static public final URI LAM = URI.create(
		"http://www.ogf.org/jsdl/2007/02/jsdl-spmd/LAM-MPI");
	static public final URI MPICH1 = URI.create(
		"http://www.ogf.org/jsdl/2007/02/jsdl-spmd/MPICH1");
	static public final URI MPICH2 = URI.create(
		"http://www.ogf.org/jsdl/2007/02/jsdl-spmd/MPICH2");
	static public final URI MPICH_GM = URI.create(
		"http://www.ogf.org/jsdl/2007/02/jsdl-spmd/MPICH-GM");
	static public final URI MPICH_MX = URI.create(
		"http://www.ogf.org/jsdl/2007/02/jsdl-spmd/MPICH-MX");
	static public final URI MVAPICH = URI.create(
		"http://www.ogf.org/jsdl/2007/02/jsdl-spmd/MVAPICH");
	static public final URI MVAPICH2 = URI.create(
		"http://www.ogf.org/jsdl/2007/02/jsdl-spmd/MVAPICH2");
	static public final URI OPEN_MPI = URI.create(
		"http://www.ogf.org/jsdl/2007/02/jsdl-spmd/OpenMPI");
	static public final URI POE = URI.create(
		"http://www.ogf.org/jsdl/2007/02/jsdl-spmd/POE");
	static public final URI PVM = URI.create(
		"http://www.ogf.org/jsdl/2007/02/jsdl-spmd/PVM");
}
