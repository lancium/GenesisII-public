/**
 * NumberOfProcesses_Type.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 10, 2016 (05:15:24 EDT) WSDL2Java emitter.
 */

package org.ogf.jsdl.spmd;

public class NumberOfProcesses_Type  implements java.io.Serializable, org.apache.axis.encoding.SimpleType {
    private org.apache.axis.types.PositiveInteger _value;

    private java.lang.Boolean actualtotalcpucount;  // attribute

    public NumberOfProcesses_Type() {
    }

    // Simple Types must have a String constructor
    public NumberOfProcesses_Type(org.apache.axis.types.PositiveInteger _value) {
        this._value = _value;
    }
    public NumberOfProcesses_Type(java.lang.String _value) {
        this._value = new org.apache.axis.types.PositiveInteger(_value);
    }

    // Simple Types must have a toString for serializing the value
    public java.lang.String toString() {
        return _value == null ? null : _value.toString();
    }


    /**
     * Gets the _value value for this NumberOfProcesses_Type.
     * 
     * @return _value
     */
    public org.apache.axis.types.PositiveInteger get_value() {
        return _value;
    }


    /**
     * Sets the _value value for this NumberOfProcesses_Type.
     * 
     * @param _value
     */
    public void set_value(org.apache.axis.types.PositiveInteger _value) {
        this._value = _value;
    }


    /**
     * Gets the actualtotalcpucount value for this NumberOfProcesses_Type.
     * 
     * @return actualtotalcpucount
     */
    public java.lang.Boolean getActualtotalcpucount() {
        return actualtotalcpucount;
    }


    /**
     * Sets the actualtotalcpucount value for this NumberOfProcesses_Type.
     * 
     * @param actualtotalcpucount
     */
    public void setActualtotalcpucount(java.lang.Boolean actualtotalcpucount) {
        this.actualtotalcpucount = actualtotalcpucount;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof NumberOfProcesses_Type)) return false;
        NumberOfProcesses_Type other = (NumberOfProcesses_Type) obj;
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
            ((this.actualtotalcpucount==null && other.getActualtotalcpucount()==null) || 
             (this.actualtotalcpucount!=null &&
              this.actualtotalcpucount.equals(other.getActualtotalcpucount())));
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
        if (getActualtotalcpucount() != null) {
            _hashCode += getActualtotalcpucount().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(NumberOfProcesses_Type.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://schemas.ogf.org/jsdl/2007/02/jsdl-spmd", "NumberOfProcesses_Type"));
        org.apache.axis.description.AttributeDesc attrField = new org.apache.axis.description.AttributeDesc();
        attrField.setFieldName("actualtotalcpucount");
        attrField.setXmlName(new javax.xml.namespace.QName("", "actualtotalcpucount"));
        attrField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        typeDesc.addFieldDesc(attrField);
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("_value");
        elemField.setXmlName(new javax.xml.namespace.QName("", "_value"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "positiveInteger"));
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
