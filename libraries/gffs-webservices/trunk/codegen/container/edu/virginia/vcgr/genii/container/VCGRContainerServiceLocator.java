/**
 * VCGRContainerServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package edu.virginia.vcgr.genii.container;

public class VCGRContainerServiceLocator extends org.apache.axis.client.Service implements edu.virginia.vcgr.genii.container.VCGRContainerService {

    public VCGRContainerServiceLocator() {
    }


    public VCGRContainerServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public VCGRContainerServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for VCGRContainerPortType
    private java.lang.String VCGRContainerPortType_address = "http://localhost:8080/wsrf/services";

    public java.lang.String getVCGRContainerPortTypeAddress() {
        return VCGRContainerPortType_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String VCGRContainerPortTypeWSDDServiceName = "VCGRContainerPortType";

    public java.lang.String getVCGRContainerPortTypeWSDDServiceName() {
        return VCGRContainerPortTypeWSDDServiceName;
    }

    public void setVCGRContainerPortTypeWSDDServiceName(java.lang.String name) {
        VCGRContainerPortTypeWSDDServiceName = name;
    }

    public edu.virginia.vcgr.genii.container.VCGRContainerPortType getVCGRContainerPortType() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(VCGRContainerPortType_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getVCGRContainerPortType(endpoint);
    }

    public edu.virginia.vcgr.genii.container.VCGRContainerPortType getVCGRContainerPortType(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            edu.virginia.vcgr.genii.container.VCGRContainerSOAPBindingStub _stub = new edu.virginia.vcgr.genii.container.VCGRContainerSOAPBindingStub(portAddress, this);
            _stub.setPortName(getVCGRContainerPortTypeWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setVCGRContainerPortTypeEndpointAddress(java.lang.String address) {
        VCGRContainerPortType_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (edu.virginia.vcgr.genii.container.VCGRContainerPortType.class.isAssignableFrom(serviceEndpointInterface)) {
                edu.virginia.vcgr.genii.container.VCGRContainerSOAPBindingStub _stub = new edu.virginia.vcgr.genii.container.VCGRContainerSOAPBindingStub(new java.net.URL(VCGRContainerPortType_address), this);
                _stub.setPortName(getVCGRContainerPortTypeWSDDServiceName());
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
        if ("VCGRContainerPortType".equals(inputPortName)) {
            return getVCGRContainerPortType();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/container/2006/07/container", "VCGRContainerService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/container/2006/07/container", "VCGRContainerPortType"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("VCGRContainerPortType".equals(portName)) {
            setVCGRContainerPortTypeEndpointAddress(address);
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
