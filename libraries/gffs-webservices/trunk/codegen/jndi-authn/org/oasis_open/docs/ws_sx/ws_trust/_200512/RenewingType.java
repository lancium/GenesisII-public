/**
 * RenewingType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package org.oasis_open.docs.ws_sx.ws_trust._200512;

public class RenewingType  implements java.io.Serializable {
    private java.lang.Boolean allow;  // attribute

    private java.lang.Boolean OK;  // attribute

    public RenewingType() {
    }

    public RenewingType(
           java.lang.Boolean allow,
           java.lang.Boolean OK) {
           this.allow = allow;
           this.OK = OK;
    }


    /**
     * Gets the allow value for this RenewingType.
     * 
     * @return allow
     */
    public java.lang.Boolean getAllow() {
        return allow;
    }


    /**
     * Sets the allow value for this RenewingType.
     * 
     * @param allow
     */
    public void setAllow(java.lang.Boolean allow) {
        this.allow = allow;
    }


    /**
     * Gets the OK value for this RenewingType.
     * 
     * @return OK
     */
    public java.lang.Boolean getOK() {
        return OK;
    }


    /**
     * Sets the OK value for this RenewingType.
     * 
     * @param OK
     */
    public void setOK(java.lang.Boolean OK) {
        this.OK = OK;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof RenewingType)) return false;
        RenewingType other = (RenewingType) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.allow==null && other.getAllow()==null) || 
             (this.allow!=null &&
              this.allow.equals(other.getAllow()))) &&
            ((this.OK==null && other.getOK()==null) || 
             (this.OK!=null &&
              this.OK.equals(other.getOK())));
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
        if (getAllow() != null) {
            _hashCode += getAllow().hashCode();
        }
        if (getOK() != null) {
            _hashCode += getOK().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(RenewingType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://docs.oasis-open.org/ws-sx/ws-trust/200512/", "RenewingType"));
        org.apache.axis.description.AttributeDesc attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("allow");
        attrField.setXmlName(new javax.xml.namespace.QName("", "Allow"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        typeDesc.addFieldDesc(attrField);
        attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("OK");
        attrField.setXmlName(new javax.xml.namespace.QName("", "OK"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
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
