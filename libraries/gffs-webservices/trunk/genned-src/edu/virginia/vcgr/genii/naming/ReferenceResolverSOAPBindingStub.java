/**
 * ReferenceResolverSOAPBindingStub.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package edu.virginia.vcgr.genii.naming;

public class ReferenceResolverSOAPBindingStub extends org.apache.axis.client.Stub implements edu.virginia.vcgr.genii.naming.ReferenceResolver {
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
        oper.setName("resolve");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://schemas.ogf.org/naming/2006/08/naming/wsdl", "resolve"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "anyType"), java.lang.Object.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2005/08/addressing", "EndpointReferenceType"));
        oper.setReturnClass(org.ws.addressing.EndpointReferenceType.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://www.w3.org/2005/08/addressing", "EndpointReference"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://schemas.ogf.org/naming/2006/08/naming", "ResolveFailedWithReferralFault"),
                      "org.ogf.schemas.naming._2006._08.naming.ResolveFailedWithReferralFaultType",
                      new javax.xml.namespace.QName("http://schemas.ogf.org/naming/2006/08/naming", "ResolveFailedWithReferralFaultType"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://schemas.ogf.org/naming/2006/08/naming", "ResolveFailedFault"),
                      "org.ogf.schemas.naming._2006._08.naming.ResolveFailedFaultType",
                      new javax.xml.namespace.QName("http://schemas.ogf.org/naming/2006/08/naming", "ResolveFailedFaultType"), 
                      true
                     ));
        _operations[0] = oper;

    }

    public ReferenceResolverSOAPBindingStub() throws org.apache.axis.AxisFault {
         this(null);
    }

    public ReferenceResolverSOAPBindingStub(java.net.URL endpointURL, javax.xml.rpc.Service service) throws org.apache.axis.AxisFault {
         this(service);
         super.cachedEndpoint = endpointURL;
    }

    public ReferenceResolverSOAPBindingStub(javax.xml.rpc.Service service) throws org.apache.axis.AxisFault {
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

            qName = new javax.xml.namespace.QName("http://schemas.ogf.org/naming/2006/08/naming", "ResolveFailedFaultType");
            cachedSerQNames.add(qName);
            cls = org.ogf.schemas.naming._2006._08.naming.ResolveFailedFaultType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://schemas.ogf.org/naming/2006/08/naming", "ResolveFailedWithReferralFaultType");
            cachedSerQNames.add(qName);
            cls = org.ogf.schemas.naming._2006._08.naming.ResolveFailedWithReferralFaultType.class;
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

    public org.ws.addressing.EndpointReferenceType resolve(java.lang.Object resolveRequest) throws java.rmi.RemoteException, org.ogf.schemas.naming._2006._08.naming.ResolveFailedWithReferralFaultType, org.ogf.schemas.naming._2006._08.naming.ResolveFailedFaultType {
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
        _call.setOperationName(new javax.xml.namespace.QName("", "resolve"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {resolveRequest});

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
        if (axisFaultException.detail instanceof org.ogf.schemas.naming._2006._08.naming.ResolveFailedWithReferralFaultType) {
              throw (org.ogf.schemas.naming._2006._08.naming.ResolveFailedWithReferralFaultType) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof org.ogf.schemas.naming._2006._08.naming.ResolveFailedFaultType) {
              throw (org.ogf.schemas.naming._2006._08.naming.ResolveFailedFaultType) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

}
