/**
 * StreamableByteIOServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package org.ggf.sbyteio;

public class StreamableByteIOServiceLocator extends org.apache.axis.client.Service implements org.ggf.sbyteio.StreamableByteIOService {

    public StreamableByteIOServiceLocator() {
    }


    public StreamableByteIOServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public StreamableByteIOServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for StreamableByteIOPortType
    private java.lang.String StreamableByteIOPortType_address = "http://localhost:8080/wsrf/services";

    public java.lang.String getStreamableByteIOPortTypeAddress() {
        return StreamableByteIOPortType_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String StreamableByteIOPortTypeWSDDServiceName = "StreamableByteIOPortType";

    public java.lang.String getStreamableByteIOPortTypeWSDDServiceName() {
        return StreamableByteIOPortTypeWSDDServiceName;
    }

    public void setStreamableByteIOPortTypeWSDDServiceName(java.lang.String name) {
        StreamableByteIOPortTypeWSDDServiceName = name;
    }

    public org.ggf.sbyteio.StreamableByteIOPortType getStreamableByteIOPortType() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(StreamableByteIOPortType_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getStreamableByteIOPortType(endpoint);
    }

    public org.ggf.sbyteio.StreamableByteIOPortType getStreamableByteIOPortType(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            org.ggf.sbyteio.StreamableByteIOSOAPBindingStub _stub = new org.ggf.sbyteio.StreamableByteIOSOAPBindingStub(portAddress, this);
            _stub.setPortName(getStreamableByteIOPortTypeWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setStreamableByteIOPortTypeEndpointAddress(java.lang.String address) {
        StreamableByteIOPortType_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (org.ggf.sbyteio.StreamableByteIOPortType.class.isAssignableFrom(serviceEndpointInterface)) {
                org.ggf.sbyteio.StreamableByteIOSOAPBindingStub _stub = new org.ggf.sbyteio.StreamableByteIOSOAPBindingStub(new java.net.URL(StreamableByteIOPortType_address), this);
                _stub.setPortName(getStreamableByteIOPortTypeWSDDServiceName());
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
        if ("StreamableByteIOPortType".equals(inputPortName)) {
            return getStreamableByteIOPortType();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://schemas.ggf.org/byteio/2005/10/streamable-access", "StreamableByteIOService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://schemas.ggf.org/byteio/2005/10/streamable-access", "StreamableByteIOPortType"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("StreamableByteIOPortType".equals(portName)) {
            setStreamableByteIOPortTypeEndpointAddress(address);
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
