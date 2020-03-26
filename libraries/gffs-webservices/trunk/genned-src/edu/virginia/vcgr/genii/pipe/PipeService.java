/**
 * PipeService.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package edu.virginia.vcgr.genii.pipe;

public interface PipeService extends javax.xml.rpc.Service {
    public java.lang.String getPipePortTypeAddress();

    public edu.virginia.vcgr.genii.pipe.PipePortType getPipePortType() throws javax.xml.rpc.ServiceException;

    public edu.virginia.vcgr.genii.pipe.PipePortType getPipePortType(java.net.URL portAddress) throws javax.xml.rpc.ServiceException;
}
