/**
 * ReferenceResolverServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package edu.virginia.vcgr.genii.naming;

public class ReferenceResolverServiceLocator extends org.apache.axis.client.Service implements edu.virginia.vcgr.genii.naming.ReferenceResolverService {

    public ReferenceResolverServiceLocator() {
    }


    public ReferenceResolverServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public ReferenceResolverServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for ReferenceResolver
    private java.lang.String ReferenceResolver_address = "http://localhost:8080/wsrf/services";

    public java.lang.String getReferenceResolverAddress() {
        return ReferenceResolver_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String ReferenceResolverWSDDServiceName = "ReferenceResolver";

    public java.lang.String getReferenceResolverWSDDServiceName() {
        return ReferenceResolverWSDDServiceName;
    }

    public void setReferenceResolverWSDDServiceName(java.lang.String name) {
        ReferenceResolverWSDDServiceName = name;
    }

    public edu.virginia.vcgr.genii.naming.ReferenceResolver getReferenceResolver() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(ReferenceResolver_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getReferenceResolver(endpoint);
    }

    public edu.virginia.vcgr.genii.naming.ReferenceResolver getReferenceResolver(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            edu.virginia.vcgr.genii.naming.ReferenceResolverSOAPBindingStub _stub = new edu.virginia.vcgr.genii.naming.ReferenceResolverSOAPBindingStub(portAddress, this);
            _stub.setPortName(getReferenceResolverWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setReferenceResolverEndpointAddress(java.lang.String address) {
        ReferenceResolver_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (edu.virginia.vcgr.genii.naming.ReferenceResolver.class.isAssignableFrom(serviceEndpointInterface)) {
                edu.virginia.vcgr.genii.naming.ReferenceResolverSOAPBindingStub _stub = new edu.virginia.vcgr.genii.naming.ReferenceResolverSOAPBindingStub(new java.net.URL(ReferenceResolver_address), this);
                _stub.setPortName(getReferenceResolverWSDDServiceName());
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
        if ("ReferenceResolver".equals(inputPortName)) {
            return getReferenceResolver();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://schemas.ogf.org/naming/2006/08/naming/wsdl", "ReferenceResolverService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://schemas.ogf.org/naming/2006/08/naming/wsdl", "ReferenceResolver"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("ReferenceResolver".equals(portName)) {
            setReferenceResolverEndpointAddress(address);
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
