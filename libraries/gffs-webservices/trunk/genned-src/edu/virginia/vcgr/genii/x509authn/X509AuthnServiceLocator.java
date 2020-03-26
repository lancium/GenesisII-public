/**
 * X509AuthnServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package edu.virginia.vcgr.genii.x509authn;

public class X509AuthnServiceLocator extends org.apache.axis.client.Service implements edu.virginia.vcgr.genii.x509authn.X509AuthnService {

    public X509AuthnServiceLocator() {
    }


    public X509AuthnServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public X509AuthnServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for X509AuthnPortType
    private java.lang.String X509AuthnPortType_address = "http://localhost:8080/wsrf/services";

    public java.lang.String getX509AuthnPortTypeAddress() {
        return X509AuthnPortType_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String X509AuthnPortTypeWSDDServiceName = "X509AuthnPortType";

    public java.lang.String getX509AuthnPortTypeWSDDServiceName() {
        return X509AuthnPortTypeWSDDServiceName;
    }

    public void setX509AuthnPortTypeWSDDServiceName(java.lang.String name) {
        X509AuthnPortTypeWSDDServiceName = name;
    }

    public edu.virginia.vcgr.genii.x509authn.X509AuthnPortType getX509AuthnPortType() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(X509AuthnPortType_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getX509AuthnPortType(endpoint);
    }

    public edu.virginia.vcgr.genii.x509authn.X509AuthnPortType getX509AuthnPortType(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            edu.virginia.vcgr.genii.x509authn.X509AuthnSOAPBindingStub _stub = new edu.virginia.vcgr.genii.x509authn.X509AuthnSOAPBindingStub(portAddress, this);
            _stub.setPortName(getX509AuthnPortTypeWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setX509AuthnPortTypeEndpointAddress(java.lang.String address) {
        X509AuthnPortType_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (edu.virginia.vcgr.genii.x509authn.X509AuthnPortType.class.isAssignableFrom(serviceEndpointInterface)) {
                edu.virginia.vcgr.genii.x509authn.X509AuthnSOAPBindingStub _stub = new edu.virginia.vcgr.genii.x509authn.X509AuthnSOAPBindingStub(new java.net.URL(X509AuthnPortType_address), this);
                _stub.setPortName(getX509AuthnPortTypeWSDDServiceName());
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
        if ("X509AuthnPortType".equals(inputPortName)) {
            return getX509AuthnPortType();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/container/2007/11/x509-authn", "X509AuthnService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/container/2007/11/x509-authn", "X509AuthnPortType"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("X509AuthnPortType".equals(portName)) {
            setX509AuthnPortTypeEndpointAddress(address);
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
