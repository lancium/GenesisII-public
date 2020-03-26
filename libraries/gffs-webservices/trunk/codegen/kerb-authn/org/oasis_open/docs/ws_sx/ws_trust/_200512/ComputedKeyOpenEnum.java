/**
 * ComputedKeyOpenEnum.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package org.oasis_open.docs.ws_sx.ws_trust._200512;

public class ComputedKeyOpenEnum  implements java.io.Serializable, org.apache.axis.encoding.SimpleType {
    private java.lang.String _value;
    // Simple Types must have a String constructor
    public ComputedKeyOpenEnum(java.lang.String _value) {
        this._value = _value;
    }
    public ComputedKeyOpenEnum(org.oasis_open.docs.ws_sx.ws_trust._200512.ComputedKeyEnum _value) {
        setComputedKeyEnumValue(_value);
    }

    public ComputedKeyOpenEnum(org.apache.axis.types.URI _value) {
        setAnyURIValue(_value);
    }

    // Simple Types must have a toString for serializing the value
    public java.lang.String toString() {
        return _value;
    }


    /**
     * Gets the computedKeyEnumValue value for this ComputedKeyOpenEnum.
     * 
     * @return computedKeyEnumValue
     */
    public org.oasis_open.docs.ws_sx.ws_trust._200512.ComputedKeyEnum getComputedKeyEnumValue() {
        return org.oasis_open.docs.ws_sx.ws_trust._200512.ComputedKeyEnum.fromString(_value);
    }


    /**
     * Sets the _value value for this ComputedKeyOpenEnum.
     * 
     * @param _value
     */
    public void setComputedKeyEnumValue(org.oasis_open.docs.ws_sx.ws_trust._200512.ComputedKeyEnum _value) {
        this._value = _value == null ? null : _value.toString();
    }


    /**
     * Gets the anyURIValue value for this ComputedKeyOpenEnum.
     * 
     * @return anyURIValue
     */
    public org.apache.axis.types.URI getAnyURIValue() {
        try {
            return new org.apache.axis.types.URI(_value);
        }
        catch (org.apache.axis.types.URI.MalformedURIException mue) {
            throw new java.lang.RuntimeException(mue.toString());
       }
    }


    /**
     * Sets the _value value for this ComputedKeyOpenEnum.
     * 
     * @param _value
     */
    public void setAnyURIValue(org.apache.axis.types.URI _value) {
        this._value = _value == null ? null : _value.toString();
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof ComputedKeyOpenEnum)) return false;
        ComputedKeyOpenEnum other = (ComputedKeyOpenEnum) obj;
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
        new org.apache.axis.description.TypeDesc(ComputedKeyOpenEnum.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://docs.oasis-open.org/ws-sx/ws-trust/200512/", "ComputedKeyOpenEnum"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("computedKeyEnumValue");
        elemField.setXmlName(new javax.xml.namespace.QName("", "ComputedKeyEnumValue"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://docs.oasis-open.org/ws-sx/ws-trust/200512/", "ComputedKeyEnum"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("anyURIValue");
        elemField.setXmlName(new javax.xml.namespace.QName("", "anyURIValue"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "anyURI"));
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
