/**
 * EnhancedRNSServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package edu.virginia.vcgr.genii.enhancedrns;

public class EnhancedRNSServiceLocator extends org.apache.axis.client.Service implements edu.virginia.vcgr.genii.enhancedrns.EnhancedRNSService {

    public EnhancedRNSServiceLocator() {
    }


    public EnhancedRNSServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public EnhancedRNSServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for EnhancedRNSPortType
    private java.lang.String EnhancedRNSPortType_address = "http://localhost:8080/wsrf/services";

    public java.lang.String getEnhancedRNSPortTypeAddress() {
        return EnhancedRNSPortType_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String EnhancedRNSPortTypeWSDDServiceName = "EnhancedRNSPortType";

    public java.lang.String getEnhancedRNSPortTypeWSDDServiceName() {
        return EnhancedRNSPortTypeWSDDServiceName;
    }

    public void setEnhancedRNSPortTypeWSDDServiceName(java.lang.String name) {
        EnhancedRNSPortTypeWSDDServiceName = name;
    }

    public edu.virginia.vcgr.genii.enhancedrns.EnhancedRNSPortType getEnhancedRNSPortType() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(EnhancedRNSPortType_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getEnhancedRNSPortType(endpoint);
    }

    public edu.virginia.vcgr.genii.enhancedrns.EnhancedRNSPortType getEnhancedRNSPortType(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            edu.virginia.vcgr.genii.enhancedrns.EnhancedRNSSOAPBindingStub _stub = new edu.virginia.vcgr.genii.enhancedrns.EnhancedRNSSOAPBindingStub(portAddress, this);
            _stub.setPortName(getEnhancedRNSPortTypeWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setEnhancedRNSPortTypeEndpointAddress(java.lang.String address) {
        EnhancedRNSPortType_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (edu.virginia.vcgr.genii.enhancedrns.EnhancedRNSPortType.class.isAssignableFrom(serviceEndpointInterface)) {
                edu.virginia.vcgr.genii.enhancedrns.EnhancedRNSSOAPBindingStub _stub = new edu.virginia.vcgr.genii.enhancedrns.EnhancedRNSSOAPBindingStub(new java.net.URL(EnhancedRNSPortType_address), this);
                _stub.setPortName(getEnhancedRNSPortTypeWSDDServiceName());
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
        if ("EnhancedRNSPortType".equals(inputPortName)) {
            return getEnhancedRNSPortType();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/container/2011/04/enhanced-rns", "EnhancedRNSService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/container/2011/04/enhanced-rns", "EnhancedRNSPortType"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("EnhancedRNSPortType".equals(portName)) {
            setEnhancedRNSPortTypeEndpointAddress(address);
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
