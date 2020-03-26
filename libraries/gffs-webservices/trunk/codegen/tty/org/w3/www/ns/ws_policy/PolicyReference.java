/**
 * PolicyReference.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package org.w3.www.ns.ws_policy;

public class PolicyReference  implements java.io.Serializable, org.apache.axis.encoding.AnyContentType {
    private org.apache.axis.message.MessageElement [] _any;

    private org.apache.axis.types.URI URI;  // attribute

    private byte[] digest;  // attribute

    private org.apache.axis.types.URI digestAlgorithm;  // attribute

    public PolicyReference() {
    }

    public PolicyReference(
           org.apache.axis.message.MessageElement [] _any,
           org.apache.axis.types.URI URI,
           byte[] digest,
           org.apache.axis.types.URI digestAlgorithm) {
           this._any = _any;
           this.URI = URI;
           this.digest = digest;
           this.digestAlgorithm = digestAlgorithm;
    }


    /**
     * Gets the _any value for this PolicyReference.
     * 
     * @return _any
     */
    public org.apache.axis.message.MessageElement [] get_any() {
        return _any;
    }


    /**
     * Sets the _any value for this PolicyReference.
     * 
     * @param _any
     */
    public void set_any(org.apache.axis.message.MessageElement [] _any) {
        this._any = _any;
    }


    /**
     * Gets the URI value for this PolicyReference.
     * 
     * @return URI
     */
    public org.apache.axis.types.URI getURI() {
        return URI;
    }


    /**
     * Sets the URI value for this PolicyReference.
     * 
     * @param URI
     */
    public void setURI(org.apache.axis.types.URI URI) {
        this.URI = URI;
    }


    /**
     * Gets the digest value for this PolicyReference.
     * 
     * @return digest
     */
    public byte[] getDigest() {
        return digest;
    }


    /**
     * Sets the digest value for this PolicyReference.
     * 
     * @param digest
     */
    public void setDigest(byte[] digest) {
        this.digest = digest;
    }


    /**
     * Gets the digestAlgorithm value for this PolicyReference.
     * 
     * @return digestAlgorithm
     */
    public org.apache.axis.types.URI getDigestAlgorithm() {
        return digestAlgorithm;
    }


    /**
     * Sets the digestAlgorithm value for this PolicyReference.
     * 
     * @param digestAlgorithm
     */
    public void setDigestAlgorithm(org.apache.axis.types.URI digestAlgorithm) {
        this.digestAlgorithm = digestAlgorithm;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof PolicyReference)) return false;
        PolicyReference other = (PolicyReference) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this._any==null && other.get_any()==null) || 
             (this._any!=null &&
              java.util.Arrays.equals(this._any, other.get_any()))) &&
            ((this.URI==null && other.getURI()==null) || 
             (this.URI!=null &&
              this.URI.equals(other.getURI()))) &&
            ((this.digest==null && other.getDigest()==null) || 
             (this.digest!=null &&
              java.util.Arrays.equals(this.digest, other.getDigest()))) &&
            ((this.digestAlgorithm==null && other.getDigestAlgorithm()==null) || 
             (this.digestAlgorithm!=null &&
              this.digestAlgorithm.equals(other.getDigestAlgorithm())));
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
        if (getURI() != null) {
            _hashCode += getURI().hashCode();
        }
        if (getDigest() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getDigest());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getDigest(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getDigestAlgorithm() != null) {
            _hashCode += getDigestAlgorithm().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(PolicyReference.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/ns/ws-policy", ">PolicyReference"));
        org.apache.axis.description.AttributeDesc attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("URI");
        attrField.setXmlName(new javax.xml.namespace.QName("", "URI"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "anyURI"));
        typeDesc.addFieldDesc(attrField);
        attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("digest");
        attrField.setXmlName(new javax.xml.namespace.QName("", "Digest"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "base64Binary"));
        typeDesc.addFieldDesc(attrField);
        attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("digestAlgorithm");
        attrField.setXmlName(new javax.xml.namespace.QName("", "DigestAlgorithm"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "anyURI"));
        typeDesc.addFieldDesc(attrField);
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
