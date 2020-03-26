/**
 * RNSEntryType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package org.ggf.rns;

public class RNSEntryType  implements java.io.Serializable {
    private org.ws.addressing.EndpointReferenceType endpoint;

    private org.ggf.rns.RNSMetadataType metadata;

    private java.lang.String entryName;  // attribute

    public RNSEntryType() {
    }

    public RNSEntryType(
           org.ws.addressing.EndpointReferenceType endpoint,
           org.ggf.rns.RNSMetadataType metadata,
           java.lang.String entryName) {
           this.endpoint = endpoint;
           this.metadata = metadata;
           this.entryName = entryName;
    }


    /**
     * Gets the endpoint value for this RNSEntryType.
     * 
     * @return endpoint
     */
    public org.ws.addressing.EndpointReferenceType getEndpoint() {
        return endpoint;
    }


    /**
     * Sets the endpoint value for this RNSEntryType.
     * 
     * @param endpoint
     */
    public void setEndpoint(org.ws.addressing.EndpointReferenceType endpoint) {
        this.endpoint = endpoint;
    }


    /**
     * Gets the metadata value for this RNSEntryType.
     * 
     * @return metadata
     */
    public org.ggf.rns.RNSMetadataType getMetadata() {
        return metadata;
    }


    /**
     * Sets the metadata value for this RNSEntryType.
     * 
     * @param metadata
     */
    public void setMetadata(org.ggf.rns.RNSMetadataType metadata) {
        this.metadata = metadata;
    }


    /**
     * Gets the entryName value for this RNSEntryType.
     * 
     * @return entryName
     */
    public java.lang.String getEntryName() {
        return entryName;
    }


    /**
     * Sets the entryName value for this RNSEntryType.
     * 
     * @param entryName
     */
    public void setEntryName(java.lang.String entryName) {
        this.entryName = entryName;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof RNSEntryType)) return false;
        RNSEntryType other = (RNSEntryType) obj;
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
        if (getEntryName() != null) {
            _hashCode += getEntryName().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(RNSEntryType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://schemas.ogf.org/rns/2009/12/rns", "RNSEntryType"));
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
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("metadata");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ogf.org/rns/2009/12/rns", "metadata"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.ogf.org/rns/2009/12/rns", "RNSMetadataType"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
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
