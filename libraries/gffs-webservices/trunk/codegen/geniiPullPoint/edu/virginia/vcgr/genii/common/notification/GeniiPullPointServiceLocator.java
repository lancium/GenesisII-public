/**
 * GeniiPullPointServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package edu.virginia.vcgr.genii.common.notification;

public class GeniiPullPointServiceLocator extends org.apache.axis.client.Service implements edu.virginia.vcgr.genii.common.notification.GeniiPullPointService {

    public GeniiPullPointServiceLocator() {
    }


    public GeniiPullPointServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public GeniiPullPointServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for GeniiPullPointPortType
    private java.lang.String GeniiPullPointPortType_address = "http://localhost:8080/wsrf/services";

    public java.lang.String getGeniiPullPointPortTypeAddress() {
        return GeniiPullPointPortType_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String GeniiPullPointPortTypeWSDDServiceName = "GeniiPullPointPortType";

    public java.lang.String getGeniiPullPointPortTypeWSDDServiceName() {
        return GeniiPullPointPortTypeWSDDServiceName;
    }

    public void setGeniiPullPointPortTypeWSDDServiceName(java.lang.String name) {
        GeniiPullPointPortTypeWSDDServiceName = name;
    }

    public edu.virginia.vcgr.genii.common.notification.GeniiPullPointPortType getGeniiPullPointPortType() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(GeniiPullPointPortType_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getGeniiPullPointPortType(endpoint);
    }

    public edu.virginia.vcgr.genii.common.notification.GeniiPullPointPortType getGeniiPullPointPortType(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            edu.virginia.vcgr.genii.common.notification.GeniiPullPointSOAPBindingStub _stub = new edu.virginia.vcgr.genii.common.notification.GeniiPullPointSOAPBindingStub(portAddress, this);
            _stub.setPortName(getGeniiPullPointPortTypeWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setGeniiPullPointPortTypeEndpointAddress(java.lang.String address) {
        GeniiPullPointPortType_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (edu.virginia.vcgr.genii.common.notification.GeniiPullPointPortType.class.isAssignableFrom(serviceEndpointInterface)) {
                edu.virginia.vcgr.genii.common.notification.GeniiPullPointSOAPBindingStub _stub = new edu.virginia.vcgr.genii.common.notification.GeniiPullPointSOAPBindingStub(new java.net.URL(GeniiPullPointPortType_address), this);
                _stub.setPortName(getGeniiPullPointPortTypeWSDDServiceName());
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
        if ("GeniiPullPointPortType".equals(inputPortName)) {
            return getGeniiPullPointPortType();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/2010/08/notification/pull-point", "GeniiPullPointService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/genii/2010/08/notification/pull-point", "GeniiPullPointPortType"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("GeniiPullPointPortType".equals(portName)) {
            setGeniiPullPointPortTypeEndpointAddress(address);
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
