/**
 * RNSServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package org.ggf.rns;

public class RNSServiceLocator extends org.apache.axis.client.Service implements org.ggf.rns.RNSService {

    public RNSServiceLocator() {
    }


    public RNSServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public RNSServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for RNSPortType
    private java.lang.String RNSPortType_address = "http://localhost:8080/wsrf/services";

    public java.lang.String getRNSPortTypeAddress() {
        return RNSPortType_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String RNSPortTypeWSDDServiceName = "RNSPortType";

    public java.lang.String getRNSPortTypeWSDDServiceName() {
        return RNSPortTypeWSDDServiceName;
    }

    public void setRNSPortTypeWSDDServiceName(java.lang.String name) {
        RNSPortTypeWSDDServiceName = name;
    }

    public org.ggf.rns.RNSPortType getRNSPortType() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(RNSPortType_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getRNSPortType(endpoint);
    }

    public org.ggf.rns.RNSPortType getRNSPortType(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            org.ggf.rns.RNSSOAPBindingStub _stub = new org.ggf.rns.RNSSOAPBindingStub(portAddress, this);
            _stub.setPortName(getRNSPortTypeWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setRNSPortTypeEndpointAddress(java.lang.String address) {
        RNSPortType_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (org.ggf.rns.RNSPortType.class.isAssignableFrom(serviceEndpointInterface)) {
                org.ggf.rns.RNSSOAPBindingStub _stub = new org.ggf.rns.RNSSOAPBindingStub(new java.net.URL(RNSPortType_address), this);
                _stub.setPortName(getRNSPortTypeWSDDServiceName());
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
        if ("RNSPortType".equals(inputPortName)) {
            return getRNSPortType();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://schemas.ogf.org/rns/2009/12/rns", "RNSService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://schemas.ogf.org/rns/2009/12/rns", "RNSPortType"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("RNSPortType".equals(portName)) {
            setRNSPortTypeEndpointAddress(address);
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
