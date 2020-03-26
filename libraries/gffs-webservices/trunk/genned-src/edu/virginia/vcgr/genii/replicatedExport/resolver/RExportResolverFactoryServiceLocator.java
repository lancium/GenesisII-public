/**
 * RExportResolverFactoryServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package edu.virginia.vcgr.genii.replicatedExport.resolver;

public class RExportResolverFactoryServiceLocator extends org.apache.axis.client.Service implements edu.virginia.vcgr.genii.replicatedExport.resolver.RExportResolverFactoryService {

    public RExportResolverFactoryServiceLocator() {
    }


    public RExportResolverFactoryServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public RExportResolverFactoryServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for RExportResolverFactoryPortType
    private java.lang.String RExportResolverFactoryPortType_address = "http://localhost:8080/wsrf/services";

    public java.lang.String getRExportResolverFactoryPortTypeAddress() {
        return RExportResolverFactoryPortType_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String RExportResolverFactoryPortTypeWSDDServiceName = "RExportResolverFactoryPortType";

    public java.lang.String getRExportResolverFactoryPortTypeWSDDServiceName() {
        return RExportResolverFactoryPortTypeWSDDServiceName;
    }

    public void setRExportResolverFactoryPortTypeWSDDServiceName(java.lang.String name) {
        RExportResolverFactoryPortTypeWSDDServiceName = name;
    }

    public edu.virginia.vcgr.genii.replicatedExport.resolver.RExportResolverFactoryPortType getRExportResolverFactoryPortType() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(RExportResolverFactoryPortType_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getRExportResolverFactoryPortType(endpoint);
    }

    public edu.virginia.vcgr.genii.replicatedExport.resolver.RExportResolverFactoryPortType getRExportResolverFactoryPortType(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            edu.virginia.vcgr.genii.replicatedExport.resolver.RExportResolverFactorySOAPBindingStub _stub = new edu.virginia.vcgr.genii.replicatedExport.resolver.RExportResolverFactorySOAPBindingStub(portAddress, this);
            _stub.setPortName(getRExportResolverFactoryPortTypeWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setRExportResolverFactoryPortTypeEndpointAddress(java.lang.String address) {
        RExportResolverFactoryPortType_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (edu.virginia.vcgr.genii.replicatedExport.resolver.RExportResolverFactoryPortType.class.isAssignableFrom(serviceEndpointInterface)) {
                edu.virginia.vcgr.genii.replicatedExport.resolver.RExportResolverFactorySOAPBindingStub _stub = new edu.virginia.vcgr.genii.replicatedExport.resolver.RExportResolverFactorySOAPBindingStub(new java.net.URL(RExportResolverFactoryPortType_address), this);
                _stub.setPortName(getRExportResolverFactoryPortTypeWSDDServiceName());
                return _stub;
            }
        }
        catch (java.lang.Throwable t) {
            throw new javax.xml.rpc.ServiceException(t);
        }
        throw new javax.xml.rpc.ServiceException("There is no stub implementation for the interface:  " + (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(javax.xml.namespace.QName portName, Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        if (portName == null) {
            return getPort(serviceEndpointInterface);
        }
        java.lang.String inputPortName = portName.getLocalPart();
        if ("RExportResolverFactoryPortType".equals(inputPortName)) {
            return getRExportResolverFactoryPortType();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/rns/2007/08/replicatedExport/resolver-factory", "RExportResolverFactoryService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/rns/2007/08/replicatedExport/resolver-factory", "RExportResolverFactoryPortType"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("RExportResolverFactoryPortType".equals(portName)) {
            setRExportResolverFactoryPortTypeEndpointAddress(address);
        }
        else 
{ // Unknown Port Name
            throw new javax.xml.rpc.ServiceException(" Cannot set Endpoint Address for Unknown Port" + portName);
        }
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(javax.xml.namespace.QName portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        setEndpointAddress(portName.getLocalPart(), address);
    }

}
