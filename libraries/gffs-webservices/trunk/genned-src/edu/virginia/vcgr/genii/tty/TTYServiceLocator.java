/**
 * TTYServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package edu.virginia.vcgr.genii.tty;

public class TTYServiceLocator extends org.apache.axis.client.Service implements edu.virginia.vcgr.genii.tty.TTYService {

    public TTYServiceLocator() {
    }


    public TTYServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public TTYServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for TTYPortType
    private java.lang.String TTYPortType_address = "http://localhost:8080/wsrf/services";

    public java.lang.String getTTYPortTypeAddress() {
        return TTYPortType_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String TTYPortTypeWSDDServiceName = "TTYPortType";

    public java.lang.String getTTYPortTypeWSDDServiceName() {
        return TTYPortTypeWSDDServiceName;
    }

    public void setTTYPortTypeWSDDServiceName(java.lang.String name) {
        TTYPortTypeWSDDServiceName = name;
    }

    public edu.virginia.vcgr.genii.tty.TTYPortType getTTYPortType() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(TTYPortType_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getTTYPortType(endpoint);
    }

    public edu.virginia.vcgr.genii.tty.TTYPortType getTTYPortType(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            edu.virginia.vcgr.genii.tty.TTYSOAPBindingStub _stub = new edu.virginia.vcgr.genii.tty.TTYSOAPBindingStub(portAddress, this);
            _stub.setPortName(getTTYPortTypeWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setTTYPortTypeEndpointAddress(java.lang.String address) {
        TTYPortType_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (edu.virginia.vcgr.genii.tty.TTYPortType.class.isAssignableFrom(serviceEndpointInterface)) {
                edu.virginia.vcgr.genii.tty.TTYSOAPBindingStub _stub = new edu.virginia.vcgr.genii.tty.TTYSOAPBindingStub(new java.net.URL(TTYPortType_address), this);
                _stub.setPortName(getTTYPortTypeWSDDServiceName());
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
        if ("TTYPortType".equals(inputPortName)) {
            return getTTYPortType();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/tty/2008/03/tty", "TTYService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/tty/2008/03/tty", "TTYPortType"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("TTYPortType".equals(portName)) {
            setTTYPortTypeEndpointAddress(address);
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
