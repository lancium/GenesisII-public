/**
 * RNSPortType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package org.ggf.rns;

public interface RNSPortType extends java.rmi.Remote {
    public org.ggf.rns.RNSEntryResponseType[] add(org.ggf.rns.RNSEntryType[] addRequest) throws java.rmi.RemoteException, org.ggf.rns.WriteNotPermittedFaultType;
    public org.ggf.rns.LookupResponseType lookup(java.lang.String[] lookupRequest) throws java.rmi.RemoteException, org.ggf.rns.ReadNotPermittedFaultType;
    public org.ggf.rns.RNSEntryResponseType[] rename(org.ggf.rns.NameMappingType[] renameRequest) throws java.rmi.RemoteException, org.ggf.rns.WriteNotPermittedFaultType;
    public org.ggf.rns.RNSEntryResponseType[] remove(java.lang.String[] removeRequest) throws java.rmi.RemoteException, org.ggf.rns.WriteNotPermittedFaultType;
    public org.ggf.rns.RNSEntryResponseType[] setMetadata(org.ggf.rns.MetadataMappingType[] setMetadataRequest) throws java.rmi.RemoteException, org.ggf.rns.WriteNotPermittedFaultType;
}
