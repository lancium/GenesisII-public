/**
 * EndpointIdentifierResolverServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package edu.virginia.vcgr.genii.naming;

public class EndpointIdentifierResolverServiceLocator extends org.apache.axis.client.Service implements edu.virginia.vcgr.genii.naming.EndpointIdentifierResolverService {

    public EndpointIdentifierResolverServiceLocator() {
    }


    public EndpointIdentifierResolverServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public EndpointIdentifierResolverServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for EndpointIdentifierResolver
    private java.lang.String EndpointIdentifierResolver_address = "http://localhost:8080/wsrf/services";

    public java.lang.String getEndpointIdentifierResolverAddress() {
        return EndpointIdentifierResolver_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String EndpointIdentifierResolverWSDDServiceName = "EndpointIdentifierResolver";

    public java.lang.String getEndpointIdentifierResolverWSDDServiceName() {
        return EndpointIdentifierResolverWSDDServiceName;
    }

    public void setEndpointIdentifierResolverWSDDServiceName(java.lang.String name) {
        EndpointIdentifierResolverWSDDServiceName = name;
    }

    public edu.virginia.vcgr.genii.naming.EndpointIdentifierResolver getEndpointIdentifierResolver() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(EndpointIdentifierResolver_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getEndpointIdentifierResolver(endpoint);
    }

    public edu.virginia.vcgr.genii.naming.EndpointIdentifierResolver getEndpointIdentifierResolver(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            edu.virginia.vcgr.genii.naming.EndpointIdentifierResolverSOAPBindingStub _stub = new edu.virginia.vcgr.genii.naming.EndpointIdentifierResolverSOAPBindingStub(portAddress, this);
            _stub.setPortName(getEndpointIdentifierResolverWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setEndpointIdentifierResolverEndpointAddress(java.lang.String address) {
        EndpointIdentifierResolver_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (edu.virginia.vcgr.genii.naming.EndpointIdentifierResolver.class.isAssignableFrom(serviceEndpointInterface)) {
                edu.virginia.vcgr.genii.naming.EndpointIdentifierResolverSOAPBindingStub _stub = new edu.virginia.vcgr.genii.naming.EndpointIdentifierResolverSOAPBindingStub(new java.net.URL(EndpointIdentifierResolver_address), this);
                _stub.setPortName(getEndpointIdentifierResolverWSDDServiceName());
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
        if ("EndpointIdentifierResolver".equals(inputPortName)) {
            return getEndpointIdentifierResolver();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://schemas.ogf.org/naming/2006/08/naming/wsdl", "EndpointIdentifierResolverService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://schemas.ogf.org/naming/2006/08/naming/wsdl", "EndpointIdentifierResolver"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("EndpointIdentifierResolver".equals(portName)) {
            setEndpointIdentifierResolverEndpointAddress(address);
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
