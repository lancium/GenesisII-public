/**
 * PipeServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package edu.virginia.vcgr.genii.pipe;

public class PipeServiceLocator extends org.apache.axis.client.Service implements edu.virginia.vcgr.genii.pipe.PipeService {

    public PipeServiceLocator() {
    }


    public PipeServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public PipeServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for PipePortType
    private java.lang.String PipePortType_address = "http://localhost:8080/wsrf/services";

    public java.lang.String getPipePortTypeAddress() {
        return PipePortType_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String PipePortTypeWSDDServiceName = "PipePortType";

    public java.lang.String getPipePortTypeWSDDServiceName() {
        return PipePortTypeWSDDServiceName;
    }

    public void setPipePortTypeWSDDServiceName(java.lang.String name) {
        PipePortTypeWSDDServiceName = name;
    }

    public edu.virginia.vcgr.genii.pipe.PipePortType getPipePortType() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(PipePortType_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getPipePortType(endpoint);
    }

    public edu.virginia.vcgr.genii.pipe.PipePortType getPipePortType(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            edu.virginia.vcgr.genii.pipe.PipeSOAPBindingStub _stub = new edu.virginia.vcgr.genii.pipe.PipeSOAPBindingStub(portAddress, this);
            _stub.setPortName(getPipePortTypeWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setPipePortTypeEndpointAddress(java.lang.String address) {
        PipePortType_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (edu.virginia.vcgr.genii.pipe.PipePortType.class.isAssignableFrom(serviceEndpointInterface)) {
                edu.virginia.vcgr.genii.pipe.PipeSOAPBindingStub _stub = new edu.virginia.vcgr.genii.pipe.PipeSOAPBindingStub(new java.net.URL(PipePortType_address), this);
                _stub.setPortName(getPipePortTypeWSDDServiceName());
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
        if ("PipePortType".equals(inputPortName)) {
            return getPipePortType();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/pipe/2011/03/pipe", "PipeService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/pipe/2011/03/pipe", "PipePortType"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("PipePortType".equals(portName)) {
            setPipePortTypeEndpointAddress(address);
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
