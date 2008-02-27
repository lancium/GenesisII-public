package edu.virginia.vcgr.genii.container.factory;

import java.rmi.RemoteException;

import edu.virginia.vcgr.genii.client.WellKnownPortTypes;

import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;
import edu.virginia.vcgr.genii.container.common.GenesisIIBase;
import edu.virginia.vcgr.genii.factory.ConstructionParametersType;
import edu.virginia.vcgr.genii.factory.CreateResourceResponseType;
import edu.virginia.vcgr.genii.factory.CreateResourceType;
import edu.virginia.vcgr.genii.factory.FactoryParametersType;
import edu.virginia.vcgr.genii.factory.GeniiFactory;

public class GeniiFactoryServiceImpl extends GenesisIIBase implements
		GeniiFactory
{
	public GeniiFactoryServiceImpl() throws RemoteException
	{
		super("GeniiFactoryPortType");
		
		addImplementedPortType(WellKnownPortTypes.GENII_FACTORY_PORT_TYPE);
	}
	
	public CreateResourceResponseType createResource(
			CreateResourceType createResourceRequest) throws RemoteException,
			ResourceUnknownFaultType
	{
		ConstructionParametersType constructionParameters =
			createResourceRequest.getConstructionParameters();
		FactoryParametersType factoryParameters =
			createResourceRequest.getFactoryParameters();
		
		factoryParameters.getCandidateSetGeneration();
		constructionParameters.get_any();
		
		// TODO Auto-generated method stub
		return null;
	}
}