/**
 * ReferenceResolverService.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package edu.virginia.vcgr.genii.naming;

public interface ReferenceResolverService extends javax.xml.rpc.Service {
    public java.lang.String getReferenceResolverAddress();

    public edu.virginia.vcgr.genii.naming.ReferenceResolver getReferenceResolver() throws javax.xml.rpc.ServiceException;

    public edu.virginia.vcgr.genii.naming.ReferenceResolver getReferenceResolver(java.net.URL portAddress) throws javax.xml.rpc.ServiceException;
}
