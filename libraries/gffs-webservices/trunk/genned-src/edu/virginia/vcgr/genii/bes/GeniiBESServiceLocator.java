/**
 * GeniiBESServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package edu.virginia.vcgr.genii.bes;

public class GeniiBESServiceLocator extends org.apache.axis.client.Service implements edu.virginia.vcgr.genii.bes.GeniiBESService {

    public GeniiBESServiceLocator() {
    }


    public GeniiBESServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public GeniiBESServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for GeniiBESPortType
    private java.lang.String GeniiBESPortType_address = "http://localhost:8080/wsrf/services";

    public java.lang.String getGeniiBESPortTypeAddress() {
        return GeniiBESPortType_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String GeniiBESPortTypeWSDDServiceName = "GeniiBESPortType";

    public java.lang.String getGeniiBESPortTypeWSDDServiceName() {
        return GeniiBESPortTypeWSDDServiceName;
    }

    public void setGeniiBESPortTypeWSDDServiceName(java.lang.String name) {
        GeniiBESPortTypeWSDDServiceName = name;
    }

    public edu.virginia.vcgr.genii.bes.GeniiBESPortType getGeniiBESPortType() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(GeniiBESPortType_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getGeniiBESPortType(endpoint);
    }

    public edu.virginia.vcgr.genii.bes.GeniiBESPortType getGeniiBESPortType(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            edu.virginia.vcgr.genii.bes.GeniiBESSOAPBindingStub _stub = new edu.virginia.vcgr.genii.bes.GeniiBESSOAPBindingStub(portAddress, this);
            _stub.setPortName(getGeniiBESPortTypeWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setGeniiBESPortTypeEndpointAddress(java.lang.String address) {
        GeniiBESPortType_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (edu.virginia.vcgr.genii.bes.GeniiBESPortType.class.isAssignableFrom(serviceEndpointInterface)) {
                edu.virginia.vcgr.genii.bes.GeniiBESSOAPBindingStub _stub = new edu.virginia.vcgr.genii.bes.GeniiBESSOAPBindingStub(new java.net.URL(GeniiBESPortType_address), this);
                _stub.setPortName(getGeniiBESPortTypeWSDDServiceName());
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
        if ("GeniiBESPortType".equals(inputPortName)) {
            return getGeniiBESPortType();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/2008/3/bes", "GeniiBESService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/2008/3/bes", "GeniiBESPortType"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("GeniiBESPortType".equals(portName)) {
            setGeniiBESPortTypeEndpointAddress(address);
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
