/**
 * RandomByteIOServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package org.ggf.rbyteio;

public class RandomByteIOServiceLocator extends org.apache.axis.client.Service implements org.ggf.rbyteio.RandomByteIOService {

    public RandomByteIOServiceLocator() {
    }


    public RandomByteIOServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public RandomByteIOServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for RandomByteIOPortType
    private java.lang.String RandomByteIOPortType_address = "http://localhost:8080/wsrf/services";

    public java.lang.String getRandomByteIOPortTypeAddress() {
        return RandomByteIOPortType_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String RandomByteIOPortTypeWSDDServiceName = "RandomByteIOPortType";

    public java.lang.String getRandomByteIOPortTypeWSDDServiceName() {
        return RandomByteIOPortTypeWSDDServiceName;
    }

    public void setRandomByteIOPortTypeWSDDServiceName(java.lang.String name) {
        RandomByteIOPortTypeWSDDServiceName = name;
    }

    public org.ggf.rbyteio.RandomByteIOPortType getRandomByteIOPortType() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(RandomByteIOPortType_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getRandomByteIOPortType(endpoint);
    }

    public org.ggf.rbyteio.RandomByteIOPortType getRandomByteIOPortType(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            org.ggf.rbyteio.RandomByteIOSOAPBindingStub _stub = new org.ggf.rbyteio.RandomByteIOSOAPBindingStub(portAddress, this);
            _stub.setPortName(getRandomByteIOPortTypeWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setRandomByteIOPortTypeEndpointAddress(java.lang.String address) {
        RandomByteIOPortType_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (org.ggf.rbyteio.RandomByteIOPortType.class.isAssignableFrom(serviceEndpointInterface)) {
                org.ggf.rbyteio.RandomByteIOSOAPBindingStub _stub = new org.ggf.rbyteio.RandomByteIOSOAPBindingStub(new java.net.URL(RandomByteIOPortType_address), this);
                _stub.setPortName(getRandomByteIOPortTypeWSDDServiceName());
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
        if ("RandomByteIOPortType".equals(inputPortName)) {
            return getRandomByteIOPortType();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://schemas.ggf.org/byteio/2005/10/random-access", "RandomByteIOService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://schemas.ggf.org/byteio/2005/10/random-access", "RandomByteIOPortType"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("RandomByteIOPortType".equals(portName)) {
            setRandomByteIOPortTypeEndpointAddress(address);
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
