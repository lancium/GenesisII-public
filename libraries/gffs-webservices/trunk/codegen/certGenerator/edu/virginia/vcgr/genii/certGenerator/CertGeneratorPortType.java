/**
 * CertGeneratorPortType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package edu.virginia.vcgr.genii.certGenerator;

public interface CertGeneratorPortType extends java.rmi.Remote {
    public edu.virginia.vcgr.genii.certGenerator.GenerateX509V3CertificateChainResponseType generateX509V3CertificateChain(edu.virginia.vcgr.genii.certGenerator.GenerateX509V3CertificateChainRequestType generateX509V3CertificateChainRequest) throws java.rmi.RemoteException, org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType, edu.virginia.vcgr.genii.certGenerator.InvalidCertificateRequestFaultType;
    public org.oasis_open.docs.wsrf.rp_2.GetResourcePropertyResponse getResourceProperty(javax.xml.namespace.QName getResourcePropertyRequest) throws java.rmi.RemoteException, org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType, org.oasis_open.docs.wsrf.rp_2.InvalidResourcePropertyQNameFaultType, org.oasis_open.docs.wsrf.r_2.ResourceUnavailableFaultType;
    public org.oasis_open.docs.wsrf.rp_2.InsertResourcePropertiesResponse insertResourceProperties(org.oasis_open.docs.wsrf.rp_2.InsertResourceProperties insertResourcePropertiesRequest) throws java.rmi.RemoteException, org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType, org.oasis_open.docs.wsrf.rp_2.UnableToModifyResourcePropertyFaultType, org.oasis_open.docs.wsrf.rp_2.InvalidModificationFaultType, org.oasis_open.docs.wsrf.rp_2.InsertResourcePropertiesRequestFailedFaultType, org.oasis_open.docs.wsrf.rp_2.InvalidResourcePropertyQNameFaultType, org.oasis_open.docs.wsrf.r_2.ResourceUnavailableFaultType;
    public org.oasis_open.docs.wsrf.rp_2.QueryResourcePropertiesResponse queryResourceProperties(org.oasis_open.docs.wsrf.rp_2.QueryResourceProperties queryResourcePropertiesRequest) throws java.rmi.RemoteException, org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType, org.oasis_open.docs.wsrf.rp_2.QueryEvaluationErrorFaultType, org.oasis_open.docs.wsrf.rp_2.InvalidResourcePropertyQNameFaultType, org.oasis_open.docs.wsrf.rp_2.InvalidQueryExpressionFaultType, org.oasis_open.docs.wsrf.r_2.ResourceUnavailableFaultType, org.oasis_open.docs.wsrf.rp_2.UnknownQueryExpressionDialectFaultType;
    public void notify(org.oasis_open.wsn.base.Notify notify) throws java.rmi.RemoteException;
    public java.lang.String ping(java.lang.String pingRequest) throws java.rmi.RemoteException;
    public org.oasis_open.wsn.base.GetCurrentMessageResponse getCurrentMessage(org.oasis_open.wsn.base.GetCurrentMessage getCurrentMessageRequest) throws java.rmi.RemoteException, org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType, org.oasis_open.wsn.base.MultipleTopicsSpecifiedFaultType, org.oasis_open.wsn.base.InvalidTopicExpressionFaultType, org.oasis_open.wsn.base.NoCurrentMessageOnTopicFaultType, org.oasis_open.wsn.base.TopicExpressionDialectUnknownFaultType, org.oasis_open.wsn.base.TopicNotSupportedFaultType;
    public org.oasis_open.docs.wsrf.rp_2.UpdateResourcePropertiesResponse updateResourceProperties(org.oasis_open.docs.wsrf.rp_2.UpdateResourceProperties updateResourcePropertiesRequest) throws java.rmi.RemoteException, org.oasis_open.docs.wsrf.rp_2.UpdateResourcePropertiesRequestFailedFaultType, org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType, org.oasis_open.docs.wsrf.rp_2.UnableToModifyResourcePropertyFaultType, org.oasis_open.docs.wsrf.rp_2.InvalidModificationFaultType, org.oasis_open.docs.wsrf.rp_2.InvalidResourcePropertyQNameFaultType, org.oasis_open.docs.wsrf.r_2.ResourceUnavailableFaultType;
    public org.oasis_open.docs.wsrf.rl_2.SetTerminationTimeResponse setTerminationTime(org.oasis_open.docs.wsrf.rl_2.SetTerminationTime setTerminationTimeRequest) throws java.rmi.RemoteException, org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType, org.oasis_open.docs.wsrf.rl_2.UnableToSetTerminationTimeFaultType, org.oasis_open.docs.wsrf.r_2.ResourceUnavailableFaultType, org.oasis_open.docs.wsrf.rl_2.TerminationTimeChangeRejectedFaultType;
    public org.oasis_open.wsn.base.SubscribeResponse subscribe(org.oasis_open.wsn.base.Subscribe subscribeRequest) throws java.rmi.RemoteException, org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType, org.oasis_open.wsn.base.UnrecognizedPolicyRequestFaultType, org.oasis_open.wsn.base.NotifyMessageNotSupportedFaultType, org.oasis_open.wsn.base.SubscribeCreationFailedFaultType, org.oasis_open.wsn.base.UnacceptableInitialTerminationTimeFaultType, org.oasis_open.wsn.base.InvalidProducerPropertiesExpressionFaultType, org.oasis_open.wsn.base.InvalidFilterFaultType, org.oasis_open.wsn.base.InvalidTopicExpressionFaultType, org.oasis_open.wsn.base.InvalidMessageContentExpressionFaultType, org.oasis_open.wsn.base.TopicExpressionDialectUnknownFaultType, org.oasis_open.wsn.base.TopicNotSupportedFaultType, org.oasis_open.wsn.base.UnsupportedPolicyRequestFaultType;
    public edu.virginia.vcgr.genii.common.RemoveMatchingParameterResponseType removeMatchingParameter(edu.virginia.vcgr.genii.common.MatchingParameter[] removeMatchingParameterRequest) throws java.rmi.RemoteException;
    public edu.virginia.vcgr.genii.common.AddMatchingParameterResponseType addMatchingParameter(edu.virginia.vcgr.genii.common.MatchingParameter[] addMatchingParameterRequest) throws java.rmi.RemoteException;
    public edu.virginia.vcgr.genii.common.rfactory.VcgrCreateResponse vcgrCreate(edu.virginia.vcgr.genii.common.rfactory.VcgrCreate vcgrCreateRequest) throws java.rmi.RemoteException, org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType, edu.virginia.vcgr.genii.common.rfactory.ResourceCreationFaultType;
    public org.oasis_open.docs.wsrf.rp_2.GetResourcePropertyDocumentResponse getResourcePropertyDocument(org.oasis_open.docs.wsrf.rp_2.GetResourcePropertyDocument getResourcePropertyDocumentRequest) throws java.rmi.RemoteException, org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType, org.oasis_open.docs.wsrf.r_2.ResourceUnavailableFaultType;
    public org.oasis_open.docs.wsrf.rp_2.SetResourcePropertiesResponse setResourceProperties(org.oasis_open.docs.wsrf.rp_2.SetResourceProperties setResourcePropertiesRequest) throws java.rmi.RemoteException, org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType, org.oasis_open.docs.wsrf.rp_2.UnableToModifyResourcePropertyFaultType, org.oasis_open.docs.wsrf.rp_2.InvalidModificationFaultType, org.oasis_open.docs.wsrf.rp_2.InvalidResourcePropertyQNameFaultType, org.oasis_open.docs.wsrf.r_2.ResourceUnavailableFaultType, org.oasis_open.docs.wsrf.rp_2.SetResourcePropertyRequestFailedFaultType;
    public org.oasis_open.docs.wsrf.rp_2.DeleteResourcePropertiesResponse deleteResourceProperties(org.oasis_open.docs.wsrf.rp_2.DeleteResourceProperties deleteResourcePropertiesRequest) throws java.rmi.RemoteException, org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType, org.oasis_open.docs.wsrf.rp_2.UnableToModifyResourcePropertyFaultType, org.oasis_open.docs.wsrf.rp_2.InvalidModificationFaultType, org.oasis_open.docs.wsrf.rp_2.DeleteResourcePropertiesRequestFailedFaultType, org.oasis_open.docs.wsrf.rp_2.InvalidResourcePropertyQNameFaultType, org.oasis_open.docs.wsrf.r_2.ResourceUnavailableFaultType;
    public edu.virginia.vcgr.genii.common.IterateHistoryEventsResponseType iterateHistoryEvents(edu.virginia.vcgr.genii.common.IterateHistoryEventsRequestType iterateHistoryEventsRequest) throws java.rmi.RemoteException;
    public org.oasis_open.docs.wsrf.rl_2.DestroyResponse destroy(org.oasis_open.docs.wsrf.rl_2.Destroy destroyRequest) throws java.rmi.RemoteException, org.oasis_open.docs.wsrf.rl_2.ResourceNotDestroyedFaultType, org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType, org.oasis_open.docs.wsrf.r_2.ResourceUnavailableFaultType;
    public org.oasis_open.docs.wsrf.rp_2.GetMultipleResourcePropertiesResponse getMultipleResourceProperties(javax.xml.namespace.QName[] getMultipleResourcePropertiesRequest) throws java.rmi.RemoteException, org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType, org.oasis_open.docs.wsrf.rp_2.InvalidResourcePropertyQNameFaultType, org.oasis_open.docs.wsrf.r_2.ResourceUnavailableFaultType;
    public edu.virginia.vcgr.genii.common.notification.NotifyResponseType notifyWithResponse(org.oasis_open.wsn.base.Notify notify) throws java.rmi.RemoteException;
}
