/**
 * Argument_Type.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package org.ggf.jsdl.posix;

public class Argument_Type  implements java.io.Serializable, org.apache.axis.encoding.SimpleType {
    private org.apache.axis.types.NormalizedString _value;

    private org.apache.axis.types.NCName filesystemName;  // attribute

    public Argument_Type() {
    }

    // Simple Types must have a String constructor
    public Argument_Type(org.apache.axis.types.NormalizedString _value) {
        this._value = _value;
    }
    public Argument_Type(java.lang.String _value) {
        this._value = new org.apache.axis.types.NormalizedString(_value);
    }

    // Simple Types must have a toString for serializing the value
    public java.lang.String toString() {
        return _value == null ? null : _value.toString();
    }


    /**
     * Gets the _value value for this Argument_Type.
     * 
     * @return _value
     */
    public org.apache.axis.types.NormalizedString get_value() {
        return _value;
    }


    /**
     * Sets the _value value for this Argument_Type.
     * 
     * @param _value
     */
    public void set_value(org.apache.axis.types.NormalizedString _value) {
        this._value = _value;
    }


    /**
     * Gets the filesystemName value for this Argument_Type.
     * 
     * @return filesystemName
     */
    public org.apache.axis.types.NCName getFilesystemName() {
        return filesystemName;
    }


    /**
     * Sets the filesystemName value for this Argument_Type.
     * 
     * @param filesystemName
     */
    public void setFilesystemName(org.apache.axis.types.NCName filesystemName) {
        this.filesystemName = filesystemName;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof Argument_Type)) return false;
        Argument_Type other = (Argument_Type) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this._value==null && other.get_value()==null) || 
             (this._value!=null &&
              this._value.equals(other.get_value()))) &&
            ((this.filesystemName==null && other.getFilesystemName()==null) || 
             (this.filesystemName!=null &&
              this.filesystemName.equals(other.getFilesystemName())));
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
        if (get_value() != null) {
            _hashCode += get_value().hashCode();
        }
        if (getFilesystemName() != null) {
            _hashCode += getFilesystemName().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(Argument_Type.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://schemas.ggf.org/jsdl/2005/11/jsdl-posix", "Argument_Type"));
        org.apache.axis.description.AttributeDesc attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("filesystemName");
        attrField.setXmlName(new javax.xml.namespace.QName("", "filesystemName"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "NCName"));
        typeDesc.addFieldDesc(attrField);
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("_value");
        elemField.setXmlName(new javax.xml.namespace.QName("", "_value"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "normalizedString"));
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
