/**
 * GlobusAuthnServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package edu.virginia.vcgr.genii.globusauthn;

public class GlobusAuthnServiceLocator extends org.apache.axis.client.Service implements edu.virginia.vcgr.genii.globusauthn.GlobusAuthnService {

    public GlobusAuthnServiceLocator() {
    }


    public GlobusAuthnServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public GlobusAuthnServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for GlobusAuthnPortType
    private java.lang.String GlobusAuthnPortType_address = "http://localhost:8080/wsrf/services";

    public java.lang.String getGlobusAuthnPortTypeAddress() {
        return GlobusAuthnPortType_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String GlobusAuthnPortTypeWSDDServiceName = "GlobusAuthnPortType";

    public java.lang.String getGlobusAuthnPortTypeWSDDServiceName() {
        return GlobusAuthnPortTypeWSDDServiceName;
    }

    public void setGlobusAuthnPortTypeWSDDServiceName(java.lang.String name) {
        GlobusAuthnPortTypeWSDDServiceName = name;
    }

    public edu.virginia.vcgr.genii.globusauthn.GlobusAuthnPortType getGlobusAuthnPortType() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(GlobusAuthnPortType_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getGlobusAuthnPortType(endpoint);
    }

    public edu.virginia.vcgr.genii.globusauthn.GlobusAuthnPortType getGlobusAuthnPortType(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            edu.virginia.vcgr.genii.globusauthn.GlobusAuthnSOAPBindingStub _stub = new edu.virginia.vcgr.genii.globusauthn.GlobusAuthnSOAPBindingStub(portAddress, this);
            _stub.setPortName(getGlobusAuthnPortTypeWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setGlobusAuthnPortTypeEndpointAddress(java.lang.String address) {
        GlobusAuthnPortType_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (edu.virginia.vcgr.genii.globusauthn.GlobusAuthnPortType.class.isAssignableFrom(serviceEndpointInterface)) {
                edu.virginia.vcgr.genii.globusauthn.GlobusAuthnSOAPBindingStub _stub = new edu.virginia.vcgr.genii.globusauthn.GlobusAuthnSOAPBindingStub(new java.net.URL(GlobusAuthnPortType_address), this);
                _stub.setPortName(getGlobusAuthnPortTypeWSDDServiceName());
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
        if ("GlobusAuthnPortType".equals(inputPortName)) {
            return getGlobusAuthnPortType();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/container/2007/11/globus-authn", "GlobusAuthnService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/container/2007/11/globus-authn", "GlobusAuthnPortType"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("GlobusAuthnPortType".equals(portName)) {
            setGlobusAuthnPortTypeEndpointAddress(address);
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
