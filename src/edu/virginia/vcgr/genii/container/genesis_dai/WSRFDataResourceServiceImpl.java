package edu.virginia.vcgr.genii.container.genesis_dai;

import java.rmi.RemoteException;


import edu.virginia.vcgr.genii.client.WellKnownPortTypes;
import edu.virginia.vcgr.genii.container.rns.EnhancedRNSServiceImpl;
import edu.virginia.vcgr.genii.genesis_dai.WSRFDataResourcePortType;


public class WSRFDataResourceServiceImpl extends EnhancedRNSServiceImpl 
	 implements WSRFDataResourcePortType {

	
	public WSRFDataResourceServiceImpl() throws RemoteException 
	{
		super("WSRFDataResourcePortType");
		addImplementedPortType(WellKnownPortTypes.GENESIS_DAI_WSRF_DATA_RESOURCE_PORT_TYPE);
	}
	
	protected WSRFDataResourceServiceImpl(String serviceName)
			throws RemoteException {
		super(serviceName);
		addImplementedPortType(WellKnownPortTypes.GENESIS_DAI_WSRF_DATA_RESOURCE_PORT_TYPE);
	}
	 /*	
	public GetResourcePropertyResponseType GetResourceProperty (
			GetResourcePropertyRequestType GetResourcePropertyRequest){}

  * <wsdl:operation name="GetResourceProperty">
			<wsdl:input message="rpw-2:GetResourcePropertyRequest" name="GetResourcePropertyRequest" />
			<wsdl:output message="rpw-2:GetResourcePropertyResponse" name="GetResourcePropertyResponse" />
			<wsdl:fault message="rw-2:ResourceUnknownFault" name="ResourceUnknownFault" />
			<wsdl:fault message="rw-2:ResourceUnavailableFault" name="ResourceUnavailableFault" />
			<wsdl:fault message="rpw-2:InvalidResourcePropertyQNameFault" name="InvalidResourcePropertyQNameFault" />
		</wsdl:operation>
		
		<wsdl:operation name="GetMultipleResourceProperties">
			<wsdl:input message="rpw-2:GetMultipleResourcePropertiesRequest" name="GetMultipleResourcePropertiesRequest" />
			<wsdl:output message="rpw-2:GetMultipleResourcePropertiesResponse" name="GetMultipleResourcePropertiesResponse" />
			<wsdl:fault message="rw-2:ResourceUnknownFault" name="ResourceUnknownFault" />
			<wsdl:fault message="rw-2:ResourceUnavailableFault" name="ResourceUnavailableFault" />
			<wsdl:fault message="rpw-2:InvalidResourcePropertyQNameFault" name="InvalidResourcePropertyQNameFault" />
		</wsdl:operation>

		<wsdl:operation name="QueryResourceProperties">
			<wsdl:input message="rpw-2:QueryResourcePropertiesRequest" name="QueryResourcePropertiesRequest" />
			<wsdl:output message="rpw-2:QueryResourcePropertiesResponse" name="QueryResourcePropertiesResponse" />
			<wsdl:fault message="rw-2:ResourceUnknownFault" name="ResourceUnknownFault" />
			<wsdl:fault message="rw-2:ResourceUnavailableFault" name="ResourceUnavailableFault" />
			<wsdl:fault message="rpw-2:InvalidResourcePropertyQNameFault" name="InvalidResourcePropertyQNameFault" />
			<wsdl:fault message="rpw-2:UnknownQueryExpressionDialectFault" name="UnknownQueryExpressionDialectFault" />
			<wsdl:fault message="rpw-2:QueryEvaluationErrorFault" name="QueryEvaluationErrorFault" />
		</wsdl:operation>

		<wsdl:operation name="Destroy">
			<wsdl:input message="rlw-2:DestroyRequest" name="DestroyRequest" />
			<wsdl:output message="rlw-2:DestroyResponse" name="DestroyResponse" />
			<wsdl:fault message="rlw-2:ResourceNotDestroyedFault" name="ResourceNotDestroyedFault" />
			<wsdl:fault message="rw-2:ResourceUnknownFault" name="ResourceUnknownFault" />
			<wsdl:fault message="rw-2:ResourceUnavailableFault" name="ResourceUnavailableFault" />
		</wsdl:operation>

		<wsdl:operation name="SetTerminationTime">
			<wsdl:input message="rlw-2:SetTerminationTimeRequest" name="SetTerminationTimeRequest" />
			<wsdl:output message="rlw-2:SetTerminationTimeResponse" name="SetTerminationTimeResponse" />
			<wsdl:fault message="rlw-2:UnableToSetTerminationTimeFault" name="UnableToSetTerminationTimeFault" />
			<wsdl:fault message="rw-2:ResourceUnknownFault" name="ResourceUnknownFault" />
			<wsdl:fault message="rw-2:ResourceUnavailableFault" name="ResourceUnavailableFault" />
			<wsdl:fault message="rlw-2:TerminationTimeChangeRejectedFault" name="TerminationTimeChangeRejectedFault" />
		</wsdl:operation>
  */


}
