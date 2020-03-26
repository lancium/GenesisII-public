/**
 * GeniiResolverServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package edu.virginia.vcgr.genii.resolver;

public class GeniiResolverServiceLocator extends org.apache.axis.client.Service implements edu.virginia.vcgr.genii.resolver.GeniiResolverService {

    public GeniiResolverServiceLocator() {
    }


    public GeniiResolverServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public GeniiResolverServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for GeniiResolverPortType
    private java.lang.String GeniiResolverPortType_address = "http://localhost:8080/wsrf/services";

    public java.lang.String getGeniiResolverPortTypeAddress() {
        return GeniiResolverPortType_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String GeniiResolverPortTypeWSDDServiceName = "GeniiResolverPortType";

    public java.lang.String getGeniiResolverPortTypeWSDDServiceName() {
        return GeniiResolverPortTypeWSDDServiceName;
    }

    public void setGeniiResolverPortTypeWSDDServiceName(java.lang.String name) {
        GeniiResolverPortTypeWSDDServiceName = name;
    }

    public edu.virginia.vcgr.genii.resolver.GeniiResolverPortType getGeniiResolverPortType() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(GeniiResolverPortType_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getGeniiResolverPortType(endpoint);
    }

    public edu.virginia.vcgr.genii.resolver.GeniiResolverPortType getGeniiResolverPortType(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            edu.virginia.vcgr.genii.resolver.GeniiResolverSOAPBindingStub _stub = new edu.virginia.vcgr.genii.resolver.GeniiResolverSOAPBindingStub(portAddress, this);
            _stub.setPortName(getGeniiResolverPortTypeWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setGeniiResolverPortTypeEndpointAddress(java.lang.String address) {
        GeniiResolverPortType_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (edu.virginia.vcgr.genii.resolver.GeniiResolverPortType.class.isAssignableFrom(serviceEndpointInterface)) {
                edu.virginia.vcgr.genii.resolver.GeniiResolverSOAPBindingStub _stub = new edu.virginia.vcgr.genii.resolver.GeniiResolverSOAPBindingStub(new java.net.URL(GeniiResolverPortType_address), this);
                _stub.setPortName(getGeniiResolverPortTypeWSDDServiceName());
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
        if ("GeniiResolverPortType".equals(inputPortName)) {
            return getGeniiResolverPortType();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/genii-resolver", "GeniiResolverService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/genii-resolver", "GeniiResolverPortType"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("GeniiResolverPortType".equals(portName)) {
            setGeniiResolverPortTypeEndpointAddress(address);
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
