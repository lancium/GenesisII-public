/**
 * RNSService.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package org.ggf.rns;

public interface RNSService extends javax.xml.rpc.Service {
    public java.lang.String getRNSPortTypeAddress();

    public org.ggf.rns.RNSPortType getRNSPortType() throws javax.xml.rpc.ServiceException;

    public org.ggf.rns.RNSPortType getRNSPortType(java.net.URL portAddress) throws javax.xml.rpc.ServiceException;
}
