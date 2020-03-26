/**
 * StreamableByteIOFactorySOAPBindingStub.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package edu.virginia.vcgr.genii.byteio.streamable.factory;

public class StreamableByteIOFactorySOAPBindingStub extends org.apache.axis.client.Stub implements edu.virginia.vcgr.genii.byteio.streamable.factory.StreamableByteIOFactory {
    private java.util.Vector cachedSerClasses = new java.util.Vector();
    private java.util.Vector cachedSerQNames = new java.util.Vector();
    private java.util.Vector cachedSerFactories = new java.util.Vector();
    private java.util.Vector cachedDeserFactories = new java.util.Vector();

    static org.apache.axis.description.OperationDesc [] _operations;

    static {
        _operations = new org.apache.axis.description.OperationDesc[1];
        _initOperationDesc1();
    }

    private static void _initOperationDesc1(){
        org.apache.axis.description.OperationDesc oper;
        org.apache.axis.description.ParameterDesc param;
        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("openStream");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/sbyteio/2006/11/factory", "openStream"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "anyType"), java.lang.Object.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/sbyteio/2006/11/factory", ">openStreamResponse"));
        oper.setReturnClass(edu.virginia.vcgr.genii.byteio.streamable.factory.OpenStreamResponse.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/sbyteio/2006/11/factory", "openStreamResponse"));
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
        _operations[0] = oper;

    }

    public StreamableByteIOFactorySOAPBindingStub() throws org.apache.axis.AxisFault {
         this(null);
    }

    public StreamableByteIOFactorySOAPBindingStub(java.net.URL endpointURL, javax.xml.rpc.Service service) throws org.apache.axis.AxisFault {
         this(service);
         super.cachedEndpoint = endpointURL;
    }

    public StreamableByteIOFactorySOAPBindingStub(javax.xml.rpc.Service service) throws org.apache.axis.AxisFault {
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

            qName = new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/sbyteio/2006/11/factory", ">openStreamResponse");
            cachedSerQNames.add(qName);
            cls = edu.virginia.vcgr.genii.byteio.streamable.factory.OpenStreamResponse.class;
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

    public edu.virginia.vcgr.genii.byteio.streamable.factory.OpenStreamResponse openStream(java.lang.Object openStreamRequest) throws java.rmi.RemoteException, org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType, edu.virginia.vcgr.genii.common.rfactory.ResourceCreationFaultType {
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
        _call.setOperationName(new javax.xml.namespace.QName("", "openStream"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {openStreamRequest});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (edu.virginia.vcgr.genii.byteio.streamable.factory.OpenStreamResponse) _resp;
            } catch (java.lang.Exception _exception) {
                return (edu.virginia.vcgr.genii.byteio.streamable.factory.OpenStreamResponse) org.apache.axis.utils.JavaUtils.convert(_resp, edu.virginia.vcgr.genii.byteio.streamable.factory.OpenStreamResponse.class);
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

}
