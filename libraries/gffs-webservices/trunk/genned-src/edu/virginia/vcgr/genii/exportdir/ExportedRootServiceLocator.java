/**
 * ExportedRootServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package edu.virginia.vcgr.genii.exportdir;

public class ExportedRootServiceLocator extends org.apache.axis.client.Service implements edu.virginia.vcgr.genii.exportdir.ExportedRootService {

    public ExportedRootServiceLocator() {
    }


    public ExportedRootServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public ExportedRootServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for ExportedRootPortType
    private java.lang.String ExportedRootPortType_address = "http://localhost:8080/wsrf/services";

    public java.lang.String getExportedRootPortTypeAddress() {
        return ExportedRootPortType_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String ExportedRootPortTypeWSDDServiceName = "ExportedRootPortType";

    public java.lang.String getExportedRootPortTypeWSDDServiceName() {
        return ExportedRootPortTypeWSDDServiceName;
    }

    public void setExportedRootPortTypeWSDDServiceName(java.lang.String name) {
        ExportedRootPortTypeWSDDServiceName = name;
    }

    public edu.virginia.vcgr.genii.exportdir.ExportedRootPortType getExportedRootPortType() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(ExportedRootPortType_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getExportedRootPortType(endpoint);
    }

    public edu.virginia.vcgr.genii.exportdir.ExportedRootPortType getExportedRootPortType(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            edu.virginia.vcgr.genii.exportdir.ExportedRootSOAPBindingStub _stub = new edu.virginia.vcgr.genii.exportdir.ExportedRootSOAPBindingStub(portAddress, this);
            _stub.setPortName(getExportedRootPortTypeWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setExportedRootPortTypeEndpointAddress(java.lang.String address) {
        ExportedRootPortType_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (edu.virginia.vcgr.genii.exportdir.ExportedRootPortType.class.isAssignableFrom(serviceEndpointInterface)) {
                edu.virginia.vcgr.genii.exportdir.ExportedRootSOAPBindingStub _stub = new edu.virginia.vcgr.genii.exportdir.ExportedRootSOAPBindingStub(new java.net.URL(ExportedRootPortType_address), this);
                _stub.setPortName(getExportedRootPortTypeWSDDServiceName());
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
        if ("ExportedRootPortType".equals(inputPortName)) {
            return getExportedRootPortType();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/rns/2006/08/exported-root", "ExportedRootService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://vcgr.cs.virginia.edu/rns/2006/08/exported-root", "ExportedRootPortType"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("ExportedRootPortType".equals(portName)) {
            setExportedRootPortTypeEndpointAddress(address);
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
