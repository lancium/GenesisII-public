/**
 * RNSSOAPBindingStub.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package org.ggf.rns;

public class RNSSOAPBindingStub extends org.apache.axis.client.Stub implements org.ggf.rns.RNSPortType {
    private java.util.Vector cachedSerClasses = new java.util.Vector();
    private java.util.Vector cachedSerQNames = new java.util.Vector();
    private java.util.Vector cachedSerFactories = new java.util.Vector();
    private java.util.Vector cachedDeserFactories = new java.util.Vector();

    static org.apache.axis.description.OperationDesc [] _operations;

    static {
        _operations = new org.apache.axis.description.OperationDesc[5];
        _initOperationDesc1();
    }

    private static void _initOperationDesc1(){
        org.apache.axis.description.OperationDesc oper;
        org.apache.axis.description.ParameterDesc param;
        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("add");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://schemas.ogf.org/rns/2009/12/rns", "AddRequest"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://schemas.ogf.org/rns/2009/12/rns", "AddRequestType"), org.ggf.rns.RNSEntryType[].class, false, false);
        param.setItemQName(new javax.xml.namespace.QName("http://schemas.ogf.org/rns/2009/12/rns", "entry"));
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://schemas.ogf.org/rns/2009/12/rns", "AddResponseType"));
        oper.setReturnClass(org.ggf.rns.RNSEntryResponseType[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://schemas.ogf.org/rns/2009/12/rns", "AddResponse"));
        param = oper.getReturnParamDesc();
        param.setItemQName(new javax.xml.namespace.QName("http://schemas.ogf.org/rns/2009/12/rns", "entry-response"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://schemas.ogf.org/rns/2009/12/rns", "WriteNotPermittedFault"),
                      "org.ggf.rns.WriteNotPermittedFaultType",
                      new javax.xml.namespace.QName("http://schemas.ogf.org/rns/2009/12/rns", "WriteNotPermittedFaultType"), 
                      true
                     ));
        _operations[0] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("lookup");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://schemas.ogf.org/rns/2009/12/rns", "LookupRequest"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://schemas.ogf.org/rns/2009/12/rns", "LookupRequestType"), java.lang.String[].class, false, false);
        param.setItemQName(new javax.xml.namespace.QName("http://schemas.ogf.org/rns/2009/12/rns", "entry-name"));
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://schemas.ogf.org/rns/2009/12/rns", "LookupResponseType"));
        oper.setReturnClass(org.ggf.rns.LookupResponseType.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://schemas.ogf.org/rns/2009/12/rns", "LookupResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://schemas.ogf.org/rns/2009/12/rns", "ReadNotPermittedFault"),
                      "org.ggf.rns.ReadNotPermittedFaultType",
                      new javax.xml.namespace.QName("http://schemas.ogf.org/rns/2009/12/rns", "ReadNotPermittedFaultType"), 
                      true
                     ));
        _operations[1] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("setMetadata");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://schemas.ogf.org/rns/2009/12/rns", "SetMetadataRequest"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://schemas.ogf.org/rns/2009/12/rns", "SetMetadataRequestType"), org.ggf.rns.MetadataMappingType[].class, false, false);
        param.setItemQName(new javax.xml.namespace.QName("http://schemas.ogf.org/rns/2009/12/rns", "set-metadata-request"));
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://schemas.ogf.org/rns/2009/12/rns", "SetMetadataResponseType"));
        oper.setReturnClass(org.ggf.rns.RNSEntryResponseType[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://schemas.ogf.org/rns/2009/12/rns", "SetMetadataResponse"));
        param = oper.getReturnParamDesc();
        param.setItemQName(new javax.xml.namespace.QName("http://schemas.ogf.org/rns/2009/12/rns", "entry-response"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://schemas.ogf.org/rns/2009/12/rns", "WriteNotPermittedFault"),
                      "org.ggf.rns.WriteNotPermittedFaultType",
                      new javax.xml.namespace.QName("http://schemas.ogf.org/rns/2009/12/rns", "WriteNotPermittedFaultType"), 
                      true
                     ));
        _operations[2] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("rename");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://schemas.ogf.org/rns/2009/12/rns", "RenameRequest"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://schemas.ogf.org/rns/2009/12/rns", "RenameRequestType"), org.ggf.rns.NameMappingType[].class, false, false);
        param.setItemQName(new javax.xml.namespace.QName("http://schemas.ogf.org/rns/2009/12/rns", "rename-request"));
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://schemas.ogf.org/rns/2009/12/rns", "RenameResponseType"));
        oper.setReturnClass(org.ggf.rns.RNSEntryResponseType[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://schemas.ogf.org/rns/2009/12/rns", "RenameResponse"));
        param = oper.getReturnParamDesc();
        param.setItemQName(new javax.xml.namespace.QName("http://schemas.ogf.org/rns/2009/12/rns", "entry-response"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://schemas.ogf.org/rns/2009/12/rns", "WriteNotPermittedFault"),
                      "org.ggf.rns.WriteNotPermittedFaultType",
                      new javax.xml.namespace.QName("http://schemas.ogf.org/rns/2009/12/rns", "WriteNotPermittedFaultType"), 
                      true
                     ));
        _operations[3] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("remove");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://schemas.ogf.org/rns/2009/12/rns", "RemoveRequest"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://schemas.ogf.org/rns/2009/12/rns", "RemoveRequestType"), java.lang.String[].class, false, false);
        param.setItemQName(new javax.xml.namespace.QName("http://schemas.ogf.org/rns/2009/12/rns", "entry-name"));
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://schemas.ogf.org/rns/2009/12/rns", "RemoveResponseType"));
        oper.setReturnClass(org.ggf.rns.RNSEntryResponseType[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://schemas.ogf.org/rns/2009/12/rns", "RemoveResponse"));
        param = oper.getReturnParamDesc();
        param.setItemQName(new javax.xml.namespace.QName("http://schemas.ogf.org/rns/2009/12/rns", "entry-response"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://schemas.ogf.org/rns/2009/12/rns", "WriteNotPermittedFault"),
                      "org.ggf.rns.WriteNotPermittedFaultType",
                      new javax.xml.namespace.QName("http://schemas.ogf.org/rns/2009/12/rns", "WriteNotPermittedFaultType"), 
                      true
                     ));
        _operations[4] = oper;

    }

    public RNSSOAPBindingStub() throws org.apache.axis.AxisFault {
         this(null);
    }

    public RNSSOAPBindingStub(java.net.URL endpointURL, javax.xml.rpc.Service service) throws org.apache.axis.AxisFault {
         this(service);
         super.cachedEndpoint = endpointURL;
    }

    public RNSSOAPBindingStub(javax.xml.rpc.Service service) throws org.apache.axis.AxisFault {
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

            qName = new javax.xml.namespace.QName("http://schemas.ogf.org/rns/2009/12/rns", ">RNSRP");
            cachedSerQNames.add(qName);
            cls = org.ggf.rns.RNSRP.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://schemas.ogf.org/rns/2009/12/rns", "AddRequestType");
            cachedSerQNames.add(qName);
            cls = org.ggf.rns.RNSEntryType[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("http://schemas.ogf.org/rns/2009/12/rns", "RNSEntryType");
            qName2 = new javax.xml.namespace.QName("http://schemas.ogf.org/rns/2009/12/rns", "entry");
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("http://schemas.ogf.org/rns/2009/12/rns", "AddResponseType");
            cachedSerQNames.add(qName);
            cls = org.ggf.rns.RNSEntryResponseType[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("http://schemas.ogf.org/rns/2009/12/rns", "RNSEntryResponseType");
            qName2 = new javax.xml.namespace.QName("http://schemas.ogf.org/rns/2009/12/rns", "entry-response");
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("http://schemas.ogf.org/rns/2009/12/rns", "EntryNameType");
            cachedSerQNames.add(qName);
            cls = java.lang.String.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(org.apache.axis.encoding.ser.BaseSerializerFactory.createFactory(org.apache.axis.encoding.ser.SimpleSerializerFactory.class, cls, qName));
            cachedDeserFactories.add(org.apache.axis.encoding.ser.BaseDeserializerFactory.createFactory(org.apache.axis.encoding.ser.SimpleDeserializerFactory.class, cls, qName));

            qName = new javax.xml.namespace.QName("http://schemas.ogf.org/rns/2009/12/rns", "LookupRequestType");
            cachedSerQNames.add(qName);
            cls = java.lang.String[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("http://schemas.ogf.org/rns/2009/12/rns", "EntryNameType");
            qName2 = new javax.xml.namespace.QName("http://schemas.ogf.org/rns/2009/12/rns", "entry-name");
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("http://schemas.ogf.org/rns/2009/12/rns", "LookupResponseType");
            cachedSerQNames.add(qName);
            cls = org.ggf.rns.LookupResponseType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://schemas.ogf.org/rns/2009/12/rns", "MetadataMappingType");
            cachedSerQNames.add(qName);
            cls = org.ggf.rns.MetadataMappingType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://schemas.ogf.org/rns/2009/12/rns", "NameMappingType");
            cachedSerQNames.add(qName);
            cls = org.ggf.rns.NameMappingType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://schemas.ogf.org/rns/2009/12/rns", "ReadNotPermittedFaultType");
            cachedSerQNames.add(qName);
            cls = org.ggf.rns.ReadNotPermittedFaultType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://schemas.ogf.org/rns/2009/12/rns", "RemoveRequestType");
            cachedSerQNames.add(qName);
            cls = java.lang.String[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("http://schemas.ogf.org/rns/2009/12/rns", "EntryNameType");
            qName2 = new javax.xml.namespace.QName("http://schemas.ogf.org/rns/2009/12/rns", "entry-name");
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("http://schemas.ogf.org/rns/2009/12/rns", "RemoveResponseType");
            cachedSerQNames.add(qName);
            cls = org.ggf.rns.RNSEntryResponseType[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("http://schemas.ogf.org/rns/2009/12/rns", "RNSEntryResponseType");
            qName2 = new javax.xml.namespace.QName("http://schemas.ogf.org/rns/2009/12/rns", "entry-response");
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("http://schemas.ogf.org/rns/2009/12/rns", "RenameRequestType");
            cachedSerQNames.add(qName);
            cls = org.ggf.rns.NameMappingType[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("http://schemas.ogf.org/rns/2009/12/rns", "NameMappingType");
            qName2 = new javax.xml.namespace.QName("http://schemas.ogf.org/rns/2009/12/rns", "rename-request");
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("http://schemas.ogf.org/rns/2009/12/rns", "RenameResponseType");
            cachedSerQNames.add(qName);
            cls = org.ggf.rns.RNSEntryResponseType[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("http://schemas.ogf.org/rns/2009/12/rns", "RNSEntryResponseType");
            qName2 = new javax.xml.namespace.QName("http://schemas.ogf.org/rns/2009/12/rns", "entry-response");
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("http://schemas.ogf.org/rns/2009/12/rns", "RNSEntryDoesNotExistFaultType");
            cachedSerQNames.add(qName);
            cls = org.ggf.rns.RNSEntryDoesNotExistFaultType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://schemas.ogf.org/rns/2009/12/rns", "RNSEntryExistsFaultType");
            cachedSerQNames.add(qName);
            cls = org.ggf.rns.RNSEntryExistsFaultType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://schemas.ogf.org/rns/2009/12/rns", "RNSEntryResponseType");
            cachedSerQNames.add(qName);
            cls = org.ggf.rns.RNSEntryResponseType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://schemas.ogf.org/rns/2009/12/rns", "RNSEntryType");
            cachedSerQNames.add(qName);
            cls = org.ggf.rns.RNSEntryType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://schemas.ogf.org/rns/2009/12/rns", "RNSMetadataType");
            cachedSerQNames.add(qName);
            cls = org.ggf.rns.RNSMetadataType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://schemas.ogf.org/rns/2009/12/rns", "RNSSupportType");
            cachedSerQNames.add(qName);
            cls = org.ggf.rns.RNSSupportType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(enumsf);
            cachedDeserFactories.add(enumdf);

            qName = new javax.xml.namespace.QName("http://schemas.ogf.org/rns/2009/12/rns", "SetMetadataRequestType");
            cachedSerQNames.add(qName);
            cls = org.ggf.rns.MetadataMappingType[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("http://schemas.ogf.org/rns/2009/12/rns", "MetadataMappingType");
            qName2 = new javax.xml.namespace.QName("http://schemas.ogf.org/rns/2009/12/rns", "set-metadata-request");
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("http://schemas.ogf.org/rns/2009/12/rns", "SetMetadataResponseType");
            cachedSerQNames.add(qName);
            cls = org.ggf.rns.RNSEntryResponseType[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("http://schemas.ogf.org/rns/2009/12/rns", "RNSEntryResponseType");
            qName2 = new javax.xml.namespace.QName("http://schemas.ogf.org/rns/2009/12/rns", "entry-response");
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("http://schemas.ogf.org/rns/2009/12/rns", "SupportsRNSType");
            cachedSerQNames.add(qName);
            cls = org.ggf.rns.SupportsRNSType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://schemas.ogf.org/rns/2009/12/rns", "WriteNotPermittedFaultType");
            cachedSerQNames.add(qName);
            cls = org.ggf.rns.WriteNotPermittedFaultType.class;
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

    public org.ggf.rns.RNSEntryResponseType[] add(org.ggf.rns.RNSEntryType[] addRequest) throws java.rmi.RemoteException, org.ggf.rns.WriteNotPermittedFaultType {
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
        _call.setOperationName(new javax.xml.namespace.QName("", "add"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {addRequest});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (org.ggf.rns.RNSEntryResponseType[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (org.ggf.rns.RNSEntryResponseType[]) org.apache.axis.utils.JavaUtils.convert(_resp, org.ggf.rns.RNSEntryResponseType[].class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof org.ggf.rns.WriteNotPermittedFaultType) {
              throw (org.ggf.rns.WriteNotPermittedFaultType) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public org.ggf.rns.LookupResponseType lookup(java.lang.String[] lookupRequest) throws java.rmi.RemoteException, org.ggf.rns.ReadNotPermittedFaultType {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[1]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("", "lookup"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {lookupRequest});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (org.ggf.rns.LookupResponseType) _resp;
            } catch (java.lang.Exception _exception) {
                return (org.ggf.rns.LookupResponseType) org.apache.axis.utils.JavaUtils.convert(_resp, org.ggf.rns.LookupResponseType.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof org.ggf.rns.ReadNotPermittedFaultType) {
              throw (org.ggf.rns.ReadNotPermittedFaultType) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public org.ggf.rns.RNSEntryResponseType[] setMetadata(org.ggf.rns.MetadataMappingType[] setMetadataRequest) throws java.rmi.RemoteException, org.ggf.rns.WriteNotPermittedFaultType {
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
        _call.setOperationName(new javax.xml.namespace.QName("", "setMetadata"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {setMetadataRequest});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (org.ggf.rns.RNSEntryResponseType[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (org.ggf.rns.RNSEntryResponseType[]) org.apache.axis.utils.JavaUtils.convert(_resp, org.ggf.rns.RNSEntryResponseType[].class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof org.ggf.rns.WriteNotPermittedFaultType) {
              throw (org.ggf.rns.WriteNotPermittedFaultType) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public org.ggf.rns.RNSEntryResponseType[] rename(org.ggf.rns.NameMappingType[] renameRequest) throws java.rmi.RemoteException, org.ggf.rns.WriteNotPermittedFaultType {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[3]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("", "rename"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {renameRequest});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (org.ggf.rns.RNSEntryResponseType[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (org.ggf.rns.RNSEntryResponseType[]) org.apache.axis.utils.JavaUtils.convert(_resp, org.ggf.rns.RNSEntryResponseType[].class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof org.ggf.rns.WriteNotPermittedFaultType) {
              throw (org.ggf.rns.WriteNotPermittedFaultType) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public org.ggf.rns.RNSEntryResponseType[] remove(java.lang.String[] removeRequest) throws java.rmi.RemoteException, org.ggf.rns.WriteNotPermittedFaultType {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[4]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("", "remove"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {removeRequest});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (org.ggf.rns.RNSEntryResponseType[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (org.ggf.rns.RNSEntryResponseType[]) org.apache.axis.utils.JavaUtils.convert(_resp, org.ggf.rns.RNSEntryResponseType[].class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof org.ggf.rns.WriteNotPermittedFaultType) {
              throw (org.ggf.rns.WriteNotPermittedFaultType) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

}
