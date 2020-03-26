/**
 * EndpointReferenceType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package org.ws.addressing;

public class EndpointReferenceType  implements java.io.Serializable, org.apache.axis.encoding.AnyContentType {
    private org.ws.addressing.AttributedURIType address;

    private org.ws.addressing.ReferenceParametersType referenceParameters;

    private org.ws.addressing.MetadataType metadata;

    private org.apache.axis.message.MessageElement [] _any;

    public EndpointReferenceType() {
    }

    public EndpointReferenceType(
           org.ws.addressing.AttributedURIType address,
           org.ws.addressing.ReferenceParametersType referenceParameters,
           org.ws.addressing.MetadataType metadata,
           org.apache.axis.message.MessageElement [] _any) {
           this.address = address;
           this.referenceParameters = referenceParameters;
           this.metadata = metadata;
           this._any = _any;
    }


    /**
     * Gets the address value for this EndpointReferenceType.
     * 
     * @return address
     */
    public org.ws.addressing.AttributedURIType getAddress() {
        return address;
    }


    /**
     * Sets the address value for this EndpointReferenceType.
     * 
     * @param address
     */
    public void setAddress(org.ws.addressing.AttributedURIType address) {
        this.address = address;
    }


    /**
     * Gets the referenceParameters value for this EndpointReferenceType.
     * 
     * @return referenceParameters
     */
    public org.ws.addressing.ReferenceParametersType getReferenceParameters() {
        return referenceParameters;
    }


    /**
     * Sets the referenceParameters value for this EndpointReferenceType.
     * 
     * @param referenceParameters
     */
    public void setReferenceParameters(org.ws.addressing.ReferenceParametersType referenceParameters) {
        this.referenceParameters = referenceParameters;
    }


    /**
     * Gets the metadata value for this EndpointReferenceType.
     * 
     * @return metadata
     */
    public org.ws.addressing.MetadataType getMetadata() {
        return metadata;
    }


    /**
     * Sets the metadata value for this EndpointReferenceType.
     * 
     * @param metadata
     */
    public void setMetadata(org.ws.addressing.MetadataType metadata) {
        this.metadata = metadata;
    }


    /**
     * Gets the _any value for this EndpointReferenceType.
     * 
     * @return _any
     */
    public org.apache.axis.message.MessageElement [] get_any() {
        return _any;
    }


    /**
     * Sets the _any value for this EndpointReferenceType.
     * 
     * @param _any
     */
    public void set_any(org.apache.axis.message.MessageElement [] _any) {
        this._any = _any;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof EndpointReferenceType)) return false;
        EndpointReferenceType other = (EndpointReferenceType) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.address==null && other.getAddress()==null) || 
             (this.address!=null &&
              this.address.equals(other.getAddress()))) &&
            ((this.referenceParameters==null && other.getReferenceParameters()==null) || 
             (this.referenceParameters!=null &&
              this.referenceParameters.equals(other.getReferenceParameters()))) &&
            ((this.metadata==null && other.getMetadata()==null) || 
             (this.metadata!=null &&
              this.metadata.equals(other.getMetadata()))) &&
            ((this._any==null && other.get_any()==null) || 
             (this._any!=null &&
              java.util.Arrays.equals(this._any, other.get_any())));
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
        if (getAddress() != null) {
            _hashCode += getAddress().hashCode();
        }
        if (getReferenceParameters() != null) {
            _hashCode += getReferenceParameters().hashCode();
        }
        if (getMetadata() != null) {
            _hashCode += getMetadata().hashCode();
        }
        if (get_any() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(get_any());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(get_any(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(EndpointReferenceType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2005/08/addressing", "EndpointReferenceType"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("address");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.w3.org/2005/08/addressing", "Address"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2005/08/addressing", "AttributedURIType"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("referenceParameters");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.w3.org/2005/08/addressing", "ReferenceParameters"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2005/08/addressing", "ReferenceParametersType"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("metadata");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.w3.org/2005/08/addressing", "Metadata"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2005/08/addressing", "MetadataType"));
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
