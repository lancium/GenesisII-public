/**
 * FaultCodesOpenEnumType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package org.ws.addressing;

public class FaultCodesOpenEnumType  implements java.io.Serializable, org.apache.axis.encoding.SimpleType {
    private java.lang.String _value;
    // Simple Types must have a String constructor
    public FaultCodesOpenEnumType(java.lang.String _value) {
        this._value = _value;
    }
    public FaultCodesOpenEnumType(org.ws.addressing.FaultCodesType _value) {
        setFaultCodesTypeValue(_value);
    }

    public FaultCodesOpenEnumType(javax.xml.namespace.QName _value) {
        setQNameValue(_value);
    }

    // Simple Types must have a toString for serializing the value
    public java.lang.String toString() {
        return _value;
    }


    /**
     * Gets the faultCodesTypeValue value for this FaultCodesOpenEnumType.
     * 
     * @return faultCodesTypeValue
     */
    public org.ws.addressing.FaultCodesType getFaultCodesTypeValue() {
        return org.ws.addressing.FaultCodesType.fromString(_value);
    }


    /**
     * Sets the _value value for this FaultCodesOpenEnumType.
     * 
     * @param _value
     */
    public void setFaultCodesTypeValue(org.ws.addressing.FaultCodesType _value) {
        this._value = _value == null ? null : _value.toString();
    }


    /**
     * Gets the QNameValue value for this FaultCodesOpenEnumType.
     * 
     * @return QNameValue
     */
    public javax.xml.namespace.QName getQNameValue() {
        return new javax.xml.namespace.QName(_value);
    }


    /**
     * Sets the _value value for this FaultCodesOpenEnumType.
     * 
     * @param _value
     */
    public void setQNameValue(javax.xml.namespace.QName _value) {
        this._value = _value == null ? null : _value.toString();
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof FaultCodesOpenEnumType)) return false;
        FaultCodesOpenEnumType other = (FaultCodesOpenEnumType) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true &&  this.toString().equals(obj.toString());
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
        if (this._value != null) {
            _hashCode += this._value.hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(FaultCodesOpenEnumType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2005/08/addressing", "FaultCodesOpenEnumType"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("faultCodesTypeValue");
        elemField.setXmlName(new javax.xml.namespace.QName("", "FaultCodesTypeValue"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2005/08/addressing", "FaultCodesType"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("QNameValue");
        elemField.setXmlName(new javax.xml.namespace.QName("", "QNameValue"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "QName"));
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
          new  org.apache.axis.encoding.ser.SimpleSerializer(
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
          new  org.apache.axis.encoding.ser.SimpleDeserializer(
            _javaType, _xmlType, typeDesc);
    }

}
