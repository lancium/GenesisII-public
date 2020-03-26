/**
 * StreamableByteIOFactoryServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package edu.virginia.vcgr.genii.byteio.streamable.factory;

public class StreamableByteIOFactoryServiceLocator extends org.apache.axis.client.Service implements edu.virginia.vcgr.genii.byteio.streamable.factory.StreamableByteIOFactoryService {

    public StreamableByteIOFactoryServiceLocator() {
    }


    public StreamableByteIOFactoryServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public StreamableByteIOFactoryServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for StreamableByteIOFactory
    private java.lang.String StreamableByteIOFactory_address = "http://localhost:8080/wsrf/services";

    public java.lang.String getStreamableByteIOFactoryAddress() {
        return StreamableByteIOFactory_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String StreamableByteIOFactoryWSDDServiceName = "StreamableByteIOFactory";

    public java.lang.String getStreamableByteIOFactoryWSDDServiceName() {
        return StreamableByteIOFactoryWSDDServiceName;
    }

    public void setStreamableByteIOFactoryWSDDServiceName(java.lang.String name) {
        StreamableByteIOFactoryWSDDServiceName = name;
    }

    public edu.virginia.vcgr.genii.byteio.streamable.factory.StreamableByteIOFactory getStreamableByteIOFactory() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(StreamableByteIOFactory_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getStreamableByteIOFactory(endpoint);
    }

    public edu.virginia.vcgr.genii.byteio.streamable.factory.StreamableByteIOFactory getStreamableByteIOFactory(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            edu.virginia.vcgr.genii.byteio.streamable.factory.StreamableByteIOFactorySOAPBindingStub _stub = new edu.virginia.vcgr.genii.byteio.streamable.factory.StreamableByteIOFactorySOAPBindingStub(portAddress, this);
            _stub.setPortName(getStreamableByteIOFactoryWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setStreamableByteIOFactoryEndpointAddress(java.lang.String address) {
        StreamableByteIOFactory_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (edu.virginia.vcgr.genii.byteio.streamable.factory.StreamableByteIOFactory.class.isAssignableFrom(serviceEndpointInterface)) {
                edu.virginia.vcgr.genii.byteio.streamable.factory.StreamableByteIOFactorySOAPBindingStub _stub = new edu.virginia.vcgr.genii.byteio.streamable.factory.StreamableByteIOFactorySOAPBindingStub(new java.net.URL(StreamableByteIOFactory_address), this);
                _stub.setPortName(getStreamableByteIOFactoryWSDDServiceName());
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
        if ("StreamableByteIOFactory".equals(inputPortName)) {
            return getStreamableByteIOFactory();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/sbyteio/2006/11/factory", "StreamableByteIOFactoryService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/sbyteio/2006/11/factory", "StreamableByteIOFactory"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("StreamableByteIOFactory".equals(portName)) {
            setStreamableByteIOFactoryEndpointAddress(address);
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
