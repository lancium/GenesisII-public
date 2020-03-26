/**
 * ReferenceResolver.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package edu.virginia.vcgr.genii.naming;

public interface ReferenceResolver extends java.rmi.Remote {
    public org.ws.addressing.EndpointReferenceType resolve(java.lang.Object resolveRequest) throws java.rmi.RemoteException, org.ogf.schemas.naming._2006._08.naming.ResolveFailedWithReferralFaultType, org.ogf.schemas.naming._2006._08.naming.ResolveFailedFaultType;
}
