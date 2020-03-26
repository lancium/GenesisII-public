/**
 * RNSMetadataType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package org.ggf.rns;

public class RNSMetadataType  implements java.io.Serializable, org.apache.axis.encoding.AnyContentType {
    private org.ggf.rns.SupportsRNSType supportsRns;

    private org.apache.axis.message.MessageElement [] _any;

    public RNSMetadataType() {
    }

    public RNSMetadataType(
           org.ggf.rns.SupportsRNSType supportsRns,
           org.apache.axis.message.MessageElement [] _any) {
           this.supportsRns = supportsRns;
           this._any = _any;
    }


    /**
     * Gets the supportsRns value for this RNSMetadataType.
     * 
     * @return supportsRns
     */
    public org.ggf.rns.SupportsRNSType getSupportsRns() {
        return supportsRns;
    }


    /**
     * Sets the supportsRns value for this RNSMetadataType.
     * 
     * @param supportsRns
     */
    public void setSupportsRns(org.ggf.rns.SupportsRNSType supportsRns) {
        this.supportsRns = supportsRns;
    }


    /**
     * Gets the _any value for this RNSMetadataType.
     * 
     * @return _any
     */
    public org.apache.axis.message.MessageElement [] get_any() {
        return _any;
    }


    /**
     * Sets the _any value for this RNSMetadataType.
     * 
     * @param _any
     */
    public void set_any(org.apache.axis.message.MessageElement [] _any) {
        this._any = _any;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof RNSMetadataType)) return false;
        RNSMetadataType other = (RNSMetadataType) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.supportsRns==null && other.getSupportsRns()==null) || 
             (this.supportsRns!=null &&
              this.supportsRns.equals(other.getSupportsRns()))) &&
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
        if (getSupportsRns() != null) {
            _hashCode += getSupportsRns().hashCode();
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
        new org.apache.axis.description.TypeDesc(RNSMetadataType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://schemas.ogf.org/rns/2009/12/rns", "RNSMetadataType"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("supportsRns");
        elemField.setXmlName(new javax.xml.namespace.QName("http://schemas.ogf.org/rns/2009/12/rns", "supports-rns"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.ogf.org/rns/2009/12/rns", "SupportsRNSType"));
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
