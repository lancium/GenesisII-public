/**
 * ResourceForkServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package edu.virginia.vcgr.genii.rfork;

public class ResourceForkServiceLocator extends org.apache.axis.client.Service implements edu.virginia.vcgr.genii.rfork.ResourceForkService {

    public ResourceForkServiceLocator() {
    }


    public ResourceForkServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public ResourceForkServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for ResourceForkPortType
    private java.lang.String ResourceForkPortType_address = "http://localhost:8080/wsrf/services";

    public java.lang.String getResourceForkPortTypeAddress() {
        return ResourceForkPortType_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String ResourceForkPortTypeWSDDServiceName = "ResourceForkPortType";

    public java.lang.String getResourceForkPortTypeWSDDServiceName() {
        return ResourceForkPortTypeWSDDServiceName;
    }

    public void setResourceForkPortTypeWSDDServiceName(java.lang.String name) {
        ResourceForkPortTypeWSDDServiceName = name;
    }

    public edu.virginia.vcgr.genii.rfork.ResourceForkPortType getResourceForkPortType() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(ResourceForkPortType_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getResourceForkPortType(endpoint);
    }

    public edu.virginia.vcgr.genii.rfork.ResourceForkPortType getResourceForkPortType(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            edu.virginia.vcgr.genii.rfork.ResourceForkSOAPBindingStub _stub = new edu.virginia.vcgr.genii.rfork.ResourceForkSOAPBindingStub(portAddress, this);
            _stub.setPortName(getResourceForkPortTypeWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setResourceForkPortTypeEndpointAddress(java.lang.String address) {
        ResourceForkPortType_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (edu.virginia.vcgr.genii.rfork.ResourceForkPortType.class.isAssignableFrom(serviceEndpointInterface)) {
                edu.virginia.vcgr.genii.rfork.ResourceForkSOAPBindingStub _stub = new edu.virginia.vcgr.genii.rfork.ResourceForkSOAPBindingStub(new java.net.URL(ResourceForkPortType_address), this);
                _stub.setPortName(getResourceForkPortTypeWSDDServiceName());
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
        if ("ResourceForkPortType".equals(inputPortName)) {
            return getResourceForkPortType();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/resource-fork", "ResourceForkService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/resource-fork", "ResourceForkPortType"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("ResourceForkPortType".equals(portName)) {
            setResourceForkPortTypeEndpointAddress(address);
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
