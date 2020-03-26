/**
 * TTYService.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package edu.virginia.vcgr.genii.tty;

public interface TTYService extends javax.xml.rpc.Service {
    public java.lang.String getTTYPortTypeAddress();

    public edu.virginia.vcgr.genii.tty.TTYPortType getTTYPortType() throws javax.xml.rpc.ServiceException;

    public edu.virginia.vcgr.genii.tty.TTYPortType getTTYPortType(java.net.URL portAddress) throws javax.xml.rpc.ServiceException;
}
