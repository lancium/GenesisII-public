/**
 * FSProxyServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package edu.virginia.vcgr.genii.exportdir.fsproxy;

public class FSProxyServiceLocator extends org.apache.axis.client.Service implements edu.virginia.vcgr.genii.exportdir.fsproxy.FSProxyService {

    public FSProxyServiceLocator() {
    }


    public FSProxyServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public FSProxyServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for FSProxyPortType
    private java.lang.String FSProxyPortType_address = "http://localhost:8080/wsrf/services";

    public java.lang.String getFSProxyPortTypeAddress() {
        return FSProxyPortType_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String FSProxyPortTypeWSDDServiceName = "FSProxyPortType";

    public java.lang.String getFSProxyPortTypeWSDDServiceName() {
        return FSProxyPortTypeWSDDServiceName;
    }

    public void setFSProxyPortTypeWSDDServiceName(java.lang.String name) {
        FSProxyPortTypeWSDDServiceName = name;
    }

    public edu.virginia.vcgr.genii.exportdir.fsproxy.FSProxyPortType getFSProxyPortType() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(FSProxyPortType_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getFSProxyPortType(endpoint);
    }

    public edu.virginia.vcgr.genii.exportdir.fsproxy.FSProxyPortType getFSProxyPortType(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            edu.virginia.vcgr.genii.exportdir.fsproxy.FSProxySOAPBindingStub _stub = new edu.virginia.vcgr.genii.exportdir.fsproxy.FSProxySOAPBindingStub(portAddress, this);
            _stub.setPortName(getFSProxyPortTypeWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setFSProxyPortTypeEndpointAddress(java.lang.String address) {
        FSProxyPortType_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (edu.virginia.vcgr.genii.exportdir.fsproxy.FSProxyPortType.class.isAssignableFrom(serviceEndpointInterface)) {
                edu.virginia.vcgr.genii.exportdir.fsproxy.FSProxySOAPBindingStub _stub = new edu.virginia.vcgr.genii.exportdir.fsproxy.FSProxySOAPBindingStub(new java.net.URL(FSProxyPortType_address), this);
                _stub.setPortName(getFSProxyPortTypeWSDDServiceName());
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
        if ("FSProxyPortType".equals(inputPortName)) {
            return getFSProxyPortType();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/export/2010/12/fsproxy", "FSProxyService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/export/2010/12/fsproxy", "FSProxyPortType"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("FSProxyPortType".equals(portName)) {
            setFSProxyPortTypeEndpointAddress(address);
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
