/**
 * RNSEntryResponseType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package org.ggf.rns;

public class RNSEntryResponseType  implements java.io.Serializable {
    private org.ws.addressing.EndpointReferenceType endpoint;

    private org.ggf.rns.RNSMetadataType metadata;

    private org.oasis_open.wsrf.basefaults.BaseFaultType fault;

    private java.lang.String entryName;  // attribute

    public RNSEntryResponseType() {
    }

    public RNSEntryResponseType(
           org.ws.addressing.EndpointReferenceType endpoint,
           org.ggf.rns.RNSMetadataType metadata,
           org.oasis_open.wsrf.basefaults.BaseFaultType fault,
           java.lang.String entryName) {
           this.endpoint = endpoint;
           this.metadata = metadata;
           this.fault = fault;
           this.entryName = entryName;
    }


    /**
     * Gets the endpoint value for this RNSEntryResponseType.
     * 
     * @return endpoint
     */
    public org.ws.addressing.EndpointReferenceType getEndpoint() {
        return endpoint;
    }


    /**
     * Sets the endpoint value for this RNSEntryResponseType.
     * 
     * @param endpoint
     */
    public void setEndpoint(org.ws.addressing.EndpointReferenceType endpoint) {
        this.endpoint = endpoint;
    }


    /**
     * Gets the metadata value for this RNSEntryResponseType.
     * 
     * @return metadata
     */
    public org.ggf.rns.RNSMetadataType getMetadata() {
        return metadata;
    }


    /**
     * Sets the metadata value for this RNSEntryResponseType.
     * 
     * @param metadata
     */
    public void setMetadata(org.ggf.rns.RNSMetadataType metadata) {
        this.metadata = metadata;
    }


    /**
     * Gets the fault value for this RNSEntryResponseType.
     * 
     * @return fault
     */
    public org.oasis_open.wsrf.basefaults.BaseFaultType getFault() {
        return fault;
    }


    /**
     * Sets the fault value for this RNSEntryResponseType.
     * 
     * @param fault
     */
    public void setFault(org.oasis_open.wsrf.basefaults.BaseFaultType fault) {
        this.fault = fault;
    }


    /**
     * Gets the entryName value for this RNSEntryResponseType.
     * 
     * @return entryName
     */
    public java.lang.String getEntryName() {
        return entryName;
    }


    /**
     * Sets the entryName value for this RNSEntryResponseType.
     * 
     * @param entryName
     */
    public void setEntryName(java.lang.String entryName) {
        this.entryName = entryName;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof RNSEntryResponseType)) return false;
        RNSEntryResponseType other = (RNSEntryResponseType) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.endpoint==null && other.getEndpoint()==null) || 
             (this.endpoint!=null &&
              this.endpoint.equals(other.getEndpoint()))) &&
            ((this.metadata==null && other.getMetadata()==null) || 
             (this.metadata!=null &&
              this.metadata.equals(other.getMetadata()))) &&
            ((this.fault==null && other.getFault()==null) || 
             (this.fault!=null &&
              this.fault.equals(other.getFault()))) &&
            ((this.entryName==null && other.getEntryName()==null) || 
             (this.entryName!=null &&
              this.entryName.equals(other.getEntryName())));
        __equalsCalc = null;
        return _equals;
    }

    private boolean __hashCodeCalc = false;
    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        if (getEndpoint() != null) {
            _hashCode += getEndpoint().hashCode();
        }
        if (getMetadata() != null) {
            _hashCode += getMetadata().hashCode();
        }
        if (getFault() != null) {
            _hashCode += getFault().hashCode();
        }
        if (getEntryName() != null) {
            _hashCode += getEntryName().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(RNSEntryResponseType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://schemas.ogf.org/rns/2009/12/rns", "RNSEntryResponseType"));
        org.apache.axis.description.AttributeDesc attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("entryName");
        attrField.setXmlName(new javax.xml.namespace.QName("", "entry-name"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://schemas.ogf.org/rns/2009/12/rns", "EntryNameType"));
        typeDesc.addFieldDesc(attrField);
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("endpoint");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ogf.org/rns/2009/12/rns", "endpoint"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2005/08/addressing", "EndpointReferenceType"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("metadata");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ogf.org/rns/2009/12/rns", "metadata"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.ogf.org/rns/2009/12/rns", "RNSMetadataType"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("fault");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ogf.org/rns/2009/12/rns", "fault"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://docs.oasis-open.org/wsrf/bf-2", "BaseFaultType"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
    }

    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

    /**
     * Get Custom Serializer
     */
    public static org.apache.axis.encoding.Serializer getSerializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanSerializer(
            _javaType, _xmlType, typeDesc);
    }

    /**
     * Get Custom Deserializer
     */
    public static org.apache.axis.encoding.Deserializer getDeserializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanDeserializer(
            _javaType, _xmlType, typeDesc);
    }

}
