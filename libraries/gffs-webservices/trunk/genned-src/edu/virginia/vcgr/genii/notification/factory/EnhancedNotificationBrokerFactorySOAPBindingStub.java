/**
 * EnhancedNotificationBrokerFactorySOAPBindingStub.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package edu.virginia.vcgr.genii.notification.factory;

public class EnhancedNotificationBrokerFactorySOAPBindingStub extends org.apache.axis.client.Stub implements edu.virginia.vcgr.genii.notification.factory.EnhancedNotificationBrokerFactoryPortType {
    private java.util.Vector cachedSerClasses = new java.util.Vector();
    private java.util.Vector cachedSerQNames = new java.util.Vector();
    private java.util.Vector cachedSerFactories = new java.util.Vector();
    private java.util.Vector cachedDeserFactories = new java.util.Vector();

    static org.apache.axis.description.OperationDesc [] _operations;

    static {
        _operations = new org.apache.axis.description.OperationDesc[21];
        _initOperationDesc1();
        _initOperationDesc2();
        _initOperationDesc3();
    }

    private static void _initOperationDesc1(){
        org.apache.axis.description.OperationDesc oper;
        org.apache.axis.description.ParameterDesc param;
        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("createNotificationBroker");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/container/2011/07/enhanced-notification-broker-factory", "NotificationBrokerLifetime"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "long"), long.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2005/08/addressing", "EndpointReferenceType"));
        oper.setReturnClass(org.ws.addressing.EndpointReferenceType.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/container/2011/07/enhanced-notification-broker-factory", "NotificationBrokerEndpoint"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/container/2011/07/enhanced-notification-broker-factory", "NotificationBrokerCreationFailedFaultType"),
                      "edu.virginia.vcgr.genii.notification.factory.NotificationBrokerCreationFailedFaultType",
                      new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/container/2011/07/enhanced-notification-broker-factory", "NotificationBrokerCreationFailedFaultType"), 
                      true
                     ));
        _operations[0] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("GetResourceProperty");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", "GetResourceProperty"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "QName"), javax.xml.namespace.QName.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", ">GetResourcePropertyResponse"));
        oper.setReturnClass(org.oasis_open.docs.wsrf.rp_2.GetResourcePropertyResponse.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", "GetResourcePropertyResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/r-2", "ResourceUnknownFault"),
                      "org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType",
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/r-2", "ResourceUnknownFaultType"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", "InvalidResourcePropertyQNameFault"),
                      "org.oasis_open.docs.wsrf.rp_2.InvalidResourcePropertyQNameFaultType",
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", "InvalidResourcePropertyQNameFaultType"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/r-2", "ResourceUnavailableFault"),
                      "org.oasis_open.docs.wsrf.r_2.ResourceUnavailableFaultType",
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/r-2", "ResourceUnavailableFaultType"), 
                      true
                     ));
        _operations[1] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("InsertResourceProperties");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", "InsertResourceProperties"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", ">InsertResourceProperties"), org.oasis_open.docs.wsrf.rp_2.InsertResourceProperties.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", ">InsertResourcePropertiesResponse"));
        oper.setReturnClass(org.oasis_open.docs.wsrf.rp_2.InsertResourcePropertiesResponse.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", "InsertResourcePropertiesResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/r-2", "ResourceUnknownFault"),
                      "org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType",
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/r-2", "ResourceUnknownFaultType"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", "UnableToModifyResourcePropertyFault"),
                      "org.oasis_open.docs.wsrf.rp_2.UnableToModifyResourcePropertyFaultType",
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", "UnableToModifyResourcePropertyFaultType"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", "InvalidModificationFault"),
                      "org.oasis_open.docs.wsrf.rp_2.InvalidModificationFaultType",
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", "InvalidModificationFaultType"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", "InsertResourcePropertiesRequestFailedFault"),
                      "org.oasis_open.docs.wsrf.rp_2.InsertResourcePropertiesRequestFailedFaultType",
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", "InsertResourcePropertiesRequestFailedFaultType"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", "InvalidResourcePropertyQNameFault"),
                      "org.oasis_open.docs.wsrf.rp_2.InvalidResourcePropertyQNameFaultType",
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", "InvalidResourcePropertyQNameFaultType"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/r-2", "ResourceUnavailableFault"),
                      "org.oasis_open.docs.wsrf.r_2.ResourceUnavailableFaultType",
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/r-2", "ResourceUnavailableFaultType"), 
                      true
                     ));
        _operations[2] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("QueryResourceProperties");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", "QueryResourceProperties"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", ">QueryResourceProperties"), org.oasis_open.docs.wsrf.rp_2.QueryResourceProperties.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", ">QueryResourcePropertiesResponse"));
        oper.setReturnClass(org.oasis_open.docs.wsrf.rp_2.QueryResourcePropertiesResponse.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", "QueryResourcePropertiesResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/r-2", "ResourceUnknownFault"),
                      "org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType",
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/r-2", "ResourceUnknownFaultType"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", "QueryEvaluationErrorFault"),
                      "org.oasis_open.docs.wsrf.rp_2.QueryEvaluationErrorFaultType",
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", "QueryEvaluationErrorFaultType"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", "InvalidResourcePropertyQNameFault"),
                      "org.oasis_open.docs.wsrf.rp_2.InvalidResourcePropertyQNameFaultType",
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", "InvalidResourcePropertyQNameFaultType"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", "InvalidQueryExpressionFault"),
                      "org.oasis_open.docs.wsrf.rp_2.InvalidQueryExpressionFaultType",
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", "InvalidQueryExpressionFaultType"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/r-2", "ResourceUnavailableFault"),
                      "org.oasis_open.docs.wsrf.r_2.ResourceUnavailableFaultType",
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/r-2", "ResourceUnavailableFaultType"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", "UnknownQueryExpressionDialectFault"),
                      "org.oasis_open.docs.wsrf.rp_2.UnknownQueryExpressionDialectFaultType",
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", "UnknownQueryExpressionDialectFaultType"), 
                      true
                     ));
        _operations[3] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("Notify");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", "Notify"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", ">Notify"), org.oasis_open.wsn.base.Notify.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(org.apache.axis.encoding.XMLType.AXIS_VOID);
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[4] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("ping");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/2006/12/resource-simple", "ping"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        oper.setReturnClass(java.lang.String.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/2006/12/resource-simple", "pingResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[5] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("GetCurrentMessage");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", "GetCurrentMessage"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", ">GetCurrentMessage"), org.oasis_open.wsn.base.GetCurrentMessage.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", ">GetCurrentMessageResponse"));
        oper.setReturnClass(org.oasis_open.wsn.base.GetCurrentMessageResponse.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", "GetCurrentMessageResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/r-2", "ResourceUnknownFault"),
                      "org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType",
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/r-2", "ResourceUnknownFaultType"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", "MultipleTopicsSpecifiedFault"),
                      "org.oasis_open.wsn.base.MultipleTopicsSpecifiedFaultType",
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", "MultipleTopicsSpecifiedFaultType"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", "InvalidTopicExpressionFault"),
                      "org.oasis_open.wsn.base.InvalidTopicExpressionFaultType",
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", "InvalidTopicExpressionFaultType"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", "NoCurrentMessageOnTopicFault"),
                      "org.oasis_open.wsn.base.NoCurrentMessageOnTopicFaultType",
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", "NoCurrentMessageOnTopicFaultType"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", "TopicExpressionDialectUnknownFault"),
                      "org.oasis_open.wsn.base.TopicExpressionDialectUnknownFaultType",
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", "TopicExpressionDialectUnknownFaultType"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", "TopicNotSupportedFault"),
                      "org.oasis_open.wsn.base.TopicNotSupportedFaultType",
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", "TopicNotSupportedFaultType"), 
                      true
                     ));
        _operations[6] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("UpdateResourceProperties");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", "UpdateResourceProperties"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", ">UpdateResourceProperties"), org.oasis_open.docs.wsrf.rp_2.UpdateResourceProperties.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", ">UpdateResourcePropertiesResponse"));
        oper.setReturnClass(org.oasis_open.docs.wsrf.rp_2.UpdateResourcePropertiesResponse.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", "UpdateResourcePropertiesResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", "UpdateResourcePropertiesRequestFailedFault"),
                      "org.oasis_open.docs.wsrf.rp_2.UpdateResourcePropertiesRequestFailedFaultType",
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", "UpdateResourcePropertiesRequestFailedFaultType"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/r-2", "ResourceUnknownFault"),
                      "org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType",
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/r-2", "ResourceUnknownFaultType"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", "UnableToModifyResourcePropertyFault"),
                      "org.oasis_open.docs.wsrf.rp_2.UnableToModifyResourcePropertyFaultType",
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", "UnableToModifyResourcePropertyFaultType"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", "InvalidModificationFault"),
                      "org.oasis_open.docs.wsrf.rp_2.InvalidModificationFaultType",
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", "InvalidModificationFaultType"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", "InvalidResourcePropertyQNameFault"),
                      "org.oasis_open.docs.wsrf.rp_2.InvalidResourcePropertyQNameFaultType",
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", "InvalidResourcePropertyQNameFaultType"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/r-2", "ResourceUnavailableFault"),
                      "org.oasis_open.docs.wsrf.r_2.ResourceUnavailableFaultType",
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/r-2", "ResourceUnavailableFaultType"), 
                      true
                     ));
        _operations[7] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("SetTerminationTime");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rl-2", "SetTerminationTime"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rl-2", ">SetTerminationTime"), org.oasis_open.docs.wsrf.rl_2.SetTerminationTime.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rl-2", ">SetTerminationTimeResponse"));
        oper.setReturnClass(org.oasis_open.docs.wsrf.rl_2.SetTerminationTimeResponse.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rl-2", "SetTerminationTimeResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/r-2", "ResourceUnknownFault"),
                      "org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType",
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/r-2", "ResourceUnknownFaultType"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rl-2", "UnableToSetTerminationTimeFault"),
                      "org.oasis_open.docs.wsrf.rl_2.UnableToSetTerminationTimeFaultType",
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rl-2", "UnableToSetTerminationTimeFaultType"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/r-2", "ResourceUnavailableFault"),
                      "org.oasis_open.docs.wsrf.r_2.ResourceUnavailableFaultType",
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/r-2", "ResourceUnavailableFaultType"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rl-2", "TerminationTimeChangeRejectedFault"),
                      "org.oasis_open.docs.wsrf.rl_2.TerminationTimeChangeRejectedFaultType",
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rl-2", "TerminationTimeChangeRejectedFaultType"), 
                      true
                     ));
        _operations[8] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("createNotificationBrokerWithForwardingPort");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/container/2011/07/enhanced-notification-broker-factory", "BrokerWithForwardingPortCreateRequest"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/container/2011/07/enhanced-notification-broker-factory", "BrokerWithForwardingPortCreateRequestType"), edu.virginia.vcgr.genii.notification.factory.BrokerWithForwardingPortCreateRequestType.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2005/08/addressing", "EndpointReferenceType"));
        oper.setReturnClass(org.ws.addressing.EndpointReferenceType.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/container/2011/07/enhanced-notification-broker-factory", "NotificationBrokerEndpoint"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/container/2011/07/enhanced-notification-broker-factory", "NotificationBrokerCreationFailedFaultType"),
                      "edu.virginia.vcgr.genii.notification.factory.NotificationBrokerCreationFailedFaultType",
                      new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/container/2011/07/enhanced-notification-broker-factory", "NotificationBrokerCreationFailedFaultType"), 
                      true
                     ));
        _operations[9] = oper;

    }

    private static void _initOperationDesc2(){
        org.apache.axis.description.OperationDesc oper;
        org.apache.axis.description.ParameterDesc param;
        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("Subscribe");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", "Subscribe"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", ">Subscribe"), org.oasis_open.wsn.base.Subscribe.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", ">SubscribeResponse"));
        oper.setReturnClass(org.oasis_open.wsn.base.SubscribeResponse.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", "SubscribeResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/r-2", "ResourceUnknownFault"),
                      "org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType",
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/r-2", "ResourceUnknownFaultType"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", "UnrecognizedPolicyRequestFault"),
                      "org.oasis_open.wsn.base.UnrecognizedPolicyRequestFaultType",
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", "UnrecognizedPolicyRequestFaultType"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", "NotifyMessageNotSupportedFault"),
                      "org.oasis_open.wsn.base.NotifyMessageNotSupportedFaultType",
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", "NotifyMessageNotSupportedFaultType"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", "SubscribeCreationFailedFault"),
                      "org.oasis_open.wsn.base.SubscribeCreationFailedFaultType",
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", "SubscribeCreationFailedFaultType"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", "UnacceptableInitialTerminationTimeFault"),
                      "org.oasis_open.wsn.base.UnacceptableInitialTerminationTimeFaultType",
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", "UnacceptableInitialTerminationTimeFaultType"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", "InvalidProducerPropertiesExpressionFault"),
                      "org.oasis_open.wsn.base.InvalidProducerPropertiesExpressionFaultType",
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", "InvalidProducerPropertiesExpressionFaultType"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", "InvalidFilterFault"),
                      "org.oasis_open.wsn.base.InvalidFilterFaultType",
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", "InvalidFilterFaultType"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", "InvalidTopicExpressionFault"),
                      "org.oasis_open.wsn.base.InvalidTopicExpressionFaultType",
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", "InvalidTopicExpressionFaultType"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", "InvalidMessageContentExpressionFault"),
                      "org.oasis_open.wsn.base.InvalidMessageContentExpressionFaultType",
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", "InvalidMessageContentExpressionFaultType"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", "TopicExpressionDialectUnknownFault"),
                      "org.oasis_open.wsn.base.TopicExpressionDialectUnknownFaultType",
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", "TopicExpressionDialectUnknownFaultType"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", "TopicNotSupportedFault"),
                      "org.oasis_open.wsn.base.TopicNotSupportedFaultType",
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", "TopicNotSupportedFaultType"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", "UnsupportedPolicyRequestFault"),
                      "org.oasis_open.wsn.base.UnsupportedPolicyRequestFaultType",
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", "UnsupportedPolicyRequestFaultType"), 
                      true
                     ));
        _operations[10] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("removeMatchingParameter");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/2006/12/common", "RemoveMatchingParameterRequestType"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/2006/12/common", "RemoveMatchingParameterRequestType"), edu.virginia.vcgr.genii.common.MatchingParameter[].class, false, false);
        param.setItemQName(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/2006/12/common", "matching-parameter"));
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/2006/12/common", "RemoveMatchingParameterResponseType"));
        oper.setReturnClass(edu.virginia.vcgr.genii.common.RemoveMatchingParameterResponseType.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/2006/12/common", "RemoveMatchingParameterResponseType"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[11] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("addMatchingParameter");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/2006/12/common", "AddMatchingParameterRequestType"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/2006/12/common", "AddMatchingParameterRequestType"), edu.virginia.vcgr.genii.common.MatchingParameter[].class, false, false);
        param.setItemQName(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/2006/12/common", "matching-parameter"));
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/2006/12/common", "AddMatchingParameterResponseType"));
        oper.setReturnClass(edu.virginia.vcgr.genii.common.AddMatchingParameterResponseType.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/2006/12/common", "AddMatchingParameterResponseType"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[12] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("vcgrCreate");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/2006/12/resource-factory", "vcgrCreate"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/2006/12/resource-factory", ">vcgrCreate"), edu.virginia.vcgr.genii.common.rfactory.VcgrCreate.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/2006/12/resource-factory", ">vcgrCreateResponse"));
        oper.setReturnClass(edu.virginia.vcgr.genii.common.rfactory.VcgrCreateResponse.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/2006/12/resource-factory", "vcgrCreateResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/r-2", "ResourceUnknownFault"),
                      "org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType",
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/r-2", "ResourceUnknownFaultType"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/2006/12/resource-factory", "ResourceCreationFaultType"),
                      "edu.virginia.vcgr.genii.common.rfactory.ResourceCreationFaultType",
                      new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/2006/12/resource-factory", "ResourceCreationFaultType"), 
                      true
                     ));
        _operations[13] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("GetResourcePropertyDocument");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", "GetResourcePropertyDocument"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", ">GetResourcePropertyDocument"), org.oasis_open.docs.wsrf.rp_2.GetResourcePropertyDocument.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", ">GetResourcePropertyDocumentResponse"));
        oper.setReturnClass(org.oasis_open.docs.wsrf.rp_2.GetResourcePropertyDocumentResponse.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", "GetResourcePropertyDocumentResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/r-2", "ResourceUnknownFault"),
                      "org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType",
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/r-2", "ResourceUnknownFaultType"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/r-2", "ResourceUnavailableFault"),
                      "org.oasis_open.docs.wsrf.r_2.ResourceUnavailableFaultType",
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/r-2", "ResourceUnavailableFaultType"), 
                      true
                     ));
        _operations[14] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("SetResourceProperties");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", "SetResourceProperties"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", ">SetResourceProperties"), org.oasis_open.docs.wsrf.rp_2.SetResourceProperties.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", ">SetResourcePropertiesResponse"));
        oper.setReturnClass(org.oasis_open.docs.wsrf.rp_2.SetResourcePropertiesResponse.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", "SetResourcePropertiesResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/r-2", "ResourceUnknownFault"),
                      "org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType",
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/r-2", "ResourceUnknownFaultType"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", "UnableToModifyResourcePropertyFault"),
                      "org.oasis_open.docs.wsrf.rp_2.UnableToModifyResourcePropertyFaultType",
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", "UnableToModifyResourcePropertyFaultType"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", "InvalidModificationFault"),
                      "org.oasis_open.docs.wsrf.rp_2.InvalidModificationFaultType",
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", "InvalidModificationFaultType"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", "InvalidResourcePropertyQNameFault"),
                      "org.oasis_open.docs.wsrf.rp_2.InvalidResourcePropertyQNameFaultType",
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", "InvalidResourcePropertyQNameFaultType"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/r-2", "ResourceUnavailableFault"),
                      "org.oasis_open.docs.wsrf.r_2.ResourceUnavailableFaultType",
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/r-2", "ResourceUnavailableFaultType"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", "SetResourcePropertyRequestFailedFault"),
                      "org.oasis_open.docs.wsrf.rp_2.SetResourcePropertyRequestFailedFaultType",
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", "SetResourcePropertyRequestFailedFaultType"), 
                      true
                     ));
        _operations[15] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("DeleteResourceProperties");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", "DeleteResourceProperties"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", ">DeleteResourceProperties"), org.oasis_open.docs.wsrf.rp_2.DeleteResourceProperties.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", ">DeleteResourcePropertiesResponse"));
        oper.setReturnClass(org.oasis_open.docs.wsrf.rp_2.DeleteResourcePropertiesResponse.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", "DeleteResourcePropertiesResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/r-2", "ResourceUnknownFault"),
                      "org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType",
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/r-2", "ResourceUnknownFaultType"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", "UnableToModifyResourcePropertyFault"),
                      "org.oasis_open.docs.wsrf.rp_2.UnableToModifyResourcePropertyFaultType",
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", "UnableToModifyResourcePropertyFaultType"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", "InvalidModificationFault"),
                      "org.oasis_open.docs.wsrf.rp_2.InvalidModificationFaultType",
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", "InvalidModificationFaultType"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", "DeleteResourcePropertiesRequestFailedFault"),
                      "org.oasis_open.docs.wsrf.rp_2.DeleteResourcePropertiesRequestFailedFaultType",
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", "DeleteResourcePropertiesRequestFailedFaultType"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", "InvalidResourcePropertyQNameFault"),
                      "org.oasis_open.docs.wsrf.rp_2.InvalidResourcePropertyQNameFaultType",
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", "InvalidResourcePropertyQNameFaultType"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/r-2", "ResourceUnavailableFault"),
                      "org.oasis_open.docs.wsrf.r_2.ResourceUnavailableFaultType",
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/r-2", "ResourceUnavailableFaultType"), 
                      true
                     ));
        _operations[16] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("iterateHistoryEvents");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/2006/12/common", "IterateHistoryEventsRequest"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/2006/12/common", "IterateHistoryEventsRequestType"), edu.virginia.vcgr.genii.common.IterateHistoryEventsRequestType.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/2006/12/common", "IterateHistoryEventsResponseType"));
        oper.setReturnClass(edu.virginia.vcgr.genii.common.IterateHistoryEventsResponseType.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/2006/12/common", "IterateHistoryEventsResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[17] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("Destroy");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rl-2", "Destroy"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rl-2", ">Destroy"), org.oasis_open.docs.wsrf.rl_2.Destroy.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rl-2", ">DestroyResponse"));
        oper.setReturnClass(org.oasis_open.docs.wsrf.rl_2.DestroyResponse.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rl-2", "DestroyResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rl-2", "ResourceNotDestroyedFault"),
                      "org.oasis_open.docs.wsrf.rl_2.ResourceNotDestroyedFaultType",
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rl-2", "ResourceNotDestroyedFaultType"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/r-2", "ResourceUnknownFault"),
                      "org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType",
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/r-2", "ResourceUnknownFaultType"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/r-2", "ResourceUnavailableFault"),
                      "org.oasis_open.docs.wsrf.r_2.ResourceUnavailableFaultType",
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/r-2", "ResourceUnavailableFaultType"), 
                      true
                     ));
        _operations[18] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("GetMultipleResourceProperties");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", "GetMultipleResourceProperties"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", ">GetMultipleResourceProperties"), javax.xml.namespace.QName[].class, false, false);
        param.setItemQName(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", "ResourceProperty"));
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", ">GetMultipleResourcePropertiesResponse"));
        oper.setReturnClass(org.oasis_open.docs.wsrf.rp_2.GetMultipleResourcePropertiesResponse.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", "GetMultipleResourcePropertiesResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/r-2", "ResourceUnknownFault"),
                      "org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType",
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/r-2", "ResourceUnknownFaultType"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", "InvalidResourcePropertyQNameFault"),
                      "org.oasis_open.docs.wsrf.rp_2.InvalidResourcePropertyQNameFaultType",
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", "InvalidResourcePropertyQNameFaultType"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/r-2", "ResourceUnavailableFault"),
                      "org.oasis_open.docs.wsrf.r_2.ResourceUnavailableFaultType",
                      new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/r-2", "ResourceUnavailableFaultType"), 
                      true
                     ));
        _operations[19] = oper;

    }

    private static void _initOperationDesc3(){
        org.apache.axis.description.OperationDesc oper;
        org.apache.axis.description.ParameterDesc param;
        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("notifyWithResponse");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", "Notify"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", ">Notify"), org.oasis_open.wsn.base.Notify.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/2010/08/notification/notification-consumer", ">NotifyResponseType"));
        oper.setReturnClass(edu.virginia.vcgr.genii.common.notification.NotifyResponseType.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/2010/08/notification/notification-consumer", "NotifyResponseType"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[20] = oper;

    }

    public EnhancedNotificationBrokerFactorySOAPBindingStub() throws org.apache.axis.AxisFault {
         this(null);
    }

    public EnhancedNotificationBrokerFactorySOAPBindingStub(java.net.URL endpointURL, javax.xml.rpc.Service service) throws org.apache.axis.AxisFault {
         this(service);
         super.cachedEndpoint = endpointURL;
    }

    public EnhancedNotificationBrokerFactorySOAPBindingStub(javax.xml.rpc.Service service) throws org.apache.axis.AxisFault {
        if (service == null) {
            super.service = new org.apache.axis.client.Service();
        } else {
            super.service = service;
        }
        ((org.apache.axis.client.Service)super.service).setTypeMappingVersion("1.2");
            java.lang.Class cls;
            javax.xml.namespace.QName qName;
            javax.xml.namespace.QName qName2;
            java.lang.Class beansf = org.apache.axis.encoding.ser.BeanSerializerFactory.class;
            java.lang.Class beandf = org.apache.axis.encoding.ser.BeanDeserializerFactory.class;
            java.lang.Class enumsf = org.apache.axis.encoding.ser.EnumSerializerFactory.class;
            java.lang.Class enumdf = org.apache.axis.encoding.ser.EnumDeserializerFactory.class;
            java.lang.Class arraysf = org.apache.axis.encoding.ser.ArraySerializerFactory.class;
            java.lang.Class arraydf = org.apache.axis.encoding.ser.ArrayDeserializerFactory.class;
            java.lang.Class simplesf = org.apache.axis.encoding.ser.SimpleSerializerFactory.class;
            java.lang.Class simpledf = org.apache.axis.encoding.ser.SimpleDeserializerFactory.class;
            java.lang.Class simplelistsf = org.apache.axis.encoding.ser.SimpleListSerializerFactory.class;
            java.lang.Class simplelistdf = org.apache.axis.encoding.ser.SimpleListDeserializerFactory.class;
        addBindings0();
        addBindings1();
    }

    private void addBindings0() {
            java.lang.Class cls;
            javax.xml.namespace.QName qName;
            javax.xml.namespace.QName qName2;
            java.lang.Class beansf = org.apache.axis.encoding.ser.BeanSerializerFactory.class;
            java.lang.Class beandf = org.apache.axis.encoding.ser.BeanDeserializerFactory.class;
            java.lang.Class enumsf = org.apache.axis.encoding.ser.EnumSerializerFactory.class;
            java.lang.Class enumdf = org.apache.axis.encoding.ser.EnumDeserializerFactory.class;
            java.lang.Class arraysf = org.apache.axis.encoding.ser.ArraySerializerFactory.class;
            java.lang.Class arraydf = org.apache.axis.encoding.ser.ArrayDeserializerFactory.class;
            java.lang.Class simplesf = org.apache.axis.encoding.ser.SimpleSerializerFactory.class;
            java.lang.Class simpledf = org.apache.axis.encoding.ser.SimpleDeserializerFactory.class;
            java.lang.Class simplelistsf = org.apache.axis.encoding.ser.SimpleListSerializerFactory.class;
            java.lang.Class simplelistdf = org.apache.axis.encoding.ser.SimpleListDeserializerFactory.class;
            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200702", "EmptyType");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.docs.ws_sx.ws_securitypolicy._200702.EmptyType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200702", "HeaderType");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.docs.ws_sx.ws_securitypolicy._200702.HeaderType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200702", "IncludeTokenOpenType");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.docs.ws_sx.ws_securitypolicy._200702.IncludeTokenOpenType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(org.apache.axis.encoding.ser.BaseSerializerFactory.createFactory(org.apache.axis.encoding.ser.SimpleSerializerFactory.class, cls, qName));
            cachedDeserFactories.add(org.apache.axis.encoding.ser.BaseDeserializerFactory.createFactory(org.apache.axis.encoding.ser.SimpleDeserializerFactory.class, cls, qName));

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200702", "IncludeTokenType");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.docs.ws_sx.ws_securitypolicy._200702.IncludeTokenType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(enumsf);
            cachedDeserFactories.add(enumdf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200702", "IssuedTokenType");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.docs.ws_sx.ws_securitypolicy._200702.IssuedTokenType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200702", "KeyValueTokenType");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.docs.ws_sx.ws_securitypolicy._200702.KeyValueTokenType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200702", "NestedPolicyType");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.docs.ws_sx.ws_securitypolicy._200702.NestedPolicyType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200702", "QNameAssertionType");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.docs.ws_sx.ws_securitypolicy._200702.QNameAssertionType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200702", "RequestSecurityTokenTemplateType");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.docs.ws_sx.ws_securitypolicy._200702.RequestSecurityTokenTemplateType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200702", "SecureConversationTokenType");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.docs.ws_sx.ws_securitypolicy._200702.SecureConversationTokenType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200702", "SePartsType");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.docs.ws_sx.ws_securitypolicy._200702.SePartsType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200702", "SerElementsType");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.docs.ws_sx.ws_securitypolicy._200702.SerElementsType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200702", "SpnegoContextTokenType");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.docs.ws_sx.ws_securitypolicy._200702.SpnegoContextTokenType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200702", "TokenAssertionType");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.docs.ws_sx.ws_securitypolicy._200702.TokenAssertionType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", ">CreatePullPoint");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.wsn.base.CreatePullPoint_Element.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", ">CreatePullPointResponse");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.wsn.base.CreatePullPointResponse.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", ">DestroyPullPoint");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.wsn.base.DestroyPullPoint.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", ">DestroyPullPointResponse");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.wsn.base.DestroyPullPointResponse.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", ">GetCurrentMessage");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.wsn.base.GetCurrentMessage.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", ">GetCurrentMessageResponse");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.wsn.base.GetCurrentMessageResponse.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", ">GetMessages");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.wsn.base.GetMessages.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", ">GetMessagesResponse");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.wsn.base.GetMessagesResponse.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", ">NotificationMessageHolderType>Message");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.wsn.base.NotificationMessageHolderTypeMessage.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", ">NotificationProducerRP");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.wsn.base.NotificationProducerRP.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", ">Notify");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.wsn.base.Notify.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", ">PauseSubscription");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.wsn.base.PauseSubscription.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", ">PauseSubscriptionResponse");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.wsn.base.PauseSubscriptionResponse.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", ">Renew");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.wsn.base.Renew.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", ">RenewResponse");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.wsn.base.RenewResponse.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", ">ResumeSubscription");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.wsn.base.ResumeSubscription.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", ">ResumeSubscriptionResponse");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.wsn.base.ResumeSubscriptionResponse.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", ">Subscribe");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.wsn.base.Subscribe.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", ">SubscribeResponse");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.wsn.base.SubscribeResponse.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", ">SubscriptionManagerRP");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.wsn.base.SubscriptionManagerRP.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", ">Unsubscribe");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.wsn.base.Unsubscribe.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", ">UnsubscribeResponse");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.wsn.base.UnsubscribeResponse.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", ">UseRaw");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.wsn.base.UseRaw.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", "AbsoluteOrRelativeTimeType");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.wsn.base.AbsoluteOrRelativeTimeType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(org.apache.axis.encoding.ser.BaseSerializerFactory.createFactory(org.apache.axis.encoding.ser.SimpleSerializerFactory.class, cls, qName));
            cachedDeserFactories.add(org.apache.axis.encoding.ser.BaseDeserializerFactory.createFactory(org.apache.axis.encoding.ser.SimpleDeserializerFactory.class, cls, qName));

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", "FilterType");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.wsn.base.FilterType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", "InvalidFilterFaultType");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.wsn.base.InvalidFilterFaultType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", "InvalidMessageContentExpressionFaultType");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.wsn.base.InvalidMessageContentExpressionFaultType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", "InvalidProducerPropertiesExpressionFaultType");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.wsn.base.InvalidProducerPropertiesExpressionFaultType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", "InvalidTopicExpressionFaultType");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.wsn.base.InvalidTopicExpressionFaultType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", "MultipleTopicsSpecifiedFaultType");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.wsn.base.MultipleTopicsSpecifiedFaultType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", "NoCurrentMessageOnTopicFaultType");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.wsn.base.NoCurrentMessageOnTopicFaultType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", "NotificationMessageHolderType");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.wsn.base.NotificationMessageHolderType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", "NotifyMessageNotSupportedFaultType");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.wsn.base.NotifyMessageNotSupportedFaultType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", "PauseFailedFaultType");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.wsn.base.PauseFailedFaultType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", "QueryExpressionType");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.wsn.base.QueryExpressionType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", "ResumeFailedFaultType");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.wsn.base.ResumeFailedFaultType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", "SubscribeCreationFailedFaultType");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.wsn.base.SubscribeCreationFailedFaultType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", "SubscriptionPolicyType");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.wsn.base.SubscriptionPolicyType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", "TopicExpressionDialectUnknownFaultType");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.wsn.base.TopicExpressionDialectUnknownFaultType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", "TopicExpressionType");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.wsn.base.TopicExpressionType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", "TopicNotSupportedFaultType");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.wsn.base.TopicNotSupportedFaultType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", "UnableToCreatePullPointFaultType");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.wsn.base.UnableToCreatePullPointFaultType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", "UnableToDestroyPullPointFaultType");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.wsn.base.UnableToDestroyPullPointFaultType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", "UnableToDestroySubscriptionFaultType");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.wsn.base.UnableToDestroySubscriptionFaultType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", "UnableToGetMessagesFaultType");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.wsn.base.UnableToGetMessagesFaultType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", "UnacceptableInitialTerminationTimeFaultType");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.wsn.base.UnacceptableInitialTerminationTimeFaultType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", "UnacceptableTerminationTimeFaultType");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.wsn.base.UnacceptableTerminationTimeFaultType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", "UnrecognizedPolicyRequestFaultType");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.wsn.base.UnrecognizedPolicyRequestFaultType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/b-2", "UnsupportedPolicyRequestFaultType");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.wsn.base.UnsupportedPolicyRequestFaultType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/t-1", "Documentation");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.docs.wsn.t_1.Documentation.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/t-1", "ExtensibleDocumented");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.docs.wsn.t_1.ExtensibleDocumented.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wsn/t-1", "TopicSetType");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.docs.wsn.t_1.TopicSetType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/bf-2", ">BaseFaultType>Description");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.wsrf.basefaults.BaseFaultTypeDescription.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(org.apache.axis.encoding.ser.BaseSerializerFactory.createFactory(org.apache.axis.encoding.ser.SimpleSerializerFactory.class, cls, qName));
            cachedDeserFactories.add(org.apache.axis.encoding.ser.BaseDeserializerFactory.createFactory(org.apache.axis.encoding.ser.SimpleDeserializerFactory.class, cls, qName));

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/bf-2", ">BaseFaultType>ErrorCode");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.wsrf.basefaults.BaseFaultTypeErrorCode.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/bf-2", ">BaseFaultType>FaultCause");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.wsrf.basefaults.BaseFaultTypeFaultCause.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/bf-2", "BaseFaultType");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.wsrf.basefaults.BaseFaultType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/r-2", "ResourceUnavailableFaultType");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.docs.wsrf.r_2.ResourceUnavailableFaultType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/r-2", "ResourceUnknownFaultType");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rl-2", ">Destroy");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.docs.wsrf.rl_2.Destroy.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rl-2", ">DestroyResponse");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.docs.wsrf.rl_2.DestroyResponse.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rl-2", ">SetTerminationTime");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.docs.wsrf.rl_2.SetTerminationTime.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rl-2", ">SetTerminationTimeResponse");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.docs.wsrf.rl_2.SetTerminationTimeResponse.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rl-2", ">TerminationNotification");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.docs.wsrf.rl_2.TerminationNotification.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rl-2", "ResourceNotDestroyedFaultType");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.docs.wsrf.rl_2.ResourceNotDestroyedFaultType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rl-2", "TerminationTimeChangeRejectedFaultType");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.docs.wsrf.rl_2.TerminationTimeChangeRejectedFaultType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rl-2", "UnableToSetTerminationTimeFaultType");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.docs.wsrf.rl_2.UnableToSetTerminationTimeFaultType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", ">DeleteResourceProperties");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.docs.wsrf.rp_2.DeleteResourceProperties.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", ">DeleteResourcePropertiesResponse");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.docs.wsrf.rp_2.DeleteResourcePropertiesResponse.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", ">GetMultipleResourceProperties");
            cachedSerQNames.add(qName);
            cls = javax.xml.namespace.QName[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "QName");
            qName2 = new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", "ResourceProperty");
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", ">GetMultipleResourcePropertiesResponse");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.docs.wsrf.rp_2.GetMultipleResourcePropertiesResponse.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", ">GetResourcePropertyDocument");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.docs.wsrf.rp_2.GetResourcePropertyDocument.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", ">GetResourcePropertyDocumentResponse");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.docs.wsrf.rp_2.GetResourcePropertyDocumentResponse.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", ">GetResourcePropertyResponse");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.docs.wsrf.rp_2.GetResourcePropertyResponse.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", ">InsertResourceProperties");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.docs.wsrf.rp_2.InsertResourceProperties.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", ">InsertResourcePropertiesResponse");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.docs.wsrf.rp_2.InsertResourcePropertiesResponse.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", ">PutResourcePropertyDocument");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.docs.wsrf.rp_2.PutResourcePropertyDocument.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", ">PutResourcePropertyDocumentResponse");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.docs.wsrf.rp_2.PutResourcePropertyDocumentResponse.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", ">QueryExpressionRPDocument");
            cachedSerQNames.add(qName);
            cls = org.apache.axis.types.URI[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", "QueryExpressionDialect");
            qName2 = null;
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", ">QueryResourceProperties");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.docs.wsrf.rp_2.QueryResourceProperties.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", ">QueryResourcePropertiesResponse");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.docs.wsrf.rp_2.QueryResourcePropertiesResponse.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", ">ResourcePropertyChangeFailureType>CurrentValue");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.docs.wsrf.rp_2.ResourcePropertyChangeFailureTypeCurrentValue.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", ">ResourcePropertyChangeFailureType>RequestedValue");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.docs.wsrf.rp_2.ResourcePropertyChangeFailureTypeRequestedValue.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", ">ResourcePropertyValueChangeNotificationType>NewValues");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.docs.wsrf.rp_2.ResourcePropertyValueChangeNotificationTypeNewValues.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", ">ResourcePropertyValueChangeNotificationType>OldValues");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.docs.wsrf.rp_2.ResourcePropertyValueChangeNotificationTypeOldValues.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", ">SetResourceProperties");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.docs.wsrf.rp_2.SetResourceProperties.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", ">SetResourcePropertiesResponse");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.docs.wsrf.rp_2.SetResourcePropertiesResponse.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

    }
    private void addBindings1() {
            java.lang.Class cls;
            javax.xml.namespace.QName qName;
            javax.xml.namespace.QName qName2;
            java.lang.Class beansf = org.apache.axis.encoding.ser.BeanSerializerFactory.class;
            java.lang.Class beandf = org.apache.axis.encoding.ser.BeanDeserializerFactory.class;
            java.lang.Class enumsf = org.apache.axis.encoding.ser.EnumSerializerFactory.class;
            java.lang.Class enumdf = org.apache.axis.encoding.ser.EnumDeserializerFactory.class;
            java.lang.Class arraysf = org.apache.axis.encoding.ser.ArraySerializerFactory.class;
            java.lang.Class arraydf = org.apache.axis.encoding.ser.ArrayDeserializerFactory.class;
            java.lang.Class simplesf = org.apache.axis.encoding.ser.SimpleSerializerFactory.class;
            java.lang.Class simpledf = org.apache.axis.encoding.ser.SimpleDeserializerFactory.class;
            java.lang.Class simplelistsf = org.apache.axis.encoding.ser.SimpleListSerializerFactory.class;
            java.lang.Class simplelistdf = org.apache.axis.encoding.ser.SimpleListDeserializerFactory.class;
            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", ">UpdateResourceProperties");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.docs.wsrf.rp_2.UpdateResourceProperties.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", ">UpdateResourcePropertiesResponse");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.docs.wsrf.rp_2.UpdateResourcePropertiesResponse.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", "DeleteResourcePropertiesRequestFailedFaultType");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.docs.wsrf.rp_2.DeleteResourcePropertiesRequestFailedFaultType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", "DeleteType");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.docs.wsrf.rp_2.DeleteType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", "InsertResourcePropertiesRequestFailedFaultType");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.docs.wsrf.rp_2.InsertResourcePropertiesRequestFailedFaultType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", "InsertType");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.docs.wsrf.rp_2.InsertType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", "InvalidModificationFaultType");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.docs.wsrf.rp_2.InvalidModificationFaultType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", "InvalidQueryExpressionFaultType");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.docs.wsrf.rp_2.InvalidQueryExpressionFaultType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", "InvalidResourcePropertyQNameFaultType");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.docs.wsrf.rp_2.InvalidResourcePropertyQNameFaultType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", "QueryEvaluationErrorFaultType");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.docs.wsrf.rp_2.QueryEvaluationErrorFaultType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", "QueryExpressionType");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.docs.wsrf.rp_2.QueryExpressionType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", "ResourcePropertyChangeFailureType");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.docs.wsrf.rp_2.ResourcePropertyChangeFailureType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", "ResourcePropertyValueChangeNotificationType");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.docs.wsrf.rp_2.ResourcePropertyValueChangeNotificationType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", "SetResourcePropertyRequestFailedFaultType");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.docs.wsrf.rp_2.SetResourcePropertyRequestFailedFaultType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", "UnableToModifyResourcePropertyFaultType");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.docs.wsrf.rp_2.UnableToModifyResourcePropertyFaultType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", "UnableToPutResourcePropertyDocumentFaultType");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.docs.wsrf.rp_2.UnableToPutResourcePropertyDocumentFaultType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", "UnknownQueryExpressionDialectFaultType");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.docs.wsrf.rp_2.UnknownQueryExpressionDialectFaultType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", "UpdateResourcePropertiesRequestFailedFaultType");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.docs.wsrf.rp_2.UpdateResourcePropertiesRequestFailedFaultType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/rp-2", "UpdateType");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.docs.wsrf.rp_2.UpdateType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd", "AttributedString");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.docs.wss._2004._01.oasis_200401_wss_wssecurity_secext_1_0_xsd.AttributedString.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(org.apache.axis.encoding.ser.BaseSerializerFactory.createFactory(org.apache.axis.encoding.ser.SimpleSerializerFactory.class, cls, qName));
            cachedDeserFactories.add(org.apache.axis.encoding.ser.BaseDeserializerFactory.createFactory(org.apache.axis.encoding.ser.SimpleDeserializerFactory.class, cls, qName));

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd", "BinarySecurityTokenType");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.docs.wss._2004._01.oasis_200401_wss_wssecurity_secext_1_0_xsd.BinarySecurityTokenType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(org.apache.axis.encoding.ser.BaseSerializerFactory.createFactory(org.apache.axis.encoding.ser.SimpleSerializerFactory.class, cls, qName));
            cachedDeserFactories.add(org.apache.axis.encoding.ser.BaseDeserializerFactory.createFactory(org.apache.axis.encoding.ser.SimpleDeserializerFactory.class, cls, qName));

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd", "EmbeddedType");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.docs.wss._2004._01.oasis_200401_wss_wssecurity_secext_1_0_xsd.EmbeddedType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd", "EncodedString");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.docs.wss._2004._01.oasis_200401_wss_wssecurity_secext_1_0_xsd.EncodedString.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(org.apache.axis.encoding.ser.BaseSerializerFactory.createFactory(org.apache.axis.encoding.ser.SimpleSerializerFactory.class, cls, qName));
            cachedDeserFactories.add(org.apache.axis.encoding.ser.BaseDeserializerFactory.createFactory(org.apache.axis.encoding.ser.SimpleDeserializerFactory.class, cls, qName));

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd", "FaultcodeEnum");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.docs.wss._2004._01.oasis_200401_wss_wssecurity_secext_1_0_xsd.FaultcodeEnum.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(enumsf);
            cachedDeserFactories.add(enumdf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd", "KeyIdentifierType");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.docs.wss._2004._01.oasis_200401_wss_wssecurity_secext_1_0_xsd.KeyIdentifierType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(org.apache.axis.encoding.ser.BaseSerializerFactory.createFactory(org.apache.axis.encoding.ser.SimpleSerializerFactory.class, cls, qName));
            cachedDeserFactories.add(org.apache.axis.encoding.ser.BaseDeserializerFactory.createFactory(org.apache.axis.encoding.ser.SimpleDeserializerFactory.class, cls, qName));

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd", "PasswordString");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.docs.wss._2004._01.oasis_200401_wss_wssecurity_secext_1_0_xsd.PasswordString.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(org.apache.axis.encoding.ser.BaseSerializerFactory.createFactory(org.apache.axis.encoding.ser.SimpleSerializerFactory.class, cls, qName));
            cachedDeserFactories.add(org.apache.axis.encoding.ser.BaseDeserializerFactory.createFactory(org.apache.axis.encoding.ser.SimpleDeserializerFactory.class, cls, qName));

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd", "ReferenceType");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.docs.wss._2004._01.oasis_200401_wss_wssecurity_secext_1_0_xsd.ReferenceType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd", "SecurityHeaderType");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.docs.wss._2004._01.oasis_200401_wss_wssecurity_secext_1_0_xsd.SecurityHeaderType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd", "SecurityTokenReferenceType");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.docs.wss._2004._01.oasis_200401_wss_wssecurity_secext_1_0_xsd.SecurityTokenReferenceType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd", "TransformationParametersType");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.docs.wss._2004._01.oasis_200401_wss_wssecurity_secext_1_0_xsd.TransformationParametersType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd", "tUsage");
            cachedSerQNames.add(qName);
            cls = org.apache.axis.types.URI[].class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(simplelistsf);
            cachedDeserFactories.add(simplelistdf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd", "UsernameTokenType");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.docs.wss._2004._01.oasis_200401_wss_wssecurity_secext_1_0_xsd.UsernameTokenType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd", "AttributedDateTime");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.docs.wss._2004._01.oasis_200401_wss_wssecurity_utility_1_0_xsd.AttributedDateTime.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(org.apache.axis.encoding.ser.BaseSerializerFactory.createFactory(org.apache.axis.encoding.ser.SimpleSerializerFactory.class, cls, qName));
            cachedDeserFactories.add(org.apache.axis.encoding.ser.BaseDeserializerFactory.createFactory(org.apache.axis.encoding.ser.SimpleDeserializerFactory.class, cls, qName));

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd", "AttributedURI");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.docs.wss._2004._01.oasis_200401_wss_wssecurity_utility_1_0_xsd.AttributedURI.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(org.apache.axis.encoding.ser.BaseSerializerFactory.createFactory(org.apache.axis.encoding.ser.SimpleSerializerFactory.class, cls, qName));
            cachedDeserFactories.add(org.apache.axis.encoding.ser.BaseDeserializerFactory.createFactory(org.apache.axis.encoding.ser.SimpleDeserializerFactory.class, cls, qName));

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd", "TimestampType");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.docs.wss._2004._01.oasis_200401_wss_wssecurity_utility_1_0_xsd.TimestampType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd", "tTimestampFault");
            cachedSerQNames.add(qName);
            cls = org.oasis_open.docs.wss._2004._01.oasis_200401_wss_wssecurity_utility_1_0_xsd.TTimestampFault.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(enumsf);
            cachedDeserFactories.add(enumdf);

            qName = new javax.xml.namespace.QName("http://schemas.ggf.org/ogsa/2006/05/wsrf-bp", "QNameListType");
            cachedSerQNames.add(qName);
            cls = javax.xml.namespace.QName[].class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(simplelistsf);
            cachedDeserFactories.add(simplelistdf);

            qName = new javax.xml.namespace.QName("http://schemas.ogf.org/ws-iterator/2008/06/iterator", "IterableElementType");
            cachedSerQNames.add(qName);
            cls = edu.virginia.vcgr.genii.iterator.IterableElementType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://schemas.ogf.org/ws-iterator/2008/06/iterator", "IterateRequestType");
            cachedSerQNames.add(qName);
            cls = edu.virginia.vcgr.genii.iterator.IterateRequestType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://schemas.ogf.org/ws-iterator/2008/06/iterator", "IterateResponseType");
            cachedSerQNames.add(qName);
            cls = edu.virginia.vcgr.genii.iterator.IterateResponseType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/container/2011/07/enhanced-notification-broker-factory", "BrokerWithForwardingPortCreateRequestType");
            cachedSerQNames.add(qName);
            cls = edu.virginia.vcgr.genii.notification.factory.BrokerWithForwardingPortCreateRequestType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/container/2011/07/enhanced-notification-broker-factory", "EnhancedNotificationBrokerFactoryFaultType");
            cachedSerQNames.add(qName);
            cls = edu.virginia.vcgr.genii.notification.factory.EnhancedNotificationBrokerFactoryFaultType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/container/2011/07/enhanced-notification-broker-factory", "NotificationBrokerCreationFailedFaultType");
            cachedSerQNames.add(qName);
            cls = edu.virginia.vcgr.genii.notification.factory.NotificationBrokerCreationFailedFaultType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/2006/12/common", "AddMatchingParameterRequestType");
            cachedSerQNames.add(qName);
            cls = edu.virginia.vcgr.genii.common.MatchingParameter[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/2006/12/common", "MatchingParameter");
            qName2 = new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/2006/12/common", "matching-parameter");
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/2006/12/common", "AddMatchingParameterResponseType");
            cachedSerQNames.add(qName);
            cls = edu.virginia.vcgr.genii.common.AddMatchingParameterResponseType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/2006/12/common", "dummy");
            cachedSerQNames.add(qName);
            cls = edu.virginia.vcgr.genii.common.Dummy.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/2006/12/common", "HistoryEventBundleType");
            cachedSerQNames.add(qName);
            cls = edu.virginia.vcgr.genii.common.HistoryEventBundleType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/2006/12/common", "IterateHistoryEventsRequestType");
            cachedSerQNames.add(qName);
            cls = edu.virginia.vcgr.genii.common.IterateHistoryEventsRequestType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/2006/12/common", "IterateHistoryEventsResponseType");
            cachedSerQNames.add(qName);
            cls = edu.virginia.vcgr.genii.common.IterateHistoryEventsResponseType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/2006/12/common", "LogEntryType");
            cachedSerQNames.add(qName);
            cls = edu.virginia.vcgr.genii.common.LogEntryType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/2006/12/common", "LogHierarchyEntryType");
            cachedSerQNames.add(qName);
            cls = edu.virginia.vcgr.genii.common.LogHierarchyEntryType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/2006/12/common", "MatchingParameter");
            cachedSerQNames.add(qName);
            cls = edu.virginia.vcgr.genii.common.MatchingParameter.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/2006/12/common", "RemoveMatchingParameterRequestType");
            cachedSerQNames.add(qName);
            cls = edu.virginia.vcgr.genii.common.MatchingParameter[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/2006/12/common", "MatchingParameter");
            qName2 = new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/2006/12/common", "matching-parameter");
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/2006/12/common", "RemoveMatchingParameterResponseType");
            cachedSerQNames.add(qName);
            cls = edu.virginia.vcgr.genii.common.RemoveMatchingParameterResponseType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/2006/12/common", "RPCCallerType");
            cachedSerQNames.add(qName);
            cls = edu.virginia.vcgr.genii.common.RPCCallerType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/2006/12/common", "RPCMetadataType");
            cachedSerQNames.add(qName);
            cls = edu.virginia.vcgr.genii.common.RPCMetadataType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/2006/12/common", "XMLCommandFunction");
            cachedSerQNames.add(qName);
            cls = edu.virginia.vcgr.genii.common.XMLCommandFunction.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/2006/12/common", "XMLCommandParameter");
            cachedSerQNames.add(qName);
            cls = edu.virginia.vcgr.genii.common.XMLCommandParameter.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/2006/12/context", "ContextNameValuePairType");
            cachedSerQNames.add(qName);
            cls = edu.virginia.vcgr.genii.context.ContextNameValuePairType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/2006/12/context", "ContextType");
            cachedSerQNames.add(qName);
            cls = edu.virginia.vcgr.genii.context.ContextType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/2006/12/context", "RNSPathDataType");
            cachedSerQNames.add(qName);
            cls = edu.virginia.vcgr.genii.context.RNSPathElementType[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/2006/12/context", "RNSPathElementType");
            qName2 = new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/2006/12/context", "path-element");
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/2006/12/context", "RNSPathElementType");
            cachedSerQNames.add(qName);
            cls = edu.virginia.vcgr.genii.context.RNSPathElementType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/2006/12/resource-factory", ">vcgrCreate");
            cachedSerQNames.add(qName);
            cls = edu.virginia.vcgr.genii.common.rfactory.VcgrCreate.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/2006/12/resource-factory", ">vcgrCreateResponse");
            cachedSerQNames.add(qName);
            cls = edu.virginia.vcgr.genii.common.rfactory.VcgrCreateResponse.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/2006/12/resource-factory", "ResourceCreationFaultType");
            cachedSerQNames.add(qName);
            cls = edu.virginia.vcgr.genii.common.rfactory.ResourceCreationFaultType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/2006/12/resource-simple", ">TryAgainFaultType");
            cachedSerQNames.add(qName);
            cls = edu.virginia.cs.vcgr.genii._2006._12.resource_simple.TryAgainFaultType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/2008/12/security", ">RequiredMessageSecurityType>min");
            cachedSerQNames.add(qName);
            cls = edu.virginia.vcgr.genii.common.security.RequiredMessageSecurityTypeMin.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(enumsf);
            cachedDeserFactories.add(enumdf);

            qName = new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/2008/12/security", "AclEntryListType");
            cachedSerQNames.add(qName);
            cls = edu.virginia.vcgr.genii.common.security.AclEntryListType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/2008/12/security", "AclType");
            cachedSerQNames.add(qName);
            cls = edu.virginia.vcgr.genii.common.security.AclType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/2008/12/security", "AuthZConfig");
            cachedSerQNames.add(qName);
            cls = edu.virginia.vcgr.genii.common.security.AuthZConfig.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/2008/12/security", "RequiredMessageSecurityType");
            cachedSerQNames.add(qName);
            cls = edu.virginia.vcgr.genii.common.security.RequiredMessageSecurityType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/2010/08/notification/notification-consumer", ">NotifyResponseType");
            cachedSerQNames.add(qName);
            cls = edu.virginia.vcgr.genii.common.notification.NotifyResponseType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/iterator-factory", "CreateIteratorRequestType");
            cachedSerQNames.add(qName);
            cls = edu.virginia.vcgr.genii.iterator.CreateIteratorRequestType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/iterator-factory", "CreateIteratorResponseType");
            cachedSerQNames.add(qName);
            cls = edu.virginia.vcgr.genii.iterator.CreateIteratorResponseType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/iterator-factory", "IteratorInitializationType");
            cachedSerQNames.add(qName);
            cls = edu.virginia.vcgr.genii.iterator.IteratorInitializationType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.w3.org/2005/08/addressing", "AttributedAnyType");
            cachedSerQNames.add(qName);
            cls = org.ws.addressing.AttributedAnyType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.w3.org/2005/08/addressing", "AttributedQNameType");
            cachedSerQNames.add(qName);
            cls = org.ws.addressing.AttributedQNameType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(org.apache.axis.encoding.ser.BaseSerializerFactory.createFactory(org.apache.axis.encoding.ser.SimpleSerializerFactory.class, cls, qName));
            cachedDeserFactories.add(org.apache.axis.encoding.ser.BaseDeserializerFactory.createFactory(org.apache.axis.encoding.ser.SimpleDeserializerFactory.class, cls, qName));

            qName = new javax.xml.namespace.QName("http://www.w3.org/2005/08/addressing", "AttributedUnsignedLongType");
            cachedSerQNames.add(qName);
            cls = org.ws.addressing.AttributedUnsignedLongType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(org.apache.axis.encoding.ser.BaseSerializerFactory.createFactory(org.apache.axis.encoding.ser.SimpleSerializerFactory.class, cls, qName));
            cachedDeserFactories.add(org.apache.axis.encoding.ser.BaseDeserializerFactory.createFactory(org.apache.axis.encoding.ser.SimpleDeserializerFactory.class, cls, qName));

            qName = new javax.xml.namespace.QName("http://www.w3.org/2005/08/addressing", "AttributedURIType");
            cachedSerQNames.add(qName);
            cls = org.ws.addressing.AttributedURIType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(org.apache.axis.encoding.ser.BaseSerializerFactory.createFactory(org.apache.axis.encoding.ser.SimpleSerializerFactory.class, cls, qName));
            cachedDeserFactories.add(org.apache.axis.encoding.ser.BaseDeserializerFactory.createFactory(org.apache.axis.encoding.ser.SimpleDeserializerFactory.class, cls, qName));

            qName = new javax.xml.namespace.QName("http://www.w3.org/2005/08/addressing", "EndpointReferenceType");
            cachedSerQNames.add(qName);
            cls = org.ws.addressing.EndpointReferenceType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.w3.org/2005/08/addressing", "FaultCodesOpenEnumType");
            cachedSerQNames.add(qName);
            cls = org.ws.addressing.FaultCodesOpenEnumType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(org.apache.axis.encoding.ser.BaseSerializerFactory.createFactory(org.apache.axis.encoding.ser.SimpleSerializerFactory.class, cls, qName));
            cachedDeserFactories.add(org.apache.axis.encoding.ser.BaseDeserializerFactory.createFactory(org.apache.axis.encoding.ser.SimpleDeserializerFactory.class, cls, qName));

            qName = new javax.xml.namespace.QName("http://www.w3.org/2005/08/addressing", "FaultCodesType");
            cachedSerQNames.add(qName);
            cls = org.ws.addressing.FaultCodesType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(enumsf);
            cachedDeserFactories.add(enumdf);

            qName = new javax.xml.namespace.QName("http://www.w3.org/2005/08/addressing", "MetadataType");
            cachedSerQNames.add(qName);
            cls = org.ws.addressing.MetadataType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.w3.org/2005/08/addressing", "ProblemActionType");
            cachedSerQNames.add(qName);
            cls = org.ws.addressing.ProblemActionType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.w3.org/2005/08/addressing", "ReferenceParametersType");
            cachedSerQNames.add(qName);
            cls = org.ws.addressing.ReferenceParametersType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.w3.org/2005/08/addressing", "RelatesToType");
            cachedSerQNames.add(qName);
            cls = org.ws.addressing.RelatesToType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(org.apache.axis.encoding.ser.BaseSerializerFactory.createFactory(org.apache.axis.encoding.ser.SimpleSerializerFactory.class, cls, qName));
            cachedDeserFactories.add(org.apache.axis.encoding.ser.BaseDeserializerFactory.createFactory(org.apache.axis.encoding.ser.SimpleDeserializerFactory.class, cls, qName));

            qName = new javax.xml.namespace.QName("http://www.w3.org/2005/08/addressing", "RelationshipType");
            cachedSerQNames.add(qName);
            cls = org.ws.addressing.RelationshipType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(enumsf);
            cachedDeserFactories.add(enumdf);

            qName = new javax.xml.namespace.QName("http://www.w3.org/2005/08/addressing", "RelationshipTypeOpenEnum");
            cachedSerQNames.add(qName);
            cls = org.ws.addressing.RelationshipTypeOpenEnum.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(org.apache.axis.encoding.ser.BaseSerializerFactory.createFactory(org.apache.axis.encoding.ser.SimpleSerializerFactory.class, cls, qName));
            cachedDeserFactories.add(org.apache.axis.encoding.ser.BaseDeserializerFactory.createFactory(org.apache.axis.encoding.ser.SimpleDeserializerFactory.class, cls, qName));

            qName = new javax.xml.namespace.QName("http://www.w3.org/ns/ws-policy", ">AppliesTo");
            cachedSerQNames.add(qName);
            cls = org.w3.www.ns.ws_policy.AppliesTo.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.w3.org/ns/ws-policy", ">Policy");
            cachedSerQNames.add(qName);
            cls = org.w3.www.ns.ws_policy.Policy.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.w3.org/ns/ws-policy", ">PolicyAttachment");
            cachedSerQNames.add(qName);
            cls = org.w3.www.ns.ws_policy.PolicyAttachment.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.w3.org/ns/ws-policy", ">PolicyReference");
            cachedSerQNames.add(qName);
            cls = org.w3.www.ns.ws_policy.PolicyReference.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.w3.org/ns/ws-policy", ">PolicyURIs");
            cachedSerQNames.add(qName);
            cls = org.apache.axis.types.URI[].class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(simplelistsf);
            cachedDeserFactories.add(simplelistdf);

            qName = new javax.xml.namespace.QName("http://www.w3.org/ns/ws-policy", ">URI");
            cachedSerQNames.add(qName);
            cls = org.w3.www.ns.ws_policy.URI.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(org.apache.axis.encoding.ser.BaseSerializerFactory.createFactory(org.apache.axis.encoding.ser.SimpleSerializerFactory.class, cls, qName));
            cachedDeserFactories.add(org.apache.axis.encoding.ser.BaseDeserializerFactory.createFactory(org.apache.axis.encoding.ser.SimpleDeserializerFactory.class, cls, qName));

            qName = new javax.xml.namespace.QName("http://www.w3.org/ns/ws-policy", "OperatorContentType");
            cachedSerQNames.add(qName);
            cls = org.w3.www.ns.ws_policy.OperatorContentType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

    }

    protected org.apache.axis.client.Call createCall() throws java.rmi.RemoteException {
        try {
            org.apache.axis.client.Call _call = super._createCall();
            if (super.maintainSessionSet) {
                _call.setMaintainSession(super.maintainSession);
            }
            if (super.cachedUsername != null) {
                _call.setUsername(super.cachedUsername);
            }
            if (super.cachedPassword != null) {
                _call.setPassword(super.cachedPassword);
            }
            if (super.cachedEndpoint != null) {
                _call.setTargetEndpointAddress(super.cachedEndpoint);
            }
            if (super.cachedTimeout != null) {
                _call.setTimeout(super.cachedTimeout);
            }
            if (super.cachedPortName != null) {
                _call.setPortName(super.cachedPortName);
            }
            java.util.Enumeration keys = super.cachedProperties.keys();
            while (keys.hasMoreElements()) {
                java.lang.String key = (java.lang.String) keys.nextElement();
                _call.setProperty(key, super.cachedProperties.get(key));
            }
            // All the type mapping information is registered
            // when the first call is made.
            // The type mapping information is actually registered in
            // the TypeMappingRegistry of the service, which
            // is the reason why registration is only needed for the first call.
/*CAK: somewhat ineffectual attempt to fix synchronization replaces use of 'this' below with 'service'.
but we kind of think we have found that axis is just not usable from multiple threads;
every object should be created individually and not reused.  we did also find that if we
synchronized on the service object, then another class of AXIS-2498 was prevented. */
			synchronized (service) {
                if (firstCall()) {
                    // must set encoding style before registering serializers
                    _call.setEncodingStyle(null);
                    for (int i = 0; i < cachedSerFactories.size(); ++i) {
                        java.lang.Class cls = (java.lang.Class) cachedSerClasses.get(i);
                        javax.xml.namespace.QName qName =
                                (javax.xml.namespace.QName) cachedSerQNames.get(i);
                        java.lang.Object x = cachedSerFactories.get(i);
                        if (x instanceof Class) {
                            java.lang.Class sf = (java.lang.Class)
                                 cachedSerFactories.get(i);
                            java.lang.Class df = (java.lang.Class)
                                 cachedDeserFactories.get(i);
                            _call.registerTypeMapping(cls, qName, sf, df, false);
                        }
                        else if (x instanceof javax.xml.rpc.encoding.SerializerFactory) {
                            org.apache.axis.encoding.SerializerFactory sf = (org.apache.axis.encoding.SerializerFactory)
                                 cachedSerFactories.get(i);
                            org.apache.axis.encoding.DeserializerFactory df = (org.apache.axis.encoding.DeserializerFactory)
                                 cachedDeserFactories.get(i);
                            _call.registerTypeMapping(cls, qName, sf, df, false);
                        }
                    }
                }
            }
            return _call;
        }
        catch (java.lang.Throwable _t) {
            throw new org.apache.axis.AxisFault("Failure trying to get the Call object", _t);
        }
    }

    public org.ws.addressing.EndpointReferenceType createNotificationBroker(long createNotificationBrokerRequest) throws java.rmi.RemoteException, edu.virginia.vcgr.genii.notification.factory.NotificationBrokerCreationFailedFaultType {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[0]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("", "createNotificationBroker"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {new java.lang.Long(createNotificationBrokerRequest)});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (org.ws.addressing.EndpointReferenceType) _resp;
            } catch (java.lang.Exception _exception) {
                return (org.ws.addressing.EndpointReferenceType) org.apache.axis.utils.JavaUtils.convert(_resp, org.ws.addressing.EndpointReferenceType.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof edu.virginia.vcgr.genii.notification.factory.NotificationBrokerCreationFailedFaultType) {
              throw (edu.virginia.vcgr.genii.notification.factory.NotificationBrokerCreationFailedFaultType) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public org.oasis_open.docs.wsrf.rp_2.GetResourcePropertyResponse getResourceProperty(javax.xml.namespace.QName getResourcePropertyRequest) throws java.rmi.RemoteException, org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType, org.oasis_open.docs.wsrf.rp_2.InvalidResourcePropertyQNameFaultType, org.oasis_open.docs.wsrf.r_2.ResourceUnavailableFaultType {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[1]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://docs.oasis-open.org/wsrf/rpw-2/GetResourceProperty/GetResourcePropertyRequest");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("", "GetResourceProperty"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {getResourcePropertyRequest});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (org.oasis_open.docs.wsrf.rp_2.GetResourcePropertyResponse) _resp;
            } catch (java.lang.Exception _exception) {
                return (org.oasis_open.docs.wsrf.rp_2.GetResourcePropertyResponse) org.apache.axis.utils.JavaUtils.convert(_resp, org.oasis_open.docs.wsrf.rp_2.GetResourcePropertyResponse.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType) {
              throw (org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof org.oasis_open.docs.wsrf.rp_2.InvalidResourcePropertyQNameFaultType) {
              throw (org.oasis_open.docs.wsrf.rp_2.InvalidResourcePropertyQNameFaultType) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof org.oasis_open.docs.wsrf.r_2.ResourceUnavailableFaultType) {
              throw (org.oasis_open.docs.wsrf.r_2.ResourceUnavailableFaultType) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public org.oasis_open.docs.wsrf.rp_2.InsertResourcePropertiesResponse insertResourceProperties(org.oasis_open.docs.wsrf.rp_2.InsertResourceProperties insertResourcePropertiesRequest) throws java.rmi.RemoteException, org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType, org.oasis_open.docs.wsrf.rp_2.UnableToModifyResourcePropertyFaultType, org.oasis_open.docs.wsrf.rp_2.InvalidModificationFaultType, org.oasis_open.docs.wsrf.rp_2.InsertResourcePropertiesRequestFailedFaultType, org.oasis_open.docs.wsrf.rp_2.InvalidResourcePropertyQNameFaultType, org.oasis_open.docs.wsrf.r_2.ResourceUnavailableFaultType {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[2]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("", "InsertResourceProperties"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {insertResourcePropertiesRequest});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (org.oasis_open.docs.wsrf.rp_2.InsertResourcePropertiesResponse) _resp;
            } catch (java.lang.Exception _exception) {
                return (org.oasis_open.docs.wsrf.rp_2.InsertResourcePropertiesResponse) org.apache.axis.utils.JavaUtils.convert(_resp, org.oasis_open.docs.wsrf.rp_2.InsertResourcePropertiesResponse.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType) {
              throw (org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof org.oasis_open.docs.wsrf.rp_2.UnableToModifyResourcePropertyFaultType) {
              throw (org.oasis_open.docs.wsrf.rp_2.UnableToModifyResourcePropertyFaultType) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof org.oasis_open.docs.wsrf.rp_2.InvalidModificationFaultType) {
              throw (org.oasis_open.docs.wsrf.rp_2.InvalidModificationFaultType) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof org.oasis_open.docs.wsrf.rp_2.InsertResourcePropertiesRequestFailedFaultType) {
              throw (org.oasis_open.docs.wsrf.rp_2.InsertResourcePropertiesRequestFailedFaultType) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof org.oasis_open.docs.wsrf.rp_2.InvalidResourcePropertyQNameFaultType) {
              throw (org.oasis_open.docs.wsrf.rp_2.InvalidResourcePropertyQNameFaultType) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof org.oasis_open.docs.wsrf.r_2.ResourceUnavailableFaultType) {
              throw (org.oasis_open.docs.wsrf.r_2.ResourceUnavailableFaultType) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public org.oasis_open.docs.wsrf.rp_2.QueryResourcePropertiesResponse queryResourceProperties(org.oasis_open.docs.wsrf.rp_2.QueryResourceProperties queryResourcePropertiesRequest) throws java.rmi.RemoteException, org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType, org.oasis_open.docs.wsrf.rp_2.QueryEvaluationErrorFaultType, org.oasis_open.docs.wsrf.rp_2.InvalidResourcePropertyQNameFaultType, org.oasis_open.docs.wsrf.rp_2.InvalidQueryExpressionFaultType, org.oasis_open.docs.wsrf.r_2.ResourceUnavailableFaultType, org.oasis_open.docs.wsrf.rp_2.UnknownQueryExpressionDialectFaultType {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[3]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://docs.oasis-open.org/wsrf/rpw-2/QueryResourceProperties/QueryResourcePropertiesRequest");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("", "QueryResourceProperties"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {queryResourcePropertiesRequest});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (org.oasis_open.docs.wsrf.rp_2.QueryResourcePropertiesResponse) _resp;
            } catch (java.lang.Exception _exception) {
                return (org.oasis_open.docs.wsrf.rp_2.QueryResourcePropertiesResponse) org.apache.axis.utils.JavaUtils.convert(_resp, org.oasis_open.docs.wsrf.rp_2.QueryResourcePropertiesResponse.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType) {
              throw (org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof org.oasis_open.docs.wsrf.rp_2.QueryEvaluationErrorFaultType) {
              throw (org.oasis_open.docs.wsrf.rp_2.QueryEvaluationErrorFaultType) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof org.oasis_open.docs.wsrf.rp_2.InvalidResourcePropertyQNameFaultType) {
              throw (org.oasis_open.docs.wsrf.rp_2.InvalidResourcePropertyQNameFaultType) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof org.oasis_open.docs.wsrf.rp_2.InvalidQueryExpressionFaultType) {
              throw (org.oasis_open.docs.wsrf.rp_2.InvalidQueryExpressionFaultType) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof org.oasis_open.docs.wsrf.r_2.ResourceUnavailableFaultType) {
              throw (org.oasis_open.docs.wsrf.r_2.ResourceUnavailableFaultType) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof org.oasis_open.docs.wsrf.rp_2.UnknownQueryExpressionDialectFaultType) {
              throw (org.oasis_open.docs.wsrf.rp_2.UnknownQueryExpressionDialectFaultType) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public void notify(org.oasis_open.wsn.base.Notify notify) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[4]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://docs.oasis-open.org/wsn/bw-2/NotificationConsumer/Notify");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("", "Notify"));

        setRequestHeaders(_call);
        setAttachments(_call);
        _call.invokeOneWay(new java.lang.Object[] {notify});

    }

    public java.lang.String ping(java.lang.String pingRequest) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[5]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("", "ping"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {pingRequest});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (java.lang.String) _resp;
            } catch (java.lang.Exception _exception) {
                return (java.lang.String) org.apache.axis.utils.JavaUtils.convert(_resp, java.lang.String.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public org.oasis_open.wsn.base.GetCurrentMessageResponse getCurrentMessage(org.oasis_open.wsn.base.GetCurrentMessage getCurrentMessageRequest) throws java.rmi.RemoteException, org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType, org.oasis_open.wsn.base.MultipleTopicsSpecifiedFaultType, org.oasis_open.wsn.base.InvalidTopicExpressionFaultType, org.oasis_open.wsn.base.NoCurrentMessageOnTopicFaultType, org.oasis_open.wsn.base.TopicExpressionDialectUnknownFaultType, org.oasis_open.wsn.base.TopicNotSupportedFaultType {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[6]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://docs.oasis-open.org/wsn/bw-2/NotificationProducer/GetCurrentMessageRequest");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("", "GetCurrentMessage"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {getCurrentMessageRequest});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (org.oasis_open.wsn.base.GetCurrentMessageResponse) _resp;
            } catch (java.lang.Exception _exception) {
                return (org.oasis_open.wsn.base.GetCurrentMessageResponse) org.apache.axis.utils.JavaUtils.convert(_resp, org.oasis_open.wsn.base.GetCurrentMessageResponse.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType) {
              throw (org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof org.oasis_open.wsn.base.MultipleTopicsSpecifiedFaultType) {
              throw (org.oasis_open.wsn.base.MultipleTopicsSpecifiedFaultType) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof org.oasis_open.wsn.base.InvalidTopicExpressionFaultType) {
              throw (org.oasis_open.wsn.base.InvalidTopicExpressionFaultType) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof org.oasis_open.wsn.base.NoCurrentMessageOnTopicFaultType) {
              throw (org.oasis_open.wsn.base.NoCurrentMessageOnTopicFaultType) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof org.oasis_open.wsn.base.TopicExpressionDialectUnknownFaultType) {
              throw (org.oasis_open.wsn.base.TopicExpressionDialectUnknownFaultType) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof org.oasis_open.wsn.base.TopicNotSupportedFaultType) {
              throw (org.oasis_open.wsn.base.TopicNotSupportedFaultType) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public org.oasis_open.docs.wsrf.rp_2.UpdateResourcePropertiesResponse updateResourceProperties(org.oasis_open.docs.wsrf.rp_2.UpdateResourceProperties updateResourcePropertiesRequest) throws java.rmi.RemoteException, org.oasis_open.docs.wsrf.rp_2.UpdateResourcePropertiesRequestFailedFaultType, org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType, org.oasis_open.docs.wsrf.rp_2.UnableToModifyResourcePropertyFaultType, org.oasis_open.docs.wsrf.rp_2.InvalidModificationFaultType, org.oasis_open.docs.wsrf.rp_2.InvalidResourcePropertyQNameFaultType, org.oasis_open.docs.wsrf.r_2.ResourceUnavailableFaultType {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[7]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("", "UpdateResourceProperties"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {updateResourcePropertiesRequest});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (org.oasis_open.docs.wsrf.rp_2.UpdateResourcePropertiesResponse) _resp;
            } catch (java.lang.Exception _exception) {
                return (org.oasis_open.docs.wsrf.rp_2.UpdateResourcePropertiesResponse) org.apache.axis.utils.JavaUtils.convert(_resp, org.oasis_open.docs.wsrf.rp_2.UpdateResourcePropertiesResponse.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof org.oasis_open.docs.wsrf.rp_2.UpdateResourcePropertiesRequestFailedFaultType) {
              throw (org.oasis_open.docs.wsrf.rp_2.UpdateResourcePropertiesRequestFailedFaultType) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType) {
              throw (org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof org.oasis_open.docs.wsrf.rp_2.UnableToModifyResourcePropertyFaultType) {
              throw (org.oasis_open.docs.wsrf.rp_2.UnableToModifyResourcePropertyFaultType) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof org.oasis_open.docs.wsrf.rp_2.InvalidModificationFaultType) {
              throw (org.oasis_open.docs.wsrf.rp_2.InvalidModificationFaultType) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof org.oasis_open.docs.wsrf.rp_2.InvalidResourcePropertyQNameFaultType) {
              throw (org.oasis_open.docs.wsrf.rp_2.InvalidResourcePropertyQNameFaultType) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof org.oasis_open.docs.wsrf.r_2.ResourceUnavailableFaultType) {
              throw (org.oasis_open.docs.wsrf.r_2.ResourceUnavailableFaultType) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public org.oasis_open.docs.wsrf.rl_2.SetTerminationTimeResponse setTerminationTime(org.oasis_open.docs.wsrf.rl_2.SetTerminationTime setTerminationTimeRequest) throws java.rmi.RemoteException, org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType, org.oasis_open.docs.wsrf.rl_2.UnableToSetTerminationTimeFaultType, org.oasis_open.docs.wsrf.r_2.ResourceUnavailableFaultType, org.oasis_open.docs.wsrf.rl_2.TerminationTimeChangeRejectedFaultType {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[8]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://docs.oasis-open.org/wsrf/rlw-2/ScheduledResourceTermination/SetTerminationTimeRequest");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("", "SetTerminationTime"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {setTerminationTimeRequest});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (org.oasis_open.docs.wsrf.rl_2.SetTerminationTimeResponse) _resp;
            } catch (java.lang.Exception _exception) {
                return (org.oasis_open.docs.wsrf.rl_2.SetTerminationTimeResponse) org.apache.axis.utils.JavaUtils.convert(_resp, org.oasis_open.docs.wsrf.rl_2.SetTerminationTimeResponse.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType) {
              throw (org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof org.oasis_open.docs.wsrf.rl_2.UnableToSetTerminationTimeFaultType) {
              throw (org.oasis_open.docs.wsrf.rl_2.UnableToSetTerminationTimeFaultType) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof org.oasis_open.docs.wsrf.r_2.ResourceUnavailableFaultType) {
              throw (org.oasis_open.docs.wsrf.r_2.ResourceUnavailableFaultType) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof org.oasis_open.docs.wsrf.rl_2.TerminationTimeChangeRejectedFaultType) {
              throw (org.oasis_open.docs.wsrf.rl_2.TerminationTimeChangeRejectedFaultType) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public org.ws.addressing.EndpointReferenceType createNotificationBrokerWithForwardingPort(edu.virginia.vcgr.genii.notification.factory.BrokerWithForwardingPortCreateRequestType brokerWithForwardingPortCreateRequest) throws java.rmi.RemoteException, edu.virginia.vcgr.genii.notification.factory.NotificationBrokerCreationFailedFaultType {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[9]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("", "createNotificationBrokerWithForwardingPort"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {brokerWithForwardingPortCreateRequest});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (org.ws.addressing.EndpointReferenceType) _resp;
            } catch (java.lang.Exception _exception) {
                return (org.ws.addressing.EndpointReferenceType) org.apache.axis.utils.JavaUtils.convert(_resp, org.ws.addressing.EndpointReferenceType.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof edu.virginia.vcgr.genii.notification.factory.NotificationBrokerCreationFailedFaultType) {
              throw (edu.virginia.vcgr.genii.notification.factory.NotificationBrokerCreationFailedFaultType) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public org.oasis_open.wsn.base.SubscribeResponse subscribe(org.oasis_open.wsn.base.Subscribe subscribeRequest) throws java.rmi.RemoteException, org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType, org.oasis_open.wsn.base.UnrecognizedPolicyRequestFaultType, org.oasis_open.wsn.base.NotifyMessageNotSupportedFaultType, org.oasis_open.wsn.base.SubscribeCreationFailedFaultType, org.oasis_open.wsn.base.UnacceptableInitialTerminationTimeFaultType, org.oasis_open.wsn.base.InvalidProducerPropertiesExpressionFaultType, org.oasis_open.wsn.base.InvalidFilterFaultType, org.oasis_open.wsn.base.InvalidTopicExpressionFaultType, org.oasis_open.wsn.base.InvalidMessageContentExpressionFaultType, org.oasis_open.wsn.base.TopicExpressionDialectUnknownFaultType, org.oasis_open.wsn.base.TopicNotSupportedFaultType, org.oasis_open.wsn.base.UnsupportedPolicyRequestFaultType {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[10]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://docs.oasis-open.org/wsn/bw-2/NotificationProducer/SubscribeRequest");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("", "Subscribe"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {subscribeRequest});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (org.oasis_open.wsn.base.SubscribeResponse) _resp;
            } catch (java.lang.Exception _exception) {
                return (org.oasis_open.wsn.base.SubscribeResponse) org.apache.axis.utils.JavaUtils.convert(_resp, org.oasis_open.wsn.base.SubscribeResponse.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType) {
              throw (org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof org.oasis_open.wsn.base.UnrecognizedPolicyRequestFaultType) {
              throw (org.oasis_open.wsn.base.UnrecognizedPolicyRequestFaultType) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof org.oasis_open.wsn.base.NotifyMessageNotSupportedFaultType) {
              throw (org.oasis_open.wsn.base.NotifyMessageNotSupportedFaultType) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof org.oasis_open.wsn.base.SubscribeCreationFailedFaultType) {
              throw (org.oasis_open.wsn.base.SubscribeCreationFailedFaultType) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof org.oasis_open.wsn.base.UnacceptableInitialTerminationTimeFaultType) {
              throw (org.oasis_open.wsn.base.UnacceptableInitialTerminationTimeFaultType) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof org.oasis_open.wsn.base.InvalidProducerPropertiesExpressionFaultType) {
              throw (org.oasis_open.wsn.base.InvalidProducerPropertiesExpressionFaultType) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof org.oasis_open.wsn.base.InvalidFilterFaultType) {
              throw (org.oasis_open.wsn.base.InvalidFilterFaultType) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof org.oasis_open.wsn.base.InvalidTopicExpressionFaultType) {
              throw (org.oasis_open.wsn.base.InvalidTopicExpressionFaultType) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof org.oasis_open.wsn.base.InvalidMessageContentExpressionFaultType) {
              throw (org.oasis_open.wsn.base.InvalidMessageContentExpressionFaultType) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof org.oasis_open.wsn.base.TopicExpressionDialectUnknownFaultType) {
              throw (org.oasis_open.wsn.base.TopicExpressionDialectUnknownFaultType) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof org.oasis_open.wsn.base.TopicNotSupportedFaultType) {
              throw (org.oasis_open.wsn.base.TopicNotSupportedFaultType) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof org.oasis_open.wsn.base.UnsupportedPolicyRequestFaultType) {
              throw (org.oasis_open.wsn.base.UnsupportedPolicyRequestFaultType) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public edu.virginia.vcgr.genii.common.RemoveMatchingParameterResponseType removeMatchingParameter(edu.virginia.vcgr.genii.common.MatchingParameter[] removeMatchingParameterRequest) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[11]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("", "removeMatchingParameter"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {removeMatchingParameterRequest});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (edu.virginia.vcgr.genii.common.RemoveMatchingParameterResponseType) _resp;
            } catch (java.lang.Exception _exception) {
                return (edu.virginia.vcgr.genii.common.RemoveMatchingParameterResponseType) org.apache.axis.utils.JavaUtils.convert(_resp, edu.virginia.vcgr.genii.common.RemoveMatchingParameterResponseType.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public edu.virginia.vcgr.genii.common.AddMatchingParameterResponseType addMatchingParameter(edu.virginia.vcgr.genii.common.MatchingParameter[] addMatchingParameterRequest) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[12]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("", "addMatchingParameter"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {addMatchingParameterRequest});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (edu.virginia.vcgr.genii.common.AddMatchingParameterResponseType) _resp;
            } catch (java.lang.Exception _exception) {
                return (edu.virginia.vcgr.genii.common.AddMatchingParameterResponseType) org.apache.axis.utils.JavaUtils.convert(_resp, edu.virginia.vcgr.genii.common.AddMatchingParameterResponseType.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public edu.virginia.vcgr.genii.common.rfactory.VcgrCreateResponse vcgrCreate(edu.virginia.vcgr.genii.common.rfactory.VcgrCreate vcgrCreateRequest) throws java.rmi.RemoteException, org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType, edu.virginia.vcgr.genii.common.rfactory.ResourceCreationFaultType {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[13]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("", "vcgrCreate"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {vcgrCreateRequest});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (edu.virginia.vcgr.genii.common.rfactory.VcgrCreateResponse) _resp;
            } catch (java.lang.Exception _exception) {
                return (edu.virginia.vcgr.genii.common.rfactory.VcgrCreateResponse) org.apache.axis.utils.JavaUtils.convert(_resp, edu.virginia.vcgr.genii.common.rfactory.VcgrCreateResponse.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType) {
              throw (org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof edu.virginia.vcgr.genii.common.rfactory.ResourceCreationFaultType) {
              throw (edu.virginia.vcgr.genii.common.rfactory.ResourceCreationFaultType) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public org.oasis_open.docs.wsrf.rp_2.GetResourcePropertyDocumentResponse getResourcePropertyDocument(org.oasis_open.docs.wsrf.rp_2.GetResourcePropertyDocument getResourcePropertyDocumentRequest) throws java.rmi.RemoteException, org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType, org.oasis_open.docs.wsrf.r_2.ResourceUnavailableFaultType {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[14]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://docs.oasis-open.org/wsrf/rpw-2/GetResourcePropertyDocument/GetResourcePropertyDocumentRequest");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("", "GetResourcePropertyDocument"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {getResourcePropertyDocumentRequest});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (org.oasis_open.docs.wsrf.rp_2.GetResourcePropertyDocumentResponse) _resp;
            } catch (java.lang.Exception _exception) {
                return (org.oasis_open.docs.wsrf.rp_2.GetResourcePropertyDocumentResponse) org.apache.axis.utils.JavaUtils.convert(_resp, org.oasis_open.docs.wsrf.rp_2.GetResourcePropertyDocumentResponse.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType) {
              throw (org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof org.oasis_open.docs.wsrf.r_2.ResourceUnavailableFaultType) {
              throw (org.oasis_open.docs.wsrf.r_2.ResourceUnavailableFaultType) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public org.oasis_open.docs.wsrf.rp_2.SetResourcePropertiesResponse setResourceProperties(org.oasis_open.docs.wsrf.rp_2.SetResourceProperties setResourcePropertiesRequest) throws java.rmi.RemoteException, org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType, org.oasis_open.docs.wsrf.rp_2.UnableToModifyResourcePropertyFaultType, org.oasis_open.docs.wsrf.rp_2.InvalidModificationFaultType, org.oasis_open.docs.wsrf.rp_2.InvalidResourcePropertyQNameFaultType, org.oasis_open.docs.wsrf.r_2.ResourceUnavailableFaultType, org.oasis_open.docs.wsrf.rp_2.SetResourcePropertyRequestFailedFaultType {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[15]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("", "SetResourceProperties"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {setResourcePropertiesRequest});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (org.oasis_open.docs.wsrf.rp_2.SetResourcePropertiesResponse) _resp;
            } catch (java.lang.Exception _exception) {
                return (org.oasis_open.docs.wsrf.rp_2.SetResourcePropertiesResponse) org.apache.axis.utils.JavaUtils.convert(_resp, org.oasis_open.docs.wsrf.rp_2.SetResourcePropertiesResponse.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType) {
              throw (org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof org.oasis_open.docs.wsrf.rp_2.UnableToModifyResourcePropertyFaultType) {
              throw (org.oasis_open.docs.wsrf.rp_2.UnableToModifyResourcePropertyFaultType) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof org.oasis_open.docs.wsrf.rp_2.InvalidModificationFaultType) {
              throw (org.oasis_open.docs.wsrf.rp_2.InvalidModificationFaultType) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof org.oasis_open.docs.wsrf.rp_2.InvalidResourcePropertyQNameFaultType) {
              throw (org.oasis_open.docs.wsrf.rp_2.InvalidResourcePropertyQNameFaultType) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof org.oasis_open.docs.wsrf.r_2.ResourceUnavailableFaultType) {
              throw (org.oasis_open.docs.wsrf.r_2.ResourceUnavailableFaultType) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof org.oasis_open.docs.wsrf.rp_2.SetResourcePropertyRequestFailedFaultType) {
              throw (org.oasis_open.docs.wsrf.rp_2.SetResourcePropertyRequestFailedFaultType) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public org.oasis_open.docs.wsrf.rp_2.DeleteResourcePropertiesResponse deleteResourceProperties(org.oasis_open.docs.wsrf.rp_2.DeleteResourceProperties deleteResourcePropertiesRequest) throws java.rmi.RemoteException, org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType, org.oasis_open.docs.wsrf.rp_2.UnableToModifyResourcePropertyFaultType, org.oasis_open.docs.wsrf.rp_2.InvalidModificationFaultType, org.oasis_open.docs.wsrf.rp_2.DeleteResourcePropertiesRequestFailedFaultType, org.oasis_open.docs.wsrf.rp_2.InvalidResourcePropertyQNameFaultType, org.oasis_open.docs.wsrf.r_2.ResourceUnavailableFaultType {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[16]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("", "DeleteResourceProperties"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {deleteResourcePropertiesRequest});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (org.oasis_open.docs.wsrf.rp_2.DeleteResourcePropertiesResponse) _resp;
            } catch (java.lang.Exception _exception) {
                return (org.oasis_open.docs.wsrf.rp_2.DeleteResourcePropertiesResponse) org.apache.axis.utils.JavaUtils.convert(_resp, org.oasis_open.docs.wsrf.rp_2.DeleteResourcePropertiesResponse.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType) {
              throw (org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof org.oasis_open.docs.wsrf.rp_2.UnableToModifyResourcePropertyFaultType) {
              throw (org.oasis_open.docs.wsrf.rp_2.UnableToModifyResourcePropertyFaultType) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof org.oasis_open.docs.wsrf.rp_2.InvalidModificationFaultType) {
              throw (org.oasis_open.docs.wsrf.rp_2.InvalidModificationFaultType) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof org.oasis_open.docs.wsrf.rp_2.DeleteResourcePropertiesRequestFailedFaultType) {
              throw (org.oasis_open.docs.wsrf.rp_2.DeleteResourcePropertiesRequestFailedFaultType) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof org.oasis_open.docs.wsrf.rp_2.InvalidResourcePropertyQNameFaultType) {
              throw (org.oasis_open.docs.wsrf.rp_2.InvalidResourcePropertyQNameFaultType) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof org.oasis_open.docs.wsrf.r_2.ResourceUnavailableFaultType) {
              throw (org.oasis_open.docs.wsrf.r_2.ResourceUnavailableFaultType) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public edu.virginia.vcgr.genii.common.IterateHistoryEventsResponseType iterateHistoryEvents(edu.virginia.vcgr.genii.common.IterateHistoryEventsRequestType iterateHistoryEventsRequest) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[17]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("", "iterateHistoryEvents"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {iterateHistoryEventsRequest});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (edu.virginia.vcgr.genii.common.IterateHistoryEventsResponseType) _resp;
            } catch (java.lang.Exception _exception) {
                return (edu.virginia.vcgr.genii.common.IterateHistoryEventsResponseType) org.apache.axis.utils.JavaUtils.convert(_resp, edu.virginia.vcgr.genii.common.IterateHistoryEventsResponseType.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public org.oasis_open.docs.wsrf.rl_2.DestroyResponse destroy(org.oasis_open.docs.wsrf.rl_2.Destroy destroyRequest) throws java.rmi.RemoteException, org.oasis_open.docs.wsrf.rl_2.ResourceNotDestroyedFaultType, org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType, org.oasis_open.docs.wsrf.r_2.ResourceUnavailableFaultType {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[18]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://docs.oasis-open.org/wsrf/rlw-2/ImmediateResourceTermination/DestroyRequest");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("", "Destroy"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {destroyRequest});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (org.oasis_open.docs.wsrf.rl_2.DestroyResponse) _resp;
            } catch (java.lang.Exception _exception) {
                return (org.oasis_open.docs.wsrf.rl_2.DestroyResponse) org.apache.axis.utils.JavaUtils.convert(_resp, org.oasis_open.docs.wsrf.rl_2.DestroyResponse.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof org.oasis_open.docs.wsrf.rl_2.ResourceNotDestroyedFaultType) {
              throw (org.oasis_open.docs.wsrf.rl_2.ResourceNotDestroyedFaultType) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType) {
              throw (org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof org.oasis_open.docs.wsrf.r_2.ResourceUnavailableFaultType) {
              throw (org.oasis_open.docs.wsrf.r_2.ResourceUnavailableFaultType) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public org.oasis_open.docs.wsrf.rp_2.GetMultipleResourcePropertiesResponse getMultipleResourceProperties(javax.xml.namespace.QName[] getMultipleResourcePropertiesRequest) throws java.rmi.RemoteException, org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType, org.oasis_open.docs.wsrf.rp_2.InvalidResourcePropertyQNameFaultType, org.oasis_open.docs.wsrf.r_2.ResourceUnavailableFaultType {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[19]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("http://docs.oasis-open.org/wsrf/rpw-2/GetMultipleResourceProperties/GetMultipleResourcePropertiesRequest");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("", "GetMultipleResourceProperties"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {getMultipleResourcePropertiesRequest});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (org.oasis_open.docs.wsrf.rp_2.GetMultipleResourcePropertiesResponse) _resp;
            } catch (java.lang.Exception _exception) {
                return (org.oasis_open.docs.wsrf.rp_2.GetMultipleResourcePropertiesResponse) org.apache.axis.utils.JavaUtils.convert(_resp, org.oasis_open.docs.wsrf.rp_2.GetMultipleResourcePropertiesResponse.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType) {
              throw (org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof org.oasis_open.docs.wsrf.rp_2.InvalidResourcePropertyQNameFaultType) {
              throw (org.oasis_open.docs.wsrf.rp_2.InvalidResourcePropertyQNameFaultType) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof org.oasis_open.docs.wsrf.r_2.ResourceUnavailableFaultType) {
              throw (org.oasis_open.docs.wsrf.r_2.ResourceUnavailableFaultType) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public edu.virginia.vcgr.genii.common.notification.NotifyResponseType notifyWithResponse(org.oasis_open.wsn.base.Notify notify) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[20]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("", "notifyWithResponse"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {notify});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (edu.virginia.vcgr.genii.common.notification.NotifyResponseType) _resp;
            } catch (java.lang.Exception _exception) {
                return (edu.virginia.vcgr.genii.common.notification.NotifyResponseType) org.apache.axis.utils.JavaUtils.convert(_resp, edu.virginia.vcgr.genii.common.notification.NotifyResponseType.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

}
